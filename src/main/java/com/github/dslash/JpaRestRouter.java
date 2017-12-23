package com.github.dslash;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Interface of JpaRestRouter.
 */
public interface JpaRestRouter {

  /**
   * Create new instance of JpaRestRouter
   *
   * @param vertx Vertx instance
   * @return a new instance of JpaRestRouter
   */
  static JpaRestRouter create(Vertx vertx) {
    return create(vertx, new JpaRestRouterOptions());
  }

  /**
   * Create new instance of JpaRestRouter
   *
   * @param vertx                Vertx instance
   * @param jpaRestRouterOptions Options for router and mapping
   * @return a new instance of JpaRestRouter
   */
  static JpaRestRouter create(Vertx vertx, JpaRestRouterOptions jpaRestRouterOptions) {
    return new JpaRestRouterImpl(vertx, jpaRestRouterOptions);
  }

  /**
   * Start the rest router
   *
   * @param listeningPort Router listening port
   * @param handler       The handler
   */
  void listen(int listeningPort, Handler<AsyncResult<Void>> handler);

  /**
   * Close the router
   *
   * @param handler The handler
   */
  void close(Handler<AsyncResult<Void>> handler);
}
