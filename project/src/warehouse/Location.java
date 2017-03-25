package warehouse;

public class Location {
  // Unique identifier for this location object
  private String id;

  // Attributes indicating this location's location in physical warehouse
  private String zone;
  private int aisle;
  private int rack;
  private int level;
  // The SKU of items stored here
  private int sku;
  // The inventory level (number of items) here
  private int inventory;
  // The maximum capacity here
  private int max = 0;
  // The minimum inventory, at which a replenish request is made
  private int min = 0;

  /**
   * Construct a location.
   * 
   * @param zone The zone of this location.
   * @param aisle The aisle of this location.
   * @param rack The rack of this location.
   * @param level The level of this location.
   * @param sku The SKU of items at this location.
   */
  public Location(String zone, int aisle, int rack, int level, int sku) {
    this.zone = zone;
    this.aisle = aisle;
    this.rack = rack;
    this.level = level;
    this.sku = sku;

    createId();
  }

  /**
   * Construct a location without sku, but with its maximum and minimum capacities.
   * 
   * @param zone The zone of this location.
   * @param aisle The aisle of this location.
   * @param rack The rack of this location.
   * @param level The level of this location.
   * @param max The capacity of this location
   * @param min The minimum number of items before this location needs replenishing.
   */
  public Location(String zone, int aisle, int rack, int level, int max, int min) {
    this.zone = zone;
    this.aisle = aisle;
    this.rack = rack;
    this.level = level;
    // Capacities
    this.max = max;
    this.min = min;

    createId();
  }

  /**
   * Construct a location, including its maximum and minimum capacities.
   * 
   * @param zone The zone of this location.
   * @param aisle The aisle of this location.
   * @param rack The rack of this location.
   * @param level The level of this location.
   * @param sku The SKU of items at this location.
   * @param max The capacity of this location
   * @param min The minimum number of items before this location needs replenishing.
   */
  public Location(String zone, int aisle, int rack, int level, int sku, int max, int min) {
    this.zone = zone;
    this.aisle = aisle;
    this.rack = rack;
    this.level = level;
    this.sku = sku;
    // Capacities
    this.max = max;
    this.min = min;

    createId();
  }

  /**
   * Construct a location from a csv string, with 30 as default max and 5 as default min capacities.
   * 
   * @param csv The comma-separated value to generate this location's attributes
   */
  public Location(String csv) {
    this(csv, 30, 5);
  }

  /**
   * Construct a location from a csv string with max and min values.
   * 
   * @param csv The comma-separated value to generate this location's attributes
   * @param max The capacity of this location
   * @param min The minimum number of items before this location needs replenishing.
   */
  public Location(String csv, int max, int min) {
    String[] split = csv.split(",");
    this.zone = split[0];
    this.aisle = Integer.valueOf(split[1]);
    this.rack = Integer.valueOf(split[2]);
    this.level = Integer.valueOf(split[3]);
    this.sku = Integer.valueOf(split[4]);
    this.max = max;
    this.min = min;
    createId();
  }

  /**
   * Create id of this location based on attributes.
   */
  private void createId() {
    this.id = zone + " " + aisle + " " + rack + " " + level;
  }

  /**
   * Get the zone of this location.
   * 
   * @return Zone of this location
   */
  public String getZone() {
    return this.zone;
  }

  /**
   * Get the aisle of this location.
   * 
   * @return aisle of this location
   */
  public int getAisle() {
    return this.aisle;
  }

  /**
   * Get the rack of this location.
   * 
   * @return rack of this location
   */
  public int getRack() {
    return this.rack;
  }

  /**
   * Get the level of this location.
   * 
   * @return Level of this location
   */
  public int getLevel() {
    return this.level;
  }

  /**
   * Get the SKU at this location.
   * 
   * @return sku at this location
   */
  public int getSku() {
    return this.sku;
  }

  /**
   * Set the SKU at this location.
   * 
   * @param sku The SKU of items here
   */
  public void setSku(int sku) {
    this.sku = sku;
  }

  /**
   * Get the inventory level at this location.
   * 
   * @return inventory level at this location
   */
  public int getInventory() {
    return this.inventory;

  }

  /**
   * Set the inventory level at this location.
   * 
   * @param amount inventory level at this location
   * @throws StockExceedsCapacityException if amount to stock location exceeds its capacity
   */
  public void setInventory(int amount) throws StockExceedsCapacityException {
    if (amount > this.max) {
      throw new StockExceedsCapacityException("Attempted stock " + amount 
          + " exceeds maximum capacity of " + this.max);
    }
    this.inventory = amount;
  }

  /**
   * Get the maximum capacity at this location.
   * 
   * @return maximum capacity at this location
   */
  public int getMax() {
    return this.max;
  }

  /**
   * Set the maximum capacity at this location.
   * 
   * @param max maximum capacity at this location
   */
  public void setMax(int max) {
    this.max = max;
  }

  /**
   * Get the minimum inventory at this location.
   * 
   * @return minimum inventory at this location
   */
  public int getMin() {
    return this.min;

  }

  /**
   * Set the minimum inventory at this location.
   * 
   * @param min minimum inventory at this location
   */
  public void setMin(int min) {
    this.min = min;
  }

  /**
   * Return a string representation of Location (id).
   * 
   * @return String The ID of this location.
   */
  public String toString() {
    return this.id;
  }


  /**
   * Checks if the inventory of this location has reached its minimum level.
   * 
   * @return boolean If location has reached min inventory
   */
  public boolean atMin() {
    return inventory <= min;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Location other = (Location) obj;
    if (aisle != other.aisle) {
      return false;
    }
    if (level != other.level) {
      return false;
    }
    if (rack != other.rack) {
      return false;
    }
    if (zone == null) {
      if (other.zone != null) {
        return false;
      }
    } else if (!zone.equals(other.zone)) {
      return false;
    }
    return true;
  }
}
