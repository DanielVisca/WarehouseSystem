package tests;

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import warehouse.WarehouseItem;
import warehouse.WarehouseSystem;
import warehouse.Worker;
import warehouse.WorkerJobException;

public class SequencerTest {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  //private Worker jim;
  private Worker bob;
  //private Worker joe;
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

    //jim = pickingManager.getWorker("Jim");
    bob = sequencingManager.getWorker("Bob");
    //joe = loadingManager.getWorker("Joe");
    //billy = inventoryManager.getWorker("Billy");
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
    pickingManager.setStatus("Jim", "to marshalling");

    sequencingManager.getJobsToDo().get(0).getItems().remove(0);

    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences 37");

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
    pickingManager.setStatus("Jim", "to marshalling");

    sequencingManager.getJobsToDo().get(0).getItems().clear();

    sequencingManager.setStatus("Bob", "ready");
    sequencingManager.setStatus("Bob", "sequences 37");
  }

  @Test
  public void sequencingWrongOrder() throws WorkerJobException {
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
    Job job = bob.getCurrentJob();
    sequencingManager.setStatus("Bob", "sequences 37");

    // Reorder items
    Pallet pallet = job.getPallets()[0];
    WarehouseItem item = pallet.getItems().remove(0);
    pallet.addItem(item);

    exception.expect(WorkerJobException.class);
    job.verify();
  }

  @Test
  public void sequencingBadJobCompletion() throws WorkerJobException {
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
    sequencingManager.setStatus("Bob", "sequences 37");
    sequencingManager.setStatus("Bob", "sequences 21");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 38");
    sequencingManager.setStatus("Bob", "sequences 22");
    sequencingManager.setStatus("Bob", "sequences 44");

    Job job = bob.getCurrentJob();
    sequencingManager.setStatus("Bob", "to loading");
    // Job was sequenced wrong, discarded and sent back to picking
    assertTrue(pickingManager.getJobsToDo().contains(job));
  }

  @Test
  public void sequencingRescan() throws WorkerJobException {
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
    sequencingManager.setStatus("Bob", "sequences 37");
    sequencingManager.setStatus("Bob", "sequences 21");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob",  "rescans");
    sequencingManager.setStatus("Bob", "sequences 37");
    sequencingManager.setStatus("Bob", "sequences 21");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 38");
    sequencingManager.setStatus("Bob", "sequences 22");
    sequencingManager.setStatus("Bob", "sequences 44");
    sequencingManager.setStatus("Bob", "sequences 44");

    Job job = bob.getCurrentJob();
    sequencingManager.setStatus("Bob", "to loading");
    assertTrue(loadingManager.getJobsToDo().contains(job));
  }
  
  @Test
  public void sequencingWrongSkuSomehow() {
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
    sequencingManager.setStatus("Bob", "sequences 37");
    sequencingManager.setStatus("Bob", "sequences 21");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 43");
    sequencingManager.setStatus("Bob", "sequences 38");
    sequencingManager.setStatus("Bob", "sequences 22");
    sequencingManager.setStatus("Bob", "sequences 44");
    sequencingManager.setStatus("Bob", "sequences 44");

    Job job = bob.getCurrentJob();
    job.getPallets()[0].getItems().get(0).setSku("15");
    sequencingManager.setStatus("Bob", "to loading");
    // Job was sequenced wrong, discarded and sent back to picking
    assertTrue(pickingManager.getJobsToDo().contains(job));
  }

 
}
