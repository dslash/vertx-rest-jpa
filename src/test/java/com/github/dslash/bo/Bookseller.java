package com.github.dslash.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * FAKE.
 */
@Entity(name = "book")
@UuidGenerator(name = "ID_S_GEN")
public class Bookseller {

  @Id
  @GeneratedValue(generator = "ID_S_GEN")
  private String uuid;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Book> books;

  @JsonProperty("uuid")
  public String uuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @JsonProperty("books")
  public List<Book> books() {
    return books;
  }

  public void setBooks(List<Book> books) {
    this.books = books;
  }

}
