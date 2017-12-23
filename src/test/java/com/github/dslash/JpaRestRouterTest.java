package com.github.dslash;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Test for {@link JpaRestRouter}.
 */
@RunWith(VertxUnitRunner.class)
public class JpaRestRouterTest {

  /**
   * Listening port.
   */
  private static final int LISTENING_PORT = 8081;

  /**
   * Timeout for all test methods.
   */
  @Rule
  public Timeout timeoutRule = new Timeout(5, TimeUnit.SECONDS);

  /**
   * Vertx instance.
   */
  @Rule
  public RunTestOnContext vertxContext = new RunTestOnContext();

  /**
   * Target under testing.
   */
  private JpaRestRouter target;

  /**
   * Vertx instance.
   */
  private Vertx vertx;

  @Before
  public void setUp(TestContext test) {
    Async async = test.async();
    vertx = vertxContext.vertx();
    target = JpaRestRouter.create(vertx, new JpaRestRouterOptions());
    target.listen(LISTENING_PORT, onDone -> async.complete());
  }

  @After
  public void tearDown(TestContext test) {
    Async async = test.async();
    target.close(onDone -> async.complete());
  }

  @Test
  public void shouldGetBooks(TestContext test) {
    HttpClient client = vertx.createHttpClient();
    Async async = test.async();

    // Check the book list
    client.getNow(LISTENING_PORT, "localhost", "/books", resp -> resp.bodyHandler(body -> {
      JsonArray books = body.toJsonArray();
      test.assertEquals(0, books.size());

      client.close();
      async.complete();
    }));
  }
}
