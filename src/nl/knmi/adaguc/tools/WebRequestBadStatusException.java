package nl.knmi.adaguc.tools;

public class WebRequestBadStatusException extends Exception {
    private static final long serialVersionUID = 1L;
    int statusCode = 0;
    String result = null;

    public WebRequestBadStatusException(int statusCode, String result) {
      this.statusCode = statusCode;
      this.result = result;
    }

    public WebRequestBadStatusException(int statusCode) {
      this.statusCode = statusCode;
      this.result = "";
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getResult() {
      return result;
    }

    public String getMessage() {
      
      //if(statusCode == 401){
        return statusCode+": "+"EXCEPTION";// org.apache.commons.httpclient.HttpStatus.getStatusText(statusCode); 
      //}
      //return ""+statusCode;
    }
  }
