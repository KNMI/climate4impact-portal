package tools;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ProxyHTTPRequest
 */
public class ProxyHTTPRequest_Deprecated2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProxyHTTPRequest_Deprecated2() {
        super();
        // TODO Auto-generated constructor stub
    }
    
   /* private static void processPostGet(HttpServletRequest request, HttpServletResponse response) {
    	System.out.println("ProxyHTTPRequest");
    	PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			System.out.print(e.getMessage());
			return;
		}
		String urlStr = null;
    	urlStr=request.getParameter("request");
    	if(urlStr==null){
    		//result="request is missing";
    		return;
    	}else{
    		try {
				urlStr=URLDecoder.decode(urlStr,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				return;
			}
    		String result = DoHTTPRequest.makeHTTPGetRequest(urlStr);
    		out.print(result);
    	}
   
    }
	*/
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	//	processPostGet(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//processPostGet(request, response);
	}

}
