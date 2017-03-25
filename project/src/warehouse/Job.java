package warehouse;

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
  private int[] skus = null;

  // The organized list of skus to be picked and the current index of picking
  private int pickingIndex = 0;
  private Location[] pickingOrder = null;

  /**
   * Default constructor for job.
   * 
   * @param skus of the job
   * @param orders describing the skus
   */
  public Job(int[] skus, String[] orders) {
    this.skus = skus;
    this.orders = orders;
    pickingOrder = WarehousePicking.optimize(skus);
    id = "";
    for (int i : this.skus) {
      id = id + String.format("%02d", i);
    }
  }

  /**
   * Replenisher's constructor for job.
   * 
   * @param location of the level to be replenished
   */
  public Job(Location location) {
    this.skus = new int[] {location.getSku()};
    this.location = location;
    this.id = String.format("%02d", this.skus[0]);
  }

  /**
   * Next instruction request. This looks at the job, the worker, and sends the appropriate message
   * to their device.
   */
  public void nextInstruction() {
    if (worker instanceof Picker) {
      Location nextPick = null;
      if (pickingIndex < pickingOrder.length) {
        nextPick = pickingOrder[pickingIndex];
        worker.setInstruction("pick " + nextPick.getSku() + " from " + nextPick);
      } else {
        worker.setInstruction("go to marshalling");
      }
    } else if (worker instanceof Sequencer) {
      worker.setInstruction("sequence " + this);
    } else if (worker instanceof Loader) {
      worker.setInstruction("load " + this);
    } else if (worker instanceof Replenisher) {
      worker.setInstruction("replenish " + getId() + " at " + location.toString());
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
    }
    // If we're done picking return null
    if (nextPick == null) {
      FileHelper.logEvent("Missing translation data for job " + this);
      return null;
    }
    setLocation(nextPick);
    pickingIndex++;
    return nextPick;
  }

  /**
   * Get skus.
   * 
   * @return int[]
   */
  public int[] getSkus() {
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
   * 
   * @return boolean whether the job is filled
   */
  public boolean verify() {
    ArrayList<Integer> tempSkus = new ArrayList<Integer>();
    for (int i = 0; i < skus.length; i++) {
      tempSkus.add(skus[i]);
    }

    // Check items
    for (WarehouseItem item : items) {
      Integer sku = Integer.valueOf(item.getsku());
      if (tempSkus.contains(sku)) {
        tempSkus.remove(sku);
      } else {
        return false;
      }
    }

    if (pallets != null) {
      int numPallets = pallets.length;
      // Check pallets
      for (int i = 0; i < pallets.length; i++) {
        int order = (int) Math.floor(i / 2);
        if (pallets[i % numPallets].getItems().get(order).getsku() != skus[i]) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Discard contents of the job.
   */
  public void discardContents() {
    pallets = null;

    if (items != null) {
      items.clear();
      pickingIndex = 0;
    }
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
   * Job complete.
   */
  public void complete() {
    setWorker(null);
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

