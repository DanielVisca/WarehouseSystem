package warehouse;

@SuppressWarnings("serial")
public class NoSuchLocationException extends Exception {

  public NoSuchLocationException(String message) {
    super(message);
  }
}
