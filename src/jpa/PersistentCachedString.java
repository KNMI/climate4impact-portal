package jpa;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.*;

@Entity
public class PersistentCachedString implements Serializable {
  // @Id
  // private int id;
  @Id
  private String query;

  @Basic(optional = false)
  @Column(name = "creationDate", insertable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date creationDate = Calendar.getInstance().getTime();
  
  @Column(columnDefinition = "MEDIUMBLOB")
  private String queryResult;
  private static final long serialVersionUID = 1L;

  public PersistentCachedString() {
    super();
  }
  
  @Column(columnDefinition = "MEDIUMBLOB")
  public String getQueryResult() {
    return this.queryResult;
  }

  public void setQueryResult(String fullname) {
    this.queryResult = fullname;
  }

  @Id
  public String getQuery() {
    return this.query;
  }

  public void setQuery(String name) {
    this.query = name;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getCreationDate() {
    return creationDate;
  }
}
