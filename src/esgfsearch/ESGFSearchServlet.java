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
       
	  private static ExecutorService threadPool = null;
	  private  static Search esgfSearch = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ESGFSearchServlet() {
        super();
        esgfSearch=getESGFSearchInstance();
    }
    


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    esgfSearch.doGet(request,response);
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
     esgfSearch = null;
   }



  public static synchronized Search getESGFSearchInstance() {
    if(esgfSearch!=null)return esgfSearch;
    Debug.println("Creating new ESGF search instance with endpoint "+Configuration.VercSearchConfig.getEsgfSearchURL());
    threadPool = Executors.newFixedThreadPool(4);
    esgfSearch = new Search(Configuration.VercSearchConfig.getEsgfSearchURL(),Configuration.getImpactWorkspace()+"/diskCache/",threadPool);
    return esgfSearch;
  }

}
