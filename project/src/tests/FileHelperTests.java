package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

// import org.junit.After;
// import org.junit.Before;
import org.junit.Test;

import warehouse.FileHelper;

public class FileHelperTests {

  @Test
  public void readWriteTest() {
    String[] lines = new String[] {"one", "two", "three", "four"};
    FileHelper.writeLinesToFile("test.csv", lines);

    ArrayList<String> linesIn = FileHelper.getLinesFromFile("test.csv");

    for (int i = 0; i < lines.length; i++) {
      assertEquals(lines[i], linesIn.get(i));
    }

  }

  @Test
  public void logTest() {

    FileHelper.logEvent("Test1", this);
    FileHelper.logEvent("Test2", this);
    FileHelper.logOrders(new String[] {"Order1", "Order2", "Order3", "Order4"}, this);

    FileHelper.writeLogToFile("test.csv");
    ArrayList<String> events = FileHelper.getLinesFromFile("test.csv");
    assertTrue(events.get(0).contains("Test1"));
    assertTrue(events.get(1).contains("Test2"));

    FileHelper.writeOrdersToFile("test.csv");
    ArrayList<String> orders = FileHelper.getLinesFromFile("test.csv");
    assertTrue(orders.get(0).contains("Order1"));
    assertTrue(orders.get(1).contains("Order2"));
    assertTrue(orders.get(2).contains("Order3"));
    assertTrue(orders.get(3).contains("Order4"));
  }
}
