package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import warehouse.FileHelper;
import warehouse.InventoryManager;
import warehouse.Job;
import warehouse.JobManager;
import warehouse.LoadingManager;
import warehouse.Location;
import warehouse.PickingManager;
import warehouse.SequencingManager;
import warehouse.StockExceedsCapacityException;
import warehouse.WarehouseSystem;
import warehouse.Worker;

public class ReplenisherTest {
  private WarehouseSystem system;
  private JobManager jobManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;
  private InventoryManager inventoryManager;
  //private Worker jim;
  //private Worker bob;
  //private Worker joe;
  private Worker billy;

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
    //bob = sequencingManager.getWorker("Bob");
    //joe = loadingManager.getWorker("Joe");
    billy = inventoryManager.getWorker("Billy");
  }

  @Test
  public void replenisherNoJob() {
    assertNull(billy.getCurrentJob());
    Location loc = inventoryManager.getLocation("2");
    int inventoryBefore = loc.getInventory();
    inventoryManager.setStatus("Billy", "replenish 2");
    assertEquals(loc.getInventory(), inventoryBefore);
  }

  @Test
  public void replenisherWrongReplenish() {
    Location loc = inventoryManager.getLocation("15");
    // Clear jobs
    inventoryManager.getJobsToDo().clear();
    inventoryManager.queueJob(new Job(loc));
    inventoryManager.setStatus("Billy", "ready");
    inventoryManager.setStatus("Billy", "replenish 5");
    assertEquals(billy.getLastInstruction(), billy.getCurrentJob().nextInstruction());
  }

  @Test
  public void replenisherJobSuccess() throws StockExceedsCapacityException {
    // Clear other jobs
    inventoryManager.getJobsToDo().clear();

    Location loc = inventoryManager.getLocation("25");
    loc.setInventory(5);
    inventoryManager.checkAndReplenish(loc);
    inventoryManager.setStatus("Billy", "ready");
    inventoryManager.setStatus("Billy", "replenishes 25");
    assertEquals(loc.getInventory(), loc.getMax());
  }
}

