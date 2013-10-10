package tools;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;


public class JSONMessageDecorator{
	String messages = "";
	String exceptions = null;
	
	HttpServletResponse response = null;
	
	PrintWriter out = null;	
	public JSONMessageDecorator(HttpServletResponse response){
	  this.response=response;

	}
	void print(String data){
		messages+=data;
	}
	void println(String data){
		messages+=data+"\n";
	}
	public void printexception(String data){
		DebugConsole.errprintln(data);
		exceptions=data;    	
		close();
	}
	void close(){
		String jsonObjectName=null;
		String result=null;
		if(exceptions != null){
			 jsonObjectName="error";
			 result=exceptions;
		}else{
			 jsonObjectName="json";
			 result=messages;
		}

		JSONObject myJSONObject = new JSONObject();
		
		if(out==null){
      try {
        out = response.getWriter();
      } catch (IOException e) {
        DebugConsole.errprint(e.getMessage());
        return;
      }
		}
    //response.setContentType("application/json");
    response.setContentType("text/plain");
		try {
			myJSONObject.put(jsonObjectName,result);
		} catch (JSONException e) {
			out.print(e.getMessage());
			return;
		}
		out.print(myJSONObject.toString());
		/*String j="{\n"+
		"    \"users\": [\n"+
		"        {\n"+
		"            \"id\": 1,\n"+
		"            \"name\": \"Ed\"\n"+
		"        }\n"+
		"    ]\n"+
		"}\n";
		out.println(j);*/
	}
	
}
