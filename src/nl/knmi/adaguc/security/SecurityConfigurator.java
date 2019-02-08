package nl.knmi.adaguc.security;

import java.util.Vector;

import impactservice.Configuration;
import lombok.Synchronized;
import nl.knmi.adaguc.tools.Debug;
import nl.knmi.adaguc.tools.ElementNotFoundException;
import nl.knmi.adaguc.tools.MyXMLParser.XMLElement;

/**
 * 
 * @author maartenplieger
 * 
<?xml version="1.0" encoding="UTF-8"?>
<adaguc-services>
  <security>
    <truststorepassword>changeit</truststorepassword>
    <truststore>/home/c3smagic/config/esg-truststore.ts</truststore>
    <trustrootscadirectory>/home/c3smagic/.globus/certificates/</trustrootscadirectory>
  </security>
</security-services>
 */


public class SecurityConfigurator {

	//	private static boolean configDone = false;
	//	
	//	@Override 
	//	public void setConfigDone() {
	//		configDone =true;
	//	}

	private static String trustStorePassword=null;
	private static String trustStore=null;
	private static String trustRootsCADirectory=null;
	private static String keyStore=null;
	private static String keyStorePassword=null;
	private static String keyStoreType="JKS";
	private static String keyAlias="tomcat";
	private static String userHeader=null;	
	private static String caCertificate = null;
	private static String caPrivateKey = null;		

	public static class ComputeNode {
		public String url = null;
		public String name = null;
	};

	static Vector<ComputeNode> computeNodes = new Vector<ComputeNode>();

//	static ConfigurationReader configurationReader = new ConfigurationReader ();
//
//	@Synchronized
//	@Override
//	public void doConfig(XMLElement  configReader){
//		
//		if(configReader.getNodeValue ("adaguc-services.security")==null){
//			Debug.println("adaguc-services.security is not configured");
//			return;
//		}
//		trustStorePassword=configReader.getNodeValue("adaguc-services.security.truststorepassword");
//		trustStore=configReader.getNodeValue("adaguc-services.security.truststore");
//		trustRootsCADirectory=configReader.getNodeValue("adaguc-services.security.trustrootscadirectory");
//		keyStore=configReader.getNodeValue("adaguc-services.security.keystore");
//		keyStorePassword=configReader.getNodeValue("adaguc-services.security.keystorepassword");
//		keyStoreType=configReader.getNodeValue("adaguc-services.security.keystoretype");
//		computeNodes.clear();
//		keyAlias=configReader.getNodeValue("adaguc-services.security.keyalias");
//		userHeader=configReader.getNodeValue("adaguc-services.security.userheader");
//		
//		if (configReader.getNodeValue("adaguc-services.security.tokenapi")!=null){
//			caCertificate=configReader.getNodeValue("adaguc-services.security.tokenapi.cacertificate");
//			caPrivateKey=configReader.getNodeValue("adaguc-services.security.tokenapi.caprivatekey");
//			if (configReader.getNodeValue("adaguc-services.security.tokenapi.remote-instances")!=null){
//				try {
//					Vector<XMLElement> computeNodeElements = configReader.get("adaguc-services").get("security").get("tokenapi").get("remote-instances").getList("adaguc-service");
//					for(int j=0;j<computeNodeElements.size();j++){
//						XMLElement computeNodeElement = computeNodeElements.get(j);
//
//						try {
//							ComputeNode computeNode = new ComputeNode();
//							computeNode.url = computeNodeElement.getValue();
//							computeNode.name = computeNodeElement.getAttrValue("name");
////							Debug.println("Added remote instance " + computeNode.url + " with name " + computeNode.name);
//							computeNodes.add(computeNode);
//						} catch (Exception e) {
//							Debug.printStackTrace(e);
//						}
//					}
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			} else {
//				Debug.println("No remote instances configured");
//			}
//
//		} else {
//			Debug.println("tokenapi is not enabled");
//		}
//
//	}

	public static Vector<ComputeNode> getComputeNodes() throws ElementNotFoundException {
	  Vector<ComputeNode> computeNode = new Vector<ComputeNode>();
	  String[] b = Configuration.WPSServicesConfig.getWPSServices();
	  for(int j=0;j<b.length;j++){
	    ComputeNode e = new ComputeNode();
	    e.url = b[j];
	    e.name= b[j];
	    computeNode.add(e);
	  }
		return computeNode;
	}
	public static String getCACertificate() throws ElementNotFoundException {
	  Debug.println("TODO getCACertificate");
	  return null;
	}
	
	public static String getCAPrivateKey() throws ElementNotFoundException {
		Debug.println("TODO getCAPrivateKey");
		return null;
	}
	public static String getTrustStorePassword() throws ElementNotFoundException {
	  return Configuration.LoginConfig.getTrustStorePassword();
	}
	public static String getTrustStore() throws ElementNotFoundException {
	  return Configuration.LoginConfig.getTrustStoreFile();
	}
	public static String getTrustRootsCADirectory() throws ElementNotFoundException {
	  return Configuration.LoginConfig.getTrustRootsLocation();
	}

	public static Object getKeyStore() throws ElementNotFoundException {
	  Debug.println("TODO getKeyStore");
    return null;
	}

	public static Object getKeyStorePassword() throws ElementNotFoundException {
    Debug.println("TODO getKeyStorePassword");
    return null;
	}

	public static Object getKeyStoreType() throws ElementNotFoundException {
	  Debug.println("TODO getKeyStoreType");
    return null;
	}

	public static Object getKeyAlias() throws ElementNotFoundException {
    Debug.println("TODO getKeyAlias");
    return null;
	}

	public static String getUserHeader() throws ElementNotFoundException {
    Debug.println("TODO getUserHeader");
    return null;
	}
}


