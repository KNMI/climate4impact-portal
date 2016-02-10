package model;

import org.json.JSONException;
import org.json.JSONObject;

public class Downscaling {


  int jobId, status, zone, sYear, eYear;
  String username, predictand, downscalingMethod, type, project, scenario, date;
  
  public Downscaling(){}
  
  public Downscaling(JSONObject jsonObject){
    try {
      this.jobId = jsonObject.getInt("jobId");
      this.status = jsonObject.getInt("status");
      this.zone = jsonObject.getInt("zone");
      this.sYear = jsonObject.getInt("sYear");
      this.eYear = jsonObject.getInt("eYear");
      
      this.username = jsonObject.getString("username");
      this.predictand = jsonObject.getString("predictand");
      this.downscalingMethod = jsonObject.getString("downscalingMethod");
      this.type = jsonObject.getString("type");
      this.project = jsonObject.getString("project");
      this.scenario = jsonObject.getString("scenario");
      this.date = jsonObject.getString("date");
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
  
  public int getJobId() {
    return jobId;
  }
  public void setJobId(int jobId) {
    this.jobId = jobId;
  }
 
  public int getZone() {
    return zone;
  }

  public void setZone(int zone) {
    this.zone = zone;
  }

  public int getsYear() {
    return sYear;
  }
  public void setsYear(int sYear) {
    this.sYear = sYear;
  }
  public int geteYear() {
    return eYear;
  }
  public void seteYear(int eYear) {
    this.eYear = eYear;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public String getPredictand() {
    return predictand;
  }
  public void setPredictand(String predictand) {
    this.predictand = predictand;
  }
  public String getDownscalingMethod() {
    return downscalingMethod;
  }
  public void setDownscalingMethod(String downscalingMethod) {
    this.downscalingMethod = downscalingMethod;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getProject() {
    return project;
  }
  public void setProject(String project) {
    this.project = project;
  }
  public String getScenario() {
    return scenario;
  }
  public void setScenario(String scenario) {
    this.scenario = scenario;
  }
  public int getStatus() {
    return status;
  }
  public void setStatus(int status) {
    this.status = status;
  }
  public String getDate() {
    return date;
  }
  public void setDate(String date) {
    this.date = date;
  }
  
  
}
