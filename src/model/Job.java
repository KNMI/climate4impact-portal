package model;

import org.json.JSONException;
import org.json.JSONObject;

public class Job {
  private int id;
  private String description;
  private String type;
  private String jobStatus;
  private int zone;
    
  private Job(JobBuilder builder){
    this.id = builder.id;
    this.description = builder.description;
    this.type = builder.type;
    this.jobStatus = builder.jobStatus;
    this.zone = builder.zone;
  }
  
  public static class JobBuilder{
    private int id;
    private String description;
    private String type;
    private String jobStatus;
    private int zone;
    public JobBuilder(JSONObject jsonObject){
      try {
        this.id = jsonObject.getInt("id");
        this.description = jsonObject.getString("description");
        this.type = jsonObject.getString("type");
        this.jobStatus = jsonObject.getString("jobStatus");
        this.description = jsonObject.getString("description");
        this.zone = jsonObject.getInt("zone");
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    public Job build(){
      return new Job(this);
    }   
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getJobStatus() {
    return jobStatus;
  }

  public void setJobStatus(String jobStatus) {
    this.jobStatus = jobStatus;
  }

  public int getZone() {
    return zone;
  }

  public void setZone(int zone) {
    this.zone = zone;
  }
  
  
  
}
