package warehouse;

public class Loader extends Worker {

  /**
   * Default constructor for loader.
   * 
   * @param name name of the loader
   * @param manager manager of this loader
   */
  public Loader(String name, ProcessManager manager) {
    super(name, "Loader", manager);

  }

  /**
   * Perform the next task in the job.
   */
  public void nextTask() {
    LoadingManager loadingManager = (LoadingManager) manager;
    Truck truck = loadingManager.getTruck();
    for (Pallet p : currentJob.getPallets()) {
      truck.loadPallet(p);
    }
    logTask("loads " + currentJob);
    jobComplete();
  }
}
