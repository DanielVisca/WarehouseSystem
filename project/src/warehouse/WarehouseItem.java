package warehouse;

/**
 * An item in the warehouse, whose traits are identified by its sku.
 *
 */
public class WarehouseItem {
  private String sku;
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
  public WarehouseItem(String sku) {
    this.sku = sku;
  }

  /**
   * Return the sku number of this item.
   * 
   * @return the sku
   */
  public String getsku() {
    return sku;
  }
  
  /**
   * Set the sku number of this item.
   * 
   * @param sku of the item
   */
  public void setSku(String sku) {
    this.sku = sku;
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
