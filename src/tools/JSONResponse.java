package tools;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  @author plieger
 *  
 *  Helper class for creating json and jsonp responses
 *  
 *  Example usages:
   
    JSONResponse jsonResponse = new JSONResponse(request);

    try{
      jsonResponse.setMessage("bla");
    } catch(Exception e){
      jsonResponse.setException("nice error message",e);
    }
    
    try {
      response.print();
    } catch (Exception e1) {
    
    }

 *
 */
public class JSONResponse {
  public JSONResponse(HttpServletRequest request){
    setJSONP(request);
    
  }
  
  public JSONResponse(){
  }
  
  public void setJSONP(HttpServletRequest request) {
    try{
      jsonp=HTTPTools.getHTTPParam(request,"jsonp");
    }catch (Exception e) {
      try{
        jsonp=HTTPTools.getHTTPParam(request,"callback");
      }catch (Exception e1) {
      }
    }
  }
  public String getMimeType() {
    return mimetype;
  }
  public void setJSONP(String jsonp) {
    this.jsonp = jsonp;
    
  }

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

  public void setException(String string, Exception e2) {
    if(e2.getClass() == tools.HTTPTools.WebRequestBadStatusException.class){
      this.statusCode = ((tools.HTTPTools.WebRequestBadStatusException)e2).getStatusCode();
    }
    JSONObject error = new JSONObject();
    try {
      error.put("error", string);
      error.put("statuscode", this.statusCode);
      error.put("exception", e2.getMessage());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    this.message = error.toString();
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

  public void setMessage(JSONObject json) {
    setMessage(json.toString());    
  }
  public boolean hasError(){
    return hasError;
  }
  public void print(HttpServletResponse response) throws IOException{
    response.setContentType(getMimeType());
    response.getOutputStream().print(getMessage());
  }
  
}
