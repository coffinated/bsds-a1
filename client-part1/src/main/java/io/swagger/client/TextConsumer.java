package io.swagger.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import io.swagger.client.api.TextbodyApi;
import io.swagger.client.model.ResultVal;
import io.swagger.client.model.TextLine;

public class TextConsumer implements Runnable {

  private final BlockingQueue<String> bq;
  private final CountDownLatch c;
  private final TextbodyApiClient client;

  public TextConsumer(BlockingQueue<String> bq, CountDownLatch c, TextbodyApiClient client) {
    this.bq = bq;
    this.c = c;
    this.client = client;
  }

  public void run() {
    TextbodyApi apiInstance = new TextbodyApi();
    String function = "wordcount";
    try {
      while (true) {
        String msg = bq.take();
        if (msg.equals("EOF")) {
          break;
        }
        TextLine body = new TextLine();
        body.setMessage(msg);

        try {
          ResultVal result = apiInstance.analyzeNewLine(body, function);
          client.wordCount(result.getMessage());
          client.successCount();
        } catch (ApiException e) {
          client.failCount();
          System.err.format("API Exception%n code = %s%n message = %s%n",
                  e.getCode(), e.getResponseBody());
        }
      }

      c.countDown(); // when done, let main thread know
    } catch (InterruptedException ex) {
      System.err.format("InterruptedException: %s%n", ex);
    }
  }
}
