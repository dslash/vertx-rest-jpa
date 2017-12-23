package com.github.dslash;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
    target = JpaRestRouter.create(vertx, new JpaRestRouterOptions()
      .setJpaUnitName("jpa-test")
      .setRootUri("/api/v1"));
    target.listen(LISTENING_PORT, onDone -> async.complete());
  }

  @After
  public void tearDown(TestContext test) {
    Async async = test.async();
    target.close(onDone -> async.complete());
  }

  @Test
  public void shouldPerformSimpleCrudOperationToBooks(TestContext test) {
    Async async = test.async();
    HttpClient client = vertx.createHttpClient();
    Future<Void> globalFuture = Future.future();
    globalFuture.setHandler(v -> {
      if (v.succeeded()) {
        async.complete();
      } else {
        test.fail(v.cause());
      }
    });

    getBooks(client).compose(jsonArray -> {
      // Check 0 book
      test.assertEquals(0, jsonArray.size());
      // A a book
      return addBook(client, new JsonObject()
        .put("author", new JsonObject().put("name", "Peter"))
        .put("name", "MyBook1")
        .put("pageCount", 456));
    }).compose(jsonObject -> {
      // Check the uuid field is filled
      test.assertNotNull(jsonObject.getString("uuid"));
      return getBooks(client);
    }).compose(jsonArray -> {
      //Check there is one book
      test.assertEquals(1, jsonArray.size());
      JsonObject book = jsonArray.getJsonObject(0);
      test.assertEquals("MyBook1", book.getString("name"));
      test.assertEquals(456, book.getInteger("pageCount"));
      test.assertNotNull(book.getJsonObject("author"));
      test.assertEquals("Peter", book.getJsonObject("author").getString("name"));
      test.assertNotNull(book.getString("uuid"));
      // Delete the book
      return deleteBook(client, book.getString("uuid"));
    }).compose(empty -> {
      // Get the book list
      return getBooks(client);
    }).compose(jsonArray -> {
      // Check 0 book
      test.assertEquals(0, jsonArray.size());
      globalFuture.complete();
    }, globalFuture);
  }

  // Get books
  private Future<JsonArray> getBooks(HttpClient client) {
    Future<JsonArray> future = Future.future();
    client.getNow(LISTENING_PORT, "localhost", "/api/v1/books", resp -> resp.bodyHandler(body -> {
      future.complete(body.toJsonArray());
    }));
    return future;
  }

  // add book
  private Future<JsonObject> addBook(HttpClient client, JsonObject data) {
    Future<JsonObject> future = Future.future();
    client.post(LISTENING_PORT, "localhost", "/api/v1/books", resp -> resp.bodyHandler(body -> {
      future.complete(body.toJsonObject());
    })).end(data.encode());
    return future;
  }

  // delete book
  private Future<Void> deleteBook(HttpClient client, String uuid) {
    Future<Void> future = Future.future();
    client.delete(LISTENING_PORT, "localhost", "/api/v1/books/" + uuid, resp -> {
      if (resp.statusCode() != 204) {
        future.fail("Status code not correct: " + resp.statusCode());
      } else {
        future.complete();
      }
    }).end();
    return future;
  }
}
