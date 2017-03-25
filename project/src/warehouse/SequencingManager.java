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
    super.jobComplete(worker);
    Job job = worker.getCurrentJob();
    system.sendToLoading(job);
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
}
