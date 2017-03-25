package warehouse;

public class Picker extends Worker {
  /**
   * Default constructor for picker.
   * 
   * @param name name of the picker
   */
  public Picker(String name, ProcessManager manager) {
    super(name, "Picker", manager);
  }

  /**
   * Perform the next task in the job.
   */
  public void nextTask() {
    // Get the location and sku of the next pick (and display on reader)
    if (currentJob == null) {
      logError("no job to pick from");
      return;
    }

    // Chcek that your pick matches the pick
    String[] splitStatus = getStatus().split(" ");
    if (splitStatus[0].equals("pick")) {
      int givenSku = Integer.parseInt(splitStatus[1]);
      Location nextPick = currentJob.getNextPick();

      // If no next pick then job is complete
      if (nextPick == null) {
        logError("picked extra item");
        discardCurrentJob();
        return;
      }

      // Sku expected based on last instruction
      int expectedSku = nextPick.getSku();

      if (givenSku != expectedSku) {
        logError("picked the wrong item");
        // Check if the item is right but order is wrong
        discardCurrentJob();
        return;
      } else {
        // Create the item (scan) thus removing from inventory
        pickItem(nextPick);

        // Get the next instruction
        currentJob.nextInstruction();
      }
    } else if (getStatus().equals("to Marshaling")) {
      if (verifyJob()) {
        if (currentJob.getItems().size() < currentJob.getSkus().length) {
          logError("not done picking");
        } else {
          // Everything's honkey dorry, job is complete
          logStatus();
          jobComplete();
        }
      }
    }
  }

  /**
   * Pick item.
   * 
   * @param location sku is picked from
   */
  public void pickItem(Location location) {
    WarehouseItem orderItem = new WarehouseItem(location.getSku());
    currentJob.addItem(orderItem);

    // Remove from inventory and log
    logTask("pick SKU " + location.getSku());
    ((PickingManager) manager).pickItem(location);
  }
}

