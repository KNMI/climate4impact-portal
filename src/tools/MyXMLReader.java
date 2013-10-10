package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class MyXMLReader {
	private String XMLFileName = null;
	private Config config = null;
  private Document doc = null;
  private enum xmlParseMode {parseXMLFile,parseXMLString;}
  private enum listWalkerMode {getNodeValues,setNodeValue;}
	/*
	 * Gets the value for a certain node in the file
	 */
	public String getNodeValue(String name){
		if(config==null){
			DebugConsole.errprintln("MyXMLReader: parseXML not called");
			return null;
		}
		return config.getValue(name);
	}
	public String[] getNodeValues(String name){
		if(config==null){
			DebugConsole.errprintln("MyXMLReader: parseXML not called");
			return null;
		}
		return config.getValues(name);
	}
	
	public boolean setNodeValue(String name,String value){
		if(config==null){
			DebugConsole.errprintln("MyXMLReader: parseXML not called");
			return false;
		}		
		return config.setValue(name,value);
	}
	public Document getXMLDocument(){
		return doc;
	}
	
	//This class is created by calling the function parseXML
	public class Config{
		ArrayList<String> items= new ArrayList<String>();
		ArrayList<String> values= new ArrayList<String>();
		private void addItem(String _item, String _value){
			int i = items.indexOf(_item);
			if(i!=-1){
				//It is an array!
				String a= values.get(i);
				a=a+"<|>"+_value;
				values.set(i, a);
			}else{
				items.add(_item);
				values.add(_value);
			}
		}

		private Node getNodeListIndexOf(NodeList nodeLst, String nodeName){
			for (int s = 0; s < nodeLst.getLength(); s++) {
			    Node fstNode = nodeLst.item(s);
			    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			    	if(fstNode.getNodeName().equals(nodeName)){
			    		return fstNode; 
			    	}
			    }
			}
			return null;
		}
		private Node getFirstElementNode(NodeList nodeLst){
			for (int s = 0; s < nodeLst.getLength(); s++) {
			    Node fstNode = nodeLst.item(s);
			    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			    	return fstNode; 
			    }
			}
			return nodeLst.item(0);
		}

		public boolean setValue(String name,String value){
			// First try to modify an existing node's value
			boolean returnValue = listWalker(config , null,doc.getChildNodes(),listWalkerMode.setNodeValue,name,value);
			
			//  If the node does not exist, append the node to both the XML Doc and the internal items/values list
			if(returnValue==false){
				Node node = doc;
				NodeList nodeList = doc.getChildNodes();
				// Decompose the path
				String[] pathElements= name.split("[.]");
				//Try to traverse as far as possible through the tree
				int j=0;
				do{
					NodeList tempNodeList = null;
					tempNodeList = node.getChildNodes();
					node = getNodeListIndexOf(tempNodeList  ,pathElements[j]);
					if(node!=null){
						nodeList = tempNodeList;
					}
					j++;
				}while(node!=null&&j<pathElements.length);
				
				//Create the nodes which do not exits
				if(node==null){
					Node parentNode  = getFirstElementNode(nodeList);
					do{
						DebugConsole.println("Creating element "+pathElements[j-1]);
						Element newNode = doc.createElement(pathElements[j-1]);
						node=newNode;
						parentNode.appendChild(node);
						parentNode = node;
						j++;
					}while(j<pathElements.length);
				}
				// TODO which of these two is correct??
				node.setTextContent(value);
				node.setNodeValue(value);
				// Add the name to the list
				config.addItem( name, value);
				return true;
			}
			return returnValue;
		}
		
		public String getValue(String item){
			int i = items.indexOf(item);
			if(i!=-1){
				//return values.get(i).split("|")[0];
				return values.get(i).split("<\\|>")[0];
			}
	  		return null;
		}
		public String []getValues(String item){
			int i = items.indexOf(item);
			if(i!=-1){
				//return values.get(i).split("|")[0];
				return values.get(i).split("<\\|>");
			}
	  		return null;
		}
	}

	private boolean listWalker(Config config, String root,NodeList nodeLst, listWalkerMode mode, String nodeName,String nodeValue){
	  boolean returnValue = false;
	  for (int s = 0; s < nodeLst.getLength(); s++) {
		    Node fstNode = nodeLst.item(s);
		    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
		      Element fstElmnt = (Element) fstNode;
		      String childName=null;
		      if(root!=null){
		    	  childName=root+"."+fstNode.getNodeName();
		      }else{
		    	  childName=fstNode.getNodeName();
		      }
		      if(mode==listWalkerMode.getNodeValues){
		    	  if(fstElmnt.getChildNodes().item(0)!=null){
				      String nodeGetValue=fstElmnt.getChildNodes().item(0).getNodeValue();
				      if(nodeGetValue!=null&&nodeGetValue.length()>0){
				    	  config.addItem( childName, nodeGetValue);
				      }
		    	  }
		      }
	    	  if(mode==listWalkerMode.setNodeValue){
	    		  if(childName.equals(nodeName)){
	    			  // TODO Does this really work?
	    			  fstElmnt.getChildNodes().item(0).setNodeValue(nodeValue);
				      String nodeGetValue=fstElmnt.getChildNodes().item(0).getNodeValue();
				      if(nodeGetValue!=null&&nodeGetValue.length()>0){
				    	  config.addItem( childName, nodeGetValue);
				    	  return true;
				      }
	    		  }
	    	  }
	      
	    	  returnValue = listWalker(config,childName,fstElmnt.getChildNodes(),mode, nodeName,nodeValue);
		    }
		  }
		  return returnValue;
	}
	public String getXMLFileName(){
		return XMLFileName;
	}
	public void parseXMLString(String xmlString) throws Exception {
		XMLFileName=null;
		 _parseXML(xmlString,xmlParseMode.parseXMLString);
	}
	public void parseXMLFile(String fileName) throws Exception {
		XMLFileName = fileName;
		_parseXML(fileName,xmlParseMode.parseXMLFile);
	}

	private void _parseXML(String XMLData, xmlParseMode mode) throws Exception{
		
		config = new Config();
		
		//config.recipeFileName = fileName;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			  DebugConsole.throwMessage("Exception in Config.ParseXML:ParserConfigurationException: "+e1.getMessage());
		}
		try {
			if(mode==xmlParseMode.parseXMLFile){
				File file = new File(XMLData);
				doc = db.parse(file);
			}
			if(mode==xmlParseMode.parseXMLString){
				doc =db.parse(new InputSource(new java.io.StringReader(XMLData)));
			}
		} catch (SAXException e1) {
			  DebugConsole.throwMessage("Exception in Config.ParseXML:SAXException: "+e1.getMessage());
		} catch (IOException e1) {
			  DebugConsole.throwMessage("Exception in Config.ParseXML:IOException: "+e1.getMessage());
		}
    doc.getDocumentElement().normalize();
		listWalker(config , null,doc.getChildNodes(),listWalkerMode.getNodeValues,null,null);
		return;
	}
	
	// Returns everything in this node as text
	// Ugly function because it assumes that this node is distinct and unique the file
	public String getNodeSectionAsXMLString(String fileName,String node){
		  // Read the XML File
		String XMLFile = "";
	    try {
	        BufferedReader in = new BufferedReader(new FileReader(fileName));
	        String str;
	        while ((str = in.readLine()) != null) {
	        	XMLFile+=str;
	        	XMLFile+="\n";
	        }
	        in.close();
	    } catch (IOException e) {
	    	DebugConsole.errprintln("IOException: " + e.getMessage());
	    	return null;
	    }
		// Search for the node
	    int start= XMLFile.indexOf("<"+node+">");
	    if(start==-1){
	    	DebugConsole.errprintln("Error: The start node <"+node+"> is not found.");
	    	return null;
	    }
	    int end= XMLFile.indexOf("</"+node+">");
	    if(end==-1){
	    	DebugConsole.errprintln("Error: The node end </"+node+"> is not found.");
	    	return null;
	    }
	    return ("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+XMLFile.substring(start,end+node.length()+3));
	}
	
	public static String getStringFromDocument(Document doc)
	{
	    try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    }
	} 
}
