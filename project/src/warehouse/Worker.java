package warehouse;

public abstract class Worker {

  // Unique name of the worker
  private String name;
  // Role of the worker
  private String role;
  // Current job the worker is performing
  private Job currentJob;
  // The last status reported by the worker
  private String lastStatus;
  // Next instruction
  private String instruction;
  // Manager of software
  private ProcessManager manager;
  // Keep track of

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
   * Set current job.
   * 
   * @param job to set
   */
  public void setCurrentJob(Job job) {
    this.currentJob = job;
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
  public void setStatus(String lastStatus) throws WorkerJobException {
    this.lastStatus = lastStatus;
    logStatus();
    if (lastStatus.equals("ready")) {
      if (currentJob != null) {
        throw new WorkerJobException("Last job lot completed");
      }
    }
  }

  /**
   * Get the last instruction sent to this worker.
   * 
   * @return String
   */
  public String getLastInstruction() {
    return instruction;
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
   * @param job to parse
   * @throws WorkerJobException for worker error
   */
  public void startJob(Job job) throws WorkerJobException {
    if (currentJob == null && job != null) {
      currentJob = job;
      currentJob.setWorker(this);
      logTask("starting job " + job);
      setInstruction(currentJob.nextInstruction());
    } else if (currentJob != null) {
      throw new WorkerJobException("Worker " + name + " already has a job.");
    } else if (job == null) {
      throw new WorkerJobException("This job does not exist");
    }
  }

  /**
   * Perform the next task in the job. This only really applies to pickers as they are the only ones
   * with more than one task per job
   * 
   * @throws WorkerJobException for worker error
   */
  public abstract void nextTask() throws WorkerJobException;

  /**
   * Log a completed task of this worker. This is for more detailed logging than logStatus.
   * 
   * @param task to log
   */
  protected void logTask(String task) {
    String taskString = role + " " + name + " " + task;
    FileHelper.logEvent(taskString, this);
  }

  /**
   * Log current status. Quick way to record actions.
   */
  protected void logStatus() {
    String statusString = "IN: " + role + " " + name + " " + getStatus();
    FileHelper.logEvent(statusString, this);
  }

  /**
   * Log instruction given to this worker.
   */
  protected void logInstruction() {
    String instructionString = role + " " + name + " instructed to " + instruction;
    FileHelper.logEvent(instructionString, this);
  }
}
