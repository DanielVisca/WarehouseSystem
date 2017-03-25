package warehouse;

@SuppressWarnings("serial")
public class StockExceedsCapacityException extends Exception {
  
  public StockExceedsCapacityException(String message) {
    super(message);
  }

}
