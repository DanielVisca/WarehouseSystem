package warehouse;

@SuppressWarnings("serial")
public class StockExceedsCapacityException extends java.lang.Exception {

  public StockExceedsCapacityException(String message) {
    super(message);
  }
}
