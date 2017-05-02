package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
// import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import warehouse.FileHelper;
import warehouse.InventoryManager;
import warehouse.Job;
import warehouse.JobManager;
import warehouse.LoadingManager;
import warehouse.Pallet;
import warehouse.PickingManager;
import warehouse.SequencingManager;
import warehouse.Truck;
import warehouse.WarehouseSystem;
import warehouse.Worker;
import warehouse.WorkerJobException;

public class LoaderTest {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  // private Worker jim;
  // private Worker bob;
  private Worker joe;
  // private Worker billy;

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

    // jim = pickingManager.getWorker("Jim");
    // bob = sequencingManager.getWorker("Bob");
    joe = loadingManager.getWorker("Joe");
    // billy = inventoryManager.getWorker("Billy");
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
    pickingManager.setStatus("Jim", "to marshalling");

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
  public void loaderHighestPriorityJobNotReady() {
    jobManager.processOrder("SES Blue");
    jobManager.processOrder("SES Red");
    jobManager.processOrder("SE Black");
    jobManager.processOrder("SE Black");

    // Pick job index 0 (highest priority on truck)
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to marshalling");

    // Pick job index 1 (second highest priority)
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to marshalling");

    // Sequencer bungles it
    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences 12");

    // Sucessfully sequences job index 1
    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences 37");
    sequencingManager.setStatus("Bob", "sequences 21");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 38");
    sequencingManager.setStatus("Bob", "sequences 22");
    sequencingManager.setStatus("Bob", "sequences 44");
    sequencingManager.setStatus("Bob", "sequences 44");
    sequencingManager.setStatus("Bob", "to loading");

    // When loader tries to load it, finds out its not the highest priority, waits
    loadingManager.setStatus("Joe", "ready");
    assertEquals(loadingManager.getJobsToDo().size(), 1);
    assertNull(joe.getCurrentJob());
  }

  @Test
  public void loadFillTruck() {
    // Send through picking
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to marshalling");

    // Send through sequencing
    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences 37");
    sequencingManager.setStatus("Bob", "sequences 21");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 38");
    sequencingManager.setStatus("Bob", "sequences 22");
    sequencingManager.setStatus("Bob", "sequences 44");
    sequencingManager.setStatus("Bob", "sequences 44");
    sequencingManager.setStatus("Bob", "to loading");

    // Load up truck with fake stuff
    for (int i = 0; i < 159; i++) {
      loadingManager.getTruck().loadPallet(new Pallet());
    }
    assertNotNull(loadingManager.getCurrentTruck());

    // Successfully load last item
    loadingManager.setStatus("Joe", "ready");
    loadingManager.setStatus("Joe", "loads");

    assertNull(loadingManager.getCurrentTruck());
  }

  @Test
  public void loaderLoadInvalidJob() throws WorkerJobException {
    // Send through picking
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "to marshalling");

    // Send through sequencing
    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences 37");
    sequencingManager.setStatus("Bob", "sequences 21");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 38");
    sequencingManager.setStatus("Bob", "sequences 22");
    sequencingManager.setStatus("Bob", "sequences 44");
    sequencingManager.setStatus("Bob", "sequences 44");
    sequencingManager.setStatus("Bob", "to loading");

    // Accept job
    loadingManager.setStatus("Joe", "ready");

    // Take an item out of the job
    Job job = joe.getCurrentJob();
    job.getPallets()[0].getItems().remove(0);
    loadingManager.setStatus("Joe", "loads");

    // Job is discarded and back on the picking queue
    pickingManager.getJobsToDo().contains(job);
  }
}

