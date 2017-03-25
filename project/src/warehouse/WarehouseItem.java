package warehouse;

/**
 * An item in the warehouse, whose traits are identified by its sku.
 *
 */
public class WarehouseItem {
  private int sku;
  private int size = 1;

  /**
   * Construct a WarehouseItem.
   */
  public WarehouseItem() {}

  /**
   * Construct a WarehouseItem with a given sku.
   * 
   * @param sku The sku number of this item.
   */
  public WarehouseItem(int sku) {
    this.sku = sku;
  }

  /**
   * Return the sku number of this item.
   * 
   * @return the sku
   */
  public int getsku() {
    return sku;
  }

  @Override
  public String toString() {
    return "Warehouse Item sku #" + this.sku;
  }

  /**
   * Get size of the item.
   * 
   * @return int
   */
  public int getSize() {
    return size;
  }

  /**
   * Set size the item.
   * 
   * @param size of the item
   */
  public void setSize(int size) {
    this.size = size;
  }
}
