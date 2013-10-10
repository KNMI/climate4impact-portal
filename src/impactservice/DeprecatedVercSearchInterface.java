package impactservice;

import javax.persistence.Query;

import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.persistence.EntityManager;

import jpa.JPAImpactQueryResultCache;
import jpa.JPAStaticResourceBean;
import jpa.PersistentCachedString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.hibernate.ejb.Ejb3Configuration;
import org.slf4j.LoggerFactory; 

import ASyncRunner.ASyncRunner;
import ASyncRunner.MyRunnableWaiter;
import DRS.DRS;

import tools.DebugConsole;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;

@SuppressWarnings("unused")
public class DeprecatedVercSearchInterface
{
  public static boolean debug = false;
  
  public static String[] categoryNames={"institute","model","experiment","frequency","realm","variable"};
  
  static public JSONObject getVercQuery3(String _query,int pageNumber,int pageLimit,boolean includeFacets,boolean includeLongNameFacets,boolean includeQueryResults){
    JSONObject r = _getVercQuery(_query,pageNumber,pageLimit,includeFacets,includeLongNameFacets,includeQueryResults);
    if(r==null){
      DebugConsole.errprintln("No result when executing query "+_query);
    }
    try {
      DebugConsole.errprintln(r.getString("error"));
    } catch (JSONException e) {}
    return r;
  }
  static private JSONObject _getVercQuery(String _query,int pageNumber,int pageLimit,boolean includeFacets,boolean includeLongNameFacets,boolean includeQueryResults){
    
    //boolean queryHasFailed;//=false;
    JSONObject error=new JSONObject();
    //JSONArray searchResult = new JSONArray();
    
    DebugConsole.println("query='"+_query+"'");
    
    if(includeFacets){
      String facets="";
      for(int j=0;j<categoryNames.length;j++){
        if(facets.length()!=0)facets+=",";
        facets=facets+categoryNames[j];
      }
      String httpQuery="type=File&facets="+facets+"&format=application%2Fsolr%2Bjson&limit=0";
      DebugConsole.println(httpQuery);
      String rawData = null;
      
      try {
       
        rawData = HTTPTools.makeHTTPGetRequest(Configuration.VercSearchConfig.getEsgfSearchURL()+(String)httpQuery);
       
      } catch (WebRequestBadStatusException e) {
        try {error.put("error", "Unable to make HTTP request, Exception: "+e.getMessage());return error;} catch (JSONException e1) {return null;}
      } catch (UnknownHostException e) {
        try {error.put("error", "Unable to make HTTP request, Exception: "+e.getMessage());return error;} catch (JSONException e1) {return null;}
      }
       
      JSONObject jsonData = new JSONObject();
      try {
        jsonData = (JSONObject) new JSONTokener(rawData).nextValue();
      } catch (JSONException e) {
        //queryHasFailed=true;
        try {error.put("error", "Unable to tokenize string");return error;} catch (JSONException e1) {return null;}
      }
      if(jsonData ==null){
        try {error.put("error", "jsonData == null");return error;} catch (JSONException e1) {return null;}
      }
      return jsonData;
      
     // DebugConsole.println(data);
    }
  
    
    //http://esg-datanode.jpl.nasa.gov/esg-search/search?type=File&facets=variable,frequency,institute,experiment,model,realm&format=application/json
    return null;
  }

  /**
   * getVercQuery provides an interface to the VERC Search rest interface. It can return the search results and facets
   * @param query The query to do, can be "" to retrieve only the possible facets
   * @param pageNumber The page number we want results for
   * @param pageLimit The size of each page with results
   * @param includeFacets The JSONObject result will contain the facets shortnames with their descriptions.
   * @param includeLongNameFacets The JSONObject will contain the facets longnames with their descriptions. This option Is ignored when includeFacets is set to false.
   * @param includeQueryResults The JSONObject will contain the search results
   * @return A JSON object with the results stored in "facets" and "topics"
   */
  
  static public JSONObject getVercQuery(String _query,int pageNumber,int pageLimit,boolean includeFacets,boolean includeLongNameFacets,boolean includeQueryResults){
 
    String query= tools.HTTPTools.makeCleanURL(_query);
    //DebugConsole.println("Query length"+_query.length()+" query="+_query+" query="+query);
    String cacheIdentifier="vq_"+query+"&id="+pageNumber+"_"+pageLimit+"_"+includeFacets+"_"+includeLongNameFacets+"_"+includeQueryResults;
    DebugConsole.println("cacheIdentifier='"+cacheIdentifier+"'");
    if(debug)DebugConsole.println("GetVercQuery");
    String cacheddata = getCachedVercObject(cacheIdentifier);
    if(cacheddata!=null){
      JSONObject data = null;
      try {
        data = (JSONObject) new JSONTokener(cacheddata).nextValue();
      } catch (JSONException e) {
        DebugConsole.errprintln("Unable to tokenize string "+cacheddata);
      }
      if(data !=null){return data;}
    }
        
     
   
    MyRunnableWaiter t = new MyRunnableWaiter();
    
    JSONObject queryAndFacetResult = new JSONObject();
    
    int facetThreadOffset=0;
    if(query=="")includeQueryResults=false;
    if(includeQueryResults){
      /*Fire the query which will return the actual results*/
      t.add(new ASyncVercQuery(query+"&limit="+pageLimit+"&offset="+pageLimit*(pageNumber-1)));
      facetThreadOffset++;
      //t.add(new ASyncVercQuery(query+"&querymode=count&"));
      //facetThreadOffset++;
    }
    

    
    if(includeFacets){
      /*Asynchronously fire all the different facet queries, results in the possible options the user can choose from.*/
      for(int j=0;j<categoryNames.length;j++){
          if(query==""){
              t.add(new ASyncVercQuery("distinct?category="+categoryNames[j]));
          }else{
            String searchFacets="";
            // Make a search facet query string without the facetname itself 
            // For example, when current category is "variable=psl", leave out "variable=psl" in the distinct search, 
            // because otherwise we get only 1 distinct value for variable (namely psl).
            for(int i=0;i<categoryNames.length;i++){
              if(categoryNames[j].equalsIgnoreCase(categoryNames[i])==false){
                List<String> value=HTTPTools.getKVPList(query,categoryNames[i]);
                if(value!=null){
                  searchFacets+=categoryNames[i]+"="+value.get(0)+"&";
                }
              }
            }
            //DebugConsole.println("****** Making query "+searchFacets);
            if(searchFacets.equals("")){
              t.add(new ASyncVercQuery("distinct?category="+categoryNames[j]));
            }else{
              t.add(new ASyncVercQuery("search?"+searchFacets+"facet="+categoryNames[j]+"&querymode=distinct&datatype=file"));
            }
          }
      }
    }
    
    //Wait until all threads have returned (All spawned GET requests)
    t.waitForCompletion();
    
    boolean queryHasFailed=false;
    JSONObject error=new JSONObject();
    JSONArray searchResult = new JSONArray();
    
    /**
     * This query contains the search results
     */
    if(includeQueryResults){
      String data = null;
      String origquery = null;
      try {
        data = ((ASyncVercQuery)t.get(0)).data;
        String errorMsg = ((ASyncVercQuery)t.get(0)).error;
        if(errorMsg!=null)throw new Exception(errorMsg);
        
        //String numRecordsStr = ((ASyncVercQuery)t.get(1)).data;
        //DebugConsole.println("Found "+numRecordsStr+" records");
        int numRecords = 200;
        /*try{
          numRecords = Integer.parseInt(numRecordsStr);
        }catch(Exception e){
          DebugConsole.errprintln("Unable to get number of search results: "+numRecordsStr);
        }*/
        
        origquery = (String) ((ASyncVercQuery)t.get(0)).getArguments();
        JSONObject queryResultsObject =  (JSONObject) new JSONTokener(data).nextValue();
        //Convert the jsonobject to a proper JSON array.
        JSONArray keyNames=(JSONArray)  queryResultsObject.names();
        for(int j=0;j<keyNames.length();j++){
          String keyName = keyNames.getString(j);
          if(keyName.equals("HITS")){
            numRecords =  Integer.parseInt(queryResultsObject.getString(keyName));
            continue;
          }
          if(keyName.equals("OFFSET")||keyName.equals("LIMIT"))continue;
        
                    
            JSONArray tempArray = new JSONArray();
            tempArray.put(keyName);
            tempArray.put(queryResultsObject.getJSONArray(keyName).get(0));
            String id =  tempArray.getString(0);
            String url = tempArray.getString(1);
            //if(!id.equals("None")&&!url.equals("None"))
            {
              //DebugConsole.println("keyname="+keyName+"\nid="+id+"\nurl="+url);
              TreeMap<String,String> drsItems = DRS.generateDRSItems(id);
              JSONObject record = new JSONObject();
              record.put("id",id);
              record.put("url",url);
              for(int facetsNr=0;facetsNr<DRS.facetNamesOrderedDRS.length;facetsNr++){
                record.put(DRS.facetNamesOrderedDRS[facetsNr],drsItems.get(DRS.facetNamesOrderedDRS[facetsNr]));
              }
              searchResult.put(record);
            }
        }
          
        queryAndFacetResult.put("topics", searchResult);
        
        
        queryAndFacetResult.put("currentPage", pageNumber);
        queryAndFacetResult.put("numPages",numRecords/pageLimit);
        queryAndFacetResult.put("totalCount",numRecords);
        
      } catch (Exception e2) {
        try {
          e2.printStackTrace();
          queryHasFailed = true;
          if(e2.getMessage().indexOf("java.lang.String")!=-1){
            try {
              String errMsg=data+"\n\nThe original query was:\n "+Configuration.VercSearchConfig.getEsgfSearchURL()+URLDecoder.decode(origquery,"UTF-8");
              DebugConsole.errprintln(errMsg);
              error.put("error", errMsg);
            } catch (UnsupportedEncodingException e) {
            }
          }else{
            String errMsg= "Unable to put search results, Exception: "+e2.getMessage();
            DebugConsole.errprintln(errMsg);
            error.put("error",errMsg);
          }
        } catch (JSONException e) {
        }
      }
    }
    
    /**
     * These queries contain the available facets
     */
    if(includeFacets&&queryHasFailed==false){
      JSONObject availableFacets = new JSONObject();
      for(int j=0;j<categoryNames.length;j++){
          String data = ((ASyncVercQuery)t.get(j+facetThreadOffset)).data;
          String errorMsg = ((ASyncVercQuery)t.get(j+facetThreadOffset)).error;
          String facetQuery=(String) ((ASyncVercQuery)t.get(j+facetThreadOffset)).getArguments();
          if(data!=null){
              try{
                  JSONArray jsonResults = (JSONArray) new JSONTokener(data).nextValue();
                  JSONArray results = new JSONArray();
                  JSONArray reset = new JSONArray();
                  reset.put("reset");
                  reset.put("all");
                  results.put(reset);
             
                  //Sort the results.
                  //String [] sortedResult = new String[jsonResults.length()];
                  List<String> sortedResult = new Vector<String>();
                 // int numberOfResults =0;
                  for(int i=0;i<jsonResults.length();i++){
                    try{
                      String result=((JSONArray) jsonResults.get(i)).getString(0);
                      sortedResult.add(result);
                    }catch(Exception e){
                    }
                  }
                 
                  java.util.Collections.sort(sortedResult);
                  //java.util.Arrays.sort(a);
                 // java.util.Arrays.sort(sortedResult);
                  //    Put into the JSON array.
                  for(int i=0;i<sortedResult.size();i++){
                    
                    String longName=getCategoryLongname(categoryNames[j],sortedResult.get(i));
                    JSONArray facetArray = new JSONArray ();
                    facetArray.put(sortedResult.get(i));
                    facetArray.put(sortedResult.get(i)+" - ("+longName+")");
                    facetArray.put(longName);
                    results.put(facetArray);
                    //Include longnames in the JSON Object as primary value. In this case description is the same as the shortname.
                    if(includeLongNameFacets){
                      JSONArray facetArrayLong = new JSONArray ();
                      facetArrayLong.put(sortedResult.get(i));
                      facetArrayLong.put(longName+" - ("+sortedResult.get(i)+")");
                      facetArrayLong.put(longName);
                      results.put(facetArrayLong);
                    }
                  }
                  //    Put the categories in the final result we return
                  availableFacets.put(categoryNames[j],results);
                } catch (Exception e) {
                    try {
                        String errMsg= "Unable to put category (1) "+categoryNames[j]+" for query "+facetQuery+"\nException: "+e.getMessage();
                        DebugConsole.errprintln(errMsg);
                        error.put("error", errMsg);
                    } catch (JSONException e1) {
                    }
                  queryHasFailed = true;
                  break;
              }
              
              if(!queryHasFailed){
                try {
                  queryAndFacetResult.put("facets", availableFacets);
                } catch (JSONException e) {
                }
              }
          }else{
              queryHasFailed = true;
              try {
                error.put("error", "Unable to put category "+categoryNames[j]+" for query+"+facetQuery+"\nException: "+errorMsg);
              } catch (JSONException e1) {
              }
              break;
          }
      }
    }
    
    t.destroy();
    
    if(queryHasFailed){
      try {
        DebugConsole.errprintln("Query has failed: "+error.getString("error"));
      } catch (JSONException e) {
      }
      return error;
    }
    
    //DebugConsole.println("Search OK!: "+queryAndFacetResult.toString());
    try{
      setCachedVercObject(queryAndFacetResult.toString(),cacheIdentifier);
    }catch(Exception e){
      DebugConsole.errprintln("Unable to set cache for query "+query+" length = "+queryAndFacetResult.toString().length());
    }
    return queryAndFacetResult;
    
  }
  
  
 
  
  /**
   * Makes an asynchronous HTTP request to the ENES VERC Search rest interface.
   * @author plieger
   * 
   */
  private static class ASyncVercQuery extends ASyncRunner{
    protected Object arguments;
    boolean failed = false;
    public ASyncVercQuery(Object arguments) {
        super();
        this.arguments=arguments;
    }
    public Object getArguments() {
      return arguments;
    }
    String data = null;
    String error = null;
    public void succes(Object data) {
        this.data=(String) data;
    }

    public void fail(String message) {
      failed = true;
      error = message;
        DebugConsole.println(message);
    }
    
    public boolean hasFailed(){
      return failed;
    }
    
    public void execute(){
        String data = null;
        String URL=Configuration.VercSearchConfig.__getDeprecatedVercSearchURL()+(String)arguments;
        try {
          if(debug)DebugConsole.println("execute");
            data = getCachedVercObject((String)arguments);
            if(data ==null){
                data = HTTPTools.makeHTTPGetRequest(URL);
                setCachedVercObject(data,(String)arguments);
            }
        } catch (WebRequestBadStatusException e) {
            fail("HTTP request "+URL+" failed: "+e.getMessage());
            return;
        } catch (UnknownHostException e) {
          fail("HTTP request "+URL+" failed: "+e.getMessage());
          return;
        }
        if(data!=null){
            succes(data);
        }else{
            fail("HTTP request "+(String)arguments+" failed");
        }
    }
  }
  
  private static String doVercQuery(String identifier){
    String data = null;
    try {
      if(debug)DebugConsole.println("doVercQuery "+identifier);
        data = getCachedVercObject((String)identifier);
        if(data ==null){
            data = HTTPTools.makeHTTPGetRequest(Configuration.VercSearchConfig.__getDeprecatedVercSearchURL()+(String)identifier);
            setCachedVercObject(data,(String)identifier);
        }
    } catch (WebRequestBadStatusException e) {
      return null;
    } catch (UnknownHostException e) {
      return null;
    }
    return data;
        
  }
  
	private static String getCachedVercObject(String identifier){
		if(!useImpactCache){return null;}
    	cleanRecords();
    	EntityManager entityManager = (EntityManager) JPAStaticResourceBean.getEMF().createEntityManager();
    	PersistentCachedString queryResult = entityManager.find(PersistentCachedString.class,identifier);
    	if(queryResult==null)return null;
		return queryResult.getQueryResult();		
	}
	
	private static void	setCachedVercObject(String data, String identifier){
      if(useImpactCache){
    	  EntityManager entityManager = (EntityManager) JPAStaticResourceBean.getEMF().createEntityManager();
	      entityManager.getTransaction().begin();
	      PersistentCachedString queryResult = entityManager.find(PersistentCachedString.class,identifier);
	      if(queryResult == null){
	    	  queryResult= new PersistentCachedString();
	      }
	      queryResult.setQueryResult(data);
	      queryResult.setQuery(identifier);
	      entityManager.persist(queryResult);
	      entityManager.getTransaction().commit();
      }
	}
  
//	static int cleanupTimeMinutes=60*24*365;
	static volatile  Calendar lastDateDone = null;
	static boolean useImpactCache = true;
	private static volatile boolean cleanRecordsBusy = false;
	private static volatile long cleanRecordsDoneSecondsAgo = 0;
	private static volatile long cleanRecordsEntryDelaySeconds = 60;
	private static JSONObject categoryDescriptions = null;
	private static synchronized void  cleanRecords() {
	  if(debug)DebugConsole.println("cleanRecords");
	  if(cleanRecordsBusy){
	    DebugConsole.println("cleanRecordsBusy");
	    return;
	  }
	  cleanRecordsBusy = true;
	  
	  
	  //Trigger trigger = new CronTrigger("trigger1", "group1");
	  //trigger.setCronExpression("0 0 15 ? * WED");

	  
	  try{
	    long currentSeconds = Calendar.getInstance().getTimeInMillis()/1000;
	    if(currentSeconds-cleanRecordsDoneSecondsAgo<cleanRecordsEntryDelaySeconds){
	      if(debug)DebugConsole.println("cleanRecords done 1 sec ago");
	      cleanRecordsBusy = false;
	      return;
      }
	    cleanRecordsDoneSecondsAgo = currentSeconds;

      String DATE_FORMAT_NOW2 = "yyyy-MM-dd HH:mm:ss";
      SimpleDateFormat sdf1 = new SimpleDateFormat(DATE_FORMAT_NOW2);

	  
	    if(lastDateDone==null){
	      lastDateDone = Calendar.getInstance();
        Date d = lastDateDone.getTime();
        d.setTime(d.getTime()-1000*60*60*24*7);
        lastDateDone.setTime(d);

	    }
	    
	    
	  
	    
      Calendar lastDateOnMonday = (Calendar) lastDateDone.clone();

      lastDateOnMonday.set(Calendar.DAY_OF_WEEK,java.util.Calendar.MONDAY);
      lastDateOnMonday.set(Calendar.HOUR_OF_DAY,0);
      lastDateOnMonday.set(Calendar.MINUTE,0);
      lastDateOnMonday.set(Calendar.SECOND,0);
      
      
	    Calendar roundedLastDateDone=(Calendar) lastDateOnMonday.clone();
	    //roundedLastDateDone.set(Calendar.DAY_OF_WEEK,java.util.Calendar.TUESDAY);
	    roundedLastDateDone.set(Calendar.HOUR_OF_DAY,0);
	    roundedLastDateDone.set(Calendar.MINUTE,0);
	    roundedLastDateDone.set(Calendar.SECOND,0);
	    

      DebugConsole.println("LATEST DATE DONE: "+ sdf1.format(lastDateOnMonday.getTime())+"Z");
      DebugConsole.println("ROUNDED DATE DONE: "+ sdf1.format(roundedLastDateDone.getTime())+"Z");
	    
	    Calendar currentDate = Calendar.getInstance();
	    
	    double a=currentDate.getTimeInMillis();
	    double b=roundedLastDateDone.getTimeInMillis();
	    double differenceInDays=((a-b)/(1000*60*60*24));

	    DebugConsole.println("DATE DIFFERENCE IN DAYS "+differenceInDays);
	    if(differenceInDays<7){
	      cleanRecordsBusy = false;
        return;
	    }
	    lastDateDone = (Calendar) currentDate.clone();

	    DebugConsole.println("START CLEANING SEARCH DB");

	
    

      DebugConsole.println("Starting cleanup");
      EntityManager entityManager = (EntityManager) JPAStaticResourceBean.getEMF().createEntityManager();
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_WEEK,java.util.Calendar.MONDAY);
      cal.set(Calendar.HOUR_OF_DAY,0);
      cal.set(Calendar.MINUTE,0);
      cal.set(Calendar.SECOND,0);


      String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
      String currentISOTimeString = sdf.format(cal.getTime())+"Z";
      DebugConsole.println("CLEANUP COMPARE DATE="+currentISOTimeString);
      entityManager.getTransaction().begin();
      //DebugConsole.println(currentISOTimeString);
      if(true){
        Query q = entityManager.createQuery("select e FROM JPAVercQueryResultCache e where creationDate < '"+currentISOTimeString+"'");
        @SuppressWarnings("unchecked")
        Collection<PersistentCachedString> queryResults=q.getResultList();
        for (Iterator<PersistentCachedString> i = queryResults.iterator(); i.hasNext();) {
          PersistentCachedString e = i.next();
          DebugConsole.println("Cleaning JPAVercQueryResultCache " + e.getCreationDate());
          entityManager.remove(e);
        }
      }
      if(true){
        Query q = entityManager.createQuery("select e FROM JPAImpactQueryResultCache e where creationDate < '"+currentISOTimeString+"'");
        @SuppressWarnings("unchecked")
        Collection<JPAImpactQueryResultCache> queryResults=q.getResultList();
        for (Iterator<JPAImpactQueryResultCache> i = queryResults.iterator(); i.hasNext();) {
          JPAImpactQueryResultCache e = i.next();
          DebugConsole.println("Cleaning JPAImpactQueryResultCache " + e.getCreationDate());
          entityManager.remove(e);
        }
      }
      entityManager.getTransaction().commit();
	  }catch(Exception e){
	    DebugConsole.println(e.getMessage());
	  }
	  try{
	    categoryDescriptions = null;
	    categoryDescriptions = getCategoryDescriptions();
	  }catch(Exception e){
	    e.printStackTrace();
	  }
    cleanRecordsBusy = false;
  }


	public static void makeFacetCacheAvailable(int level,String query){
	  JSONObject mainFacets = getVercQuery(query,0,0,true,false,false);
	  try {
      JSONObject facets = mainFacets.getJSONObject("facets");
      JSONArray[] category=new JSONArray[categoryNames.length];  
      int[] facetNrDone = new int[categoryNames.length];
      for(int categoryNr=0;categoryNr<categoryNames.length;categoryNr++){
        category[categoryNr]=facets.getJSONArray(categoryNames[categoryNr]);
        facetNrDone[categoryNr]=0;
      }
      
      //aap noot mies
      //geeft geen gas
      

      
      //aap
      //noot
      //mies
      //geeft
      //geen 
      //gas
      //aap&geeft
      
      if(query==""){
        query="search?";
      }
      
      for(int j=0;j<categoryNames.length;j++){
        for(int l=0;l<category[j].length();l++){

//(DoHTTPRequest.java:126) Making get: http://verc.enes.org/myapp/cmip5/ws/rest/search?institute=CCCma&&facet=model&querymode=distinct&datatype=file
          String newQuery=query+"&"+categoryNames[j]+"="+((JSONArray)category[j].get(l)).getString(0);
          System.out.println("query: "+newQuery);
        }
                  
      }


    } catch (JSONException e) {
      DebugConsole.errprintln(e.getMessage());
    } 
	  
	}

  /**

  DEPRECATED
  
  public static JSONObject getCategoriesForQuery(String query) {
    return getVercQuery(query,0,0,true,false);
  }

  public static JSONObject makeCached_vercsearch_RequestAsJson(String query,
      int currentPage, int pageSize) {
    return getVercQuery(query,currentPage,pageSize,true,true);
  }
   */
	
	
	 static public String getCategoryLongname(String drsCategory,String shortName){
	   try {
	     if(categoryDescriptions==null){
	       categoryDescriptions = getCategoryDescriptions();
	     }

	     JSONArray catDesc =(JSONArray) categoryDescriptions.get(drsCategory);
	     for(int j=0;j<catDesc.length();j++){
	       JSONArray cat = (JSONArray) catDesc.get(j);
	       if(cat.getString(0).equals(shortName)){
	         String longName = cat.getString(1);
	         if(longName.equalsIgnoreCase("None"))longName=shortName;
	         return longName;
	       }
	       //System.out.println(cat.getString(0)+" and "+cat.getString(1));
	     }
	   } catch (JSONException e) {
	   }
	   return shortName;
	 }
	
	 static public JSONObject getCategoryDescriptions(){
	    JSONObject error=new JSONObject();
	    String categoryDescriptions = categoryNames[0];
	    for(int j=1;j<categoryNames.length;j++)categoryDescriptions+="+"+categoryNames[j];
	    String data = doVercQuery("distcribe?descat="+categoryDescriptions);
	    JSONObject queryResultsObject = null;
	    try{
	      queryResultsObject =  (JSONObject) new JSONTokener(data).nextValue();
	    } catch (Exception e2) {
	        try {
	          error.put("error", "Unable to put search results: \nException: "+e2.getMessage());
	          return error;
	        } catch (JSONException e) {
	        }
	    }

//	  /https://verc.enes.org/myapp/cmip5/ws/rest/distcribe?descat=experiment+model+frequency
	    return queryResultsObject;
	  }
	  
	  public final static void main(String[] args) {
	    System.out.println("Hello world");
	    //getCategoryDescriptions();
	    System.out.println(getCategoryLongname("variable","psl"));
	  }
	  
}