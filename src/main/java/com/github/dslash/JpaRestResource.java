package com.github.dslash;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.atteo.evo.inflector.English;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Rest resource
 *
 * @param <T> Type of entity
 */
public class JpaRestResource<T> {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JpaRestResource.class);

  /**
   * The router.
   */
  private final Router router;

  /**
   * Entity class.
   */
  private final Class<T> entityClass;

  /**
   * Manager factory.
   */
  private final EntityManagerFactory emf;

  /**
   * Constructor
   *
   * @param router      the router
   * @param entityClass the entity class
   */
  public JpaRestResource(Router router, Class<T> entityClass) {
    emf = Persistence.createEntityManagerFactory("jpa-unit");
    this.entityClass = entityClass;
    this.router = router;
  }

  /**
   * Create end points for the entity
   *
   * @param rootPath the root path
   */
  public void createEndPoints(String rootPath) {
    this.createEndPoints(rootPath, this.entityClass);
  }

  /**
   * Register endPoints
   *
   * @param rootPath    the root Path
   * @param entityClass the entity class
   */
  private void createEndPoints(String rootPath, Class<?> entityClass) {
    String resourceName = "/" + English.plural(entityClass.getSimpleName().toLowerCase());
    String resourcePath = rootPath + resourceName;

    LOGGER.debug("Registration of [GET] " + resourcePath);
    router.get(resourcePath).blockingHandler(this::onGetReceived);

    LOGGER.debug("Registration of [POST] " + resourcePath);
    router.post(resourcePath).blockingHandler(this::onPostReceived);

    String path = resourcePath + "/:id";
    LOGGER.debug("Registration of [DELETE] " + path);
    router.delete(path).blockingHandler(this::onDeleteReceived);
  }

  /**
   * Process get entities
   *
   * @param routingContext the routing context
   */
  private void onGetReceived(RoutingContext routingContext) {
    LOGGER.debug("[GET] >> " + routingContext.request().uri());
    EntityManager em = emf.createEntityManager();

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<T> q = cb.createQuery(entityClass);
    TypedQuery<T> query = em.createQuery(q);
    List<T> results = query.getResultList();

    makeResponse(routingContext, results);

    em.close();
  }

  /**
   * Process add entity
   *
   * @param routingContext the routing context
   */
  private void onPostReceived(RoutingContext routingContext) {
    LOGGER.debug("[POST] >> " + routingContext.request().uri() + " " + routingContext.getBodyAsJson());
    EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();
    String data = routingContext.getBodyAsString();
    T entity = Json.decodeValue(data, entityClass);
    em.persist(entity);
    em.getTransaction().commit();

    makeResponse(routingContext, entity);

    em.close();
  }

  /**
   * Process remove entity
   *
   * @param routingContext the routing context
   */
  private void onDeleteReceived(RoutingContext routingContext) {
    LOGGER.debug("[Delete] >> " + routingContext.request().uri());
    EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();
    String uuid = routingContext.request().params().get("id");
    T entity = em.find(entityClass, uuid);
    em.remove(entity);
    em.getTransaction().commit();

    makeResponse(routingContext, null);

    em.close();
  }

  /**
   * Terminate the request
   *
   * @param routingContext Routing context
   * @param data           object to send
   */
  private void makeResponse(RoutingContext routingContext, Object data) {
    HttpServerResponse response = routingContext.response();
    response.putHeader("content-type", "application/json");

    if (data != null) {
      LOGGER.debug("<< 200 " + Json.encode(data));
      response.end(Json.encode(data));
    } else {
      LOGGER.debug("<< 204");
      response.setStatusCode(204).end();
    }
  }

}
