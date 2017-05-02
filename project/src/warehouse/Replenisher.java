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
  public void nextTask() throws WorkerJobException {
    if (getCurrentJob() == null) {
      throw new WorkerJobException("no job to replenish.");
    }

    String[] splitStatus = getStatus().split(" ");
    String reportedSku = splitStatus[1];
    String expectedSku = getCurrentJob().getSkus()[0];
    // Check that we're replenishing the right sku
    if (splitStatus[0].equals("replenish") && !reportedSku.equals(expectedSku)) {
      throw new ReplenisherJobException("wrong sku to replenish");
    }

    getManager().jobComplete(this);
  }
}
