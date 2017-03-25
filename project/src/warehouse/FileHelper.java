package warehouse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

public class FileHelper {

  private static boolean silentLogging = false;
  private static ArrayList<String> events = new ArrayList<String>();
  private static ArrayList<String> instructions = new ArrayList<String>();
  private static ArrayList<String> orders = new ArrayList<String>();

  private static int prevLogTicker = -1;
  private static int logTicker = 0;

  /**
   * Tick the log, this is to more easily identify steps in the simulation.
   */
  public static void tickLog() {
    logTicker++;
  }

  /**
   * Get log prefix according to the tick. If its the first for the tick, give a number, otherwise
   * whitespace.
   */
  private static String getLogPrefix() {
    if (logTicker != prevLogTicker) {
      prevLogTicker = logTicker;

      return String.format("%2s", logTicker) + ": ";
    } else {
      // Return 4 spaces to align
      return new String(new char[4]).replace("\0", " ");
    }
  }

  /**
   * Set silent logging.
   * 
   * @param silent whether its silent
   */
  public static void setSilentLogging(boolean silent) {
    silentLogging = silent;
  }

  /**
   * Get a list of lines from a given file.
   * 
   * @return ArrayList
   */
  public static ArrayList<String> getLinesFromFile(String file) {
    BufferedReader br = null;
    FileReader fr = null;
    String line = "";
    ArrayList<String> lines = new ArrayList<String>();
    try {
      fr = new FileReader(file);
      br = new BufferedReader(fr);
      while ((line = br.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException exp) {
      exp.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException exp) {
          exp.printStackTrace();
        }
      }
      if (fr != null) {
        try {
          fr.close();
        } catch (IOException exp) {
          exp.printStackTrace();
        }
      }
    }
    return lines;
  }

  /**
   * Write lines to file.
   * 
   * @param file where to write it
   * @param lines to write
   */
  public static void writeLinesToFile(String file, String[] lines) {
    BufferedWriter bw = null;
    FileWriter fw = null;
    try {
      fw = new FileWriter(file);
      bw = new BufferedWriter(fw);
      for (String line : lines) {
        bw.write(line + "\n");
      }
    } catch (IOException exp) {
      exp.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException exp) {
          exp.printStackTrace();
        }
      }
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException exp) {
          exp.printStackTrace();
        }
      }
    }
  }


  /**
   * Log an event in the warehouse.
   * 
   * @param event event to log
   */
  public static void logEvent(String event) {
    String logPrefix = getLogPrefix();
    if (!silentLogging) {
      System.out.println(logPrefix + event);
    }
    events.add(event);
  }

  /**
   * Log orders.
   * 
   * @param orders to log
   */
  public static void logOrders(String[] orders) {
    String logPrefix = getLogPrefix();
    for (int i = 0; i < orders.length; i++) {
      if (!silentLogging) {
        System.out.println(logPrefix + orders[i] + " is on a truck");
      }
      FileHelper.orders.add(orders[i]);
    }
  }

  /**
   * Log instructions.
   * 
   * @param instruction to log
   */
  public static void logInstruction(String instruction) {
    String logPrefix = getLogPrefix();
    if (!silentLogging) {
      System.out.println(logPrefix + instruction);
    }
    instructions.add(instruction);
  }

  /**
   * Write events to file.
   * 
   * @param file name of file
   */
  public static void writeEventsToFile(String file) {
    writeLinesToFile(file, events.toArray(new String[events.size()]));
  }

  /**
   * Write orders to file.
   * 
   * @param file name of file
   */
  public static void writeOrdersToFile(String file) {
    writeLinesToFile(file, orders.toArray(new String[orders.size()]));
  }

  /**
   * Write instructions to file.
   * 
   * @param file name of file
   */
  public static void writeInstructionsToFile(String file) {
    writeLinesToFile(file, instructions.toArray(new String[instructions.size()]));
  }
}
