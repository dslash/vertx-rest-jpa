package com.github.dslash.bo;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * FAKE.
 */
@Entity(name = "book")
@UuidGenerator(name="ID_GEN")
public class Book {

  @Id
  @GeneratedValue(generator="ID_GEN")
  private String uuid;

  @Column
  private String name;

  @Column(name = "page_count")
  private int pageCount;

  public String uuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String name() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int pageCount() {
    return pageCount;
  }

  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }
}
