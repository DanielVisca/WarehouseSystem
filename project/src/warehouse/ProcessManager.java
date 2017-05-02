package warehouse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public abstract class ProcessManager {

  // Map of workers for easy access by name
  private LinkedHashMap<String, Worker> workers = new LinkedHashMap<String, Worker>();
  // Queue of jobs to do
  private ArrayList<Job> jobsToDo = new ArrayList<Job>();
  // Map of jobs in progress
  private ArrayList<Job> jobsInProgress = new ArrayList<Job>();

  // Master system
  private WarehouseSystem system;

  /**
   * Default constructor for ProcessManager.
   * 
   * @param system master system
   */
  public ProcessManager(WarehouseSystem system) {
    this.system = system;
  }

  /**
   * Set the status of a worker.
   * 
   * @param name of the worker
   * @param status description
   */
  public void setStatus(String name, String status) {
    Worker worker = getWorker(name);

    try {
      worker.setStatus(status);

      // If they're ready, give them a job
      if (status.equals("ready")) {
        // If there are jobs to do, give them one, otherwise they can wait
        if (jobsToDo.size() > 0) {
          assignJob(worker, getNextJob());
        }
      } else {
        // Otherwise, this means the worker has performed a task.
        // Need to let the worker know they've done the task, and give them the next one
        worker.nextTask();
      }
    } catch (WorkerJobException exp) {
      FileHelper.logError(exp, this);
      if (exp instanceof PickerJobException) {
        // Picker screwed up, instruct them to redo the pick
        Picker picker = (Picker) worker;
        if (picker.getCurrentJob() != null) {
          picker.setInstruction(picker.getCurrentJob().nextInstruction());
        } else {
          picker.setInstruction("Put it back. You have no job, and therefor, no purpose");
        }
      } else if (exp instanceof SequencerJobException) {
        // Picker or Sequencer screwed up or the job is invalid, throw it out
        worker.setInstruction("discard job");
        discardJob(worker);
      } else if (exp instanceof ReplenisherJobException) {
        // Quick reset the job back to the replenisher
        Replenisher replenisher = (Replenisher) worker;
        replenisher.setInstruction(replenisher.getCurrentJob().nextInstruction());
      }
      system.raiseWarning();
    }
  }

  /**
   * Get the next job that should be worked on.
   * 
   * @return Job
   * @throws WorkerJobException for worker error
   */
  public Job getNextJob() throws WorkerJobException {
    Job job = jobsToDo.remove(0);

    if (job == null) {
      throw new WorkerJobException("No job in queue");
    }

    return job;
  }

  /**
   * Assign a job to a given worker.
   * 
   * @param worker to perform job
   * @param job to do
   */
  private void assignJob(Worker worker, Job job) throws WorkerJobException {
    worker.startJob(job);
    jobsInProgress.add(job);
  }

  /**
   * Creates and/or checks status of a worker.
   * 
   * @param name name of the worker
   * @return Worker
   */
  public Worker getWorker(String name) {
    // Get worker from map
    Worker worker = workers.get(name);
    // If no worker exists hire new one
    if (worker == null) {
      worker = hireWorker(name);
    }
    return worker;
  }

  /**
   * Queue job to be performed by worker.
   * 
   * @param job to be done
   */
  public void queueJob(Job job) {
    for (Entry<String, Worker> entry : workers.entrySet()) {
      Worker worker = entry.getValue();
      if (worker.getStatus().equals("ready")) {
        try {
          assignJob(worker, job);
          return;
        } catch (WorkerJobException exp) {
          system.raiseWarning();
          FileHelper.logError(exp, this);
        }
      }
    }
    jobsToDo.add(job);
  }

  /**
   * Job is complete, hand it off.
   *
   * @param worker who completed job
   * @throws WorkerJobException for worker error
   */
  public void jobComplete(Worker worker) throws WorkerJobException {
    Job job = worker.getCurrentJob();
    try {
      job.verify();
      jobsInProgress.remove(job);
      job.setWorker(null);
      worker.setCurrentJob(null);
      worker.setInstruction("");
    } catch (WorkerJobException exp) {
      // Error in the job, notify sub manager so they can deal with it
      throw new WorkerJobException(exp.getMessage());
    }
  }

  /**
   * Discard job.
   * 
   * @param worker whos job should be discarded
   */
  public void discardJob(Worker worker) {
    Job job = worker.getCurrentJob();
    job.resetJob();
    worker.setCurrentJob(null);
    FileHelper.logEvent(
        "Job " + job.getId() + " items discarded. Sending to beginning of Job queue.", this);
    jobsInProgress.remove(job);
    system.sendToPicking(job);
  }

  /**
   * Hire designated worker type.
   * 
   * @param name of the worker
   * @return Worker
   */
  public abstract Worker hireWorker(String name);

  /**
   * Add a worker to the worker pool.
   * 
   * @param worker to add
   */
  public void addWorker(Worker worker) {
    workers.put(worker.getName(), worker);
  }

  /**
   * Get the jobs to do.
   * 
   * @ArrayList
   */
  public ArrayList<Job> getJobsToDo() {
    return jobsToDo;
  }

  /**
   * Get the jobs in progress.
   * 
   * @ArrayList
   */
  public ArrayList<Job> getJobsInProgress() {
    return jobsInProgress;
  }

  /**
   * Get the master system.
   * 
   * @return WarehouseSystem
   */
  public WarehouseSystem getSystem() {
    return system;
  }
}
