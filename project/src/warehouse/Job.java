package warehouse;

import java.lang.String;
import java.util.ArrayList;

public class Job {

  // Unique identifier
  private String id;
  // Worker assigned to this job
  private Worker worker = null;

  // Below are potential properties of the job
  // Picked items
  private ArrayList<WarehouseItem> items = new ArrayList<WarehouseItem>();
  // Pallets in the job
  private Pallet[] pallets = null;

  // Location of job
  private Location location;

  // Descriptive list of orders
  private String[] orders;

  // List of the actual skus
  private String[] skus = null;

  // The organized list of skus to be picked and the current index of picking
  private int pickingIndex = 0;
  private Location[] pickingOrder = null;

  /**
   * Default constructor for job.
   * 
   * @param system this job functions within, to optimize traversal
   * @param skus of the job
   * @param orders describing the skus
   */
  public Job(WarehouseSystem system, String[] skus, String[] orders) {
    this.skus = skus;
    this.orders = orders;
    pickingOrder = system.getInventoryManager().optimize(skus);
    // For some reason string.join would not compile at command line
    // id = String.join("-", skus);
    id = "";
    for (int i = 0; i < skus.length; i++) {
      if (i != 0) {
        id += "-";
      }
      id += skus[i];
    }
  }

  /**
   * Replenisher's constructor for job.
   * 
   * @param location of the level to be replenished
   */
  public Job(Location location) {
    this.skus = new String[] {location.getSku()};
    this.location = location;
    this.id = this.skus[0];
  }

  /**
   * Next instruction request. This looks at the job, the worker, and sends the appropriate message
   * to their device.
   */
  public String nextInstruction() {
    if (worker instanceof Picker) {
      Location nextPick = null;
      if (pickingIndex < pickingOrder.length) {
        nextPick = pickingOrder[pickingIndex];
        return "pick " + nextPick.getSku() + " from " + nextPick;
      } else {
        return "go to marshalling";
      }
    } else if (worker instanceof Sequencer) {
      return "sequence " + this;
    } else if (worker instanceof Loader) {
      return "load " + this;
    } else { // if (worker instanceof Replenisher) {
      return "replenish " + getId() + " at " + location.toString();
    } 
  }

  /**
   * Get the next location to pick.
   * 
   * @return String representing location and sku
   */
  public Location getNextPick() {
    Location nextPick = null;
    if (pickingIndex < pickingOrder.length) {
      nextPick = pickingOrder[pickingIndex];
    } else {
      return null;
    }
    setLocation(nextPick);
    pickingIndex++;
    return nextPick;
  }

  /**
   * Pick failed, roll back index.
   */
  public void pickFailed() {
    pickingIndex--;
  }

  /**
   * Get skus.
   * 
   * @return String[]
   */
  public String[] getSkus() {
    return skus;
  }

  /**
   * Get picking order.
   * 
   * @return Location[]
   */
  public Location[] getPickingOrder() {
    return pickingOrder;
  }

  /**
   * Get the orders in this job.
   * 
   * @return String[]
   */
  public String[] getOrders() {
    return orders;
  }

  /**
   * Get items.
   * 
   * @return ArrayList
   */
  public ArrayList<WarehouseItem> getItems() {
    return items;
  }

  /**
   * Add item to the jobs contents.
   * 
   * @param item to add
   */
  public void addItem(WarehouseItem item) {
    items.add(item);
  }


  /**
   * Prepare a number of pallets.
   * 
   * @param numPallets to prepare
   */
  public void preparePallets(int numPallets) {
    // If pallets exists, remove the items from them
    if (pallets != null) {
      for (int i = 0; i < pallets.length; i++) {
        for (WarehouseItem item : pallets[i].getItems()) {
          addItem(item);
        }
      }
    }
    pallets = new Pallet[numPallets];
    for (int i = 0; i < numPallets; i++) {
      pallets[i] = new Pallet();
    }
  }


  /**
   * Set the pallets for this job.
   * 
   * @param pallets for this job
   */
  public void setPallets(Pallet[] pallets) {
    this.pallets = pallets;
  }

  /**
   * Get pallets.
   * 
   * @return ArrayList
   */
  public Pallet[] getPallets() {
    return pallets;
  }

  /**
   * Get id of the job.
   * 
   * @return string
   */
  public String getId() {
    return id;
  }

  /**
   * Set id of the job.
   * 
   * @param id of the job
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * String representation of the job.
   * 
   * @return String
   */
  public String toString() {
    return id;
  }

  /**
   * Verify the current job.
   */
  public void verify() throws WorkerJobException {
    // If there are no pallets, its a picking validation
    if (pallets == null && worker instanceof Picker) {
      if (getItems().size() < getSkus().length) {
        throw new SequencerJobException("Wrong number of items picked");
      }

      // Verify all skus accounted for
      ArrayList<String> tempSkus = new ArrayList<String>();
      for (int i = 0; i < skus.length; i++) {
        tempSkus.add(skus[i]);
      }

      // Check items
      for (WarehouseItem item : items) {
        String sku = item.getsku();
        if (tempSkus.contains(sku)) {
          tempSkus.remove(sku);
        } else {
          throw new SequencerJobException("Invalid item(s) in picked job.");
        }
      }
      // Otherwise, its a seqeuncing validation
    } else if (worker instanceof Sequencer || worker instanceof Loader) {
      // Verify the correct amount of items on pallets
      int totalItems = 0;
      for (int i = 0; i < pallets.length; i++) {
        totalItems += pallets[i].getItems().size();
      }

      if (totalItems != skus.length) {
        throw new SequencerJobException("Wrong number of items on pallets");
      }

      // Verify all skus accounted for
      ArrayList<String> tempSkus = new ArrayList<String>();
      for (int i = 0; i < skus.length; i++) {
        tempSkus.add(skus[i]);
      }
      for (int i = 0; i < pallets.length; i++) {
        for (WarehouseItem item : pallets[i].getItems()) {
          String sku = item.getsku();
          if (tempSkus.contains(sku)) {
            tempSkus.remove(sku);
          } else {
            // Wrong item on pallet!
            throw new SequencerJobException("Invalid item(s) in picked job.");
          }
        }
      }
    }
  }

  /**
   * Discard contents of the job.
   */
  public void resetJob() {
    pallets = null;

    if (items != null) {
      items.clear();
      pickingIndex = 0;
    }

    setWorker(null);
  }

  /**
   * Set location of the job.
   * 
   * @param location as String
   */
  public void setLocation(Location location) {
    this.location = location;
  }

  /**
   * Get the location of this job.
   * 
   * @return Location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Set the worker assigned to this job.
   * 
   * @param worker to assign
   */
  public void setWorker(Worker worker) {
    this.worker = worker;
  }

  /**
   * Get the worker assigned to this job.
   * 
   * @return Worker
   */
  public Worker getWorker() {
    return worker;
  }

  /**
   * Get picking index.
   * 
   * @return int
   */
  public int getPickingIndex() {
    return pickingIndex;
  }
}

