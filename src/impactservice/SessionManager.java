package impactservice;

import tools.Debug;

public class SessionManager {
  public static class SearchSession{
    String variable="";
    String time_frequency="";
    String institute="";
    String experiment="";
    String model="";
    String realm="";
    String domain="";
    String from="";
    String to="";
    String where="";
    int pagelimit=1;//advancedsearch.js
    int basicsearchpagenr=1;
    int advancedsearchpagenr=1;
    
    public String getAsJSON(){
      if(variable==null)variable="";
      if(time_frequency==null)time_frequency="";
      if(institute==null)institute="";
      if(experiment==null)experiment="";
      if(model==null)model="";
      if(realm==null)realm="";
      if(domain==null)domain="";
      if(from==null)from="";
      if(to==null)to="";
      if(where==null)where="";
      
      String searchSessionJson = "{\n" +
      		"  \"variable\":\""+variable+"\",\n" +
      		"  \"time_frequency\":\""+time_frequency+"\",\n" +
  				"  \"institute\":\""+institute+"\",\n" +
          "  \"experiment\":\""+experiment+"\",\n" +
          "  \"model\":\""+model+"\",\n" +
          "  \"realm\":\""+realm+"\",\n" +
          "  \"domain\":\""+domain+"\",\n" +
          "  \"from\":\""+from+"\",\n" +
          "  \"to\":\""+to+"\",\n" +
          "  \"basicsearchpagenr\":\""+basicsearchpagenr+"\",\n" +
          "  \"advancedsearchpagenr\":\""+advancedsearchpagenr+"\",\n" +
          "  \"pagelimit\":\""+pagelimit+"\",\n" +
          "  \"where\":\""+where + "\"\n"+
          "}\n";
      //Debug.println("SearchSession JSON:\n"+searchSessionJson);
      return searchSessionJson;
    }
   /* public void reset() {
       variable="";
       frequency="";
       institute="";
       experiment="";
       model="";
       realm="";
       from="";
       to="";
       where="";      
       pagelimit=10;
       basicsearchpagenr=1;
       advancedsearchpagenr=1;
    }*/
  }
  
  
  public static class  DatasetViewerSession{
    String datasetURL = "";
    public String getAsJSON(){
      if(datasetURL==null)datasetURL="";
      return "{datasetURL:'"+datasetURL+"'};";
    }
  }
}
