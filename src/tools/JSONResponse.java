package tools;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *  JSONResponse jsonResponse = new JSONResponse();

    jsonResponse.setMessage("bla");
    
    try {
      jsonResponse.setJSONP(request);
      response.setContentType(jsonResponse.getMimeType());
      response.getOutputStream().print(jsonResponse.getMessage());
    } catch (Exception e1) {
    
    }
 * @author plieger
 *
 */
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
  public void setJSONP(HttpServletRequest request) throws Exception {
    try{
      jsonp=HTTPTools.getHTTPParam(request,"jsonp");
    }catch (Exception e) {
      try{
        jsonp=HTTPTools.getHTTPParam(request,"callback");
      }catch (Exception e1) {
      }
    }
  }
  public void setMessage(JSONObject json) {
    setMessage(json.toString());    
  }
  public boolean hasError(){
    return hasError;
  }
  
}
