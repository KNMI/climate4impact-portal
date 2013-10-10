package impactservice;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;


import org.json.JSONObject;
import org.json.JSONTokener;


import tools.DebugConsole;

import tools.MyXMLParser.XMLElement;
import tools.Tools;
import wps.ProcessorRegister;




public class GenericCart {
  private User user = null;
  private  String genericId = "";
  
  public  GenericCart(String id,User user){
    DebugConsole.println("Creating new GenericCart with id "+id+" for user "+user);
    this.genericId=id;
    this.user=user;
    
  }
	
	private class DataLocator{
		DataLocator(String id,String cartData){
			this.id =id;
			this.cartData=cartData;
			String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		  SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		  Calendar cal = Calendar.getInstance();
		  String currentISOTimeString = sdf.format(cal.getTime())+"Z";
		  this.addDateMilli=cal.getTimeInMillis();
			this.addDate=currentISOTimeString;
		}
		DataLocator(String id,String cartData,String addDate,long addDateMilli){
      this.id =id;
      this.cartData=cartData;
      this.addDateMilli=addDateMilli;
      this.addDate=addDate;
    }
		String id = null;
		String cartData = null;
		long addDateMilli = 0;
	  String addDate = null;
	}
	
	private Vector<DataLocator> dataLocatorList = new Vector<DataLocator>();
	
	/**
	 * Adds a new dataset to the generic cart, based on its ID and URL. 
	 * Only unique ID's are added to list, the same ID cannot be added twice.
	 * 
	 * @param id The dataset id, e.g. cmip5.output1.BCC.bcc-csm1-1.1pctCO2.mon.atmos.Amon.r1i1p1.psl.1.aggregation
	 * @param dataLocation e.g. http://etc.nc
	 */
  	public synchronized void addDataLocator(String id, String dataLocation) {
  	  DebugConsole.println("Adding "+id);
  	  dataLocation=dataLocation.replace("http://cmip-dn.badc.rl.ac.uk", "http://cmip-dn1.badc.rl.ac.uk");
  		DataLocator d = new DataLocator(id,dataLocation);
  		for(int j=0;j<dataLocatorList.size();j++){
  		  
  		  if(dataLocatorList.get(j).id.equals(id)){
          DebugConsole.println("Already added "+id);
          return;
        }
  		}
  		dataLocatorList.add(d);
  		saveToStore();
  	}

  	public synchronized void addDataLocator(String id, String dataLocation,String addDate,long addDateMillis,boolean saveToStore) {
     // DebugConsole.println("Adding "+id+" with date "+addDate);
      dataLocation=dataLocation.replace("http://cmip-dn.badc.rl.ac.uk", "http://cmip-dn1.badc.rl.ac.uk");
      DataLocator d = new DataLocator(id,dataLocation,addDate,addDateMillis);
      for(int j=0;j<dataLocatorList.size();j++){
        
        if(dataLocatorList.get(j).id.equals(id)){
          DebugConsole.println("Already added "+id);
          return;
        }
      }
      dataLocatorList.add(d);
      if(saveToStore)saveToStore();
    }

	 public synchronized void removeDataLocator(String id) {
	    DebugConsole.println("Removing "+id);
	    Iterator<DataLocator> itr = dataLocatorList.iterator();
	    while(itr.hasNext()) {
	      DataLocator element = itr.next(); 
	      if(element.id.equals(id)){
	        itr.remove();
	      }
	    }
	    saveToStore();
	  }

	public int getNumProducts(){
		return dataLocatorList.size();
	}
	

  public void loadFromStore() throws Exception {
    DebugConsole.println("Loading from store");
    String file=user.usersDir+"/"+genericId+".xml";
    XMLElement catalogElement = new XMLElement();
    try {
      catalogElement.parseFile(file);
    } catch (Exception e) {
      DebugConsole.errprintln(e.getMessage());
      return;
    }
   
    XMLElement sc=catalogElement.get("GenericCart");
    Vector<XMLElement>elements=sc.getList("element");
    for(int j=0;j<elements.size();j++){
      addDataLocator(elements.get(j).getAttrValue("id"),URLDecoder.decode(elements.get(j).getAttrValue("data"),"UTF-8"),elements.get(j).getAttrValue("adddate"),Long.parseLong(elements.get(j).getAttrValue("adddatemillis")),false);
      //DebugConsole.println("j: "+j+" "+Long.parseLong(elements.get(j).getAttrValue("adddatemillis")));
    }


  }
  public synchronized void saveToStore(){
    DebugConsole.println("Saving to store");
    String file=user.usersDir+"/"+genericId+".xml";
    String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
    data+="<GenericCart>\n";
    Iterator<DataLocator> itr = dataLocatorList.iterator();
    while(itr.hasNext()) {
      DataLocator element = itr.next(); 
      try {
        data+="  <element id=\""+element.id+"\" data=\""+URLEncoder.encode(element.cartData,"UTF-8")+"\" adddate=\""+element.addDate+"\" adddatemillis=\""+element.addDateMilli+"\"/>\n";
      } catch (UnsupportedEncodingException e) {
         DebugConsole.errprintln("Unable to add XML element "+element.id+": "+e.getMessage());
      }
    }
    data+="</GenericCart>\n";
    try {
      Tools.writeFile(file,data);
    } catch (IOException e) {
      DebugConsole.errprintln(e.getMessage());
    }
  }

 
  public static class CartPrinters{
    /**
     * Generates a html snippet with contents of the job list
     * @param genericCart
     * @param request
     * @return
     */
    public static String showJobList(GenericCart genericCart,HttpServletRequest request){
      DebugConsole.println("Show joblist");
      String htmlResp = "Jobs for: <strong>"+User.getUserId(request)+"</strong><br/>";
      htmlResp += "<table class=\"basket\">";
      Iterator<DataLocator> itr = genericCart.dataLocatorList.iterator();
      int j=1;

      htmlResp+="<tr>";
      
      htmlResp+="<td style=\"width:150px;background-color:#DDD;\"><b>Started on:</b></td>";
      htmlResp+="<td style=\"width:360px;background-color:#DDD;\"><b>WPS Identifier</b></td>";
      htmlResp+="<td style=\"width:360px;background-color:#DDD;\"><b>Unique Id</b></td>";
      htmlResp+="<td style=\"width:30px;background-color:#DDD;\"><b>Progress</b></td>";

      
        htmlResp+="<td style=\"background-color:#DDD;\"><b>View</b></td>";
      
      
      htmlResp+="<td style=\"background-color:#DDD;\"><b>X</b></td>";
      htmlResp+="</tr>";

      while(itr.hasNext()) {
        DataLocator element = itr.next(); 
        htmlResp+="<tr>";
        
        htmlResp+="<td>"+element.addDate+"</td>";
      
        
        try {
          //DebugConsole.println(element.cartData);
          JSONObject elementProps =  (JSONObject) new JSONTokener(element.cartData).nextValue();
          String statusLocation = elementProps.getString("wpsurl");
          String id= elementProps.getString("id");
          htmlResp+="<td>"+id+"</td>";
          htmlResp+="<td>"+element.id+"</td>";
          
          
          JSONObject progressObject = (JSONObject) ProcessorRegister.monitorProcess(element.id,statusLocation);
          String progress= progressObject.getString("progress");
          
        
          
          
          
          if(progress.equals("100")){
            htmlResp+="<td>ready</td>";
          }else{
            htmlResp+="<td>"+progress+" % </td>";
          }
          htmlResp+="<td><a onclick='processProgressMonitoring("+elementProps.toString()+");'>view</a></td>";
        } catch (Exception e) {
          DebugConsole.println(e.getMessage());
          htmlResp+="<td>failed</td>";
          htmlResp+="<td>-</td>";
          
          
        }
        
     
        htmlResp+="<td><a href=\"#\" onclick='removeId(\""+element.id+"\");return false;'>X</a></td>";
        htmlResp+="</tr>";
        j++;
      } 
      
      if(j==0){
        htmlResp+="<tr>";
        htmlResp+="<td>No items added to the basket (empty basket)</td>";
        htmlResp+="</tr>";
      }
      
      htmlResp+="</table>";
      return htmlResp;
    }
    
    
    
    /**
     * Generates a html snippet with contents of the generic basket
     * @param genericCart
     * @param request
     * @return
     */
    public static String showDataSetList(GenericCart genericCart,HttpServletRequest request){
      DebugConsole.println("Show datasetlist");
      String htmlResp = "Basket for: <strong>"+User.getUserId(request)+"</strong><br/>";
      htmlResp += "<table class=\"basket\">";
      Iterator<DataLocator> itr = genericCart.dataLocatorList.iterator();
      int j=1;

      htmlResp+="<tr>";
      
      htmlResp+="<td style=\"width:150px;background-color:#DDD;\"><b>Added on:</b></td>";
      htmlResp+="<td style=\"width:600px;background-color:#DDD;\"><b>Identifier</b></td>";

      
        htmlResp+="<td style=\"background-color:#DDD;\"><b>View</b></td>";
        htmlResp+="<td style=\"background-color:#DDD;\"><b>Get</b></td>";
        htmlResp+="<td style=\"background-color:#DDD;\"><b>Subset</b></td>";
      
      htmlResp+="<td style=\"background-color:#DDD;\"><b>X</b></td>";
      htmlResp+="</tr>";

      while(itr.hasNext()) {
        DataLocator element = itr.next(); 
        htmlResp+="<tr>";
        
        htmlResp+="<td>"+element.addDate+"</td>";
        htmlResp+="<td>"+element.id+"</td>";

        if(element.cartData.equals("null")){
          htmlResp+="<td>-</td>";
          htmlResp+="<td>-</td>";
        }else{
          JSONObject elementProps = null;
          String dapURL = null;
          String httpURL = null;
          String catalogURL = null;
          try {
            elementProps =  (JSONObject) new JSONTokener(element.cartData).nextValue();
            try{dapURL =elementProps.getString("OPENDAP");}catch(Exception e){}
            try{httpURL =elementProps.getString("HTTPServer");}catch(Exception e){}
            try{catalogURL =elementProps.getString("catalogURL");}catch(Exception e){}
          } catch (Exception e) {
            DebugConsole.errprintln(e.getMessage()+" on \n"+element.cartData);
            catalogURL=element.cartData;
          }
          
          if("null".equals(catalogURL))catalogURL = null;
          if("null".equals(dapURL))dapURL = null;
          if("null".equals(httpURL))httpURL = null;
          
          DebugConsole.println(element.id+"|"+catalogURL+"|"+dapURL+"|"+httpURL);
          
          if(catalogURL==null){
            
            if(dapURL==null&&httpURL!=null){
              if(httpURL.indexOf("fileServer")>0){
                dapURL=httpURL.replace("fileServer", "dodsC");
              }
            }else{
              if(dapURL!=null&&httpURL==null){
                if(dapURL.indexOf("dodsC")>0){
                  if(dapURL.indexOf("aggregation")==-1){
                    httpURL=dapURL.replace("dodsC","fileServer");
                  }
                }
              } 
            }
           if(dapURL!=null){
            htmlResp+="<td><a href='"+Configuration.getHomeURL()+"/data/datasetviewer.jsp?dataset="+dapURL+"'>view</a></td>";
           }else{
             htmlResp+="<td>-</td>";
           }
           if(httpURL!=null){
             htmlResp+="<td><a target='_blank' href='"+httpURL+"'>get</a></td>";
           }else{
             htmlResp+="<td>-</td>";
           }
            
           if(dapURL!=null){
             //htmlResp+="<td><a href='"+dapURL+"'>subset</a></td>";
             htmlResp+="<td>subset</td>";
            }else{
              htmlResp+="<td>-</td>";
            }
          }else{
            htmlResp+="<td><a href='"+Configuration.getHomeURL()+"/data/datasetviewer.jsp?dataset="+catalogURL+"'>browse</a></td>";
            htmlResp+="<td></td>";
            htmlResp+="<td></td>";
          }
          
          
        }
        htmlResp+="<td><a href=\"#\" onclick='removeId(\""+element.id+"\");return false;'>X</a></td>";
        htmlResp+="</tr>";
        j++;
      } 
      
      if(j==0){
        htmlResp+="<tr>";
        htmlResp+="<td>No items added to the basket (empty basket)</td>";
        htmlResp+="</tr>";
      }
      
      htmlResp+="</table>";
      
      /*if(j>0){
        try {
          genericCart.saveToStore(User.getUser(request));
        } catch (Exception e) {
          DebugConsole.errprintln("Unable to save store to file: "+e.getMessage());
        }
      }*/
      return htmlResp; 
    }

  }
  
	
  
}
