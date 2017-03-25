package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import warehouse.Location;
import warehouse.StockExceedsCapacityException;

public class LocationTest {

  @Test
  public void testGetZone() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    assertEquals(l1.getZone(), "H");
    assertEquals(l2.getZone(), "G");
    assertEquals(l3.getZone(), "J");
  }

  @Test
  public void testGetAisle() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    assertEquals(l1.getAisle(), 20);
    assertEquals(l2.getAisle(), 25);
    assertEquals(l3.getAisle(), 30);
  }

  @Test
  public void testGetRack() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    assertEquals(l1.getRack(), 50);
    assertEquals(l2.getRack(), 75);
    assertEquals(l3.getRack(), 25);
  }

  @Test
  public void testGetLevel() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    assertEquals(l1.getLevel(), 10);
    assertEquals(l2.getLevel(), 20);
    assertEquals(l3.getLevel(), 30);
  }

  @Test
  public void testGetSku() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    assertEquals(l1.getSku(), 8384);
    assertEquals(l2.getSku(), 0);
    assertEquals(l3.getSku(), 8586);
  }

  @Test
  public void testSetSku() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    l1.setSku(0);
    l2.setSku(1516);
    l3.setSku(910);
    assertEquals(l1.getSku(), 0);
    assertEquals(l2.getSku(), 1516);
    assertEquals(l3.getSku(), 910);
  }

  @Test
  public void testGetAndSetInventory() throws StockExceedsCapacityException {
    Location location = new Location("H", 20, 50, 10, 8384);
    location.setInventory(300);

    assertEquals(location.getInventory(), 300);
  }

  @Test
  public void testGetMax() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    assertEquals(l1.getMax(), 0);
    assertEquals(l2.getMax(), 100);
    assertEquals(l3.getMax(), 50);
  }

  @Test
  public void testSetMax() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    l1.setMax(500);
    l2.setMax(0);
    l3.setMax(20);

    assertEquals(l1.getMax(), 500);
    assertEquals(l2.getMax(), 0);
    assertEquals(l3.getMax(), 20);
  }

  @Test
  public void testGetMin() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    assertEquals(l1.getMin(), 0);
    assertEquals(l2.getMin(), 20);
    assertEquals(l3.getMin(), 10);
  }

  @Test
  public void testSetMin() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    l1.setMin(25);
    l2.setMin(50);
    l3.setMin(0);

    assertEquals(l1.getMin(), 25);
    assertEquals(l2.getMin(), 50);
    assertEquals(l3.getMin(), 0);
  }

  @Test
  public void testToString() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    Location l3 = new Location("J", 30, 25, 30, 8586, 50, 10);

    assertEquals(l1.toString(), "H 20 50 10");
    assertEquals(l2.toString(), "G 25 75 20");
    assertEquals(l3.toString(), "J 30 25 30");
  }

  @Test
  public void testAtMin() throws StockExceedsCapacityException {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("G", 25, 75, 20, 100, 20);
    l1.setInventory(40);
    l2.setInventory(5);

    assertFalse(l1.atMin());
    assertTrue(l2.atMin());
  }

  @Test
  public void testEqualsObject() {
    Location l1 = new Location("H", 20, 50, 10, 8384);
    Location l2 = new Location("H", 20, 40, 10, 8384);
    Location l3 = new Location("G", 20, 50, 10, 8384);
    Location l4 = new Location("H", 20, 50, 20, 8384);
    final Location l5 = new Location("H", 20, 50, 10, 8788);
    final Location l6 = new Location("H", 20, 50, 10, 8384);

    assertFalse(l1.equals(l2));
    assertFalse(l1.equals(l3));
    assertFalse(l1.equals(l4));
    assertTrue(l1.equals(l5));
    assertTrue(l1.equals(l6));
  }
}
