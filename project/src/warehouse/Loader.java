package warehouse;

public class Loader extends Worker {

  private int scanIndex = 0;
  
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
   * Set current job.
   * 
   * @param job to set
   */
  public void setCurrentJob(Job job) {
    scanIndex = 0;
    super.setCurrentJob(job);
  }

  /**
   * Perform the next task in the job.
   */
  public void nextTask() throws WorkerJobException {
    String[] splitStatus = getStatus().split(" ");
    if (splitStatus[0].equals("rescans")) {
      scanIndex = 0;
    } else if (splitStatus[0].equals("scans")) {

      // Check the job exists
      if (getCurrentJob() == null) {
        throw new WorkerJobException("no job to scan items from");
      }

      if (splitStatus.length < 2) {
        throw new WorkerJobException("no sku attached to input status");
      }

      // Get variables and prepare pallets
      String[] skus = getCurrentJob().getSkus();
      int numOrders = getCurrentJob().getOrders().length;
      String scannedSku = getStatus().split(" ")[1];
      int numPallets = (int) Math.ceil(skus.length / numOrders);
      
      // Calculate index
      int expectedSkuIndex =
          (scanIndex % numOrders) * numPallets + (int) Math.floor(scanIndex / numOrders);

      // make sure the item scanned is correct item
      if (scannedSku.equals(skus[expectedSkuIndex])) {
        FileHelper.logEvent("Item " + skus[expectedSkuIndex] + " in correct sequence", this);
        scanIndex = (scanIndex + 1) % skus.length;
      } else {
        throw new SequencerJobException("pallet has wrong sequence of items");
      }
    } else if (splitStatus[0].equals("loads")) {
      LoadingManager loadingManager = (LoadingManager) getManager();
      Truck truck = loadingManager.getTruck();
      if (getCurrentJob() == null) {
        throw new WorkerJobException("no job to load");
      }

      for (Pallet p : getCurrentJob().getPallets()) {
        truck.loadPallet(p);
      }
      getManager().jobComplete(this);
    }
  }
}
