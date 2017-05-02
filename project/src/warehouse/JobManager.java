package warehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class JobManager {

  // Queue of orders
  private LinkedList<String> orders = new LinkedList<String>();
  // Reference table for item skus
  private HashMap<String, String> skus = new HashMap<String, String>();
  // All jobs currently in the warehouse
  private ArrayList<Job> jobs = new ArrayList<Job>();
  // Master system
  private WarehouseSystem system;

  /**
   * Default constructor for JobManager.
   * 
   * @param system to connect to
   * @param skuFile to get sku information from
   */
  public JobManager(WarehouseSystem system, String skuFile) {
    this.system = system;
    addSkusFromFile(skuFile);
  }

  /**
   * Add skus from a file using a given add method.
   * 
   * @param file to load from
   */
  private void addSkusFromFile(String file) {
    ArrayList<String> lines = FileHelper.getLinesFromFile(file);
    lines.remove(0);
    for (String s : lines) {
      addFascia(s);
    }
  }

  /**
   * Add a fascia to the skus.
   * 
   * @param fascia a string representation of the fascia
   */
  private void addFascia(String fascia) {
    String[] translation = fascia.split(",");
    String key = translation[1] + translation[0];
    addSku(key + "F", translation[2]);
    addSku(key + "B", translation[3]);
  }

  /**
   * Add a sku to list.
   * 
   * @param key the descriptive key value of the sku
   * @param sku to add
   */
  public void addSku(String key, String sku) {
    skus.put(key, sku);
  }

  /**
   * Process a string order. After 4 orders queued creates a job
   * 
   * @param order to process
   */
  public void processOrder(String order) {
    orders.add(order);
    FileHelper.logEvent("IN: Order " + order + " (" + orders.size() + "/4)", this);
    if (orders.size() >= 4) {
      createJob();
    }
  }

  /**
   * Create a job from 4 orders.
   */
  private void createJob() {
    // List of skus and order names
    String[] jobSkus = new String[8];
    String[] jobOrders = new String[4];

    // Get skus and names of 4 orders
    for (int i = 0; i < 4; i++) {
      String order = orders.remove();
      jobOrders[i] = order;
      // Strip whitespace
      order = order.replaceAll("\\s+", "");
      // Add front/back codes
      jobSkus[i * 2] = skus.get(order + "F");
      jobSkus[i * 2 + 1] = skus.get(order + "B");
    }

    // Create job and queue it
    Job job = new Job(this.system, jobSkus, jobOrders);
    addJob(job);
    system.sendToPicking(job);
  }

  /**
   * Add job to the warehouse.
   * 
   * @param job to add
   */
  public void addJob(Job job) {
    jobs.add(job);
  }

  /**
   * Remove job from the warehouse.
   * 
   * @param job that finished
   */
  public void removeJob(Job job) {
    jobs.remove(job);
  }

  /**
   * Get jobs current in the warehouse.
   * 
   * @return ArrayList
   */
  public ArrayList<Job> getJobs() {
    return jobs;
  }
}
