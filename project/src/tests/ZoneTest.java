package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import warehouse.Location;
import warehouse.Zone;

public class ZoneTest {
  ArrayList<Location> locations = new ArrayList<>();
  Location l1 = new Location("H", 20, 50, 10, "a8384");
  
  @Test
  public void testGetName() {
    Zone zone = new Zone("A", 10, 50, 5);
    assertEquals(zone.getName(), "A");
  }
  
  @Test
  public void testSetName() {
    Zone zone = new Zone("A", 10, 50, 5);
    zone.setName("C");
    assertEquals(zone.getName(), "C");
  }
  
  @Test
  public void testToString() {
    Zone zone = new Zone("A", 10, 50, 5);
    assertEquals(zone.toString(), "A");
  }
  
  @Test
  public void testGetLocations() {
    Zone zone = new Zone("A", 10, 50, 5);
    ArrayList<Location> arraylist = new ArrayList<>();
    arraylist.add(l1);
    
    zone.getLocations().add(l1);
    
    assertEquals(zone.getLocations(), arraylist);
  }
  
  @Test
  public void testEqualsObject() {
    Zone z1 = new Zone("A", 10, 50, 5);
    Zone z2 = new Zone("A", 10, 50, 5);
    Zone z3 = new Zone("B", 10, 50, 5);
    Zone z4 = z1;
    
    assertTrue(z1.equals(z2));
    assertFalse(z1.equals(z3));
    assertTrue(z1.equals(z4));
    assertFalse(z1.equals(null));
    
    // Test when with different classes
    assertFalse(z1.equals(l1));
    
    Zone z5 = new Zone(null, 10, 50, 5);
    assertFalse(z5.equals(z1));
  }
  
  @Test
  public void testGetters() {
    Zone z1 = new Zone("A", 10, 50, 5);
    assertEquals(z1.getAisles(), 10);
    assertEquals(z1.getRacks(), 50);
    assertEquals(z1.getLevels(), 5);
  }
  
  @Test
  public void getLocationFromZone() {
    Zone zone = new Zone("A", 50, 100, 2);
    int aisles = zone.getAisles();
    int racks = zone.getRacks();
    int levels = zone.getLevels();
    Location location = zone.getLocationFromZone(aisles, racks, levels);
    
    assertEquals(location, null);
  }
}
