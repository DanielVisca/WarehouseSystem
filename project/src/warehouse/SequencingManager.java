package warehouse;

public class SequencingManager extends ProcessManager {

  /**
   * Default constructor for SequencingManager.
   * 
   * @param system master system
   */
  public SequencingManager(WarehouseSystem system) {
    super(system);
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
      getSystem().sendToLoading(job);
    } catch (WorkerJobException exp) {
      getSystem().raiseWarning();
      FileHelper.logError(exp, this);
      discardJob(worker);
    }
  }

  /**
   * Hire new sequencer.
   * 
   * @return Worker
   */
  public Worker hireWorker(String name) {
    Worker worker = new Sequencer(name, this);
    addWorker(worker);
    return worker;
  }

  /**
   * Sequence item on pallets.
   * 
   * @param worker who's seqeuncing
   * @param pallet loaded onto
   * @param item to load
   */
  public void sequenceItem(Worker worker, int pallet, WarehouseItem item) {
    // Add the item to the pallet in alternating order
    worker.getCurrentJob().getPallets()[pallet].addItem(item);
    FileHelper.logEvent("Item " + item.getsku() + " in correct sequence", this);
  }
}
