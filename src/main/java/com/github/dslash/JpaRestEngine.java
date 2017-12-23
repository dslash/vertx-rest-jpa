package com.github.dslash;

import io.vertx.ext.web.Router;

import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

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
   * @param options
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
      if (Collection.class.isAssignableFrom(field.getType())) {
        Type genericType = field.getGenericType();
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Type actualTypeArgument = actualTypeArguments[0];
        // restResource.addSubCollection((Class<?>) actualTypeArgument);
      } else if (!field.getType().isPrimitive() && !field.getType().getName().startsWith("java")) {
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        if (oneToOne != null && oneToOne.fetch() == FetchType.LAZY) { // load on demand..
          restResource.addSubEntity(currentPath, field.getType()); // build endPoints
        }
      }
    }
  }
}
