package downscaling;

import impactservice.Configuration;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.Debug;
import esgfsearch.Search;

/**
 * Servlet implementation class DownscalingSearch
 * accessible via /impactportal/DownscalingSearch?
 * 
 * Configurable via:
 * <dpbasesearchresturl>https://esg-dn1.nsc.liu.se/esg-search/search?</dpbasesearchresturl>
 */
public class DownscalingSearch extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  private static ExecutorService downscalingThreadPool = null;
  private  static Search downscalingSearch = null;
  
  /**
   * @see HttpServlet#HttpServlet()
   */
  public DownscalingSearch() {
      super();
      String searchService=Configuration.DownscalingConfig.getDpBaseSearchRestUrl();
      Debug.println("Creating new ESGF search instance with endpoint "+searchService);
      downscalingThreadPool = Executors.newFixedThreadPool(4);
      downscalingSearch = new Search(searchService,Configuration.getImpactWorkspace()+"/diskCache/",downscalingThreadPool);
  }
  
  
  
  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    downscalingSearch.doGet(request,response);
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
     downscalingThreadPool.shutdown();
     downscalingSearch = null;
   }
  
  
  
  public static Search getESGFSearchInstance() {
    return downscalingSearch;
  }

}
