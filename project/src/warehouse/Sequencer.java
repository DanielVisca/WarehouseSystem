package warehouse;

import java.util.ArrayList;

public class Sequencer extends Worker {

  private int scanIndex = 0;

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
   * Set current job.
   * 
   * @param job to set
   */
  public void setCurrentJob(Job job) {
    scanIndex = 0;
    super.setCurrentJob(job);
  }

  /**
   * Perform the next task in the job.
   */
  public void nextTask() throws WorkerJobException {
    // Check the sku of the item they sequenced and make sure it is the next sku that should be
    // sequenced
    // all items the ware house makes, use this to set the next item?
    String[] splitStatus = getStatus().split(" ");
    if (splitStatus[0].equals("rescans")) {
      scanIndex = 0;
    } else if (splitStatus[0].equals("sequences")) {

      // Check the job exists
      if (getCurrentJob() == null) {
        throw new WorkerJobException("no job to seqeunce items from");
      }

      if (splitStatus.length < 2) {
        throw new WorkerJobException("no sku attached to input status");
      }

      // Get variables and prepare pallets
      String[] skus = getCurrentJob().getSkus();
      int numOrders = getCurrentJob().getOrders().length;
      String scannedSku = getStatus().split(" ")[1];
      // Pallets can store 4 items (8 items = 2 pallets)
      int numPallets = (int) Math.ceil(skus.length / numOrders);
      
      if (scanIndex == 0) {
        getCurrentJob().preparePallets(numPallets);
      }

      // Make sure there are items to sequence
      ArrayList<WarehouseItem> items = getCurrentJob().getItems();
      if (items.size() == 0) {
        throw new SequencerJobException("no items to sequence for job " + getCurrentJob().getId());
      }

      // Calculate index
      int expectedSkuIndex =
          (scanIndex % numOrders) * numPallets + (int) Math.floor(scanIndex / numOrders);
      // make sure the item scanned is correct item
      if (scannedSku.equals(skus[expectedSkuIndex])) {
        WarehouseItem nextItem = null;
        for (WarehouseItem item : items) {
          if (item.getsku().equals(skus[expectedSkuIndex])) {
            nextItem = item;
            items.remove(item);
            break;
          }
        }
        // Oops, there's a problem, missing item!
        if (nextItem == null) {
          scanIndex = 0;
          throw new SequencerJobException("missing items to sequence");
        } else {
          ((SequencingManager) getManager()).sequenceItem(this, expectedSkuIndex % numPallets,
              nextItem);
        }
        scanIndex = (scanIndex + 1) % skus.length;
      } else {
        throw new SequencerJobException("wrong item sequenced");
      }
    } else if (getStatus().equals("to loading")) {
      // Everything's honkey dorry, job is complete
      getManager().jobComplete(this);
    }
  }
}
