package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import warehouse.Location;
import warehouse.StockExceedsCapacityException;

public class LocationTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGetZone() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    assertEquals(l1.getZone(), "H");
    assertEquals(l2.getZone(), "G");
    assertEquals(l3.getZone(), "J");
  }

  @Test
  public void testGetAisle() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    assertEquals(l1.getAisle(), 20);
    assertEquals(l2.getAisle(), 25);
    assertEquals(l3.getAisle(), 30);
  }

  @Test
  public void testGetRack() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    assertEquals(l1.getRack(), 50);
    assertEquals(l2.getRack(), 75);
    assertEquals(l3.getRack(), 25);
  }

  @Test
  public void testGetLevel() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    assertEquals(l1.getLevel(), 10);
    assertEquals(l2.getLevel(), 20);
    assertEquals(l3.getLevel(), 30);
  }

  @Test
  public void testGetSku() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    assertEquals(l1.getSku(), "a8384");
    assertNull(l2.getSku());
    assertEquals(l3.getSku(), "a8586");
  }

  @Test
  public void testSetSku() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    l1.setSku("0");
    l2.setSku("z1516");
    l3.setSku("c910");
    assertEquals(l1.getSku(), "0");
    assertEquals(l2.getSku(), "z1516");
    assertEquals(l3.getSku(), "c910");
  }

  @Test
  public void testGetAndSetInventory() throws StockExceedsCapacityException {
    Location location = new Location("H", 20, 50, 10, "a8384", 30, 5);

    location.setInventory(5);
    assertEquals(location.getInventory(), 5);
    
    exception.expect(StockExceedsCapacityException.class);
    location.setInventory(300);
  }

  @Test
  public void testGetMax() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    assertEquals(l1.getMax(), 0);
    assertEquals(l2.getMax(), 100);
    assertEquals(l3.getMax(), 50);
  }

  @Test
  public void testSetMax() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    l1.setMax(500);
    l2.setMax(0);
    l3.setMax(20);

    assertEquals(l1.getMax(), 500);
    assertEquals(l2.getMax(), 0);
    assertEquals(l3.getMax(), 20);
  }

  @Test
  public void testGetMin() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    assertEquals(l1.getMin(), 0);
    assertEquals(l2.getMin(), 20);
    assertEquals(l3.getMin(), 10);
  }

  @Test
  public void testSetMin() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    l1.setMin(25);
    l2.setMin(50);
    l3.setMin(0);

    assertEquals(l1.getMin(), 25);
    assertEquals(l2.getMin(), 50);
    assertEquals(l3.getMin(), 0);
  }

  @Test
  public void testToString() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, "a8586", 50, 10);

    assertEquals(l1.toString(), "H 20 50 10");
    assertEquals(l2.toString(), "G 25 75 20");
    assertEquals(l3.toString(), "J 30 25 30");
  }

  @Test
  public void testAtMin() throws StockExceedsCapacityException {
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    
    l2.setInventory(20);
    assertTrue(l2.atMin());
    
    exception.expect(StockExceedsCapacityException.class);
    
    Location l1 = new Location("H", 20, 50, 10, "b8384");
    l1.setInventory(-5);
    l1.setInventory(40);

    assertFalse(l1.atMin());
  }

  @Test
  public void testEqualsObject() {
    Location l1 = new Location("H", 20, 50, 10, "a8384");
    Location l2 = new Location("H", 20, 40, 10, "a8384");
    Location l3 = new Location("G", 20, 50, 10, "a8384");
    Location l4 = new Location("H,20,50,10,a8384");
    final Location l0 = l1;

    //Test with different racks
    assertFalse(l1.equals(l2));
    
    //Test with different zone
    assertFalse(l1.equals(l3));
    
    //Test with different way of constructor 
    assertTrue(l1.equals(l4));
    
    //Test with this == other
    assertTrue(l1.equals(l0));

    Location l5 = new Location("H", 20, 50, 10, "b8788");
    Location l6 = new Location("H", 20, 50, 10, "a8384");

    //Test with different id
    assertFalse(l1.equals(l5));
    
    //Test when this and obj have all same features
    assertTrue(l1.equals(l6));
    
    //String test
    String test = "H 20 50 10";
    assertFalse(l1.equals(test));
    assertFalse(l1.equals(null));
  }
}
