package esgfsearch;

import impactservice.Configuration;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
      

      int pageLimit = 25;
      try{
        String pageLimitStr=HTTPTools.getHTTPParam(request,"pagelimit");
        if(pageLimitStr!=null){
          pageLimit=Integer.parseInt(pageLimitStr);
        }
      }catch (Exception e) {
      }
      
      int pageNr = 0;
      try{
        String pageNrStr=HTTPTools.getHTTPParam(request,"pagenumber");
        if(pageNrStr!=null){
          pageNr=Integer.parseInt(pageNrStr);
        }
      }catch (Exception e) {
      }
      
      if(service.equalsIgnoreCase("search")){
        //Thread.sleep(100);

        if(mode.equalsIgnoreCase("getfacets")){
          HttpSession session=request.getSession();
          String savedQuery=(String)session.getAttribute("savedquery");
          
          if (query.equals("clear=clear")){
            query="";
          } else if (query.equals("clear=onload")){
            query=savedQuery!=null?savedQuery:"";
          }
          session.setAttribute("savedquery", query);

          JSONResponse jsonresponse = esgfSearch.getFacets(facets,query,pageNr,pageLimit);
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
        if(mode.equalsIgnoreCase("getSearchResultAsJSON")){
          JSONResponse jsonresponse = esgfSearch.getSearchResultAsJSON(query,request);
          jsonresponse.setJSONP(jsonp);
          response.setContentType(jsonresponse.getMimeType());
          response.getOutputStream().print(jsonresponse.getMessage());
        }
        if(mode.equalsIgnoreCase("getSearchResultAsCSV")){
          String responseString = esgfSearch.getSearchResultAsCSV(query,request);
          
          response.setContentType("text/plain");
          response.getOutputStream().print(responseString);
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
