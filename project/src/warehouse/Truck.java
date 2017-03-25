package warehouse;

import java.util.ArrayList;

public class Truck {

  private final int capacity = 160;

  private ArrayList<Pallet> pallets = new ArrayList<Pallet>();

  /**
   * Default constructor.
   */
  public Truck() {

  }

  /**
   * Load a pallet onto the truck.
   * 
   * @param pallet to load
   * @return whether there was room
   */
  public boolean loadPallet(Pallet pallet) {
    if (!full()) {
      pallets.add(pallet);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Whether the truck is at capacity.
   */
  public boolean full() {
    return pallets.size() == capacity;
  }
}
