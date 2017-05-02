package warehouse;

import java.util.ArrayList;

public class Zone {
  /**
   * The name of this zone.
   */
  private String name;
  /**
   * The locations containing inventory in this zone.
   */
  private ArrayList<Location> locations = new ArrayList<Location>();
  /**
   * Maximum number of aisles per zone.
   */
  private int aisles;
  /**
   * Maximum number of aisles per aisle.
   */
  private int racks;
  /**
   * Maximum number of aisles per rack.
   */
  private int levels;


  /**
   * Construct a zone for the warehouse.
   * 
   * @param name of the zone
   */
  public Zone(String name, int aisles, int racks, int levels) {
    this.name = name;
    this.aisles = aisles;
    this.racks = racks;
    this.levels = levels;
  }

  /**
   * Get this zone's name.
   * 
   * @return the name of this zone
   */
  public String getName() {
    return name;
  }

  /**
   * Get the maximum number of aisles.
   * 
   * @return int
   */
  public int getAisles() {
    return aisles;
  }

  /**
   * Get the maximum number of racks.
   * 
   * @return int
   */
  public int getRacks() {
    return racks;
  }

  /**
   * Get the maximum number of levels.
   * 
   * @return int
   */
  public int getLevels() {
    return levels;
  }

  /**
   * Set this zone's name.
   * 
   * @param name this zone should have
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the locations stored in this zone.
   * 
   * @return locations stored in this zone
   */
  public ArrayList<Location> getLocations() {
    return locations;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.name;
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
    Zone other = (Zone) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  /**
   * Uses algorithm to quickly return the location indicated by the given aisle, rack, and level
   * numbers.
   * 
   * @param aisle the aisle where this location is
   * @param rack the rack where this location is
   * @param level the level of this location
   * @return location in this zone of the warehouse
   */
  public Location getLocationFromZone(int aisle, int rack, int level) {
    // Calculate the location's index using max numbers of aisles, racks, levels
    int locationIndex = aisle * racks * levels + rack * levels + level;
    // Check the range is right
    if (locationIndex >= locations.size()) {
      return null;
    }
    // Return that location
    Location location = locations.get(locationIndex);
    return location;
  }
}
