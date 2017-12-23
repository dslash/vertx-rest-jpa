package com.github.dslash;

/**
 * Options for {@link JpaRestRouter}.
 */
public class JpaRestRouterOptions {

  /**
   * Default JPA Unit name.
   */
  private static final String JPA_UNIT_NAME = "jpa";

  /**
   * Default JPA Unit file path.
   */
  private static final String JPA_UNIT_PATH = "META-INF/persistence.xml";

  private String rootEntityType;
  private String jpaUnitName;
  private String jpaUnitPath;
  private String rootUri;

  /**
   * Constructor.
   */
  public JpaRestRouterOptions() {
    jpaUnitName = JPA_UNIT_NAME;
    jpaUnitPath = JPA_UNIT_PATH;
    rootUri = "";
  }

  public String rootEntityType() {
    return rootEntityType;
  }

  public JpaRestRouterOptions setRootEntityType(String rootEntityType) {
    this.rootEntityType = rootEntityType;
    return this;
  }

  public String jpaUnitName() {
    return jpaUnitName;
  }

  public JpaRestRouterOptions setJpaUnitName(String jpaUnitName) {
    this.jpaUnitName = jpaUnitName;
    return this;
  }

  public String jpaUnitPath() {
    return jpaUnitPath;
  }

  public JpaRestRouterOptions setJpaUnitPath(String jpaUnitPath) {
    this.jpaUnitPath = jpaUnitPath;
    return this;
  }

  public String rootUri() {
    return rootUri;
  }

  public JpaRestRouterOptions setRootUri(String rootUri) {
    this.rootUri = rootUri;
    return this;
  }

}
