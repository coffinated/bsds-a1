package io.swagger.client;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class TextbodyApiClient {

  private int fails;
  private int successes;
  private int totalWordCount;

  synchronized void successCount() {
    this.successes++;
  }

  private int getSuccesses() {
    return this.successes;
  }

  synchronized void failCount() {
    this.fails++;
  }

  private int getFails() {
    return this.fails;
  }

  synchronized void wordCount(int count) {
    this.totalWordCount += count;
  }

  public TextbodyApiClient() {
    this.fails = 0;
    this.successes = 0;
    this.totalWordCount = 0;
  }

  public static void main(String[] args) throws InterruptedException {

    TextbodyApiClient client = new TextbodyApiClient();
    int maxThreads;
    final BlockingQueue<String> bq = new LinkedBlockingQueue<>();
    final BlockingQueue<RequestStat> stats = new LinkedBlockingQueue<>();
    Path file;

    if (args.length > 0) {
      maxThreads = Integer.parseInt(args[0]);
      file = Paths.get(args[1]);
    } else {
      System.err.println("Need number of threads and file to run!");
      return;
    }

    long start = System.currentTimeMillis();

    RequestStatCollector statCollector = new RequestStatCollector(stats);
    new Thread(statCollector).start();
    TextProducer prod = new TextProducer(bq, file, maxThreads);
    new Thread(prod).start();

    System.out.println("Starting " + maxThreads + " threads...");
    CountDownLatch completed = new CountDownLatch(maxThreads);

    for (int i = 0; i < maxThreads; i++) {
      TextConsumer con = new TextConsumer(bq, completed, client, stats);
      new Thread(con).start();
    }
    completed.await();

    RequestStat notify = RequestStat.newEndOfStats();
    stats.put(notify);

    long end = System.currentTimeMillis();
    long wall = end - start;
    double throughput = (client.getSuccesses() + client.getFails()) / (wall/1000.0);

    System.out.println("++++++++++++++++Stats++++++++++++++++");
    System.out.println("Successful requests: " + client.getSuccesses());
    System.out.println("Failed requests: " + client.getFails());
    System.out.println("Mean response time: " + statCollector.getMean() + " ms");
    System.out.println("Median response time: " + statCollector.getMedian() + " ms");
    System.out.println("Wall time: " + wall + "ms");
    System.out.println("Throughput: " + throughput + " requests per second");
    System.out.println("p99 response time: " + statCollector.getPercentile(99) + " ms");
    System.out.println("Max response time: " + statCollector.getMax() + " ms");
    System.out.println("+++++++++++++++++++++++++++++++++++++");
    System.exit(0);
  }
}