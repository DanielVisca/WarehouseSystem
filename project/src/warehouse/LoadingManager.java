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
   * Job is complete, hand it off.
   * 
   * @param worker who completed job
   */
  public void jobComplete(Worker worker) {
    super.jobComplete(worker);
    Job job = worker.getCurrentJob();
    trucks.remove(currentTruck);
    system.jobLoaded(job);

    FileHelper.logOrders(job.getOrders());
    if (currentTruck.full()) {
      currentTruck = null;
      FileHelper.logEvent("Shipment sent");
    }
  }
}
