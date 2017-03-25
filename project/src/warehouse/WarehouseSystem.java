package warehouse;

import java.util.ArrayList;

public class WarehouseSystem {
  private ArrayList<String> input = new ArrayList<String>();

  // All the managers than make the warehouse work
  private JobManager jobManager;
  private InventoryManager inventoryManager;
  private PickingManager pickingManager;
  private SequencingManager sequencingManager;
  private LoadingManager loadingManager;

  // Whether to produce a warning in the simulation.
  private boolean simulationWarning = false;

  /**
   * Create a warning in the simulation, if an error was introduced it must be properly dealt with
   * in the orders input.
   */
  public void raiseWarning() {
    simulationWarning = true;
  }

  /**
   * Get job manager.
   * 
   * @return JobManager
   */
  public JobManager getJobManager() {
    return jobManager;
  }

  /**
   * Get inventory manager.
   * 
   * @return InventoryManager
   */
  public InventoryManager getInventoryManager() {
    return inventoryManager;
  }

  /**
   * Get picking manager.
   * 
   * @return PickingManager
   */
  public PickingManager getPickingManager() {
    return pickingManager;
  }

  /**
   * Get sequencing manager.
   * 
   * @return SequencingManager
   */
  public SequencingManager getSequencingManager() {
    return sequencingManager;
  }

  /**
   * Get loading manager.
   * 
   * @return LoadingManager;
   */
  public LoadingManager getLoadingManager() {
    return loadingManager;
  }

  /**
   * Default Constructor for warehouse.
   */
  public WarehouseSystem() {
    // Initialize managers and simulation
    FileHelper.logEvent("Initializing Warehouse");
    jobManager = new JobManager(this);
    inventoryManager =
        new InventoryManager(this, "traversal_table.csv", "initial.csv", 2, 2, 3, 4, 30, 5);
    pickingManager = new PickingManager(this);
    sequencingManager = new SequencingManager(this);
    loadingManager = new LoadingManager(this);
  }



  /**
   * Constructor with a given input File.
   * 
   * @param inputFile orders to allocate
   */
  public WarehouseSystem(String inputFile) {
    this();
    this.input = FileHelper.getLinesFromFile(inputFile);
  }

  /**
   * Return the input.
   * 
   * @return ArrayList
   */
  public ArrayList<String> getInput() {
    return this.input;
  }

  /**
   * Add input from the given file (FIFO).
   * 
   * @param newInput new orders to add
   */
  public void updateInput(String newInput) {
    input.addAll(FileHelper.getLinesFromFile(newInput));
  }

  /**
   * Updates the status of all managers and input given input.
   */
  public void simulate() {
    while (input.size() > 0) {
      if (simulationWarning) {
        FileHelper
            .logEvent("Warning. Simulation may be invalid. Account for worker error in orders.txt");
        simulationWarning = false;
      }

      FileHelper.tickLog();

      String line = input.remove(0);
      receiveStatus(line);

    }
  }

  /**
   * Receive status of job, and send to the manager for that step of process.
   * 
   * @param status received from system
   */
  public void receiveStatus(String status) {
    String[] lineSegments = status.split(" ", 3);
    // ex: for picker input, last segment is "pick 1"
    // update the status of each worker and orders
    switch (lineSegments[0]) {
      case "Order":
        jobManager.processOrder(lineSegments[1] + " " + lineSegments[2]);
        break;
      case "Picker":
        pickingManager.setStatus(lineSegments[1], lineSegments[2]);
        break;
      case "Loader":
        loadingManager.setStatus(lineSegments[1], lineSegments[2]);
        break;
      case "Sequencer":
        sequencingManager.setStatus(lineSegments[1], lineSegments[2]);
        break;
      case "Replenisher":
        inventoryManager.setStatus(lineSegments[1], lineSegments[2]);
        break;
      default:
        break;
    }
  }

  /**
   * Send to picking.
   * 
   * @param job to send
   */
  public void sendToPicking(Job job) {
    pickingManager.queueJob(job);
    FileHelper.logEvent("Job " + job + " ready for picking");
  }

  /**
   * Send to marshalling.
   * 
   * @param job to send
   */
  public void sendToMarshalling(Job job) {
    FileHelper.logEvent("Job " + job + " ready for sequencing");
    sequencingManager.queueJob(job);
  }


  /**
   * Send to loading.
   * 
   * @param job to send
   */
  public void sendToLoading(Job job) {
    FileHelper.logEvent("Job " + job + " ready for loading");
    loadingManager.queueJob(job);
  }

  /**
   * Job is loaded.
   * 
   * @param job that was loaded
   */
  public void jobLoaded(Job job) {
    jobManager.removeJob(job);
    FileHelper.logEvent("Job " + job + " loaded on a truck");
  }

  /**
   * Remove inventory.
   * 
   * @param location to remove inventory from
   */
  public void removeFromInventory(Location location) {
    FileHelper.logEvent("Item " + location.getSku() + " removed from inventory");
    try {
      inventoryManager.removeInventory(location);
    } catch (NoSuchLocationException e) {
      FileHelper.logEvent("The location " + location.toString() + " does not exist!");
      e.printStackTrace();
    }
  }


  /**
   * Replenish inventory.
   * 
   * @param location The location which needs to be replenished
   */
  public void replenishInventory(Location location) {
    inventoryManager.replenish(location);
    FileHelper.logEvent("Item " + location.getSku() + " replenished");
  }

  /**
   * End the day at the factory.
   */
  public void endDay() {
    inventoryManager.reportFloorStock();
    // FileHelper.writeEventsToFile("events.csv");
    FileHelper.writeOrdersToFile("orders.csv");
    // FileHelper.writeInstructionsToFile("instructions.csv");
  }

  /**
   * Main loop.
   */
  public static void main(String[] args) {
    WarehouseSystem warehouseSystem = new WarehouseSystem("orders.txt");
    warehouseSystem.simulate();
    warehouseSystem.endDay();
  }
}
