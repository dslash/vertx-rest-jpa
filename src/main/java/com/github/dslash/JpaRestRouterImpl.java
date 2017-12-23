package com.github.dslash;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Type;
import java.io.InputStream;
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
   * Options.
   */
  private final JpaRestRouterOptions options;

  /**
   * Constructor (package only)
   *
   * @param vertx                Vertx instance
   * @param jpaRestRouterOptions Options
   */
  public JpaRestRouterImpl(Vertx vertx, JpaRestRouterOptions jpaRestRouterOptions) {
    router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    server = vertx.createHttpServer();
    options = jpaRestRouterOptions;
    createRestEngine();
  }

  /**
   * Create routes for all found entities.
   */
  private void createRestEngine() {
    InputStream fileStream = getClass().getClassLoader().getResourceAsStream(options.jpaUnitPath());
    if (fileStream != null) {
      JpaRestEngine engine = new JpaRestEngine(router, options);
      List<Class<?>> entities = getEntitiesFromJpaContext();
      engine.register(options.rootUri(), entities.get(0));
    } else {
      LOGGER.error("Can not find JPA configuration file. Please check if file 'options.jpaUnitPath()' exists");
    }
  }

  /**
   * @return The entities (class.getName())
   */
  private List<Class<?>> getEntitiesFromJpaContext() {
    EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(options.jpaUnitName());
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
