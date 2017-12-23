package com.github.dslash;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.atteo.evo.inflector.English;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Type;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link JpaRestRouter}
 */
public class JpaRestRouterImpl implements JpaRestRouter {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JpaRestRouterImpl.class);

  /**
   * Router.
   */
  private final Router router;

  /**
   * Http Server.
   */
  private final HttpServer server;

  /**
   * Constructor
   *
   * @param vertx                Vertx instance
   * @param jpaRestRouterOptions Options
   */
  public JpaRestRouterImpl(Vertx vertx, JpaRestRouterOptions jpaRestRouterOptions) {
    router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    server = vertx.createHttpServer();
    createAllRoutes();
  }

  /**
   * Create routes for all found entities.
   */
  private void createAllRoutes() {
    InputStream fileStream = getClass().getClassLoader().getResourceAsStream("META-INF/persistence.xml");
    if (fileStream != null) {
      List<Class<?>> entities = getEntitiesFromJpaContext(fileStream);
      entities.stream().forEach(x -> createRouteForEntity(x));
    }else{
      LOGGER.error("Can not find JPA configuration file. Please check if file 'META-INF/persistence.xml' exists");
    }
  }

  /**
   * Create all routes for entity
   * @param entity entity
   */
  private void createRouteForEntity(Class<?> entity) {
    String resourceName = "/" + English.plural(entity.getSimpleName().toLowerCase());

    router.route(HttpMethod.GET, resourceName).handler(rc ->{
        rc.response().end(new JsonArray().encode());
    });
  }

  /**
   * @param fileStream Jpa configuration file
   * @return The entities (class.getName())
   */
  private List<Class<?>> getEntitiesFromJpaContext(InputStream fileStream) {
    EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("jpa-unit");
    Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();
    return entities.stream().map(Type::getJavaType).collect(Collectors.toList());
  }

  @Override
  public void listen(int listeningPort, Handler<AsyncResult<Void>> handler) {
    server.requestHandler(router::accept).listen(listeningPort, onDone -> {
      if (onDone.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(onDone.cause()));
      }
    });
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    server.close(handler);
  }
}
