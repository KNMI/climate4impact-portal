package impactservice;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.Debug;
import tools.HTTPTools;

/**
 * Servlet to handle File upload request from Client
 * @author Javin Paul, Maarten Plieger
 */
public class FileUploadHandler extends HttpServlet {
    /**
   * 
   */
  private static final long serialVersionUID = 1L;
   
  
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
      ImpactUser user = null;
   
      try{
          user=LoginManager.getUser(request,response);
          if(user == null)return;
      }catch(Exception e){
          user = null;
      }
      if(user == null){
        //request.getRequestDispatcher("/account/index.jsp").forward(request, response);
        response.setContentType("application/json");
        response.getOutputStream().print("{\"files\":[{\"name\":\"\",\"error\":\"You are not logged in.\"}]}");
        return;
      }
      
      Debug.println("Start upload for user "+user.getId());
      
      long BASKET_UPLOAD_MAX_FILE_SIZE = 100000000;
      String [] allowedExtensions = {".nc",".shp",".sbx",".shx",".dbf",".sbn",".xml",".prj",".csv",".h5"};
      
      //request.get
      String UPLOAD_DIRECTORY = null;
      try{
         UPLOAD_DIRECTORY = user.getDataDir();
      }catch(Exception e){
        e.printStackTrace();
        MessagePrinters.emailFatalErrorException("FileUploadHandler", e);
        response.setContentType("application/json");
        response.getOutputStream().print("{\"files\":[{\"name\":\"\",\"error\":\"Could not create user directory\"}]}");
        return;
      }
      
      Debug.println(UPLOAD_DIRECTORY);
      
      JSONObject jsonResponse = new JSONObject();
      JSONArray jsonFileList = new JSONArray();
      try {
        jsonResponse.put("files", jsonFileList);
      } catch (JSONException e) {
      }
        //process only if its multipart content
      
        if(ServletFileUpload.isMultipartContent(request)){
          DiskFileItemFactory factory = new DiskFileItemFactory();    
          factory.setSizeThreshold((int) BASKET_UPLOAD_MAX_FILE_SIZE);
          ServletFileUpload upload = new ServletFileUpload(factory);
          upload.setSizeMax(BASKET_UPLOAD_MAX_FILE_SIZE);
        

            try {
                List<FileItem> multiparts = upload.parseRequest((RequestContext) request);
              
                for(FileItem item : multiparts){
                    if(!item.isFormField()){
                        long fileSize = item.getSize();
                        JSONObject jsonFile = new JSONObject();
                        jsonFileList.put(jsonFile);
                        
                        File remotefile =  new File(item.getName());
                        String name = remotefile.getName();
                        
                        
                        if(name.length()<1){
                          throw new Exception("No file selected!");
                        }
                        //Unix basename
                        name = name.substring(name.lastIndexOf("/")+1);
                        //Windows basename
                        name = name.substring(name.lastIndexOf("\\")+1);
                        //Validate other tokens.
                        name = HTTPTools.validateInputTokens(name);
                        
                        boolean hasAValidExtension = false;
                        for(int j=0;j<allowedExtensions.length;j++){
                          if(name.endsWith(allowedExtensions[j]))hasAValidExtension = true;
                        }
                        
                        jsonFile.put("name", name);
                        jsonFile.put("size", fileSize);
                        
                        if(hasAValidExtension){
                          if(fileSize<BASKET_UPLOAD_MAX_FILE_SIZE){
                            Debug.println("Uploading file "+name+" "+fileSize);
                            try{
                              File file = new File(UPLOAD_DIRECTORY + File.separator + name);
                              item.write( file);
                            }catch(Exception e){
                              jsonFile.put("error", e.getMessage());
                            }
                          }else{
                            jsonFile.put("error", "File is bigger than "+BASKET_UPLOAD_MAX_FILE_SIZE+" bytes");
                          }
                        }else{
                          String supportedExtensions = "";
                          for(int j=0;j<allowedExtensions.length;j++){
                            if(supportedExtensions.length()>0)supportedExtensions+=", ";
                            supportedExtensions += allowedExtensions[j];
                          }
                          Debug.errprintln("User "+user.internalName+" tried to upload invalid extension '"+name+"'");
                          jsonFile.put("error", "invalid extension, supported are: "+supportedExtensions);;
                        }
                        
                        
                        
                        
                        
                    }
                }
           
               //File uploaded successfully
               request.setAttribute("message", "File uploaded successfully!");
            } catch (Exception ex) {
              //DebugConsole.printStackTrace(ex);
              JSONObject jsonFile = new JSONObject();
              jsonFileList.put(jsonFile);
              try {
                jsonFile.put("name",  ex.getMessage());
                jsonFile.put("error",  ex.getMessage());
                Debug.errprintln("User "+user.internalName+" tried to upload: exception: "+ex.getMessage());
              } catch (JSONException e) {
              }
              request.setAttribute("message", ex.getMessage());
              
            }          
         
        }else{
            request.setAttribute("message","Sorry this Servlet only handles file upload request");
        }
    
        response.setContentType("application/json");
        response.getOutputStream().println(jsonResponse.toString());
        //request.getRequestDispatcher("/account/fileuploadresult.jsp").forward(request, response);
     
    }
  
}


