package warehouse;

public class Replenisher extends Worker {

  /**
   * Default constructor for replenisher.
   * 
   * @param name name of the replenisher
   * @param manager manager of this replenisher
   */
  public Replenisher(String name, ProcessManager manager) {
    super(name, "Replenisher", manager);
  }

  /**
   * Perform the next task in the job.
   */
  public void nextTask() {
    logTask("replenish " + currentJob.getId());
    jobComplete();
  }
}
