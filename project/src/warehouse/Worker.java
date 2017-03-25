package warehouse;

public abstract class Worker {

  // Unique name of the worker
  private String name;
  // Role of the worker
  private String role;
  // Current job the worker is performing
  protected Job currentJob;
  // The last status reported by the worker
  private String lastStatus;
  // Next instruction
  protected String instruction;
  // Manager of software
  protected ProcessManager manager;

  /**
   * Default constructor for worker.
   * 
   * @param name name of the worker
   */
  public Worker(String name, String role, ProcessManager manager) {
    this.name = name;
    this.role = role;
    this.manager = manager;
    this.lastStatus = "";
  }

  /**
   * Get name.
   * 
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Get the role of this worker.
   * 
   * @return String
   */
  public String getRole() {
    return role;
  }

  /**
   * Get the current job.
   * 
   * @return Job
   */
  public Job getCurrentJob() {
    return currentJob;
  }

  /**
   * Get lastStatus.
   * 
   * @return String
   */
  public String getStatus() {
    return lastStatus;
  }

  /**
   * Set lastStatus.
   * 
   * @param lastStatus to set
   */
  public void setStatus(String lastStatus) {
    this.lastStatus = lastStatus;
    if (lastStatus.equals("ready")) {
      if (currentJob != null) {
        logError("has not completed last job.");
      } else {
        logStatus();
      }
    }
  }

  /**
   * Set next instruction.
   * 
   * @param instruction to display on worker device
   */
  public void setInstruction(String instruction) {
    this.instruction = instruction;
    if (!this.instruction.equals("")) {
      logInstruction();
    }
  }

  /**
   * Get manager.
   * 
   * @return ProcessManager
   */
  public ProcessManager getManager() {
    return manager;
  }

  /**
   * String representation of the worker.
   */
  public String toString() {
    return name;
  }

  /**
   * Parse the job.
   * 
   * @param job job to parse
   * @return boolean
   */
  public boolean startJob(Job job) {
    if (currentJob == null) {
      currentJob = job;
      currentJob.setWorker(this);
      setStatus("starting job");
      logTask("has begun job " + job);
      currentJob.nextInstruction();
      return true;
    } else {
      return false;
    }
  }

  /**
   * Perform the next task in the job. This only really applies to pickers as they are the only ones
   * with more than one task per job
   */
  public abstract void nextTask();

  /**
   * Log a completed task of this worker. This is for more detailed logging than logStatus.
   * 
   * @param task to log
   */
  protected void logTask(String task) {
    FileHelper.logEvent(role + " " + name + " " + task);
  }

  /**
   * Log current status. Quick way to record actions.
   */
  protected void logStatus() {
    FileHelper.logEvent(role + " " + name + " " + getStatus());
  }

  /**
   * Log instruction given to this worker.
   */
  protected void logInstruction() {
    FileHelper.logInstruction(role + " " + name + " instructed to " + instruction);
  }

  /**
   * Log an error.
   * 
   * @param error to log
   */
  protected void logError(String error) {
    FileHelper.logEvent("Error: " + role + " " + name + " " + error);
    manager.workerError();
  }

  /**
   * The job is complete, report it.
   */
  public void jobComplete() {
    manager.jobComplete(this);
    currentJob = null;
    setInstruction("");
  }

  /**
   * Verify the job has been done correctly.
   * 
   * @return boolean
   */
  public boolean verifyJob() {
    return currentJob.verify();
  }

  /**
   * Discard the current job.
   */
  public void discardCurrentJob() {
    setInstruction("discard job");
    manager.discardJob(this, currentJob);
    currentJob.discardContents();
    currentJob = null;
  }
}
