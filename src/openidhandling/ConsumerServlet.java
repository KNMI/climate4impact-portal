package openidhandling;


import impactservice.Configuration;
import impactservice.MessagePrinters;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;




import tools.Debug;

/**
 * @author Sutra Zhou, Maarten Plieger
 * 
 */
public class ConsumerServlet extends javax.servlet.http.HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5998885243419513055L;

	private final Log log = LogFactory.getLog(this.getClass());

	private ServletContext context;

	private ConsumerManager manager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		context = config.getServletContext();

		log.debug("context: " + context);

		// --- Forward proxy setup (only if needed) ---
		// ProxyProperties proxyProps = new ProxyProperties();
		// proxyProps.setProxyName("proxy.example.com");
		// proxyProps.setProxyPort(8080);
		// HttpClientFactory.setProxyProperties(proxyProps);
		try {
			this.manager = new ConsumerManager();
		} catch (ConsumerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager.setAssociations(new InMemoryConsumerAssociationStore());
		manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
    boolean keepId = false;

    String keepIdentifier = req.getParameter("keepid");
    if(keepIdentifier!=null){
      
      if(keepIdentifier.equals("on")){
        keepId=true;
        Cookie cookie = new Cookie("keep_openid_identifier","true");
        cookie.setMaxAge(3600*24*7);
        resp.addCookie(cookie);
      }
      if(!keepId){
        //remove single signon cookie if it hasn't been validated yet
        removeOpenIdCookie(req,resp);
        Cookie cookie = new Cookie("keep_openid_identifier","false");
        cookie.setMaxAge(3600*24*7);
        resp.addCookie(cookie);
      }
    }
    
		if ("true".equals(req.getParameter("fromopenid"))) {

			processReturn(req, resp);
		} else {

			String identifier = req.getParameter("openid_identifier");
			String referrer = req.getHeader("referer"); 
			if (identifier != null) {
				
				identifier=identifier.trim();
				Debug.println("User entered openid identifier ["+identifier+"]");
 			 
				Debug.println("IN: User came from path "+referrer);
				if(keepId==true){
			     if(identifier.startsWith("http")){
             addOpenIdCookie(req,resp,identifier);
           }
				}
				

				this.authRequest(identifier, req, resp);
			} else {
			  Debug.println("No OpenID given: directing to login page");
			  Debug.println("INPUT ERROR: User came from path "+referrer);
				this.getServletContext().getRequestDispatcher(referrer)
						.forward(req, resp); 
			}
		}
	}

	private void addOpenIdCookie(HttpServletRequest req, HttpServletResponse resp,String identifier) {
	  Debug.println("Setting COOKIE openid_identifier="+identifier);
    Cookie cookie = new Cookie("openid_identifier",identifier);
    cookie.setMaxAge(3600*24*7);
    resp.addCookie(cookie);
    
  }

  private void removeOpenIdCookie(HttpServletRequest request,HttpServletResponse resp) {
	  resp.setContentType("text/html");
	  
	  Cookie cookies [] = request.getCookies ();
    if(cookies!=null){
      for (int i = 0; i < cookies.length; i++){
        if (cookies [i].getName().equals ("openid_identifier")){
          cookies [i].setMaxAge(0);
          cookies [i].setPath("/");
          resp.addCookie(cookies [i]);
          Debug.println("EXPIRING COOKIE openid_identifier at " + System.currentTimeMillis());
        }
      }
    }
    Cookie cookie  = new Cookie("openid_identifier", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    resp.addCookie(cookie);
    
    Debug.println("EXPIRING COOKIE openid_identifier at " + System.currentTimeMillis());
    addOpenIdCookie(request,resp,"");
        
  }

  private void processReturn(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
    Debug.println("Validating request...");
		Identifier identifier = this.verifyResponse(req);
		log.debug("identifier: " + identifier);
		if (identifier != null) {
		  Debug.println("Validation is OK");
			req.setAttribute("identifier", identifier.getIdentifier());
			//req.setAttribute("email", identifier.getIdentifier());
			req.getSession().setAttribute("openid_identifier",identifier.getIdentifier());
			req.getSession().setAttribute("user_identifier",identifier.getIdentifier());
		}else{
		  Debug.println("Validation is INVALID");
		}
	 
		String referrer = getValidReferrer(req);
	 
	  
	  //this.getServletContext().getRequestDispatcher("/account/login_embed.jsp").forward(req, resp);
		this.getServletContext().getRequestDispatcher(referrer).forward(req, resp);
	}

	private String getValidReferrer(HttpServletRequest req) {
	  String returnURL = "/account/login.jsp";
	  String referrer = null; 
    try {
      referrer = URLDecoder.decode(req.getParameter("returnurl"),"UTF-8");
    } catch (Exception e) {
    }
    
    if(referrer==null){
      String r=req.getHeader("referer");
      if(r!=null){
        referrer = r;
      }
    }
	  
	  if(referrer!=null){
	    Debug.println("Referrer was "+referrer);
      if(referrer.startsWith("/") == false){
        String homeURL=null;
        if(referrer.startsWith("http")){
          homeURL = Configuration.getHomeURLHTTP();
        }
        if(referrer.startsWith("https")){
          homeURL = Configuration.getHomeURLHTTPS();
        }
        Debug.println("HomeURL: "+homeURL);
     
        if(referrer.startsWith(homeURL)){
          returnURL = referrer.substring(homeURL.length());
        }
      }else{
        returnURL = referrer;
      }
    }
  	Debug.println("processReturn: User came from: "+returnURL);
    return returnURL;
  }

  // --- placing the authentication request ---
	@SuppressWarnings("unchecked")
	public String authRequest(String userSuppliedString,
			HttpServletRequest httpReq, HttpServletResponse httpResp)
			throws IOException, ServletException {
		try {
			// configure the return_to URL where your application will receive
			// the authentication responses from the OpenID provider
			// String returnToUrl = "http://example.com/openid";
			String returnToUrl = httpReq.getRequestURL().toString() + "?fromopenid=true&";
			//DebugConsole.println("OriginURL="+httpReq.get);
			//DebugConsole.println("OriginURL="+httpReq.getPathTranslated());
			Debug.println("ReturnURL= "+returnToUrl);
			String referrer = getValidReferrer(httpReq);;
      Debug.println("User came from path "+referrer);
      if(referrer!=null){
        returnToUrl+="returnurl="+URLEncoder.encode(referrer, "UTF-8");
      }

			// perform discovery on the user-supplied identifier
			List<DiscoveryInformation> discoveries = manager.discover(userSuppliedString);

			// attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			DiscoveryInformation discovered = manager.associate(discoveries);

			
			
			// store the discovery information in the user's session
			httpReq.getSession().setAttribute("openid-disc", discovered);

			// obtain a AuthRequest message to be sent to the OpenID provider
			AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

			// Attribute Exchange example: fetching the 'email' attribute
			FetchRequest fetch = FetchRequest.createFetchRequest();
			SRegRequest sregReq = SRegRequest.createFetchRequest();
			
		/*	Enumeration<String> paramNames = httpReq.getParameterNames();
			
			while(paramNames.hasMoreElements()){
			  String param = (String) paramNames.nextElement();
			  DebugConsole.println("param "+param);
		  }*/
			
	    /* Clear the attribute in advance :) */
      httpReq.getSession().setAttribute("emailaddress", null);
      
			//fetch.addAttribute("urn:esg:email:address",true);
			//fetch.addAttribute("esg:email:address",true);
			//fetch.addAttribute("EmailAddress", "http://www.w3.org/2001/XMLSchema", true);
			//fetch.addAttribute("Email", "http://schema.openid.net/contact/email", true);
			/* According to BADC openid this should be http://openid.net/schema/contact/internet/email instead of http://schema.openid.net/contact/email" */
			fetch.addAttribute("email", "http://openid.net/schema/contact/internet/email", true);
			

			//sregReq.addAttribute("urn:esg:email:address",true);
			//sregReq.addAttribute("esg:email:address",true);
			//sregReq.addAttribute("Email", "http://schema.openid.net/contact/email", true);
			
/*			if ("1".equals(httpReq.getParameter("nickname"))) {
				// fetch.addAttribute("nickname",
				// "http://schema.openid.net/contact/nickname", false);
				sregReq.addAttribute("nickname", false);
			}
*/			if ("1".equals(httpReq.getParameter("email"))) {
				//fetch.addAttribute("email","http://schema.openid.net/contact/email", false);
				fetch.addAttribute("email","http://openid.net/schema/contact/internet/email", false);
				sregReq.addAttribute("email", false);
			}
//			if ("1".equals(httpReq.getParameter("fullname"))) {
//				fetch.addAttribute("fullname",
//						"http://schema.openid.net/contact/fullname", false);
//				sregReq.addAttribute("fullname", false);
//			}
//			if ("1".equals(httpReq.getParameter("dob"))) {
//				fetch.addAttribute("dob",
//						"http://schema.openid.net/contact/dob", true);
//				sregReq.addAttribute("dob", false);
//			}
//			if ("1".equals(httpReq.getParameter("gender"))) {
//				fetch.addAttribute("gender",
//						"http://schema.openid.net/contact/gender", false);
//				sregReq.addAttribute("gender", false);
//			}
//			if ("1".equals(httpReq.getParameter("postcode"))) {
//				fetch.addAttribute("postcode",
//						"http://schema.openid.net/contact/postcode", false);
//				sregReq.addAttribute("postcode", false);
//			}
//			if ("1".equals(httpReq.getParameter("country"))) {
//				fetch.addAttribute("country",
//						"http://schema.openid.net/contact/country", false);
//				sregReq.addAttribute("country", false);
//			}
//			if ("1".equals(httpReq.getParameter("language"))) {
//				fetch.addAttribute("language",
//						"http://schema.openid.net/contact/language", false);
//				sregReq.addAttribute("language", false);
//			}
//			if ("1".equals(httpReq.getParameter("timezone"))) {
//				fetch.addAttribute("timezone",
//						"http://schema.openid.net/contact/timezone", false);
//				sregReq.addAttribute("timezone", false);
//			}

			// attach the extension to the authentication request
			if (!sregReq.getAttributes().isEmpty()) {
			  Debug.println("SREG Attributes defined, adding extension");
				authReq.addExtension(sregReq);
			
			}
			
			if(!fetch.getAttributes().isEmpty()){
			  Debug.println("AX Attributes defined, adding extension");
        authReq.addExtension(fetch);
			}

			boolean useGetRequestInstead = true;
			/**
			 * Post requests are not supported by PCMDI, only GET requests are
			 * This ensures that only get requests are made to PCMDI.
			 */
			if (!discovered.isVersion2()||useGetRequestInstead) {
				// Option 1: GET HTTP-redirect to the OpenID Provider endpoint
				// The only method supported in OpenID 1.x
				// redirect-URL usually limited ~2048 bytes
				httpResp.sendRedirect(authReq.getDestinationUrl(true));
				return null;
			} else {
				// Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)

				RequestDispatcher dispatcher = getServletContext()
						.getRequestDispatcher("/formredirection.jsp");
				httpReq.setAttribute("parameterMap", httpReq.getParameterMap());
				httpReq.setAttribute("message", authReq);
				// httpReq.setAttribute("destinationUrl", httpResp
				// .getDestinationUrl(false));
				dispatcher.forward(httpReq, httpResp);
			}
		} catch (OpenIDException e) {
			//PrintWriter out = httpResp.getWriter();

			//out.println("OpenIDException occured: "+e.getMessage());
			//httpReq.getSession().setAttribute("openid-disc", discovered);
			//httpReq.setAttribute("message", "OpenIDException: "+e.getMessage());
		  int code = e.getErrorCode();
		  
		  
		  String errorMsg="We tried to login with your Open ID identifier, but something went wrong (Code "+code+").<br/>";
		  errorMsg+="You entered:<br/><strong>"+userSuppliedString+"</strong><br/><br/>";
		  if(code == 1798){
		    errorMsg+="<strong>The OpenID identifier you entered does probably not exist.</strong><br/><br/>";
		  }
		  if(code == 1796){
        errorMsg+="<strong>The OpenID provider is probably not recognized by climate4impact.</strong><br/><br/>";
      }
		      errorMsg+="The problem has the following details:<br/>"+e.getMessage();
		  Debug.errprintln(errorMsg);
		  //DebugConsole.printStackTrace(e);

      String identifier = httpReq.getParameter("openid_identifier");
		  MessagePrinters.emailFatalErrorException("Login Failed for "+identifier, e);
			httpReq.getSession().setAttribute("message", errorMsg);
			this.getServletContext().getRequestDispatcher("/exception.jsp").forward(httpReq, httpResp);
			// present error to the user 
		}

		return null;
	}

	// --- processing the authentication response ---
	@SuppressWarnings("unchecked")
	public Identifier verifyResponse(HttpServletRequest httpReq) {
		try {
			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)
			ParameterList response = new ParameterList(httpReq
					.getParameterMap());
			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = (DiscoveryInformation) httpReq
					.getSession().getAttribute("openid-disc");

			// extract the receiving URL from the HTTP request
			StringBuffer receivingURL = httpReq.getRequestURL();
			String queryString = httpReq.getQueryString();
			if (queryString != null && queryString.length() > 0)
				receivingURL.append("?").append(httpReq.getQueryString());

			// verify the response; ConsumerManager needs to be the same
			// (static) instance used to place the authentication request
			VerificationResult verification = manager.verify(receivingURL
					.toString(), response, discovered);

			// examine the verification result and extract the verified
			// identifier
			Identifier verified = verification.getVerifiedId();
			if (verified != null) {
				AuthSuccess authSuccess = (AuthSuccess) verification
						.getAuthResponse();
				Debug.println("Verifications succesfull");

				if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG11)) {
				  Debug.println("Has extension OPENID_NS_SREG1");
				}
				
				if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
				  Debug.println("Has extension OPENID_NS_SREG");
				  
					MessageExtension ext = authSuccess
							.getExtension(SRegMessage.OPENID_NS_SREG);
					if (ext instanceof SRegResponse) {
						SRegResponse sregResp = (SRegResponse) ext;
						for (Iterator<String> iter = sregResp.getAttributeNames()
								.iterator(); iter.hasNext();) {
							String name = (String) iter.next();
							Debug.println("name="+name);
							String value = sregResp.getParameterValue(name);
							httpReq.setAttribute(name, value);
						}
					}
				}
				
        if (!authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
          Debug.errprintln("No extensions received from provider");
        }
        
        
				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
				  Debug.println("Has extension OPENID_NS_AX");
					FetchResponse fetchResp = (FetchResponse) authSuccess
							.getExtension(AxMessage.OPENID_NS_AX);

					/*List emails1 = fetchResp.getAttributeValues("urn:esg:email:address");
					List emails2 = fetchResp.getAttributeValues("esg:email:address");
					DebugConsole.println("***********1 "+emails1.size());
					DebugConsole.println("***********2 "+emails2.size());
					
				 */
				 	
			    String emailAddress = fetchResp.getAttributeValue("email");
			    Debug.println("User email is "+emailAddress);
			    
			
			    if(emailAddress!=null){
			      if(emailAddress.length()>0){
			        httpReq.getSession().setAttribute("emailaddress", emailAddress);
			      }
			    }
			   
				 
					// String email = (String) emails.get(0);

					List<String> aliases = fetchResp.getAttributeAliases();
					
					
					//DebugConsole.println("Number of aliases: "+aliases.size());
					for (Iterator<String> iter = aliases.iterator(); iter.hasNext();) {
						String alias = (String) iter.next();
						List<String> values = fetchResp.getAttributeValues(alias);
						if (values.size() > 0) {
							log.debug(alias + " : " + values.get(0));
							Debug.println("name="+alias+" value="+values.get(0));
							httpReq.setAttribute(alias, values.get(0));
						}
					}
				}

				return verified; // success
			}
		} catch (OpenIDException e) {
		//	PrintWriter out = httpResp.getWriter();
			//out.println(e.getMessage());
			// present error to the user
		}

		return null;
	}
}
