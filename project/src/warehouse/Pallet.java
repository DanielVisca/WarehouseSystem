package warehouse;

import java.util.ArrayList;

public class Pallet {

  private ArrayList<WarehouseItem> items = new ArrayList<WarehouseItem>();

  /**
   * Default constructor.
   */
  public Pallet() {

  }

  /**
   * Add item to the pallet.
   * 
   * @param item to be added
   */
  public void addItem(WarehouseItem item) {
    items.add(item);
  }

  /**
   * Get the items contained in this pallet.
   * 
   * @return ArrayList
   */
  public ArrayList<WarehouseItem> getItems() {
    return items;
  }
}
