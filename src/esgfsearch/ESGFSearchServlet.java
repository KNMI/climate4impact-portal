package esgfsearch;

import impactservice.Configuration;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.Debug;
import tools.HTTPTools;
import tools.JSONResponse;
/**
 * Servlet implementation class ESGFSearchServlet
 */
public class ESGFSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	  static ExecutorService threadPool = null;
	  static Search esgfSearch = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ESGFSearchServlet() {
        super();
        getSearch();
    }
    
    public static Search getSearch(){
      if(esgfSearch!=null)return esgfSearch;
      threadPool = Executors.newFixedThreadPool(4);
      esgfSearch = new Search(Configuration.VercSearchConfig.getEsgfSearchURL(),Configuration.getImpactWorkspace()+"/diskCache/",threadPool);
      return esgfSearch;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	  
	  try {
	    String service=HTTPTools.getHTTPParam(request,"service");
      String mode=HTTPTools.getHTTPParam(request,"request");
      
      String jsonp = null;
      try{
        jsonp=HTTPTools.getHTTPParam(request,"jsonp");
      }catch (Exception e) {
        try{
          jsonp=HTTPTools.getHTTPParam(request,"callback");
        }catch(Exception e2){
        }
      }
      
      String query = null;
      try{
        query=HTTPTools.getHTTPParam(request,"query");
      }catch (Exception e) {
      }
      
      String facets = null;
      try{
        facets=HTTPTools.getHTTPParam(request,"facet");
      }catch (Exception e) {
      }
      
      if(service.equalsIgnoreCase("search")){
        //Thread.sleep(100);
        if(mode.equalsIgnoreCase("getfacets")){
          JSONResponse jsonresponse = esgfSearch.getFacets(facets,query);
          jsonresponse.setJSONP(jsonp);
          response.setContentType(jsonresponse.getMimeType());
          response.getOutputStream().print(jsonresponse.getMessage());
        }
        if(mode.equalsIgnoreCase("checkurl")){
          JSONResponse jsonresponse = esgfSearch.checkURL(query,request);
          jsonresponse.setJSONP(jsonp);
          response.setContentType(jsonresponse.getMimeType());
          response.getOutputStream().print(jsonresponse.getMessage());
        }
        if(mode.equalsIgnoreCase("addtobasket")){
          JSONResponse jsonresponse = esgfSearch.addtobasket(query,request);
          jsonresponse.setJSONP(jsonp);
          response.setContentType(jsonresponse.getMimeType());
          response.getOutputStream().print(jsonresponse.getMessage());
        }
      }
      
    } catch (Exception e) {
    }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  doGet(request,response);
	}
	
   public void destroy(){
     Debug.println("Shutting down");
     super.destroy();
     threadPool.shutdown();
   }

}
