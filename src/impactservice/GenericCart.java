package impactservice;



import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
  
  public Vector<DataLocator> dataLocatorList = new Vector<DataLocator>();
  
  private ImpactUser user = null;
  private  String genericId = "";

  public  GenericCart(String id,ImpactUser user){
    //Debug.println("Creating new GenericCart with id "+id+" for user "+user.getId());
    this.genericId=id;
    this.user=user;

  }

  public class DataLocator{
    DataLocator(String id,String cartData){
      addDataLocator(id,cartData);
    }

    DataLocator(String id,JSONObject _cartData){
      addDataLocator(id,_cartData.toString());
    }
    
    private void addDataLocator(String id,String cartData){
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
    public JSONObject getCartData() throws JSONException{
      return (JSONObject) new JSONTokener(cartData).nextValue();
    }
    public void setCartData(JSONObject _cartData) throws JSONException{
      cartData = _cartData.toString();
    }
    
    String id = null;
    public String cartData = null;
    long addDateMilli = 0;
    String addDate = null;
  };

  

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
    Debug.println("Checking "+user.getDataDir()+"/"+id);
    boolean isLocalFile = false;
    File file = new File(user.getDataDir()+"/"+id);
    if(file.exists()){
      isLocalFile = true;
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
    if(isLocalFile == false){
      boolean fileWasRemoved = false;
     
      Iterator<DataLocator> itr = dataLocatorList.iterator();
      Debug.println("There are "+dataLocatorList.size() +" files");
      while(itr.hasNext()) {
        
        DataLocator element = itr.next();
        //Debug.println("Checking "+element.id);
        if(element.id.equals(id)){
          Debug.println("Removing "+element.id);
          try {
            Debug.println("Checking "+element.getCartData().toString());
          } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          itr.remove();
          fileWasRemoved = true;
        }
      }
      Debug.println("There are "+dataLocatorList.size() +" files");
      if(fileWasRemoved){
        saveToStore();
      }
    }
  }

  public int getNumProducts(){
    //Debug.println("GetNumProducts" );
    int numCustomFiles = 0;
    return dataLocatorList.size()+numCustomFiles;
  }


  public synchronized void  loadFromStore() throws Exception {
    //Debug.println("Loading from store");
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
  private synchronized void saveToStore(){
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
     * Generates a JSONObject with contents of the job list
     * @param genericCart
     * @param request
     * @return
     * @throws Exception 
     */
    public static JSONObject showJobList(GenericCart genericCart,HttpServletRequest request) throws Exception{
      Debug.println("Show joblist");
      
      DataLocator[] dataLocatorListArray = (DataLocator[]) genericCart.dataLocatorList.toArray(new DataLocator[genericCart.dataLocatorList.size()]);
      
      Arrays.sort(dataLocatorListArray, new Comparator<DataLocator>(){
        public int compare(DataLocator o1, DataLocator o2) {
          return Long.valueOf(o2.addDateMilli).compareTo(o1.addDateMilli);
        }
      });
      
      
      
  
      JSONObject jobListObject = new JSONObject();
      JSONArray jobArray = new JSONArray();
      jobListObject.put("jobs", jobArray);
      
      
     
      
      
      for(int j=0;j<dataLocatorListArray.length;j++){
        DataLocator element = dataLocatorListArray[j];
       
        JSONObject job = new JSONObject();
        jobArray.put(job);
        
        boolean readFromWPSStatusLocation = true;//By default, read from the WPS status location
        boolean hasError = false;
        boolean isCompleted = false;
        boolean isSaxException = false;
        String progress= "-";
        
      
        

        try {
          JSONObject elementProps =  element.getCartData();
          String statusLocation = null;
          try{
            statusLocation = elementProps.getString("statuslocation");
          }catch(Exception e){
            statusLocation = elementProps.getString("wpsurl");
          }
          
          String id= elementProps.getString("id");
          
          /* Check if process is set to ready, if ready we do not need to read the statuslocation again*/
          try{
            String finished = elementProps.getString("finished");
            if(finished.equals("true")){
              readFromWPSStatusLocation = false;
              isCompleted = true;
            }
          }catch(Exception e){
          }
          
          /*Check for errors */
          try{
            String error = elementProps.getString("error");
            if(error!=null){
              if(!error.equals("false")){
                //Debug.errprintln(error);
                hasError = true;
                try{
                  if(error.indexOf("SAXException")!=-1){
                    isSaxException = true;
                    readFromWPSStatusLocation = true;
                  }
                }catch(Exception e){
                }
              }
            }
          }catch(Exception e){
          }
          
          //readFromWPSStatusLocation=true;
          if(readFromWPSStatusLocation){
            Debug.println("Read "+statusLocation);
            JSONObject progressObject = (JSONObject) WebProcessingInterface.monitorProcess(statusLocation,request);
           
            if(progressObject != null){
              try{
                progress= progressObject.getString("progress");
              }catch(Exception e){
              }
              
              String error = null;
              try{
                error= progressObject.getString("error");
                if(error.indexOf("SAXException")!=-1){
                  isSaxException = true;
                }
              }catch(Exception e){
              }
              
              if(progress.equals("100")){
                //Now set that this process is completed, we do not need to read the WPS statuslocation again.
                Debug.errprintln("WPS All Good");
                error = null;
                hasError = false;
                elementProps.put("finished", "true");
                elementProps.put("error",false);
                element.setCartData(elementProps);
                genericCart.saveToStore();
                isCompleted = true;
              }
              
              if(error!=null && isSaxException == false){
                Debug.errprintln("WPS Error, not SAX Exception");
                elementProps.put("error",error);
                hasError = true;
                elementProps.put("finished", "true");
                element.setCartData(elementProps);
                genericCart.saveToStore();
                isCompleted = true;
              }
              
              
              if(error!=null && isSaxException == true){
                Debug.errprintln("SAX Exception");
                elementProps.put("error",error);
                hasError = true;
              }
              
              if(error==null && progress.equals("100")==false){
                Debug.println("Still calculating:" +statusLocation);
              }
            
              
            }else{
              Debug.errprintln("Unable to get progressObject");
              hasError = true;
            }
          }
          
          if(hasError){
            try{
              job.put("error",elementProps.get("error"));
              hasError = true;
            }catch(Exception e){
              job.put("error",false);
            }
          }
          
          
//          Debug.println("readFromWPSStatusLocation: "+readFromWPSStatusLocation+" for "+id);
//          Debug.println("progress: "+progress);
//          Debug.println("isCompleted: "+isCompleted);
//          Debug.println("hasError: "+hasError);
//          

       
          job.put("creationdate",element.addDate);

          job.put("wpsid",id);
          job.put("processid",element.id);
          job.put("statuslocation",statusLocation);
          
          try{
            job.put("wpspostdata",elementProps.get("wpspostdata"));
          }catch(Exception e){
            job.put("wpspostdata",false);
          }
      
          
          try{
            if(hasError == false){
              if(isCompleted){
                job.put("progress","ready");
                job.put("status","ready");
              }else{
                job.put("progress",""+progress+" %");
                job.put("status","running");
              }
            }else{
              job.put("progress","failed");
              job.put("status","failed");
            }
          }catch(Exception e){
            job.put("progress","failed");
            job.put("status","failed");
          }
        } catch (Exception e) {
          Debug.errprintln(e.getMessage());
          job.put("progress","failed");
          job.put("status","crashed");
        }
      } 
      return jobListObject;
    };

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
            try{dapURL =elementProps.getString("opendap");}catch(Exception e){}
            try{httpURL =elementProps.getString("httpserver");}catch(Exception e){}
            
            //For deprecated baskets:
            try{dapURL =elementProps.getString("OpenDAP");}catch(Exception e){}
            try{httpURL =elementProps.getString("httpServer");}catch(Exception e){}
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
      Arrays.sort(fileEntry, new Comparator<File>(){
        public int compare(File f1, File f2)
        {
            return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
        } });
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
          childrenObject.put("date",tools.DateFunctions.getTimeStampInMillisToISO8601(fileEntry[f].lastModified()));
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

          if(fileEntry[f].getName().endsWith(".nc")||
              fileEntry[f].getName().endsWith(".nc3")||
              fileEntry[f].getName().endsWith(".nc4")||
              fileEntry[f].getName().endsWith(".json")||
              fileEntry[f].getName().endsWith(".geojson")){
            dataset.put("dapurl",dapLocationHTTPS+impactUser.getUserId()+"/"+path+fileEntry[f].getName());
            dataset.put("hasdap",true);
            dataset.put("iconCls", "typeOF");
          }
          if(fileEntry[f].getName().lastIndexOf(".catalog")>=0){
            dataset.put("catalogurl",impactUser.getDataURL()+fileEntry[f].getName());
            dataset.put("catalog",true);
          }
          dataset.put("httpurl",dapLocationHTTPS+impactUser.getUserId()+"/"+path+fileEntry[f].getName());
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