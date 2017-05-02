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
   * Constructor for warehouse.
   * 
   * @param ordersFile the file containing the simulation orders
   * @param dimensionsFile the file containing the dimensions of the warehouse floor
   * @praam traversalFile the file containing the sku locations
   * @param translationFile the file containing fascia to sku translations
   * @param initialFile the file containing the warehouse initial stock 
   */
  public WarehouseSystem(String ordersFile, String dimensionsFile, String traversalFile,
      String translationFile, String initialFile) {
    // Initialize managers and simulation
    FileHelper.logEvent("Initializing Warehouse", this);
    // Store the orders for the simulation
    input = FileHelper.getLinesFromFile(ordersFile);

    // Initialize job manager with input file
    jobManager = new JobManager(this, translationFile);

    // Initialize inventory with input files
    inventoryManager = new InventoryManager(this, dimensionsFile, traversalFile, initialFile);

    // Create process managers
    pickingManager = new PickingManager(this);
    sequencingManager = new SequencingManager(this);
    loadingManager = new LoadingManager(this);
  }

  /**
   * Constructor with a given input File.
   */
  public WarehouseSystem() {
    this("orders.txt", "inventory_dimensions.csv", "traversal_table.csv", "translation.csv",
        "initial.csv");
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
        /*
         * FileHelper.logEvent(
         * "Warning. Simulation may be invalid. Account for worker error in orders file", this);
         */
        simulationWarning = false;
      }
      String line = input.remove(0);

      if (line.equals("") || line.contains("#")) {
        continue;
      }

      FileHelper.tickLog();

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
    FileHelper.logEvent("Job " + job + " ready for picking", this);
  }

  /**
   * Send to marshalling.
   * 
   * @param job to send
   */
  public void sendToMarshalling(Job job) {
    FileHelper.logEvent("Job " + job + " ready for sequencing", this);
    sequencingManager.queueJob(job);
  }

  /**
   * Send to loading.
   * 
   * @param job to send
   */
  public void sendToLoading(Job job) {
    FileHelper.logEvent("Job " + job + " ready for loading", this);
    loadingManager.queueJob(job);
  }

  /**
   * Job is loaded.
   * 
   * @param job that was loaded
   */
  public void jobLoaded(Job job) {
    jobManager.removeJob(job);
    FileHelper.logEvent("Job " + job + " loaded on a truck", this);
  }

  /**
   * Remove inventory.
   * 
   * @param location to remove inventory from
   */
  public void removeFromInventory(Location location) {
    try {
      inventoryManager.removeInventory(location);
      FileHelper.logEvent("Item " + location.getSku() + " removed from inventory", this);
    } catch (NoSuchLocationException exp) {
      FileHelper.logEvent("The location " + location.toString() + " does not exist!", this);
      // exp.printStackTrace();
    }
  }

  /**
   * Replenish inventory.
   * 
   * @param location The location which needs to be replenished
   */
  public void replenishInventory(Location location) {
    inventoryManager.replenish(location);
    FileHelper.logEvent("Item " + location.getSku() + " replenished", this);
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
    String orders = null;
    String dimensions = null;
    String traversal = null;
    String translation = null;
    String initial = null;
    if (args.length != 5) {
      System.out.println("Error: missing input files, using default");
      orders = "orders.txt";
      dimensions = "inventory_dimensions.csv";
      traversal = "traversal_table.csv";
      translation = "translation.csv";
      initial = "initial.csv";
    } else {
      orders = args[0];
      dimensions = args[1];
      traversal = args[2];
      translation = args[3];
      initial = args[4];
    }

    WarehouseSystem warehouseSystem =
        new WarehouseSystem(orders, dimensions, traversal, translation, initial);
    warehouseSystem.simulate();
    warehouseSystem.endDay();
  }
}
