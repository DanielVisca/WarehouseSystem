package tests;

import static org.junit.Assert.assertEquals;

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

    FileHelper.logEvent("Test1");
    FileHelper.logEvent("Test2");
    FileHelper.logOrders(new String[] {"Order1", "Order2", "Order3", "Order4"});

    FileHelper.writeEventsToFile("test.csv");
    ArrayList<String> events = FileHelper.getLinesFromFile("test.csv");
    assertEquals(events.get(0), "Test1");
    assertEquals(events.get(1), "Test2");

    FileHelper.writeOrdersToFile("test.csv");
    ArrayList<String> orders = FileHelper.getLinesFromFile("test.csv");
    assertEquals(orders.get(0), "Order1");
    assertEquals(orders.get(1), "Order2");
    assertEquals(orders.get(2), "Order3");
    assertEquals(orders.get(3), "Order4");
  }
}
