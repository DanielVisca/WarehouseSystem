package warehouse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class used solely for optimizing lists of SKUs for best picking order.
 *
 */
public class WarehousePicking {

  // Cache of locations
  private static HashMap<Integer, Location> locations = null;


  /**
   * Get the list of locations in the warehouse by sku.
   * 
   * @return HashMap
   */
  public static HashMap<Integer, Location> getLocations() {
    // Cache locations
    if (locations == null) {
      locations = new HashMap<Integer, Location>();
      ArrayList<String> lines = FileHelper.getLinesFromFile("traversal_table.csv");
      // Store locations by sku key
      for (String line : lines) {
        Location location = new Location(line);
        locations.put(location.getSku(), location);
      }
    }
    return locations;
  }

  /**
   * Get the location of a specific sku.
   * 
   * @param sku integer
   */
  public static Location getLocation(int sku) {
    return getLocations().get(sku);
  }

  /**
   * Based on the Integer SKUs in List 'skus', return a List of locations, where each location is a
   * String containing 5 pieces of information: the zone character (in the range ['A'..'B']), the
   * aisle number (an integer in the range [0..1]), the rack number (an integer in the range
   * ([0..2]), and the level on the rack (an integer in the range [0..3]), and the SKU number.
   * 
   * @param skus the list of SKUs to retrieve.
   * @return the List of locations.
   */
  public static Location[] optimize(int[] skus) {
    Location[] optimizedLocations = new Location[skus.length];

    // Go through each SKU
    for (int i = 0; i < skus.length; i++) {
      // Get locations from sku

      optimizedLocations[i] = getLocation(skus[i]);
    }

    return optimizedLocations;
  }
}
