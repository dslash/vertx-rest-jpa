package com.github.dslash;

import io.vertx.ext.web.Router;

/**
 * Build routes for entities
 */
public class JpaRestEngine {

  /**
   * The router.
   */
  private final Router router;

  /**
   * Constructor
   *
   * @param router the router
   */
  public JpaRestEngine(Router router) {
    this.router = router;
  }

  /**
   * Register entity and build associated routes
   *
   * @param <T>         Type of entity
   * @param path        The resource path
   * @param entityClass The entity class
   */
  public <T> void register(String path, Class<T> entityClass) {
    JpaRestResource<T> restResource = new JpaRestResource<>(router, entityClass);
    restResource.createEndPoints(path);
  }
}
