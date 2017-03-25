package warehouse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public abstract class ProcessManager {

  // Map of workers for easy access by name
  protected LinkedHashMap<String, Worker> workers = new LinkedHashMap<String, Worker>();
  // Queue of jobs to do
  protected ArrayList<Job> jobsToDo = new ArrayList<Job>();
  // Map of jobs in progress
  protected ArrayList<Job> jobsInProgress = new ArrayList<Job>();

  // Master system
  protected WarehouseSystem system;

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
    worker.setStatus(status);

    // If they're ready, give them a job
    if (status.equals("ready")) {
      // If there are jobs to do, give them one, otherwise they can wait
      if (jobsToDo.size() > 0) {
        Job job = jobsToDo.remove(0);
        assignJob(worker, job);
      }
    } else {
      // If the workers current status is ready/does not have a job then something wrong
      if (worker.getCurrentJob() == null) {
        FileHelper.logEvent("Error: " + name + " has no job to " + status);
      } else {
        // Otherwise, this means the worker has performed a task.
        // Need to let the worker know they've done the task, and give them the next one
        worker.nextTask();
      }
    }
  }

  /**
   * Assign a job to a given worker.
   * 
   * @param worker to perform job
   * @param job to do
   */
  private void assignJob(Worker worker, Job job) {
    jobsInProgress.add(job);
    if (!worker.startJob(job)) {
      FileHelper.logEvent(
          "Error: " + worker.getName() + " already has job " + worker.getCurrentJob().getId());
    }
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
        assignJob(worker, job);
        return;
      }
    }

    jobsToDo.add(job);
  }

  /**
   * Job is complete, hand it off.
   * 
   * @param worker who completed job
   */
  public void jobComplete(Worker worker) {
    Job job = worker.getCurrentJob();
    job.complete();
    jobsInProgress.remove(job);
  }

  /**
   * Discard job.
   * 
   * @param worker who screwed up
   * @param job to discard
   */
  public void discardJob(Worker worker, Job job) {
    FileHelper
        .logEvent("Job " + job.getId() + " items discarded. Sending to beginning of Job queue.");
    jobsInProgress.remove(job);
    system.sendToPicking(job);
    system.raiseWarning();
  }

  /**
   * Worker error. Raise a simulation warning to the system.
   */
  public void workerError() {
    system.raiseWarning();
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
}
