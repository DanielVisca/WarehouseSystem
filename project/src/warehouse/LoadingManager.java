package warehouse;

import java.util.ArrayList;

public class LoadingManager extends ProcessManager {

  private ArrayList<Truck> trucks = new ArrayList<Truck>();
  private Truck currentTruck;

  /**
   * Default constructor for SequencingManager.
   * 
   * @param system master system
   */
  public LoadingManager(WarehouseSystem system) {
    super(system);
  }

  /**
   * Create a new worker.
   * 
   * @param name of the worker
   * @return Worker
   */
  public Worker hireWorker(String name) {
    Worker loader = new Loader(name, this);
    addWorker(loader);
    return loader;
  }

  /**
   * Get the next job that should be worked on. For a loader thats the one in the order they came
   * in.
   * 
   * @return Job
   * @throws WorkerJobException for worker error
   */
  public Job getNextJob() throws WorkerJobException {
    // Get the job manager
    JobManager jobManager = getSystem().getJobManager();

    Job nextJob = jobManager.getJobs().get(0);
    if (getJobsToDo().contains(nextJob)) {
      getJobsToDo().remove(nextJob);
      return nextJob;
    } else {
      throw new LoaderJobException("Highest priority job has not reached loading");
    }
  }

  /**
   * Get truck.
   * 
   * @return Truck
   */
  public Truck getTruck() {
    if (currentTruck == null) {
      currentTruck = new Truck();
      trucks.add(currentTruck);
    }
    return currentTruck;
  }
  
  /**
   * Get current truck.
   * 
   * @return Truck
   */
  public Truck getCurrentTruck() {
    return currentTruck;
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
      trucks.remove(currentTruck);
      getSystem().jobLoaded(job);

      FileHelper.logOrders(job.getOrders(), this);
      if (currentTruck.full()) {
        currentTruck = null;
        FileHelper.logEvent("Shipment sent", this);
      }
    } catch (WorkerJobException exp) {
      getSystem().raiseWarning();
      FileHelper.logError(exp, this);
      discardJob(worker);
    }
  }
}
