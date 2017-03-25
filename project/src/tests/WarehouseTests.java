package tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import warehouse.WarehouseItem;

public class WarehouseTests {

  // WarehouseItem test starts here.
  @Test
  public void getskuWarehouseItemTest() {
    WarehouseItem item1 = new WarehouseItem(7980);
    WarehouseItem item2 = new WarehouseItem();

    assertEquals(item1.getsku(), 7980);
    assertEquals(item2.getsku(), 0);
  }

  @Test
  public void sizeTest() {
    WarehouseItem item1 = new WarehouseItem(7980);
    WarehouseItem item2 = new WarehouseItem();
    WarehouseItem item3 = new WarehouseItem();
    item3.setSize(10);

    assertEquals(item1.getSize(), 1);
    assertEquals(item2.getSize(), 1);
    assertEquals(item3.getSize(), 10);
  }

  @Test
  public void toStringTest() {
    WarehouseItem item = new WarehouseItem(8182);
    String result = "Warehouse Item sku #8182";

    assertEquals(item.toString(), result);
  }
}
