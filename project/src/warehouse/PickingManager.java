package warehouse;

public class PickingManager extends ProcessManager {

  /**
   * Constructor for storing InventoryManager.
   * 
   * @param system master system
   */
  public PickingManager(WarehouseSystem system) {
    super(system);
  }

  /**
   * Create a new worker.
   * 
   * @param name of the worker
   * @return Worker
   */
  public Worker hireWorker(String name) {
    Worker picker = new Picker(name, this);
    addWorker(picker);
    return picker;
  }

  /**
   * Job is complete, hand it off.
   * 
   * @param worker who completed job
   */
  public void jobComplete(Worker worker) {
    Job job = worker.getCurrentJob();
    try {
      super.jobComplete(worker);
      getSystem().sendToMarshalling(job);
    } catch (WorkerJobException exp) {
      getSystem().raiseWarning();
      FileHelper.logError(exp, this);
      discardJob(worker);
    }
  }

  /**
   * Item picked. Tell system to remove it from the inventory.
   * 
   * @param worker that picked it
   * @param location item was picked from
   */
  public void pickItem(Worker worker, Location location) {
    WarehouseItem orderItem = new WarehouseItem(location.getSku());
    Job job = worker.getCurrentJob();
    job.addItem(orderItem);
    getSystem().removeFromInventory(location);
    worker.setInstruction(job.nextInstruction());
  }
}
