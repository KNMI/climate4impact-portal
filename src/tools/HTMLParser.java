package tools;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import tools.Debug;
import tools.HTTPTools;
import tools.HTTPTools.WebRequestBadStatusException;


public class HTMLParser {
  
  private static boolean debug = false;
  
	public interface HTMLElementCallback{
		void callback(HTMLParserNode e);
	}
	
	public static class HTMLParserNode{
		public boolean isStartElement=true;
		public String content = "";
		public String attributes = null;
		public String name = null;
		public Vector<HTMLParserNode> nodes = new Vector<HTMLParserNode>();
		public HTMLParserNode parent;
		public int depth = -1;
		public HTMLParserNode getNode(int nr){
			return nodes.get(nr);
		}
		public int getNumNodes(){
			return nodes.size();
		}
		public void forEachElement(HTMLElementCallback a) {
			for(int j=0;j<nodes.size();j++){
				a.callback(nodes.get(j));
				nodes.get(j).forEachElement(a);
			}
		}
	  public String printTree(){
	    String data="";

	    //for(int j=0;j<e.depth;j++)System.out.print("  ");
	    if(this.isStartElement){
	      if(this.attributes.length()>0)data="<"+this.attributes+">";
	      data+=this.content;
	    }
	    if(!this.isStartElement){
	      if(this.name.length()>0){
	        data="</"+this.name+">";
	      }
	      data+=this.content;
	    }
	  
	    for(int j=0;j<this.nodes.size();j++){
	      data=data+this.nodes.get(j).printTree();
	    }
	    return data;
	  }
	}
	
	static String doGetRequest(String contentsURL) throws IOException {
		Debug.println("doGetRequest: '" + contentsURL + "'");
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String hostname = addr.getHostName();
			contentsURL = contentsURL.replaceAll(hostname, "localhost");
			Debug.println("hostname: '" + hostname + "'");
			if (hostname.equals("bhlnmgis.knmi.nl")) {
				String externalName = "://webgis.nmdc.eu";
				contentsURL = contentsURL.replaceAll(externalName,
						"://localhost");
				Debug.println("doGetRequest-replace: '" + contentsURL
						+ "'");
			}

		} catch (UnknownHostException e) {
		}
		try {
			return HTTPTools.makeHTTPGetRequest(contentsURL,0);
		} catch (WebRequestBadStatusException e) {
			Debug.errprintln(e.getMessage());
			return "Statuscode: "+ e.getStatusCode();
		}
	}

	/**
	 * @param args
	 */
	

	
	
	
	private HTMLParserNode elementStart(int domDepth,String name,String elementBody,HTMLParserNode hTMLParserNode) {
	  if(hTMLParserNode == null)return null;
	  if(debug){
	    for (int k = 0; k < domDepth; k++)System.out.print("  ");
	    System.out.println("<" + elementBody + ">");
	  }
		HTMLParserNode el = new HTMLParserNode();
		hTMLParserNode.nodes.add(el);
		el.isStartElement=true;
		el.name = name;
		el.attributes=elementBody;
		el.parent=hTMLParserNode;
		el.depth=domDepth;
		return el;
	}

	public HTMLParserNode elementEnds(int domDepth, String elementName,HTMLParserNode hTMLParserNode) {
	  if(debug){
	    for (int k = 0; k < domDepth; k++)System.out.print("  ");
		  System.out.println("</" + elementName + ">");
	  }
		
		HTMLParserNode el = new HTMLParserNode();
		hTMLParserNode.nodes.add(el);
		el.isStartElement=false;
		el.name=elementName;
		el.attributes="/"+elementName;
		el.parent=hTMLParserNode;
		el.depth=domDepth;
		return el;
	}

	private void elementContent(int domDepth, String content,HTMLParserNode hTMLParserNode){
	  if(debug){
	    for (int k = 0; k < domDepth + 1; k++)System.out.print("  ");
	    System.out.println("["+content.trim()+"]");
	  }
		
		
		hTMLParserNode.content=content;
	}
	
	
	public HTMLParserNode parseHTMLDocument(String htmlData) {
		HTMLParserNode hTMLParserNode = new HTMLParserNode();
		HTMLParserNode contentsElement=hTMLParserNode;
		HTMLParserNode topElement=hTMLParserNode;
		
		byte[] doc = null;
		try {
			doc = htmlData.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			Debug.errprintln("Unable to convert htmldata to byte array");
			return null;
		}

		int docLength = doc.length;
		int domDepth = -1;
		int maxElementLength = 4096 * 4;
		byte[] elementData = new byte[maxElementLength];
		String[] elementTagList = new String[128];
		boolean isElementEnd = false;
		int elementContentStart = 0;
		int elementContentStop = 0;
		boolean isScriptTag = false;
		int isComment = 0;
		for (int j = 0; j < docLength; j++) {
			if (doc[j] == '<' &&doc[j+1]=='!'&&doc[j+2]=='-'&&doc[j+3]=='-') {
				isComment++;
				
				//System.out.println("isComment!="+isComment);
				
			}
			if (doc[j] == '<' &&doc[j+1]!='!'&&isComment==0) {
				elementContentStop = j;
				if (elementContentStart < elementContentStop && elementContentStart != -1
						&& elementContentStop < docLength) {
					String elementsContent = null;
					try {
						elementsContent = new String(doc, elementContentStart, elementContentStop - elementContentStart, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						Debug.errprintln("UnsupportedEncodingException");
						elementsContent="";
					}
					//elementsContent = elementsContent.trim();
					if (elementsContent.length() > 0) {
						elementContent(domDepth,elementsContent,contentsElement);
					}
				}
				int elNameLength = 0;
				int elContentLength = 0;
				for (int i = j + 1; i < docLength && doc[i] != '>' && elNameLength < maxElementLength; i++) {
					//Get the name of the tag, can be delimited by ' ' or '>'.
					if (elContentLength == 0)if (doc[i] == ' ')elContentLength = elNameLength;
					elementData[elNameLength++] = doc[i];
				}
				
				
				if (elContentLength == 0)elContentLength = elNameLength;

				if (elementData[0] != '/')isElementEnd = false;else isElementEnd = true;
				
				try {
					String elementName = (new String(elementData, "UTF-8").substring(0,elContentLength).replace("/", ""));
					elementName = elementName.trim();
					if (isElementEnd == false && isScriptTag == false) {
						domDepth++;
						if (domDepth < 0)domDepth = 0;

						int elementBodyLength = elNameLength;

						String elementBody = (new String(elementData, "UTF-8").substring(0, elementBodyLength));
						contentsElement = elementStart(domDepth,elementName,elementBody,hTMLParserNode);
						if(contentsElement != null){
  						hTMLParserNode=contentsElement;
  						if (elementName.equals("script")) {
  							isScriptTag = true;
  						}
  					
  						elementTagList[domDepth] = elementName;
  						if (elementData[elNameLength - 1] == '/') {
  							domDepth--;
  							hTMLParserNode=hTMLParserNode.parent;
  						}
						}
					}

					if (isElementEnd == true) {
						if (elementName.equals("script")) {
							isScriptTag = false;
						}
						
						if (isScriptTag == false) {
							do {
								if (domDepth < 0)domDepth = 0;
								if (elementTagList[domDepth].equals(elementName))
									break;
								contentsElement = elementEnds(domDepth, elementTagList[domDepth],hTMLParserNode);
								hTMLParserNode=hTMLParserNode.parent;
								domDepth--;
							} while (domDepth > 0);
							if(domDepth==0)return topElement.getNode(0);;
							contentsElement = elementEnds(domDepth, elementName,hTMLParserNode);
							hTMLParserNode=hTMLParserNode.parent;
						}

					}
				} catch (UnsupportedEncodingException e) {
					Debug.errprintln("UnsupportedEncodingException");
				}
				if (isElementEnd == true)
					domDepth--;
			}
			if (j > 0 && doc[j] == '>') {
				elementContentStart = j + 1;
				if(j>2&&isComment>0){
					if (doc[j-1]=='-'&&doc[j-2]=='-') {
						isComment--;
						//System.out.println("/isComment!="+isComment);
						
						//contentsElement = elementEnds(domDepth, "--",hTMLParserNode);
						/*hTMLParserNode=hTMLParserNode.parent;
						if (isElementEnd == true)
							domDepth--;*/
					}
				}
			}
		}
		return topElement.getNode(0);
	}
	

	


  public HTMLParserNode getBody(HTMLParserNode e){
    if(e.isStartElement){
      //System.out.println(e.attributes+" == "+search);
      if(e.name.equalsIgnoreCase("body")){
        return e;
      }
    }
    for(int j=0;j<e.nodes.size();j++){
      HTMLParserNode re=getBody(e.nodes.get(j));
      if(re!=null)return re;
    }   
    return null;
  }

	
	public HTMLParserNode searchTreeNodes(HTMLParserNode e,String search){
		if(e.isStartElement){
			//System.out.println(e.attributes+" == "+search);
			if(e.attributes.equals(search)){
				return e;
			}
		}
		for(int j=0;j<e.nodes.size();j++){
			HTMLParserNode re=searchTreeNodes(e.nodes.get(j),search);
			if(re!=null)return re;
		}		
		return null;
	}

	public static void main(String[] args) {
		/*HTMLParser htmlParser = new HTMLParser();
	
		
		String htmlData = null;
		try {htmlData = doGetRequest("http://webgis.nmdc.eu/knmi/is-enes/drupal-7.8/?q=Uncertainties");} catch (IOException e1) {}
		htmlData = DoHTTPRequest.makeHTTPGetRequest("");
		
		HTMLParserNode hTMLParserNode = htmlParser.parseHTMLDocument(htmlData);
		
		htmlParser.printTree(htmlParser.searchTreeNodes(hTMLParserNode,"div class=\"breadcrumb\""));
		htmlParser.printTree(htmlParser.searchTreeNodes(hTMLParserNode,"h1 class=\"title\" id=\"page-title\""));
		htmlParser.printTree(htmlParser.searchTreeNodes(hTMLParserNode,"div id=\"block-system-main\" class=\"block block-system\""));
		*/
	}
	
	

/*
    GNU LESSER GENERAL PUBLIC LICENSE
    Copyright (C) 2006 The XAMJ Project

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Contact info: lobochief@users.sourceforge.net
*/
	 public static String textToHTML(String text) {
	    if(text == null) {
	      return null;
	    }
	    int length = text.length();
	    boolean prevSlashR = false;
	    StringBuffer out = new StringBuffer();
	    for(int i = 0; i < length; i++) {
	      char ch = text.charAt(i);
	      switch(ch) {
	      case '\r':
	        if(prevSlashR) {
	          out.append("<br/>");         
	        }
	        prevSlashR = true;
	        break;
	      case '\n':
	        prevSlashR = false;
	        out.append("<br/>");
	        break;
	      case '"':
	        if(prevSlashR) {
	          out.append("<br/>");
	          prevSlashR = false;         
	        }
	        out.append("&quot;");
	        break;
	      case '<':
	        if(prevSlashR) {
	          out.append("<br/>");
	          prevSlashR = false;         
	        }
	        out.append("&lt;");
	        break;
	      case '>':
	        if(prevSlashR) {
	          out.append("<br/>");
	          prevSlashR = false;         
	        }
	        out.append("&gt;");
	        break;
	      case '&':
	        if(prevSlashR) {
	          out.append("<br/>");
	          prevSlashR = false;         
	        }
	        out.append("&amp;");
	        break;
	      default:
	        if(prevSlashR) {
	          out.append("<br/>");
	          prevSlashR = false;         
	        }
	        out.append(ch);
	        break;
	      }
	    }
	    return out.toString();
	  }

}

