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
   * 
   * @throws WorkerJobException for worker error
   */
  public void nextTask() throws WorkerJobException {
    // Get the location and sku of the next pick (and display on reader)
    if (getCurrentJob() == null) {
      throw new PickerJobException("no job to pick from");
    }

    // Chcek that your pick matches the pick
    String[] splitStatus = getStatus().split(" ");
    if (splitStatus[0].equals("pick")) {
      String givenSku = splitStatus[1];
      Location nextPick = getCurrentJob().getNextPick();

      // If no next pick then job is complete
      if (nextPick == null) {
        throw new PickerJobException("picked extra item.");
      }

      // Sku expected based on last instruction
      String expectedSku = nextPick.getSku();

      if (!givenSku.equals(expectedSku)) {
        getCurrentJob().pickFailed();
        throw new PickerJobException("picked wrong item.");
      } else {

        // Send result of picking attempt
        ((PickingManager) getManager()).pickItem(this, nextPick);
      }
    } else if (getStatus().equals("to marshalling")) {
      // Everything's honkey dorry, job is complete
      getManager().jobComplete(this);
    }
  }
}

