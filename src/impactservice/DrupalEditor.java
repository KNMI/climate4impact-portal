package impactservice;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import tools.DebugConsole;
import tools.HTTPTools;
import tools.HTMLParser;
import tools.HTMLParser.HTMLElementCallback;
import tools.HTMLParser.HTMLParserNode;
import tools.HTTPTools.WebRequestBadStatusException;


public class DrupalEditor {
	//Configuration:
  static public class DrupalEditorException extends Exception  {

    private static final long serialVersionUID = 1L;
    String message;
    int code;
    public DrupalEditorException(){
      super();
      message = "unknown";
      code=200;
    }
    public DrupalEditorException(String message,int code){
      super(message);
      this.message=message;
      this.code=code;
    }
    public DrupalEditorException(String message){
      super(message);
      this.message=message;
      this.code=200;
    }
    public String getMessage(){
      return message;
    }
    public int getCode(){
      return code;
    }
  }
	
	static String doGetRequest(String contentsURL) throws IOException, WebRequestBadStatusException{
		//DebugConsole.println("doGetRequest: '"+contentsURL+"'");
		//The server cannot access his own pages with the home address URL, therefore the home address URL needs to be replaced with localhost which is internally accessible.
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String hostname = addr.getHostName();
			/*contentsURL=contentsURL.replaceAll(hostname, "localhost");*/
//			DebugConsole.println("hostname: '"+hostname+"'");
			if(hostname.equals("bhlnmgis.knmi.nl")){
				String externalName="://webgis.nmdc.eu";
				contentsURL=contentsURL.replaceAll(externalName, "://localhost");
				DebugConsole.println("doGetRequest-replace: '"+contentsURL+"'");  
			}
		
		} catch (UnknownHostException e) {
		}
		
		return    HTTPTools.makeHTTPGetRequest(contentsURL);
		
	}

	static public String showDrupalContent(String defaultPage,HttpServletRequest request) throws DrupalEditorException{
	  boolean showEditButton = false;
	  try {
      User user = User.getUser(request);
      if(user!=null){
        showEditButton = true;
      }
    } catch (Exception e) {
    }
	  return showDrupalContent(defaultPage,request, showEditButton );
	}
	static public String showDrupalContent(String defaultPage,HttpServletRequest request, boolean showEditButton) throws DrupalEditorException{
		String returnMessage="";
		//String homeURL=drupalHost+drupalBaseURL+drupalDirectory;
		String homeURL=Configuration.DrupalConfig.getDrupalHost()+Configuration.DrupalConfig.getDrupalBaseURL()+Configuration.DrupalConfig.getDrupalDirectory();
		//DebugConsole.println(homeURL);
		//homeURL=HTTPTools.makeCleanURL(homeURL);
		//DebugConsole.println(homeURL);
		String drupalURL=homeURL;
		String requestedPageNumber = null;
		String requestedPage=null;
		if(request!=null){
			requestedPage=request.getParameter("q");
			
			if(requestedPage!=null){
			  if(requestedPage.indexOf("search/node")!=-1){
			    showEditButton=false;
			  }
			}
			
			requestedPageNumber=request.getParameter("page");
		}
		
		String drupalHost=Configuration.DrupalConfig.getDrupalHost();
    String drupalBaseURL=Configuration.DrupalConfig.getDrupalBaseURL();
    String drupalDirectory=Configuration.DrupalConfig.getDrupalDirectory();
    
    //DebugConsole.println("Requestedpage='"+requestedPage+"'");
    boolean validPage=true;
    if(requestedPage==null){
      validPage=false;
    }else if (requestedPage.equals("")){
      validPage=false;
    }
    
		if(validPage==true){
			
			try {
				requestedPage=URLEncoder.encode(requestedPage,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				DebugConsole.errprintln("URLEncode failed: "+e.getMessage());
			}
			homeURL=homeURL+"?q="+requestedPage;
			if(requestedPageNumber!=null){
			  homeURL=homeURL+"&page="+requestedPageNumber;
			}
		}else if(defaultPage!=null){
			homeURL=homeURL+defaultPage;
		}

		try{
			returnMessage="";
			/*int divDepth=0;
			int contentStartDivDepth=0;
			int internalDivDepth=0;
			int initialDivDepth=0;*/
			String htmlData;
			htmlData=doGetRequest(homeURL);
			HTMLParser htmlParser = new HTMLParser();
			
			HTMLParserNode element = htmlParser.parseHTMLDocument(htmlData);
			
			HTMLParserNode breadCrumbs=htmlParser.searchTreeNodes(element,"div class=\"breadcrumb\"");
			HTMLParserNode titleNode=htmlParser.searchTreeNodes(element,"h1 class=\"title\" id=\"page-title\"");
			if(breadCrumbs.getNumNodes()>2){
				HTMLParserNode el = new HTMLParser.HTMLParserNode();
				breadCrumbs.nodes.insertElementAt(el,breadCrumbs.getNumNodes()-1);
				el.isStartElement=true;
				el.name = "";
				el.attributes="";
				el.parent=breadCrumbs;
				if(breadCrumbs.getNumNodes()>3)el.content = " » " + titleNode.content.trim();else 	el.content = titleNode.content.trim();
				// Print breadcrumbs
				returnMessage+="<div class=\"breadcrumb\">\n";for(int j=0;j<breadCrumbs.getNumNodes();j++){if(!breadCrumbs.getNode(j).content.equals("Home")){returnMessage+=breadCrumbs.getNode(j).printTree();}}

			}
			
			
			
			
			
			// Print title
			returnMessage += titleNode.printTree();
			
			// Print body
			//HTMLParserNode body = htmlParser.searchTreeNodes(element,"div id=\"block-system-main\" class=\"block block-system\"");
			HTMLParserNode body = htmlParser.searchTreeNodes(element,"div class=\"field-item even\" property=\"content:encoded\"");
			
			if(body == null){
			  body = htmlParser.searchTreeNodes(element,"div id=\"block-system-main\" class=\"block block-system\"");
			}
			
			
			class RemoveFieldImageAndLinkWrapper implements HTMLElementCallback{
				public void callback(HTMLParserNode e){
					if(e.attributes.equals("div class=\"field field-name-field-image field-type-image field-label-above\"")){
						e.parent.nodes.remove(e);
					}
					if(e.attributes.equals("div class=\"link-wrapper\"")){
						e.parent.nodes.remove(e);
					}
					//DebugConsole.println(e.attributes+" == "+e.attributes.indexOf("form id=\"search-form\""));
					if(e.attributes.indexOf("form class=\"search-form\"")!=-1){
            e.parent.nodes.remove(e);
          }
				}
			};
			body.forEachElement(new RemoveFieldImageAndLinkWrapper ());
			
			returnMessage += "\n";
			body.attributes = "";
			returnMessage += body.printTree();
			
			
			returnMessage=returnMessage.replaceAll("href=\""+homeURL,"href=\"?");
			returnMessage=returnMessage.replaceAll("href=\"/"+drupalBaseURL+drupalDirectory,"href=\"");
			returnMessage=returnMessage.replaceAll("href=\"/"+drupalDirectory,"href=\"");
			
			returnMessage=returnMessage.replaceAll("/files/", drupalHost+"/files/");
			
			returnMessage=returnMessage.replaceAll("href=\"http://localhost/","href=\""+drupalHost);
//			DebugConsole.println(drupalURL);
			returnMessage=returnMessage.replaceAll("href=\""+drupalURL,"href=\"");
			returnMessage=returnMessage.replaceAll("<p>There is currently no content classified with this term.</p>", "");
			
		}catch(WebRequestBadStatusException e){
			throw new DrupalEditorException("Could not obtain page "+homeURL+"<br/>\n"+e.getMessage()+"\n",e.getStatusCode());
		}catch(Exception e){
		
      throw new DrupalEditorException("Could not obtain page "+homeURL+"<br/>\n"+e.getMessage()+"\n");
    }

		//returnMessage+=("<hr></hr>");
		if(homeURL.indexOf("?")==-1)homeURL+="?";
		returnMessage+="<script type=\"text/javascript\">\n";
		returnMessage+="function edit(url){newwindow=window.open(url,'drupaleditor','width=800,height=600,fullscreen=0,scrollbars=yes,menubar=no,toolbar=yes,titlebar=no');if (window.focus) {newwindow.focus()}return false;}";
		returnMessage+="</script>\n";
		if(showEditButton==true){
  		if(homeURL.indexOf("q=node")!=-1){
  			returnMessage+=("<div class=\"editcms\"><a onclick=\"edit('"+homeURL+"/edit')\">edit</a></div>");
  		}else{
  			returnMessage+=("<div class=\"editcms\"><a onclick=\"edit('"+homeURL+"')\">edit</a></div>");
  		}
		}
		return returnMessage;
	}

}


