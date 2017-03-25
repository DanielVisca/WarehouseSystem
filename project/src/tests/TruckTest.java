package tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import warehouse.Pallet;
import warehouse.Truck;

public class TruckTest {

  @Test
  public void testLoadPallet() {
    Truck t1 = new Truck();
    Pallet pallet = new Pallet();
    boolean result = t1.loadPallet(pallet);
    assertTrue(result);

    Truck t2 = new Truck();
    for (int i = 0; i <= 160; i++) {
      t2.loadPallet(pallet);
    }
    boolean result1 = t2.loadPallet(pallet);
    assertFalse(result1);
  }

  @Test
  public void testFull() {
    Truck t1 = new Truck();
    Truck t2 = new Truck();
    Pallet p1 = new Pallet();
    Pallet p2 = new Pallet();

    t1.loadPallet(p1);
    assertFalse(t1.full());

    for (int i = 0; i <= 160; i++) {
      t2.loadPallet(p2);
    }
    assertTrue(t2.full());
  }

}
