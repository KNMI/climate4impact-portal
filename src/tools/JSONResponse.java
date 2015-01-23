package tools;

import org.json.JSONException;
import org.json.JSONObject;

import tools.Debug;

public class JSONResponse {
  private String message = "";
  private String mimetype = "application/json";
  private String jsonp = null;
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
  public void setErrorMessage(String string){
    JSONObject error = new JSONObject();
    try {
      error.put("error", string);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    this.message = error.toString();
    this.hasError = true;
  }
}
