package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import warehouse.FileHelper;
import warehouse.InventoryException;
import warehouse.InventoryManager;
import warehouse.Job;
import warehouse.JobManager;
import warehouse.Loader;
import warehouse.LoadingManager;
import warehouse.Location;
import warehouse.NoSuchLocationException;
import warehouse.Pallet;
import warehouse.Picker;
import warehouse.PickingManager;
import warehouse.Sequencer;
import warehouse.SequencingManager;
import warehouse.StockExceedsCapacityException;
import warehouse.WarehouseItem;
import warehouse.WarehouseSystem;
import warehouse.Worker;
import warehouse.WorkerJobException;
import warehouse.Zone;

public class ManagerTests {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  private Worker jim;
  private Worker bob;
  private Worker joe;
  //private Worker billy;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  /**
   * Get all the variables from the warehouse system and add orders/workers.
   */
  @Before
  public void initialize() {
    FileHelper.setSilentLogging(true);
    system = new WarehouseSystem();
    jobManager = system.getJobManager();
    pickingManager = system.getPickingManager();
    sequencingManager = system.getSequencingManager();
    loadingManager = system.getLoadingManager();
    inventoryManager = system.getInventoryManager();

    jobManager.processOrder("SES Blue");
    jobManager.processOrder("SES Red");
    jobManager.processOrder("SE Black");
    jobManager.processOrder("SE Black");

    pickingManager.hireWorker("Jim");
    sequencingManager.hireWorker("Bob");
    loadingManager.hireWorker("Joe");
    inventoryManager.hireWorker("Billy");

    jim = pickingManager.getWorker("Jim");
    bob = sequencingManager.getWorker("Bob");
    joe = loadingManager.getWorker("Joe");
    //billy = inventoryManager.getWorker("Billy");
  }

  /**
   * JobManager tests.
   */
  @Test
  public void jobManagerTranslation() throws WorkerJobException {
    Job job = jobManager.getJobs().get(0);
    assertNotNull(job);

    // Sku testing
    String[] skus = job.getSkus();
    assertNotNull(skus);

    assertEquals(skus[0], "37");
    assertEquals(skus[1], "38");
    assertEquals(skus[2], "21");
    assertEquals(skus[3], "22");
    assertEquals(skus[4], "43");
    assertEquals(skus[5], "44");
    assertEquals(skus[6], "43");
    assertEquals(skus[7], "44");

    // Pick Testing
    Location[] pickingOrder = job.getPickingOrder();
    assertEquals(pickingOrder[0].toString(), "B 1 0 0");
    assertEquals(pickingOrder[7].toString(), "B 1 1 3");

    Location nextPick = job.getNextPick();
    assertEquals(nextPick.toString(), "B 1 0 0");
    for (int i = 0; i < 7; i++) {
      job.getNextPick();
    }
    assertNull(job.getNextPick());

    // Order testing
    String[] orders = job.getOrders();
    assertEquals(orders[0], "SES Blue");
    assertEquals(orders[3], "SE Black");


    // Worker
    Worker worker = new Picker("Jerry", null);
    worker.startJob(job);
    assertEquals(job.getWorker(), worker);

    // Complete
    pickingManager.jobComplete(worker);
    assertNull(job.getWorker());

    // ID
    job.setId("test");
    assertEquals(job.getId(), "test");
  }

  @Test
  public void jobLocation() {
    Job job = jobManager.getJobs().get(0);

    // Location testing
    Location testLocation = new Location("B", 3, 2, 1, "37");
    job.setLocation(testLocation);
    assertEquals(job.getLocation().toString(), "B 3 2 1");
  }

  @Test
  public void jobContents() throws WorkerJobException {
    Job job = jobManager.getJobs().get(0);
    String[] skus = job.getSkus();

    // Items and pallets
    WarehouseItem item1 = new WarehouseItem(skus[0]);
    WarehouseItem item2 = new WarehouseItem(skus[1]);
    WarehouseItem item3 = new WarehouseItem(skus[2]);
    WarehouseItem item4 = new WarehouseItem(skus[3]);

    job.addItem(item1);
    job.addItem(item2);
    job.addItem(item3);
    job.addItem(item4);
    assertEquals(job.getItems().size(), 4);

    Pallet[] pallets = new Pallet[1];
    pallets[0] = new Pallet();
    pallets[0].addItem(item1);
    pallets[0].addItem(item2);
    pallets[0].addItem(item3);
    pallets[0].addItem(item4);

    job.setPallets(pallets);
    assertEquals(job.getPallets()[0], pallets[0]);

    job.verify();

    job.resetJob();
    assertEquals(job.getItems().size(), 0);
    assertNull(job.getPallets());
  }

  @Test
  public void jobContentsFail() throws WorkerJobException {
    Job job = jobManager.getJobs().get(0);
    String[] skus = job.getSkus();
    jim.startJob(job);

    // Items and pallets
    WarehouseItem item1 = new WarehouseItem(skus[0]);
    WarehouseItem item2 = new WarehouseItem(skus[1]);
    WarehouseItem item3 = new WarehouseItem(skus[2]);
    WarehouseItem item4 = new WarehouseItem("12");

    job.addItem(item1);
    job.addItem(item2);
    job.addItem(item3);
    job.addItem(item4);

    exception.expect(WorkerJobException.class);
    job.verify();
  }

  @Test
  public void jobManagerAddRemoval() {
    Job job1 = new Job(new Location("A", 0, 1, 2, "39"));
    Job job2 = new Job(new Location("B", 1, 0, 3, "40"));

    jobManager.addJob(job1);
    jobManager.addJob(job2);
    jobManager.removeJob(job1);

    ArrayList<Job> jobs = jobManager.getJobs();
    assertEquals(jobs.size(), 2);
    assertEquals(jobs.get(1), job2);

    jobManager.removeJob(job2);
    assertEquals(jobs.size(), 1);
  }

  @Test
  public void processManagerFailure() {
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    jim.getCurrentJob().resetJob();
    pickingManager.discardJob(jim);

    assertNull(jim.getCurrentJob());
    assertEquals(pickingManager.getJobsToDo().size(), 1);

    pickingManager.setStatus("Jim", "ready");
    assertNotNull(jim.getCurrentJob());
    assertEquals(pickingManager.getJobsToDo().size(), 0);
    assertEquals(pickingManager.getJobsInProgress().size(), 1);
  }

  @Test
  public void processManagerNextJob() throws WorkerJobException {
    assertNotNull(pickingManager.getNextJob());

    sequencingManager.queueJob(null);
    exception.expect(WorkerJobException.class);
    sequencingManager.getNextJob();
  }

  /**
   * Basic worker tests.
   */
  @Test
  public void workerValidity() {

    assertNotNull(jim);
    assertTrue(jim instanceof Picker);
    assertTrue(bob instanceof Sequencer);
    assertTrue(joe instanceof Loader);
    assertEquals(joe.getManager(), loadingManager);
    assertNotNull(pickingManager.getWorker("Steven"));
  }

  @Test
  public void workerAlreadyHasJob() throws WorkerJobException {
    jim.startJob(pickingManager.getNextJob());

    jobManager.processOrder("SES Blue");
    jobManager.processOrder("SES Red");
    jobManager.processOrder("SE Black");
    jobManager.processOrder("SE Black");
    exception.expect(WorkerJobException.class);
    jim.startJob(pickingManager.getNextJob());
  }

  @Test
  public void workerNullJob() throws WorkerJobException {
    exception.expect(WorkerJobException.class);
    jim.startJob(null);
  }

  @Test
  public void workerMethods() {
    assertEquals(jim.getRole(), "Picker");
    assertEquals(joe.getRole(), "Loader");
    assertEquals(bob.toString(), "Bob");

    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getLastInstruction(), jim.getCurrentJob().nextInstruction());
  }

  @Test
  public void workerNoJob() throws WorkerJobException {
    // Check status
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    jim.setCurrentJob(null);
    assertNull(jim.getCurrentJob());
    exception.expect(WorkerJobException.class);
    jim.nextTask();
  }

  @Test
  public void workerDoubleJob() throws WorkerJobException {
    // Check status
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "ready");
    final Job job = jim.getCurrentJob();

    // Place new orders which attemps to give jim a new job
    jobManager.processOrder("SES Blue");
    jobManager.processOrder("SES Red");
    jobManager.processOrder("SE Black");
    jobManager.processOrder("SE Black");

    assertEquals(jim.getCurrentJob(), job);
  }

  /**
   * InventoryManager tests.
   */
  @Test
  public void inventoryLocationWrongSize() {
    Location l1 = new Location("A", 1, 2, 3, "A");
    Location l2 = new Location("A", 5, 6, 7, "B");

    assertTrue(inventoryManager.locationObeysDimensions(l1));
    assertFalse(inventoryManager.locationObeysDimensions(l2));
  }

  @Test
  public void inventoryFloorSize() {
    LinkedHashMap<String, Zone> floor = inventoryManager.getFloor();

    assertTrue(floor.entrySet().size() > 0);
  }

  @Test
  public void inventoryRemoveFromEmpty()
      throws NoSuchLocationException, StockExceedsCapacityException {
    Location loc = inventoryManager.getLocation("15");
    loc.setInventory(0);

    // Remove what isnt there
    inventoryManager.removeInventory(loc);
    assertEquals(loc.getInventory(), 0);
  }

  @Test
  public void inventoryInitialWrongDimensions() throws InventoryException {
    exception.expect(InventoryException.class);
    inventoryManager.setDimensions("inventory_dimensions_wrong.csv");
  }

  @Test
  public void inventoryGetWrongZone() throws NoSuchLocationException {
    exception.expect(NoSuchLocationException.class);
    inventoryManager.getLocationAtCoordinates("ABSSDFASDF", 0, 1, 3);
  }

  @Test
  public void inventoryGetWrongLocation() throws NoSuchLocationException {
    exception.expect(NoSuchLocationException.class);
    inventoryManager.getLocationAtCoordinates("A", 3, 3, 3);
  }

  @Test
  public void inventoryInitialStockOver() throws InventoryException, StockExceedsCapacityException {
    exception.expect(StockExceedsCapacityException.class);
    inventoryManager.stockFloor("initial_overstock.csv");
  }

  @Test
  public void inventoryInitialStockWrongFileType()
      throws InventoryException, StockExceedsCapacityException {
    exception.expect(InventoryException.class);
    inventoryManager.stockFloor("initial_wrong.csv");
  }

  @Test
  public void inventoryInitialLayoutOversized() throws InventoryException, NoSuchLocationException {
    exception.expect(NoSuchLocationException.class);
    inventoryManager.csvToFloor("traversal_table_oversized.csv");
  }

  @Test
  public void inventoryInitialLayoutWrong() throws InventoryException, NoSuchLocationException {
    exception.expect(InventoryException.class);
    inventoryManager.csvToFloor("traversal_table_wrong.csv");
  }

  /**
   * General system test.
   */
  @Test
  public void systemTest() throws NoSuchLocationException, StockExceedsCapacityException {
    // Getters
    assertEquals(system.getJobManager(), jobManager);
    assertEquals(system.getPickingManager(), pickingManager);
    assertEquals(system.getSequencingManager(), sequencingManager);
    assertEquals(system.getLoadingManager(), loadingManager);
    assertEquals(system.getInventoryManager(), inventoryManager);

    // Constructor
    system = new WarehouseSystem();
    ArrayList<String> input = system.getInput();
    assertNotNull(input);
    system.simulate();
    system.endDay();
    assertEquals(system.getInput().size(), 0);
    system.updateInput("orders.txt");
    assertEquals(system.getInput(), input);
    system.simulate();
    system.endDay();
    assertEquals(system.getInput().size(), 0);

    // Call functions
    WarehouseSystem.main(new String[] {"orders.txt"});

    Location location = inventoryManager.getLocationAtCoordinates("A", 0, 0, 1);
    location.setInventory(2);
    system.replenishInventory(location);
    inventoryManager.replenish(location);
    assertEquals(location.getMax(), location.getInventory());
  }
}

