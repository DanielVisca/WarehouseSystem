package warehouse;

import java.util.ArrayList;

public class Sequencer extends Worker {

  /**
   * Default constructor for sequencer.
   * 
   * @param name name of the sequencer
   * @param manager manager of this sequencer
   */
  public Sequencer(String name, ProcessManager manager) {
    super(name, "Sequencer", manager);
  }

  /**
   * Perform the next task in the job.
   */
  public void nextTask() {
    ArrayList<WarehouseItem> items = currentJob.getItems();
    int[] skus = currentJob.getSkus();

    if (items.size() == 0) {
      logError("no items to sequence for job " + currentJob.getId());
      discardCurrentJob();
      return;
    }

    // Pallets can store 4 'unit size'
    int numPallets = (int) Math.ceil(items.size() * items.get(0).getSize() / 4);
    currentJob.preparePallets(numPallets);

    // Get the next item with sku
    for (int i = 0; i < skus.length; i++) {
      // Get item of sku value
      WarehouseItem nextItem = null;
      for (WarehouseItem item : items) {
        if (item.getsku() == skus[i]) {
          nextItem = item;
          items.remove(item);
          break;
        }
      }

      // Oops, there's a problem, missing item!
      if (nextItem == null) {
        logError("missing items to sequence");
        discardCurrentJob();
        return;
      } else {
        sequenceItem(i % numPallets, nextItem);
      }
    }

    // Verify job, guaranteed correct at this point because computer sequenced it not a person
    /*
     * if (!verifyJob()) { logError("sequencing is incorrect"); discardCurrentJob(); } else {
     */
    logTask("sequences " + currentJob);
    jobComplete();
    // }
  }

  /**
   * Sequence item on pallets.
   * 
   * @param item to load
   */
  private void sequenceItem(int pallet, WarehouseItem item) {
    // Add the item to the pallet in alternating order
    currentJob.getPallets()[pallet].addItem(item);
  }
}
