package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import warehouse.PickingManager;
import warehouse.SequencingManager;
import warehouse.WarehouseSystem;
import warehouse.Worker;
import warehouse.WorkerJobException;

public class PickerTest {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  private Worker jim;

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
  }

  @Test
  public void pickerMethods() throws WorkerJobException {
    // Check status
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");

    // Check verify
    exception.expect(WorkerJobException.class);
    jim.getCurrentJob().verify();
  }

  @Test
  public void pickerPickTypes() throws WorkerJobException {
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    // Picked wrong item!
    pickingManager.setStatus("Jim", "pick 12");

    assertEquals(jim.getCurrentJob().getPickingIndex(), 3);

    pickingManager.discardJob(jim);

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
    pickingManager.setStatus("Jim", "to marshalling");

    // Pick when theres no pick
    assertNull(jim.getCurrentJob());

    exception.expect(WorkerJobException.class);
    jim.nextTask();
  }

  @Test
  public void pickerPickButNoJob() {
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "to marshalling");

    pickingManager.setStatus("Jim", "pick 2");
  }

  @Test
  public void pickerEarlyMarshalling() {
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "to marshalling");
    assertNull(jim.getCurrentJob());
    // assertEquals(jim.getCurrentJob().getItems().size(), 7);
  }

  @Test
  public void pickerOverPick() {
    pickingManager.setStatus("Jim", "ready");
    assertEquals(jim.getStatus(), "ready");
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
    assertEquals(jim.getCurrentJob().getPickingIndex(), 8);
  }

  @Test
  public void pickerWrongSkuSomehow() {
    pickingManager.setStatus("Jim", "ready");
    pickingManager.setStatus("Jim", "pick 37");
    pickingManager.setStatus("Jim", "pick 38");
    pickingManager.setStatus("Jim", "pick 21");
    pickingManager.setStatus("Jim", "pick 22");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    pickingManager.setStatus("Jim", "pick 43");
    pickingManager.setStatus("Jim", "pick 44");
    Job job = jim.getCurrentJob();
    job.getItems().get(0).setSku("15");
    pickingManager.setStatus("Jim", "to marshalling");

    // Job was picked wrong, discarded and sent back to picking
    assertTrue(pickingManager.getJobsToDo().contains(job));
  }
}