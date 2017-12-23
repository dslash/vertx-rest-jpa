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
 */
public class JpaRestResource {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JpaRestResource.class);

  /**
   * The router.
   */
  private final Router router;

  /**
   * Manager factory.
   */
  private final EntityManagerFactory emf;


  /**
   * Constructor
   *
   * @param router the router
   * @param options the options
   */
  public JpaRestResource(Router router, JpaRestRouterOptions options) {
    emf = Persistence.createEntityManagerFactory(options.jpaUnitName());
    this.router = router;
  }

  /**
   * Register endPoints
   *
   * @param rootPath    the root Path
   * @param entityClass the entity class
   */
  public <T> String createEndPoints(String rootPath, Class<T> entityClass) {
    String className = entityClass.getSimpleName().toLowerCase();
    String resourceName = "/" + English.plural(className);
    String resourcePath = rootPath + resourceName;

    LOGGER.debug("Registration of [GET] " + resourcePath);
    router.get(resourcePath).blockingHandler(rc -> onGetReceived(rc, entityClass));

    LOGGER.debug("Registration of [POST] " + resourcePath);
    router.post(resourcePath).blockingHandler(rc -> onPostReceived(rc, entityClass));

    String path = resourcePath + "/:" + className + "_id";
    LOGGER.debug("Registration of [DELETE] " + path);
    router.delete(path).blockingHandler(rc -> onDeleteReceived(rc, entityClass, className + "_id"));

    return path;
  }

  /**
   * Permet d'ajout une sous route liée à l'entitée gérée
   *
   * @param type Le type
   */
  public <T> void addSubEntity(String rootPath, Class<T> type) {
    createEndPoints(rootPath, type);
  }

  /**
   * Process get entities
   *
   * @param routingContext the routing context
   */
  private <T> void onGetReceived(RoutingContext routingContext, Class<T> entityClass) {
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
  private <T> void onPostReceived(RoutingContext routingContext, Class<T> entityClass) {
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
   * @param paramId Param name
   */
  private <T> void onDeleteReceived(RoutingContext routingContext, Class<T> entityClass, String paramId) {
    LOGGER.debug("[Delete] >> " + routingContext.request().uri());
    EntityManager em = emf.createEntityManager();

    em.getTransaction().begin();
    String uuid = routingContext.request().params().get(paramId);
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
