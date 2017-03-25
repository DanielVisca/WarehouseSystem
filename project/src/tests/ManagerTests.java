package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

// import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import warehouse.FileHelper;
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
import warehouse.Truck;
import warehouse.WarehouseItem;
import warehouse.WarehouseSystem;
import warehouse.Worker;

public class ManagerTests {
  WarehouseSystem system;
  JobManager jobManager;
  PickingManager pickingManager;
  SequencingManager sequencingManager;
  LoadingManager loadingManager;
  InventoryManager inventoryManager;
  Worker jim;
  Worker bob;
  Worker joe;

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

    jim = pickingManager.getWorker("Jim");
    bob = sequencingManager.getWorker("Bob");
    joe = loadingManager.getWorker("Joe");
  }

  @Test
  public void jobManagerTranslation() {
    Job job = jobManager.getJobs().get(0);
    assertNotNull(job);

    // Sku testing
    int[] skus = job.getSkus();
    assertNotNull(skus);

    assertEquals(skus[0], 37);
    assertEquals(skus[1], 38);
    assertEquals(skus[2], 21);
    assertEquals(skus[3], 22);
    assertEquals(skus[4], 43);
    assertEquals(skus[5], 44);
    assertEquals(skus[6], 43);
    assertEquals(skus[7], 44);

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
    job.setWorker(worker);
    assertEquals(job.getWorker(), worker);

    // Complete
    job.complete();
    assertNull(job.getWorker());

    // ID
    job.setId("test");
    assertEquals(job.getId(), "test");
  }


  @Test
  public void jobLocation() {
    Job job = jobManager.getJobs().get(0);

    // Location testing
    Location testLocation = new Location("B", 3, 2, 1, 37);
    job.setLocation(testLocation);
    assertEquals(job.getLocation().toString(), "B 3 2 1");
  }

  @Test
  public void jobContents() {
    Job job = jobManager.getJobs().get(0);
    int[] skus = job.getSkus();

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

    assertTrue(job.verify());

    job.discardContents();
    assertEquals(job.getItems().size(), 0);
    assertNull(job.getPallets());
  }

  @Test
  public void jobContentsFail() {
    Job job = jobManager.getJobs().get(0);
    int[] skus = job.getSkus();

    // Items and pallets
    WarehouseItem item1 = new WarehouseItem(skus[0]);
    WarehouseItem item2 = new WarehouseItem(skus[1]);
    WarehouseItem item3 = new WarehouseItem(skus[2]);
    WarehouseItem item4 = new WarehouseItem(12);

    job.addItem(item1);
    job.addItem(item2);
    job.addItem(item3);
    job.addItem(item4);

    assertFalse(job.verify());
  }

  @Test
  public void jobManagerAddRemoval() {
    Job job1 = new Job(new Location("A", 0, 1, 2, 39));
    Job job2 = new Job(new Location("B", 1, 0, 3, 40));

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
  public void workerValidity() {

    assertNotNull(jim);
    assertTrue(jim instanceof Picker);
    assertTrue(bob instanceof Sequencer);
    assertTrue(joe instanceof Loader);
    assertEquals(joe.getManager(), loadingManager);
    assertNotNull(pickingManager.getWorker("Steven"));
  }

  @Test
  public void processManagerJobSequence() {
    assertEquals(pickingManager.getJobsToDo().size(), 1);

    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");

    assertEquals(pickingManager.getJobsToDo().size(), 0);
    assertEquals(pickingManager.getJobsInProgress().size(), 1);

    pickingManager.setStatus("Jim", "to Marshaling");

    assertEquals(pickingManager.getJobsInProgress().size(), 0);
    assertEquals(sequencingManager.getJobsToDo().size(), 1);

    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences");
    // bob.nextTask();

    assertEquals(sequencingManager.getJobsInProgress().size(), 0);
    assertEquals(loadingManager.getJobsToDo().size(), 1);

    loadingManager.setStatus("Joe", "ready");
    loadingManager.setStatus("Joe", "loads");
    // joe.nextTask();

    assertEquals(loadingManager.getJobsInProgress().size(), 0);

    // Picking manager get job when there is none
    // pickingManager.nextJob("Jim");
    assertNull(jim.getCurrentJob());
  }

  @Test
  public void loadingFull() {
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to Marshaling");

    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences");

    // Truck full, send shipment
    Truck truck = loadingManager.getTruck();
    // Fill truck
    for (int i = 0; i < 158; i++) {
      truck.loadPallet(new Pallet());
    }

    loadingManager.setStatus("Joe", "ready");
    loadingManager.setStatus("Joe", "loads");
  }

  @Test
  public void processManagerFailure() {
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    jim.discardCurrentJob();

    assertNull(jim.getCurrentJob());
    assertEquals(pickingManager.getJobsToDo().size(), 1);

    pickingManager.setStatus("Jim", "ready");
    assertNotNull(jim.getCurrentJob());
    assertEquals(pickingManager.getJobsToDo().size(), 0);
    assertEquals(pickingManager.getJobsInProgress().size(), 1);
  }

  @Test
  public void workerAlreadyHasJob() {
    jobManager.processOrder("SES Blue");
    jobManager.processOrder("SES Red");
    jobManager.processOrder("SE Black");
    jobManager.processOrder("SE Black");

    ArrayList<Job> jobs = pickingManager.getJobsToDo();
    jim.startJob(jobs.get(0));
    assertFalse(jim.startJob(jobs.get(1)));
    assertEquals(jim.getCurrentJob(), jobs.get(0));
  }

  @Test
  public void workerMethods() {
    // Check status
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "starting job");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");

    // Check verify
    jim.verifyJob();
    assertNotNull(jim.getCurrentJob());


    assertEquals(bob.getStatus(), "");
    jim.jobComplete();
    sequencingManager.setStatus("Bob", "ready");
    assertEquals(bob.getStatus(), "starting job");

    // Test get string
    assertEquals(bob.toString(), "Bob");
  }

  @Test
  public void workerNoJob() {
    // Check status
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "starting job");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    jim.discardCurrentJob();
    assertNull(jim.getCurrentJob());
    pickingManager.setStatus("Jim", "pick 43");
  }

  @Test
  public void workerDoubleJob() {
    // Check status
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "starting job");
    final Job job = jim.getCurrentJob();
    jim.setStatus("ready");

    // Place new orders which attemps to give jim a new job
    jobManager.processOrder("SES Blue");
    jobManager.processOrder("SES Red");
    jobManager.processOrder("SE Black");
    jobManager.processOrder("SE Black");

    assertEquals(jim.getCurrentJob(), job);
  }

  @Test
  public void pickerPickTypes() {
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "starting job");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    // Picked wrong item!
    pickingManager.setStatus("Jim", "pick 12");

    assertNull(jim.getCurrentJob());

    // Job is reset
    pickingManager.setStatus("Jim", "ready");

    // Full pick
    assertEquals(jim.getCurrentJob().getPickingIndex(), 0);

    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    assertEquals(jim.getCurrentJob().getPickingIndex(), 7);
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to Marshaling");

    // Pick when theres no pick
    assertNull(jim.getCurrentJob());
    jim.nextTask();
    assertNull(jim.getCurrentJob());
  }

  @Test
  public void earlyMarshalling() {
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "starting job");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "to Marshaling");
    assertEquals(jim.getCurrentJob().getItems().size(), 7);
  }

  @Test
  public void overPick() {
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "starting job");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    // Extra pick
    pickingManager.setStatus("Jim", "pick 44");
    assertNull(jim.getCurrentJob());
  }

  @Test
  public void sequencingFewItems() {
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to Marshaling");

    sequencingManager.getJobsToDo().get(0).getItems().remove(0);

    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences");

    assertEquals(pickingManager.getJobsToDo().size(), 1);
  }

  @Test
  public void sequencingNoItems() {
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to Marshaling");

    sequencingManager.getJobsToDo().get(0).getItems().clear();

    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences");

    assertEquals(pickingManager.getJobsToDo().size(), 1);
  }

  @Test
  public void sequencingWrongOrder() {
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to Marshaling");

    sequencingManager.setStatus("Bob", "ready");
    Job job = bob.getCurrentJob();
    sequencingManager.setStatus("Bob", "sequences");

    // Reorder items
    Pallet pallet = job.getPallets()[0];
    WarehouseItem item = pallet.getItems().remove(0);
    pallet.addItem(item);

    assertFalse(job.verify());
  }

  @Test
  public void systemTest() throws NoSuchLocationException, 
                                  StockExceedsCapacityException {

    // Getters
    assertEquals(system.getJobManager(), jobManager);
    assertEquals(system.getPickingManager(), pickingManager);
    assertEquals(system.getSequencingManager(), sequencingManager);
    assertEquals(system.getLoadingManager(), loadingManager);
    assertEquals(system.getInventoryManager(), inventoryManager);

    // Constructor
    system = new WarehouseSystem("orders.txt");
    ArrayList<String> input = system.getInput();
    assertNotNull(input);
    system.simulate();
    system.endDay();
    assertEquals(system.getInput().size(), 0);
    system.updateInput("orders.txt");
    assertEquals(system.getInput(), input);

    // Call main
    WarehouseSystem.main(new String[] {});

    Location location = inventoryManager.getLocationAtCoordinates("A", 0, 0, 1);
    location.setInventory(2);
    system.replenishInventory(location);
    inventoryManager.replenish(location);
    assertEquals(location.getMax(), location.getInventory());
  }
}

