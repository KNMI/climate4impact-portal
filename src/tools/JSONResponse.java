package tools;

import org.apache.commons.lang3.reflect.TypeUtilsTest.This;
import org.json.JSONException;
import org.json.JSONObject;

import tools.Debug;

public class JSONResponse {
  private String message = "";
  private String mimetype = "application/json";
  private String jsonp = null;
  private int statusCode = 200;
  private String redirectURL = null;
  private String errorMessage = null;
  boolean hasError = false; 
  public String getMessage(){
    if(jsonp!=null){
      if(jsonp.length()>0){
        return jsonp+"("+message+");";
      }
    }
    return message;
  }
  public void setMessage(String message){
    if(this.hasError){
      Debug.errprintln("Message can not be set because error has occured");
    }else{
      this.message = message;
    }
  }
  public String getMimeType() {
    // TODO Auto-generated method stub
    return mimetype;
  }
  public void setJSONP(String jsonp) {
    this.jsonp = jsonp;
    
  }
  public void setException(String string, Exception e2) {
    this.message = "{\"error\":\""+string+"\",\"exception\":\""+e2.getMessage()+"\"}";
    this.hasError = true;
  }
  public void setErrorMessage(String errorMessage, int statusCode){
    setErrorMessage(errorMessage, statusCode,null,null,null);
  }
  public void setErrorMessage(String errorMessage, int statusCode,String redirectURL,String currentPage,String resource){
    JSONObject error = new JSONObject();
    this.errorMessage = errorMessage;
    this.statusCode = statusCode;
    this.redirectURL = redirectURL;
    try {
      error.put("error", this.errorMessage);
      error.put("statuscode", this.statusCode);
      if(redirectURL!=null){error.put("redirect", this.redirectURL);}
      if(currentPage!=null){error.put("current", currentPage);}
      if(resource!=null){error.put("resource", resource);}
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    this.message = error.toString();
    this.hasError = true;
  }
  public int getStatusCode(){
    return statusCode;
  }
  
}
