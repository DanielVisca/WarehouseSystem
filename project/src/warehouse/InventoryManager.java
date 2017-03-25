package warehouse;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * The manager of inventory, handles locations, inventory levels, replenish requests.
 *
 */
public class InventoryManager extends ProcessManager {
  // ATTRIBUTES HERE
  /**
   * A string of possible names for newly created zones.
   */
  public static String zoneNames = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  /**
   * An array of all locations that store inventory in the floor.
   */
  protected ArrayList<Location> floor = new ArrayList<Location>();


  // Maximum number of zones
  protected int zones;
  // Maximum number of aisles per zone
  protected int aisles;
  // Maximum number of racks per aisle
  protected int racks;
  // Maximum number of levels per rack
  protected int levels;


  // METHODS FOLLOW
  /**
   * Get the zones in the floor.
   * 
   * @return The zones in the floor.
   */
  public ArrayList<Location> getFloor() {
    return floor;
  }

  /**
   * Construct an InventoryManager and the floor it manages, using a given traversal file as a guide
   * to the locations of the items. Assume all levels have the same maximum and minimum capacities.
   * 
   * @param system The WarehouseSystem this manager communicates with
   * @param traversalFile The file to generate a map of the inventory
   * @param initialFile The file listing locations that start with less than full inventory
   * @param zones The number of zones of the floor of the warehouse.
   * @param aisles The number of aisles per zone
   * @param racks The number of racks per aisle
   * @param levels The number of levels per rack
   * @param max The maximum number of items per level
   * @param min The minimum number of items per level
   */
  public InventoryManager(WarehouseSystem system, String traversalFile, String initialFile,
      int zones, int aisles, int racks, int levels, int max, int min) {
    // Connect to WarehouseSystem
    super(system);
    this.zones = zones;
    this.aisles = aisles;
    this.racks = racks;
    this.levels = levels;
    csvToFloor(traversalFile, max, min);
    // Fill floor with inventory
    try {
      stockFloor(initialFile);
    } catch (StockExceedsCapacityException e) {
      System.out.println("Please double-check initial.csv to ensure stock levels" +
                         "don't exceed the maximum capacity of locations in the inventory.");
      e.printStackTrace();
    }
  }


  /**
   * Generates the inventory floor's locations using a traversal table.
   * 
   * @param inputFile The CSV file based on which to organize the locations in the floor
   */
  public void csvToFloor(String inputFile, int max, int min) {
    // Generate an array of size 5 to get location values from
    ArrayList<String> locations = FileHelper.getLinesFromFile(inputFile);
    // Go through strings in array from file to populate map
    for (int i = 0; i < locations.size(); i++) {
      // Check that this row contains enough values for location
      String[] line = locations.get(i).split(",");
      if (line.length < 5) {
        System.out.println("Error: The traversal file contains missing values in row " + (i + 1)
            + ". Please check your input file and try again.");
      } else if (line.length > 5) {
        System.out.println("Error: The traversal file contains extra values in row " + (i + 1)
            + ". Please check your input file and try again.");
      } else {
        Location location = new Location(locations.get(i), max, min);
        floor.add(location);
      }
    }
  }


  /**
   * Stock all locations in the warehouse with according to the input CSV file.
   * 
   * @param initialFile The input file, a CSV of all locations and their inventory levels
   * @throws StockExceedsCapacityException if an input line has stock level exceeding max capacity
   */
  public void stockFloor(String initialFile) throws StockExceedsCapacityException {
    // Turn input file into an Array of strings
    ArrayList<String> locationStock = FileHelper.getLinesFromFile(initialFile);
    // Stock levels not listed in initialFile to full
    stockEmptyLevels();
    // Iterate through each string to get location data for less than full levels
    for (int i = 0; i < locationStock.size(); i++) {
      // Turn the first four parts of the string into location details
      String line = locationStock.get(i);
      // Remove commas
      String[] details = line.split(",");
      // Check that there are enough values in this line
      if (details.length < 5) {
        System.out.println("Error: The input file contains missing values in row " + (i + 1)
            + ". Please check your input file and try again.");
      } else if (details.length > 5) {
        System.out.println("Error: The input file contains extra values in row " + (i + 1)
            + ". Please check your input file and try again.");
      } else {
        try {
          Location location = getLocationAtCoordinates(details[0], Integer.valueOf(details[1]),
              Integer.valueOf(details[2]), Integer.valueOf(details[3]));
          int amount = Integer.parseInt(details[4]);
          // Check that amount is not above max capacity
          if (amount > location.getMax()) {
            throw new StockExceedsCapacityException("The input file suggests a stock level in row "
                + (i + 1) + " that exceeds maximum capacity " + location.getMax());
          } else {
            location.setInventory(amount);
            // Replenish racks as necessary (otherwise orders stall)
            checkAndReplenish(location);            
          }
        } catch (NoSuchLocationException noLocation) {
          System.out.println("Error: The location listed at row " + (i + 1)
              + " of the input file does not exist in the warehouse. Please double-check.");
        }
      }
    }
  }

  /**
   * Fills all empty levels in the inventory floor.
   */
  public void stockEmptyLevels() {
    for (Location location : floor) {
      try {
        location.setInventory(location.getMax());
      } catch (StockExceedsCapacityException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Report the stock of all levels in the warehouse, and output to final.csv
   */
  public void reportFloorStock() {
    ArrayList<String> locationStock = new ArrayList<String>();

    // Iterate through locations
    for (Location location : floor) {      
      String locationString = location.toString();
      if(location.getInventory() == location.getMax())
        continue;
      // Add commas
      String locationcsv = locationString.replaceAll(" ", ",");
      locationStock.add(locationcsv + "," + location.getInventory());
    }

    FileHelper.writeLinesToFile("final.csv",
        locationStock.toArray(new String[locationStock.size()]));
  }

  /**
   * Return a location by its string representation.
   * 
   * @param locationString The string representation of the location
   * @return The location
   */
  public Location getLocationByString(String locationString) {
    for (Location location : floor) {
      if (location.toString() == locationString) {
        return location;
      }
    }
    return null;
  }

  /**
   * Return the location of this item based on its sku.
   * 
   * @param sku The sku of the item whose location must be returned
   * @return The location of this item, listing its location attributes in an ArrayList
   */
  public Location getLocationFromSku(int sku) {
    for (Location location : floor) {
      if (location.getSku() == sku) {
        return location;
      }
    }
    return null;
  }

  /**
   * Return the sku at this location.
   * 
   * @param location The location whose sku must be found
   * @return The sku at this location
   */
  public int getSkuFromLocation(Location location) {
    for (Location anyLocation : floor) {
      if (anyLocation.equals(location)) {
        return anyLocation.getSku();
      }
    }
    throw new NoSuchElementException("This location does not exist.");
  }

  /**
   * Get location at coordinates.
   * 
   * @param zone of location
   * @param aisle of location
   * @param rack of location
   * @param level of location
   * @return Location
   * @throws NoSuchLocationException if these coordinates point to a nonexistent location
   */
  public Location getLocationAtCoordinates(String zone, int aisle, int rack, int level) 
      throws NoSuchLocationException {
    // Throw exception if this location does not exist
    Location lastLocation = floor.get(floor.size() - 1);
    char lastZone = lastLocation.getZone().charAt(0);
    if (zone.charAt(0) > lastZone || aisle > lastLocation.getAisle() || 
        rack > lastLocation.getRack() || level > lastLocation.getLevel()) {
      throw new NoSuchLocationException("This location doesn't exist in the inventory floor.");
    }
    int zoneIndex = zoneNames.indexOf(zone.charAt(0));
    // Calculate the index
    int locationIndex = (((zoneIndex * aisles) + aisle) * racks + rack) * levels + level;
    Location location = floor.get(locationIndex);
    return location;
  }

  /**
   * Get location from location. Gives a local version of a given location.
   * 
   * @param location to match
   * @return Location
   * @throws NoSuchLocationException if this location does not exist (but it should)
   */
  public Location getLocalLocation(Location location) throws NoSuchLocationException {
    return getLocationAtCoordinates(location.getZone(), location.getAisle(), location.getRack(),
        location.getLevel());
  }

  /**
   * Remove an item from the associated location. After removal, check if replenishing is needed.
   * 
   * @param inputLocation The location of the item to remove
   * @throws NoSuchLocationException if this location does not exist
   */
  public void removeInventory(Location inputLocation) throws NoSuchLocationException {
    // Since the input location is a duplicate from the WarehousePicking.optimize system, equivalent
    // location needs to be found
    Location location = getLocalLocation(inputLocation);
    // Go to location and remove item
    try {
      location.setInventory(location.getInventory() - 1);
    } catch (StockExceedsCapacityException e) {
      e.printStackTrace();
    }
    // Check for replenishing
    checkAndReplenish(location);
  }

  /**
   * Check a given location to see if it needs replenishing. If it does, give the job to a
   * replenisher.
   * 
   * @param location The location to check
   */
  public void checkAndReplenish(Location location) {
    // Check if that level needs replenishing
    if (location.atMin()) {
      // Make sure this job isn't already queued
      boolean jobExists = false;
      for (Job j : jobsToDo) {
        if (j.getLocation().equals(location)) {
          jobExists = true;
          break;
        }
      }
      if (!jobExists) {
        // Create a replenishing job, assign to a worker, and add to queue
        Job job = new Job(location);
        this.queueJob(job);
        FileHelper.logEvent(
            "Item " + location.getSku() + " at " + job.getLocation() + " needs replenishing");
      }
    }
  }

  /**
   * Replenish this location.
   * 
   * @param location The location to replenish.
   */
  public void replenish(Location location) {
    try {
      location.setInventory(location.getMax());
    } catch (StockExceedsCapacityException e) {
      e.printStackTrace();
    }
  }

  /**
   * Remove completed replenishing job from job queue.
   * 
   * @param worker The worker who has completed the job
   */
  public void jobComplete(Worker worker) {
    // Get location from location string of job
    Location location = worker.currentJob.getLocation();
    super.jobComplete(worker);
    system.replenishInventory(location);
  }

  /**
   * Create a new worker.
   * 
   * @param name of the worker
   * @return Worker
   */
  public Worker hireWorker(String name) {
    Worker replenisher = new Replenisher(name, this);
    addWorker(replenisher);
    return replenisher;
  }
}
