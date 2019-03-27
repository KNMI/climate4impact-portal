package nl.knmi.adaguc.tools;



import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


@SuppressWarnings("deprecation")
public class MyXMLParser {
	public static PrintStream Debug=System.err;

	public enum Options {NONE,STRIPNAMESPACES};
	/**
	 * XML attribute 
	 * @author plieger
	 *
	 */
	static public class XMLAttribute {
		private String value = null;
		private String name = null;
	}
	/**
	 * XML element, the base to start parsing XML documents.
	 * @author plieger
	 *
	 */
	static public class XMLElement {
		private Vector<XMLAttribute> attributes = null;
		private Vector<XMLElement> xmlElements = null;
		private String value = null;
		private String name = null;
		public XMLElement(){
			attributes = new Vector<XMLAttribute>();
			xmlElements = new Vector<XMLElement>();
		}
		public XMLElement(String name) {
			this();
			this.name=name;
		}

		public String getValue(){
			return value;
		}

		public Vector <XMLElement> getElements(){
			return xmlElements;
		}

		public Vector <XMLAttribute> getAttributes(){
			return attributes;
		}

		public String getName(){
			return name;
		}

		public XMLElement get(String s,int index) throws Exception{
			int NR=0;
			for(int j=0;j<xmlElements.size();j++){
				if(xmlElements.get(j).name.equals(s)){
					if(NR==index)return xmlElements.get(j);
					NR++;
				}
			}
			if(NR!=0){
				throw new Exception("XML element \""+s+"\" with index \""+index+"\" out of bounds ("+NR+" available)");
			}
			throw new Exception("XML element \""+s+"\" with index \""+index+"\" not found");
		}
		public XMLElement get(String s) throws Exception{
			return get(s,0);
		}
		public String getAttrValue(String name) throws Exception{
			for(int j=0;j<attributes.size();j++){
				if(attributes.get(j).name.equals(name))return attributes.get(j).value;
			}
			throw new Exception("XML Attribute \""+name+"\" not found in element "+this.name);
		}

		public void add(XMLElement el) {
			this.xmlElements.add(el);
		}

		public void setAttr(String attr, String value) {
			XMLAttribute at=new XMLAttribute();
			at.name=attr;
			at.value=value;
			this.attributes.add(at);
		}

		public void setValue(String value) {
			this.value=value;
		}

		/**
		 * Parses document
		 * @param document
		 */
		private void parse(Document document){
			name="root";
			value="root";
			NodeList nodeLst=	document.getChildNodes();
			parse(nodeLst);
		}

		/** 
		 * Parses a XML file on disk
		 * @param file XML file on disk
		 * @throws Exception
		 */
		public void parseFile(String file) throws Exception {
			//Debug.println("Loading "+file);
			try{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream inputStream = new FileInputStream(file);
				Document document = db.parse(inputStream);
				parse(document);
				inputStream.close();
			} catch (SAXException e) {
				String msg="SAXException: "+e.getMessage();
				Debug./*err*/println(msg);
				throw new SAXException(msg);
			} catch (IOException e) {
				String msg="IOException:: "+e.getMessage();
				//Debug./*err*/println(msg);
				throw new IOException(msg);
			}catch(Exception e){
				String msg="Exception: "+e.getMessage();
				Debug./*err*/println(msg);
				throw new Exception(msg);
			}

		}

		/** 
		 * Parses XML string 
		 * @param string The XML formatted string
		 * @throws Exception
		 */
		public void parseString(String string) throws Exception {
			try{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();

				InputStream inputStream = new ByteArrayInputStream(string.getBytes());

				Document document = db.parse(inputStream);
				parse(document);
				inputStream.close();
			} catch (SAXException e) {
				String msg="SAXException: "+e.getMessage()+":\n\"";//+string+"\"";
				Debug./*err*/println(msg);
				throw new SAXException(msg);
			} catch (IOException e) {
				String msg="IOException: "+e.getMessage();
				Debug./*err*/println(msg);
				throw new IOException(msg);
			}catch(Exception e){
				String msg="Exception: "+e.getMessage();
				Debug./*err*/println(msg);
				throw new Exception(msg);
			}

		}

		/**
		 * Parses remote XML file via URL 
		 * @param url The URL to load
		 * @throws Exception
		 */
		public void parse(URL url) throws WebRequestBadStatusException,Exception{
			//DebugConsole.println("Loading "+url);
			this.xmlElements.clear();
			HttpURLConnection connection = null;
			try{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
				Debug.println("  Making XML GET: "+url.toString());
				/*if(Configuration.GlobalConfig.isInOfflineMode()==true){
		      if(url.getHost().equals("localhost")==false){
		        DebugConsole.println("Offline mode");
		        throw new Exception("Offline mode.");
		      }
		    }	*/
				connection = (HttpURLConnection) url.openConnection();
				InputStream inputStream = connection.getInputStream();



				Document document = db.parse(inputStream);
				parse(document);
				inputStream.close();
				long stopTimeInMillis = Calendar.getInstance().getTimeInMillis();
				Debug.println("Finished XML GET: "+url.toString()+" ("+(stopTimeInMillis-startTimeInMillis)+" ms)");
			} catch (IOException e) {
				String msg="IOException: "+e.getMessage()+" for URL "+url.toString();;
				int statusCode = connection.getResponseCode();
				if(statusCode>300){
					throw new WebRequestBadStatusException(statusCode);
				}
				Debug./*err*/println("Status code: "+ connection.getResponseCode());
				Debug./*err*/println(msg);
				throw new IOException(msg);
			}catch (SAXException e) {
				//      Debug.printStackTrace(e);
				e.printStackTrace(Debug);
				String msg="SAXException: "+e.getMessage()+" for URL "+url.toString();
				Debug./*err*/println(msg);
				throw new SAXException(msg);
			} catch(Exception e){
				String msg="Exception: "+e.getMessage()+" for URL "+url.toString();;
				Debug./*err*/println(msg);
				throw new Exception(msg);
			}
		}

		/**
		 * Function which does a POST request
		 * @param url The URL
		 * @param data The data to post
		 * @throws Exception
		 */
		public void parse(URL url,String data) throws Exception{
			//DebugConsole.println("Loading "+url+" with data \n"+data);
			try{
				// Send data
				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(data);
				wr.flush();

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();

				Document document = db.parse(conn.getInputStream());
				parse(document);
				wr.close();

			} catch (SAXException e) {
				String msg="SAXException: "+e.getMessage();
				Debug./*err*/println(msg);
				throw new SAXException(msg);
			} catch (IOException e) {
				String msg="IOException: "+e.getMessage();
				Debug./*err*/println(msg);
				throw new IOException(msg);
			}catch(Exception e){
				String msg="Exception: "+e.getMessage();
				Debug./*err*/println(msg);
				throw new Exception(msg);
			}
			Debug.println("Ready");
		}


		private void _parseJSON(JSONObject jsonObject, Vector<XMLElement> xmlElements, XMLElement child) throws Exception {
			JSONArray keys = jsonObject.names ();
			if (keys == null) return;
			for (int i = 0; i < keys.length (); ++i) {
				String key = keys.getString(i);
				Object obj = jsonObject.get(key);
				if (obj instanceof JSONObject) {
					JSONObject subJsonObject = (JSONObject)(obj);
					if (key.equals("attr")) {
						JSONArray attrKeys = subJsonObject.names ();
						for (int a = 0; a < attrKeys.length (); ++a) {
							String attrKey = attrKeys.getString(a);
							String attrValue = subJsonObject.getString(attrKey);
							XMLAttribute attr=new XMLAttribute();
							attr.name=attrKey;
							attr.value=attrValue;
							child.attributes.add(attr);
						}
					} else {
						XMLElement newChild = new XMLElement();
						xmlElements.add(newChild);
						newChild.name = key;
						_parseJSON(subJsonObject, newChild.xmlElements, newChild);
					}
				} else if (obj instanceof JSONArray) {
					JSONArray array = (JSONArray) obj;
					for(int j=0;j<array.length();j++){
						XMLElement newChild = new XMLElement();
						xmlElements.add(newChild);
						newChild.name = key;
						_parseJSON(array.getJSONObject(j), newChild.xmlElements, newChild);
					}
				}else {
					if (!obj.getClass().getName().equals("java.lang.String")) {
						nl.knmi.adaguc.tools.Debug.errprintln("JSONObject is not a string: "+ key + "[" + obj.getClass().getName() + "]");
						throw new Exception("JSONObject is not a string");
					}
					if (obj.getClass().getName().equals("java.lang.String")) {
						if (key.equals("value")) {
							child.value = obj.toString();
						}
					}
					if (obj.getClass().getName().equals("java.lang.JSONArray")) {
						if (key.equals("value")) {
							child.value = obj.toString();
						}
					}
				}
			}
		}
		public void parse (JSONObject jsonObject) throws Exception {
			if (jsonObject == null) {
				throw new Exception ("Unable to parse empty jsonobject (is null)");
			}
			_parseJSON(jsonObject, xmlElements, null);
		}

		/** 
		 * Parses to our XMLElement XMLAttribute tree
		 * @param nodeLst
		 */
		private void parse(NodeList nodeLst){
			for (int s = 0; s < nodeLst.getLength(); s++){
				Node fstNode = nodeLst.item(s);
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
					Element fstElmnt = (Element) fstNode;
					XMLElement child = new XMLElement();
					xmlElements.add(child);
					child.name=fstNode.getNodeName();
					if(fstNode.hasAttributes()){
						for(int a=0;a<fstNode.getAttributes().getLength();a++){
							XMLAttribute attr=new XMLAttribute();
							attr.name=fstNode.getAttributes().item(a).getNodeName();
							attr.value=fstNode.getAttributes().item(a).getNodeValue();
							child.attributes.add(attr);
						}
					}

					if(fstElmnt.getChildNodes().item(0)!=null){
						String nodeGetValue=fstElmnt.getChildNodes().item(0).getNodeValue();
						if(nodeGetValue!=null&&nodeGetValue.length()>0){

							child.value=nodeGetValue.trim();
						}
					}
					NodeList childNodes = fstNode.getChildNodes();
					if(childNodes.getLength()>0){
						child.parse(childNodes);
					}
				}
			}
		}

		public Vector<XMLElement> getList(String s) {
			Vector<XMLElement> v = new Vector<XMLElement>();
			for(int j=0;j<xmlElements.size();j++){
				if(xmlElements.get(j).name.equals(s)){
					v.add(xmlElements.get(j));
				}
			}
			return v;
		}
		public XMLElement getFirst() {
			return xmlElements.get(0);
		}

		/**
		 * Convert XML element to a string, can be any element from the tree. The XML header is not given.
		 * @param el The element to convert to a string
		 * @param depth Depth (can be zero).
		 * @return String of the XML element
		 */
		public String toXML(XMLElement el,int depth){
			String data = "";
			if(el==null)return data;
			for(int i=0;i<depth;i++)data+="  ";
			data+="<"+el.name;
			for(int j=0;j<el.getAttributes().size();j++){
				data+=" "+el.getAttributes().get(j).name+"=\""+StringEscapeUtils.escapeXml(el.getAttributes().get(j).value)+"\"";
			}
			data+=">\n";
			for(int j=0;j<el.xmlElements.size();j++){
				data+=toXML(el.xmlElements.get(j),depth+1);
			}
			if(el.getValue()!=null){
				if(el.getValue().length()>0){
					for(int i=0;i<depth;i++)data+="  ";
					data+="  "+el.getValue()+"\n";
				}
			}
			for(int i=0;i<depth;i++)data+="  ";
			data+= "</"+el.name+">\n";
			return data;

		}

		/**
		 * Carriage returns in a JSON object are not allowed and should be replaced with the "\n" sequence
		 * @param in the unencoded JSON string
		 * @return the encoded JSON string
		 */
		private String jsonEncode(String in){
			//Debug.println(in);

			in = in.replaceAll("\r\n", ":carriagereturn:");
			in = in.replaceAll("\n", ":carriagereturn:");
			in = in.replaceAll("\\\\", "");
			in = in.replaceAll("\"", "\\\\\"");
			in = in.replaceAll(":carriagereturn:","\\\\n");
			//      in = in.replaceAll("\\\\ ", " ");
			//      in = in.replaceAll("\\\\\\[", "[");
			//      in = in.replaceAll("\\\\\\]", "]");
			//      in = in.replaceAll("\\\\\\_", "_");
			//      in = in.replaceAll("\\\\\\:", ":");
			//      in = in.replaceAll("\\\\\\-", "-");
			//      in = in.replaceAll("\\\\\\+", "+");
			//      in = in.replaceAll("\\\\\\.", ".");
			//      in = in.replaceAll("\\\\\\,", ",");
			//      in = in.replaceAll("\\\\\\'", "'");


			// Debug.println(in);
			return in;
		}

		/**
		 * Returns a String with the attributes of this XML element encoded to JSON
		 * @param el The XMLElement with attributes to convert to JSON formatted attributes
		 * @return String with JSON formatted data
		 */
		private String printJSONAttributes(XMLElement el,Options options){
			String data="";
			if(el.getAttributes().size()>0){
				data+="\"attr\":{";
				for(int j=0;j<el.getAttributes().size();j++){
					if(j>0)data+=",";
					String name = el.getAttributes().get(j).name;
					if(options == Options.STRIPNAMESPACES){
						name = name.substring(name.indexOf(":")+1);
					}
					name  =jsonEncode(name);
					data+="\""+name+"\":\""+jsonEncode(el.getAttributes().get(j).value)+"\"";
				}
				data+="}";
			}
			return data;
		}

		/**
		 * Print the XMLElement's value in a JSON formatted way
		 * @param el The XMLElement value to be printed in a JSON formatted way
		 * @return String with JSON formatted data
		 */
		private String printJSONValue(XMLElement el){
			if(el.getValue()!=null){
				if(el.getValue().length()>0){
					return "\"value\":\""+jsonEncode(el.getValue())+"\"";
				}
			}
			return "";
		}

		/**
		 * Converts a list of XML elements all with the same name to a JSON string.
		 * @param vector The list of XML elements with the same name
		 * @param depth The depth of the XML elements
		 * @return JSON formatted string
		 */
		private String xmlElementstoJSON(Vector<XMLElement> vector,int depth,Options options){
			String data = "";
			String name = vector.get(0).name;

			if(options == Options.STRIPNAMESPACES){
				name = name.substring(name.indexOf(":")+1);
			}
			name  =jsonEncode(name);
			//DebugConsole.println(name);
			data+="\""+name+"\":";
			boolean isArray=false;
			if(vector.size()>1)isArray=true;

			if(isArray){
				data+="[\n";
			}
			for(int j=0;j<vector.size();j++){
				if(j>0){
					data+=",\n";
				}
				data+="{";
				data+=toJSON(vector.get(j),depth+1,options);
				data+="}";
			}
			if(isArray){
				data+="]\n";
			}    
			data+="";
			return data;    
		}

		/**
		 * Converts a XML element to JSON string and walks through all nested XML elements
		 * @param el The XML element to convert to a json string
		 * @param depth The current depth of the XML element
		 * @return JSON string
		 */
		private String toJSON(XMLElement el,int depth,Options options){
			String data = "";
			if(el==null)return data;

			boolean firstDataDone=false;

			//Print the json attributes
			data+=printJSONAttributes(el,options);
			if(el.attributes.size()>0)firstDataDone=true;

			//Make a Set of the XML elements names
			Set<String> set = new HashSet<String>();
			for(int j=0;j<el.xmlElements.size();j++){
				String name = el.xmlElements.get(j).getName();
				set.add(name);
			}

			//Loop through the XML elements with unique names
			for (String temp : set){
				if(firstDataDone){data+=",\n";}firstDataDone=true;
				data+=xmlElementstoJSON(el.getList(temp),depth+1,options);
			}
			//Clear and remove the set
			set.clear();set=null;

			//Print the JSON value
			String jsonValue=printJSONValue(el);
			if(jsonValue.length()>0){
				if(firstDataDone){data+=",\n";}firstDataDone=true;
				data+=jsonValue;
			}



			return data;
		}

		/**
		 * Returns The XML object as a well formatted XML string.
		 */
		public String toString(){
			String data ="";
			data="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
			for(int j=0;j<xmlElements.size();j++){
				data+=toXML(xmlElements.get(j),0);
			}
			return data;
		}

		/**
		 * Returns The XML object as a JSON string XML string.
		 * Values are denoted as 'value' and attributes with 'attr'.
		 */
		public String toJSON(Options options){
			String data ="{\n";
			data+=xmlElementstoJSON(xmlElements,0,options);
			data+="\n}\n";
			return data;

		}


		/**
		 * Converts the XML document to JSON
		 * @return JSONObject representing the XML
		 * @throws Exception 
		 */
		public JSONObject toJSONObject(Options options) throws Exception{
			//DebugConsole.println("Constructing JSON");
			String jsonString = null;
			try{
				jsonString = toJSON(options);
			}catch(Exception e){
				e.printStackTrace();
				throw new Exception("Unable to convert XML to JSON: "+e.getMessage());
			}
			//DebugConsole.println("JSON constructed:"+jsonString);
			JSONObject jsonObject=new JSONObject();
			try {
				jsonObject = (JSONObject) new JSONTokener(jsonString).nextValue();
			} catch (JSONException e) {
				Debug./*err*/println("Unable to tokenize JSON string to JSONObject \n"+jsonString);
				return null;
			}
			return jsonObject;
		}
		public String getNodeValue(String string) {

			String [] values = getNodeValues(string);
			if(values==null)return null;
			return values[0];
		}
		public String[] getNodeValues(String string) {
			String[] elements = string.split("\\.");
			int j=0;
			XMLElement a = this;
			try{
				while(j<elements.length-1){
					a = a.get(elements[j]);
					j++;
				};
				Vector<XMLElement> b = a.getList(elements[j]);
				if(b.size()>0){
					String[] results = new String[b.size()];
					for(int i=0;i<b.size();i++){
						String value = b.get(i).getValue();

						results[i] = value;
					}
					return results;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		public String getNodeValueMustNotBeUndefined(String string) throws ElementNotFoundException {
			String nodeValue = getNodeValue(string);
			if(nodeValue == null){
				throw new ElementNotFoundException(string);
			}
			return nodeValue;
		}

	};
}
