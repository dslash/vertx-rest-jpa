package com.github.dslash.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "author")
@UuidGenerator(name = "ID_AUTHOR_GEN")
public class Author {
  @Id
  @GeneratedValue(generator = "ID_AUTHOR_GEN")
  private String uuid;

  @Column
  private String name;

  @JsonProperty("uuid")
  public String uuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @JsonProperty("name")
  public String name() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
