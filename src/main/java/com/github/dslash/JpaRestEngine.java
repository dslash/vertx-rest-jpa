package com.github.dslash;

import io.vertx.ext.web.Router;

import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;

/**
 * Build routes for entities
 */
public class JpaRestEngine {

  /**
   * The router.
   */
  private final Router router;

  /**
   * Options.
   */
  private final JpaRestRouterOptions options;


  /**
   * Constructor
   *
   * @param router the router
   * @param options The options
   */
  public JpaRestEngine(Router router, JpaRestRouterOptions options) {
    this.router = router;
    this.options = options;
  }

  /**
   * Register entity and build associated routes
   *
   * @param <T>         Type of entity
   * @param path        The resource path
   * @param rootEntity The root entity class
   */
  public <T> void register(String path, Class<T> rootEntity) {
    JpaRestResource restResource = new JpaRestResource(router, options);
    String currentPath = restResource.createEndPoints(path, rootEntity);

    for (Field field : rootEntity.getDeclaredFields()) {
      if (!field.getType().isPrimitive() && !field.getType().getName().startsWith("java")) {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        if (oneToOne != null && oneToOne.fetch() == FetchType.LAZY) { // load on demand..
          restResource.addSubEntity(currentPath, field.getType()); // build endPoints
        }
      }
    }
  }
}
