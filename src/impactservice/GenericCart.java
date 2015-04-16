package impactservice;



import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import tools.Debug;
import tools.HTTPTools;
import tools.MyXMLParser.XMLElement;
import tools.Tools;
import wps.WebProcessingInterface;




public class GenericCart {
  private ImpactUser user = null;
  private  String genericId = "";

  public  GenericCart(String id,ImpactUser user){
    Debug.println("Creating new GenericCart with id "+id+" for user "+user.getId());
    this.genericId=id;
    this.user=user;

  }

  public class DataLocator{
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
    public String cartData = null;
    long addDateMilli = 0;
    String addDate = null;
  }

  public Vector<DataLocator> dataLocatorList = new Vector<DataLocator>();

  /**
   * Adds a new dataset to the generic cart, based on its ID and URL. 
   * Only unique ID's are added to list, the same ID cannot be added twice.
   * 
   * @param id The dataset id, e.g. cmip5.output1.BCC.bcc-csm1-1.1pctCO2.mon.atmos.Amon.r1i1p1.psl.1.aggregation
   * @param dataLocation e.g. http://etc.nc
   */
  public synchronized void addDataLocator(String id, String dataLocation) {
    Debug.println("Adding "+id);
    dataLocation=dataLocation.replace("http://cmip-dn.badc.rl.ac.uk", "http://cmip-dn1.badc.rl.ac.uk");
    DataLocator d = new DataLocator(id,dataLocation);
    for(int j=0;j<dataLocatorList.size();j++){

      if(dataLocatorList.get(j).id.equals(id)){
        Debug.println("Already added "+id);
        return;
      }
    }
    dataLocatorList.add(d);
    saveToStore();
  }

  public synchronized void addDataLocator(String id, String fileInfo,String addDate,long addDateMillis,boolean saveToStore) {
    // DebugConsole.println("Adding "+id+" with date "+addDate);
    //dataLocation=dataLocation.replace("http://cmip-dn.badc.rl.ac.uk", "http://cmip-dn1.badc.rl.ac.uk");
    DataLocator d = new DataLocator(id,fileInfo,addDate,addDateMillis);
    for(int j=0;j<dataLocatorList.size();j++){

      if(dataLocatorList.get(j).id.equals(id)){
        Debug.println("Already added "+id);
        return;
      }
    }
    dataLocatorList.add(d);
    if(saveToStore)saveToStore();
  }

  public synchronized void removeDataLocators(String[] id) {
    for(int j=0;j<id.length;j++){
      removeDataLocator(id[j]);
    }
  }
  public synchronized void removeDataLocator(String id) {
    Debug.println("Removing "+id);
    try {
      id = HTTPTools.validateInputTokens(id);
    } catch (Exception e1) {
      Debug.errprintln("Invalid tokens while removing "+id);
      return;
    }
    try {
      Debug.println("Checking "+user.getDataDir()+"/"+id);
      File file = new File(user.getDataDir()+"/"+id);
      if(file.exists()){
        if(file.isFile()){
          Debug.println("Deleting file "+file.getAbsolutePath());
          file.delete();
        }else{
          if(file.isDirectory()){
            Debug.println("Deleting directory "+file.getAbsolutePath());
            Tools.rmdir(file);
          }
        }
      }
    } catch (IOException e) {

    }
    Iterator<DataLocator> itr = dataLocatorList.iterator();
    while(itr.hasNext()) {
      DataLocator element = itr.next(); 
      if(element.id.equals(id)){
        itr.remove();
      }
    }
    saveToStore();
  }

  public int getNumProducts(HttpServletRequest request){
    Debug.println("GetNumProducts" );
    int numCustomFiles = 0;
    //	  ImpactUser impactUser;
    //    try {
    //      impactUser = LoginManager.getUser(request);
    //      String dataDir = impactUser.getDataDir();
    //      File dataDirFile = new File(dataDir);
    //      
    //     
    //      if(dataDirFile.exists()){
    //        numCustomFiles = dataDirFile.listFiles().length;
    //      }
    //    } catch (Exception e) {
    //
    //    }

    return dataLocatorList.size()+numCustomFiles;
  }


  public void loadFromStore() throws Exception {
    Debug.println("Loading from store");
    String file=user.getWorkspace()+"/"+genericId+".xml";
    XMLElement catalogElement = new XMLElement();
    try {
      catalogElement.parseFile(file);
    } catch (Exception e) {
      Debug.errprintln(e.getMessage());
      return;
    }

    XMLElement sc=catalogElement.get("GenericCart");

    if(dataLocatorList.size()>0){
      dataLocatorList.clear();
    }

    Vector<XMLElement>elements=sc.getList("element");
    for(int j=0;j<elements.size();j++){
      addDataLocator(elements.get(j).getAttrValue("id"),URLDecoder.decode(elements.get(j).getAttrValue("data"),"UTF-8"),elements.get(j).getAttrValue("adddate"),Long.parseLong(elements.get(j).getAttrValue("adddatemillis")),false);
      //DebugConsole.println("j: "+j+" "+Long.parseLong(elements.get(j).getAttrValue("adddatemillis")));
    }


  }
  public synchronized void saveToStore(){
    Debug.println("Saving to store");
    String file=user.getWorkspace()+"/"+genericId+".xml";
    String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
    data+="<GenericCart>\n";
    Iterator<DataLocator> itr = dataLocatorList.iterator();
    while(itr.hasNext()) {
      DataLocator element = itr.next(); 
      try {
        data+="  <element id=\""+element.id+"\" data=\""+URLEncoder.encode(element.cartData,"UTF-8")+"\" adddate=\""+element.addDate+"\" adddatemillis=\""+element.addDateMilli+"\"/>\n";
      } catch (UnsupportedEncodingException e) {
        Debug.errprintln("Unable to add XML element "+element.id+": "+e.getMessage());
      }
    }
    data+="</GenericCart>\n";
    try {
      Tools.writeFile(file,data);
    } catch (IOException e) {
      Debug.errprintln(e.getMessage());
    }
  }


  public static class CartPrinters{
    /**
     * Generates a html snippet with contents of the job list
     * @param genericCart
     * @param request
     * @return
     * @throws Exception 
     */
    public static String showJobList(GenericCart genericCart,HttpServletRequest request) throws Exception{
      Debug.println("Show joblist");
      String htmlResp = "Jobs for: <strong>"+LoginManager.getUser(request,null).getUserName()+"</strong><br/>\n";
      htmlResp += "<table class=\"basket\">\n";
      Iterator<DataLocator> itr = genericCart.dataLocatorList.iterator();
      int j=1;

      htmlResp+="<tr>\n";

      htmlResp+="<td style=\"width:150px;background-color:#DDD;\"><b>Started on:</b></td>\n";
      htmlResp+="<td style=\"width:360px;background-color:#DDD;\"><b>WPS Identifier</b></td>\n";
      htmlResp+="<td style=\"width:360px;background-color:#DDD;\"><b>Status location</b></td>\n";
      htmlResp+="<td style=\"width:30px;background-color:#DDD;\"><b>Progress</b></td>\n";


      htmlResp+="<td style=\"background-color:#DDD;\"><b>View</b></td>\n";


      htmlResp+="<td style=\"background-color:#DDD;\"><b>X</b></td>\n";
      htmlResp+="</tr>\n";

      while(itr.hasNext()) {
        DataLocator element = itr.next(); 
        htmlResp+="<tr>\n";

        htmlResp+="<td>"+element.addDate+"</td>\n";


        try {
          //DebugConsole.println(element.cartData);
          JSONObject elementProps =  (JSONObject) new JSONTokener(element.cartData).nextValue();
          String statusLocation = elementProps.getString("wpsurl");
          String id= elementProps.getString("id");
          htmlResp+="<td>"+id+"</td>\n";
          htmlResp+="<td><a href=\""+statusLocation+"\">"+element.id+"</a></td>\n";


          JSONObject progressObject = (JSONObject) WebProcessingInterface.monitorProcess(statusLocation,request);
          try{
            String progress= progressObject.getString("progress");
            if(progress.equals("100")){
              htmlResp+="<td>ready</td>\n";
              htmlResp+="<td><a onclick='showStatusReport("+elementProps.toString()+");'>view</a></td>\n";
            }else{
              htmlResp+="<td>"+progress+" % </td>\n";
              htmlResp+="<td><a onclick='processProgressMonitoring("+elementProps.toString()+");'>view</a></td>\n";
            }
          }catch(Exception e){
            htmlResp+="<td>failed</td>";
            htmlResp+="<td><a onclick='showStatusReport("+elementProps.toString()+");'>view</a></td>\n";
          }

        } catch (Exception e) {
          Debug.println(e.getMessage());
          htmlResp+="<td>crashed</td>\n";
          htmlResp+="<td>-</td>\n";


        }


        htmlResp+="<td><a href=\"#\" onclick='removeId(\""+element.id+"\");return false;'>X</a></td>\n";
        htmlResp+="</tr>\n";
        j++;
      } 

      if(j==0){
        htmlResp+="<tr>\n";
        htmlResp+="<td>No items added to the basket (empty basket)</td>\n";
        htmlResp+="</tr>\n";
      }

      htmlResp+="</table>\n";
      return htmlResp;
    }



    /**
     * Generates a html snippet with contents of the generic basket
     * @param genericCart
     * @param request
     * @return
     * @throws Exception 
     */
    /*  public static String showDataSetListOld(GenericCart genericCart,HttpServletRequest request){
      DebugConsole.println("Show datasetlist");
      String htmlResp = "Basket for: <strong>"+LoginManager.getUserId(request,null)+"</strong><br/>";
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

          //DebugConsole.println(element.id+"|"+catalogURL+"|"+dapURL+"|"+httpURL);

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


      return htmlResp; 
    }*/





    public static JSONObject showDataSetList(GenericCart genericCart,HttpServletRequest request) throws Exception{
      Debug.println("Show datasetlist");
      JSONObject datasetList = new JSONObject();

      JSONArray datasets = new JSONArray();
      datasetList.put("children", datasets);
      datasetList.put("text", genericCart.user.getOpenId());
      datasetList.put("leaf", false);
      datasetList.put("viewer", "/"+Configuration.getHomeURLPrefix()+"/data/datasetviewer.jsp?");
      datasetList.put("browser", "/"+Configuration.getHomeURLPrefix()+"/data/catalogbrowser.jsp?");

      JSONObject linkStorage = new JSONObject();

      linkStorage.put("text","Remote data");
      linkStorage.put("date","");
      linkStorage.put("leaf",false);
      linkStorage.put("expanded",true);
      linkStorage.put("iconCls", "typeFolder");

      JSONArray linkStorageChilds = new JSONArray();
      linkStorage.put("children",linkStorageChilds);
      datasets.put(linkStorage);

      Iterator<DataLocator> itr = genericCart.dataLocatorList.iterator();
      int j=1;

      while(itr.hasNext()) {
        DataLocator element = itr.next(); 

        if(element.cartData.equals("null")){
        }else{
          JSONObject dataset = new JSONObject();
          linkStorageChilds.put(dataset);
          JSONObject elementProps = null;
          String dapURL = null;
          String httpURL = null;
          String catalogURL = null;
          String fileSize = "-";
          try {
            elementProps =  (JSONObject) new JSONTokener(element.cartData).nextValue();
            try{dapURL =elementProps.getString("OPENDAP");}catch(Exception e){}
            try{httpURL =elementProps.getString("HTTPServer");}catch(Exception e){}
            try{catalogURL =elementProps.getString("catalogURL");}catch(Exception e){}
            try{fileSize =elementProps.getString("filesize");}catch(Exception e){}
          } catch (Exception e) {
            Debug.errprintln(e.getMessage()+" on \n"+element.cartData);
            catalogURL=element.cartData;
          }

          if("null".equals(catalogURL))catalogURL = null;
          if("null".equals(dapURL))dapURL = null;
          if("null".equals(httpURL))httpURL = null;

          //DebugConsole.println(element.id+"|"+catalogURL+"|"+dapURL+"|"+httpURL);

          if(catalogURL==null){

            /*if(dapURL==null&&httpURL!=null){
            if(httpURL.indexOf("fileServer")>0){
              dapURL=httpURL.replace("fileServer", "dodsC");
            }
          }*/
            /*else{
            if(dapURL!=null&&httpURL==null){
              if(dapURL.indexOf("dodsC")>0){
                if(dapURL.indexOf("aggregation")==-1){
                  httpURL=dapURL.replace("dodsC","fileServer");
                }
              }
            } 
          }*/

            if(dapURL!=null){
              dataset.put("dapurl",dapURL);
              dataset.put("hasdap",true);
            }
            if(httpURL!=null){
              dataset.put("httpurl",httpURL);
              dataset.put("hashttp",true);
            }
            dataset.put("type","file");
            if (httpURL!=null) {
              if (dapURL!=null) {
                dataset.put("iconCls", "typeOF");
              } else {
                dataset.put("iconCls", "typeF");
              }
            }else {
              dataset.put("iconCls", "typeO");
            }
          }else{
            dataset.put("catalogurl",catalogURL);
            dataset.put("type","catalog");
            dataset.put("iconCls", "typeCAT");
          }

          dataset.put("id",element.id);
          dataset.put("text",element.id);
          dataset.put("leaf",true);
          dataset.put("date",element.addDate);
          dataset.put("filesize",fileSize);
          dataset.put("index",j);
        }

        j++;
      } 

      /** Add datasets added to the basket by processing, listed in the /data/ folder ***/
      ImpactUser impactUser = LoginManager.getUser(request);
      String dataDir = impactUser.getDataDir();
      File dataDirFile = new File(dataDir);
      if(dataDirFile.exists()){
        JSONObject userStorage = new JSONObject();

        userStorage.put("text","My data");
        userStorage.put("date","");
        userStorage.put("leaf",false);
        userStorage.put("expanded",true);
        userStorage.put("iconCls", "typeFolder");

        JSONArray userStorageChilds = new JSONArray();
        userStorage.put("children",userStorageChilds);
        datasets.put(userStorage);
        j=putFiles(userStorageChilds,"",dataDirFile,j,impactUser);
      }
      /*if(j>0){
      try {
        genericCart.saveToStore(User.getUser(request));
      } catch (Exception e) {
        DebugConsole.errprintln("Unable to save store to file: "+e.getMessage());
      }
    }*/
      return datasetList; 
    }



    private static int putFiles(JSONArray userStorageChilds, String path,File dataDirFile,int j,ImpactUser impactUser) throws JSONException {
      File[] fileEntry = dataDirFile.listFiles();
      for(int f=0;f<fileEntry.length;f++){
        if(fileEntry[f].isDirectory()){
          JSONObject childrenObject = new JSONObject();
          //userStorage.put("type","folder");
          childrenObject.put("text",fileEntry[f].getName());
          childrenObject.put("id",path+fileEntry[f].getName());
          childrenObject.put("leaf",false);
          childrenObject.put("expanded",false);
          childrenObject.put("type","folder");
          childrenObject.put("iconCls", "typeFolder");
          JSONArray childArray = new JSONArray();
          childrenObject.put("children",childArray);
          userStorageChilds.put(childrenObject);
          j=putFiles(childArray,path+fileEntry[f].getName()+"/",fileEntry[f],j, impactUser );
        }
        if(fileEntry[f].isFile()){
          JSONObject dataset = new JSONObject();
          userStorageChilds.put(dataset);

          dataset.put("iconCls",  "typeF");
          //String dapLocationHTTP = Configuration.GlobalConfig.getServerHTTPURL()+Configuration.getHomeURL()+"/DAP/";
          String dapLocationHTTPS = (Configuration.getHomeURLHTTPS()+"/DAP/");

          if(fileEntry[f].getName().lastIndexOf(".nc")>=0){
            dataset.put("dapurl",dapLocationHTTPS+impactUser.internalName+"/"+path+fileEntry[f].getName());
            dataset.put("hasdap",true);
            dataset.put("iconCls", "typeOF");
          }
          dataset.put("httpurl",dapLocationHTTPS+impactUser.internalName+"/"+path+fileEntry[f].getName());
          dataset.put("hashttp",true);
          dataset.put("id",path+fileEntry[f].getName());
          dataset.put("type","file");
          dataset.put("text",fileEntry[f].getName());
          dataset.put("leaf",true);
          String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
          SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
          //Calendar cal = Calendar.getInstance();
          String currentISOTimeString = sdf.format(fileEntry[f].lastModified())+"Z";
          String addDate = currentISOTimeString;
          dataset.put("date",addDate);

          long fileSize = fileEntry[f].length();
          float fileSizeF = fileSize;
          String fileSizeH = "-";
          if(fileSize>1000*1000*1000){

            fileSizeH = ((double)Math.round(fileSizeF/(1000*1000)))/1000+"G";
          }else if(fileSize>1000*1000){
            fileSizeH = ((double)Math.round(fileSizeF/(1000)))/1000+"M";
          }else if(fileSize>1000){
            fileSizeH = (fileSizeF/1000)+"K";
          }else{
            fileSizeH = fileSize+"B";
          }


          dataset.put("filesize",fileSizeH);
          dataset.put("index",j);
          j++;
        }
      }
      return j;
    }

  }


}