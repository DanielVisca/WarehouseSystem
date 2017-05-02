package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import warehouse.Pallet;
import warehouse.WarehouseItem;

public class PalletTest {
  @Test
  public void testAddItem() {
    Pallet pallet = new Pallet();
    WarehouseItem item1 = new WarehouseItem("d12");
    WarehouseItem item2 = new WarehouseItem("g34");
    WarehouseItem item3 = new WarehouseItem("7172");
    pallet.addItem(item1);
    pallet.addItem(item2);
    pallet.addItem(item3);

    ArrayList<WarehouseItem> items = new ArrayList<WarehouseItem>();
    items.add(item1);
    items.add(item2);
    items.add(item3);

    assertEquals(pallet.getItems().get(0), items.get(0));
    assertEquals(pallet.getItems().get(1), items.get(1));
    assertNotEquals(pallet.getItems().get(0), items.get(1));
    assertNotEquals(pallet.getItems().get(1), items.get(2));
  }

  @Test
  public void testGetItems() {
    Pallet pallet = new Pallet();
    WarehouseItem item1 = new WarehouseItem("d12");
    WarehouseItem item2 = new WarehouseItem("g34");
    WarehouseItem item3 = new WarehouseItem("7172");
    pallet.addItem(item1);
    pallet.addItem(item2);
    pallet.addItem(item3);

    ArrayList<WarehouseItem> items = new ArrayList<>();
    items.add(item1);
    items.add(item2);
    items.add(item3);

    boolean bool = true;
    for (int i = 0; i < pallet.getItems().size(); i++) {
      if (pallet.getItems().get(i) != items.get(i)) {
        bool = false;
      }
    }
    assertTrue(bool);
  }

}
