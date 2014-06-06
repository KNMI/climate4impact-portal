package opendap;

import impactservice.ImpactUser;
import impactservice.LoginManager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import tools.DebugConsole;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class Debug{
  static boolean DebugOpenDAP =false;
}

class DimInfo{
  int start[] = null;
  int count[] = null;
  int stride[] = null;
  int size = 0;
  boolean useStartStopStride = false;
  List<Dimension> dims = null;
  public void parse(Variable variable, String variableDodsQuery) {
    String dodsSubset = "";
    String[] dodsSubsetQueries = null;
    int pos = variableDodsQuery.indexOf("[");
    if(pos>0){
      dodsSubset = variableDodsQuery.substring(pos);

      dodsSubsetQueries = dodsSubset.split("\\]");
      for(int j=0;j<dodsSubsetQueries .length;j++){
        dodsSubsetQueries[j] = dodsSubsetQueries[j].replaceAll("\\[", "");

      }
    }



    dims = variable.getDimensions();

    start = new int[dims.size()];
    count = new int[dims.size()];
    stride = new int[dims.size()];

    for(int j=0;j<dims.size();j++){
      start[j]=0;
      count[j] = dims.get(j).getLength();
      stride[j]=1;
    }

    if(dodsSubsetQueries != null){
      for(int j=0;j<dodsSubsetQueries .length && j<dims.size();j++){
        useStartStopStride = true;
        //if(Debug.DebugOpenDAP)DebugConsole.println("dods subset: "+dims.get(j).getName()+ " = "+ dodsSubsetQueries[j]);

        //Is start+cout given or only count?

        String[] startCount = dodsSubsetQueries[j].split("\\:");
        if(startCount.length == 1){
          start[j] = Integer.parseInt(startCount[0]);
          count[j]=1;
        }
        if(startCount.length == 2){
          start[j] = Integer.parseInt(startCount[0]);
          count[j]= (Integer.parseInt(startCount[1])-start[j])+1;
        }
        if(Debug.DebugOpenDAP)DebugConsole.println("dods subset for dim "+dims.get(j).getName()+" start: "+start[j]+" count "+count[j]);
      }
    }

    size = 1;
    for(int j=0;j<start.length;j++){
      size = size * count[j];
    }
  }

};

public class OpenDAP {
  //http://opendap.nmdc.eu/knmi/thredds/dodsC/testsets/australia.nc.dds
  enum CDMTypes {String,Int16,Int32,Float32,Float64};

  public static CDMTypes ncTypeToCDMType(String CDMType){
    if(CDMType.equals("String"))return CDMTypes.String;
    if(CDMType.equals("char"))return CDMTypes.String;
    if(CDMType.equals("short"))return CDMTypes.Int16;
    if(CDMType.equals("int"))return CDMTypes.Int32;
    if(CDMType.equals("float"))return CDMTypes.Float32;
    if(CDMType.equals("double"))return CDMTypes.Float64;
    if(Debug.DebugOpenDAP){
      System.out.println(CDMType);
    }
    return null;
  }

  public static String CDMTypeToString(CDMTypes cdmType){
    if(cdmType == CDMTypes.String)return "String";
    if(cdmType == CDMTypes.Int16)return "Int16";
    if(cdmType == CDMTypes.Int32)return "Int32";
    if(cdmType == CDMTypes.Float32)return "Float32";
    if(cdmType == CDMTypes.Float64)return "Float64";
    return null;
  }

  public static String getDDSFromNetCDFFile(NetcdfFile ncfile,String fileName){
    List<Variable> var = ncfile.getVariables();

    StringBuilder ddsResult = new StringBuilder();
    ddsResult.append("Dataset {\n");
    for(int j=0;j<var.size();j++){

      ddsResult.append(getDDSForVariable(var.get(j),ncfile,""));

    }
    ddsResult.append("} "+fileName+";\n");
    return ddsResult.toString();
  }

  private static StringBuilder getDDSForVariable(Variable variable,NetcdfFile ncfile,String variableDodsQuery) {

    StringBuilder ddsResult = new StringBuilder();

    int rank = variable.getRank();
    if(rank == 0){
      ddsResult.append("    "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+variable.getFullName());
    }
    if(rank == 1){
      ddsResult.append("    "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+variable.getFullName());
      Dimension dim = variable.getDimension(0);
      ddsResult.append("["+dim.getName()+" = "+dim.getLength()+"]");  
    }
    if(rank>1){
      if(variableDodsQuery.equals("")){
        ddsResult.append("    Grid {\n");
        ddsResult.append("     ARRAY:\n        "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+variable.getFullName());
        List<Dimension> dims = variable.getDimensions();
        for(int i=0;i<dims.size();i++){
          ddsResult.append("["+dims.get(i).getName()+" = "+dims.get(i).getLength()+"]");
        }

        ddsResult.append(";\n");

        ddsResult.append("     MAPS:\n");

        for(int i=0;i<dims.size();i++ ){
          Variable vare = ncfile.findVariable(dims.get(i).getName());
          if(vare != null){
            //DebugConsole.println("findVariable" + vare+" for "+dims.get(i).getName());
            ddsResult.append("        "+CDMTypeToString(ncTypeToCDMType(vare.getDataType().toString()))+" "+vare.getFullName());
            Dimension dim = vare.getDimension(0);
            ddsResult.append("["+dim.getName()+" = "+dim.getLength()+"];\n");  
          }
        }
        ddsResult.append("    } "+variable.getFullName());
      }
      if(variableDodsQuery.length()>0){
        if(Debug.DebugOpenDAP)DebugConsole.println("dods query: "+variableDodsQuery);

        DimInfo dimInfo = new DimInfo();

        dimInfo.parse(variable,variableDodsQuery);




        ddsResult.append("    Structure {\n");
        ddsResult.append("        "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+variable.getFullName());

        for(int i=0;i<dimInfo.dims.size();i++){
          ddsResult.append("["+dimInfo.dims.get(i).getName()+" = "+dimInfo.count[i]+"]");
        }

        ddsResult.append(";\n");


        ddsResult.append("    } "+variable.getFullName());
      }
    }

    ddsResult.append(";\n");
    return ddsResult;
  }
  public static byte[] getDatasetDDSFromNetCDFFile(NetcdfFile ncfile,String fileName,String queryString, boolean includeData) throws IOException {
    
    if(queryString == null){
      queryString = "";
    }
    if(queryString.equals("null")) queryString = "";
    
    if(Debug.DebugOpenDAP)DebugConsole.println("getDatasetDDSFromNetCDFFile: "+queryString);
    if(queryString.length() == 0){
      return getDDSFromNetCDFFile(ncfile,fileName).getBytes();
    }

    String[] varNames = queryString.split(",");

    StringBuilder ddsResult = new StringBuilder();
    ddsResult.append("Dataset {\n");

    DimInfo[] dimInfo = new DimInfo[varNames.length];
    for(int j=0;j<varNames.length;j++){
      String variableDodsQuery = "";
      int subSetRequestP = varNames[j].indexOf(".");;
      String varName = varNames[j];

      if(subSetRequestP!=-1){
        varName = varNames[j].substring(0, subSetRequestP);
        variableDodsQuery = varNames[j].substring(subSetRequestP+1);
      }else{
        int subsetRequest2 = varNames[j].indexOf("[");
        if(subsetRequest2!=-1){
          varName = varNames[j].substring(0, subsetRequest2);
          variableDodsQuery = varNames[j];//.substring(subSetRequestP+1);
        }
      }
      //DebugConsole.println("variable "+varName);
      Variable var = ncfile.findVariable(varName);
      if(var == null){
        DebugConsole.println("Variable "+varName+" not found");
        return null;
      }
      ddsResult.append(getDDSForVariable(var,ncfile,variableDodsQuery));


      dimInfo[j] = new DimInfo();
      dimInfo[j].parse(var,variableDodsQuery);

    }
    ddsResult.append("} "+fileName+";\n");

    if(includeData == false){
      return ddsResult.toString().getBytes();
    }


    ddsResult.append("\nData:\n");

    //DebugConsole.println("DDS part:\n"+ddsResult.toString());
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write(ddsResult.toString().getBytes());
    bos.flush();
  

    for(int j=0;j<varNames.length;j++){
     
     
      int subSetRequestP = varNames[j].indexOf(".");;
      String varName = varNames[j];

      if(subSetRequestP!=-1){
        varName = varNames[j].substring(0, subSetRequestP);
      }
      Variable variable = ncfile.findVariable(varName);
      int varSize = dimInfo[j].size;
      
      if(Debug.DebugOpenDAP){
        DebugConsole.println(j+"): name: "+varName+" size: "+varSize+ " all: " + (int) variable.getSize()+" type: "+variable.getDataType().toString());
      }

      if(variable.isScalar()==false){
        //dos.writeInt(1);
        //dos.writeInt(0);
        //}else{
        //dos.
        //bos.write(b)
        //os.writeInt(varSize);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(varSize);
        dos.writeInt(varSize);
        //dos.writeInt(varSize);
        dos.flush();
     
        //byte[] b = variable.read().getDataAsByteBuffer().array();


        byte[] b = null;
        try {
          
          //DebugConsole.println("ByteBuffer "+byteBuffer.hasArray()+" - "+b.length);
          ByteBuffer byteBuffer = variable.read(dimInfo[j].start, dimInfo[j].count).getDataAsByteBuffer();
          
          b = byteBuffer.array();
          
          
          CDMTypes type = ncTypeToCDMType(variable.getDataType().toString());
          int elsize = 1;
          if(type == CDMTypes.Int16)elsize=2;
          if(type == CDMTypes.Int32)elsize=4;
          if(type == CDMTypes.Float32)elsize=4;
          if(type == CDMTypes.Float64)elsize=8;
          
          //if(b.length!=varSize){
            //DebugConsole.errprintln(""+b.length/elsize+" and "+varSize);
          //}

          //DebugConsole.println("ByteBuffer "+byteBuffer.hasArray()+" - "+b.length+" elsize="+elsize);
          byte[] c = new byte[elsize];
          
          //DataOutputStream dos2 = new DataOutputStream(bos);
          
          for(int g=0;g<b.length;g=g+elsize){
            for(int i=0;i<elsize;i++){
              c[i]=b[i+g];
             // DebugConsole.println(""+c[i]);
            }
            //dos2.writeFloat(0);
            bos.write(c);
            
          }
          c=null;
          bos.flush();
        } catch (InvalidRangeException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        int dapLength = ((int)((b.length/4.0+0.9)))*4;
        if(Debug.DebugOpenDAP)DebugConsole.println("Numbytes: "+b.length+ "dapLength "+dapLength);

       
        for(int i=b.length;i<dapLength;i++){
          dos.writeByte(0);
          if(Debug.DebugOpenDAP)DebugConsole.println("X");
        }
      }else{
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(1);
        //dos.writeInt(0);
        dos.flush();
      }
      
    }
    //return dos.g
    return bos.toByteArray();
  }

  private static String getAttribute(Attribute attr){
    DataType type = attr.getDataType();
    String attrStr = ("        "+CDMTypeToString(ncTypeToCDMType(type.toString()))+" "+attr.getName())+" ";
    //+" \""+attr.getStringValue()+"\";\n");
    boolean foundType = false;
    if(type == DataType.STRING){
      //attrStr+="\""+attr.getStringValue()+"\";\n";
      String s = attr.getStringValue();
      if(s == null) s = "";
      
      s = s.replaceAll(";", "");
/*      if(attr.getName().startsWith("a"))s="";
      if(attr.getName().startsWith("s"))s="";
      if(attr.getName().startsWith("u"))s="";
      if(attr.getName().startsWith("l"))s="";
      if(attr.getName().startsWith("c"))s="";
      if(attr.getName().startsWith("e"))s="";
      if(attr.getName().startsWith("r"))s="";
      s = s.replaceAll("<", "");
      s = s.replaceAll(">", "");*/
      s = s.replaceAll("\"", "\\\\\"");
      //if(attr.getName().startsWith("p"))s="";
      //DebugConsole.println(s);
      attrStr+="\""+s+"\";\n";
      foundType = true;
    }else{
      Array vals = attr.getValues();
      
        for(int j=0;j<vals.getSize();j++){
          if(type == DataType.BYTE){
            attrStr+=vals.getByte(j);
            foundType = true;
          }
          if(type == DataType.CHAR){
            attrStr+=vals.getChar(j);
            foundType = true;
          }
          if(type == DataType.SHORT){
            attrStr+=vals.getShort(j);
            foundType = true;
          }
          
          if(type == DataType.STRUCTURE){
            attrStr+="";
            foundType = true;
          }

          if(type == DataType.INT){
            attrStr+=vals.getInt(j);
            foundType = true;
          }
          if(type == DataType.LONG){
            attrStr+=vals.getLong(j);
          }
          if(type == DataType.FLOAT){
            attrStr+=vals.getFloat(j);
            foundType = true;
          }
          if(type == DataType.DOUBLE){
            attrStr+=vals.getDouble(j);
            foundType = true;
          }
          if(j>0)attrStr+=",";
      }
      attrStr+=";\n";
    }

    if(foundType == false){
      DebugConsole.errprintln("Attribute type not known: "+type.toString());
    }
    return attrStr;

  }

  public static String getDASFromNetCDFFile(NetcdfFile ncfile) {
    if(Debug.DebugOpenDAP)DebugConsole.println("getDatasetDASFromNetCDFFile: ");
    List<Variable> var = ncfile.getVariables();

    StringBuilder ddsResult = new StringBuilder();
    ddsResult.append("Attributes {\n");
    for(int j=0;j<var.size();j++){
      ddsResult.append("    "+var.get(j).getFullName()+" {\n");
      List<Attribute> attr = var.get(j).getAttributes();
      for(int i=0;i<attr.size();i++){

        ddsResult.append(getAttribute(attr.get(i)));
      }
      ddsResult.append("    }\n");
    }

    ddsResult.append("    "+"NC_GLOBAL"+" {\n");
    List<Attribute> attr = ncfile.getGlobalAttributes();
    for(int i=0;i<attr.size();i++){
      ddsResult.append(getAttribute(attr.get(i)));
    }
    ddsResult.append("    }\n");


    ddsResult.append("}\n");
    return ddsResult.toString();
  }



  protected static void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String queryString = request.getQueryString();
    if(queryString == null)queryString="";

    String path = request.getPathInfo();
    if(path==null)return;
   
    String filename = null;//"/home/c4m/Downloads/australia.nc";


    //DebugConsole.println("path: "+path);

    String fileNameFromPath = path.substring(path.lastIndexOf("/")+1); 
    String opendapNameFromPath = fileNameFromPath.substring(0,fileNameFromPath.lastIndexOf("."));
    String userIdFromPath = path.substring(1,path.lastIndexOf("/"));
    //DebugConsole.println("file: "+fileNameFromPath);
    //DebugConsole.println("userid: "+userIdFromPath);
    try {
      ImpactUser user =  LoginManager.getUser(request,response);
      
      
      String filePath = userIdFromPath.substring(user.internalName.length());
      
      fileNameFromPath = user.getDataDir()+"/"+filePath+"/"+fileNameFromPath;
      
      filename = user.getDataDir()+"/"+filePath+"/"+opendapNameFromPath;
      
      
      DebugConsole.println("Local file name is "+filename);
      
      DebugConsole.println("Comparing "+user.internalName + "==" + userIdFromPath);
      if(!userIdFromPath.startsWith(user.internalName)){
        response.setStatus(403);
        response.getOutputStream().print("403 Forbidden (Wrong user id)");
        return;
      }
    } catch (Exception e) {
      String message = "401 No user information provided: "+e.getMessage();
      response.setStatus(401);
      DebugConsole.errprintln(message);
      response.getOutputStream().print(message);
      
      return;
      
     
    }

    String externalFileName = opendapNameFromPath;

    NetcdfFile ncFile = null;
    try {
      if(queryString!=null){
        if(Debug.DebugOpenDAP)DebugConsole.println("Path: "+path+" QueryString: "+queryString);
      }
      
      if(path.endsWith(".dds")){
        response.setContentType("text/plain");
        ncFile = NetcdfFile.open(filename);
        response.getOutputStream().write(getDatasetDDSFromNetCDFFile(ncFile,externalFileName,URLDecoder.decode(queryString,"UTF-8"),false));
      }else if(path.endsWith(".das")){
        response.setContentType("text/plain");
        ncFile = NetcdfFile.open(filename);
        response.getOutputStream().print(getDASFromNetCDFFile(ncFile));
      }else if(path.endsWith(".dods")){
        response.setContentType("application/octet");
        ncFile = NetcdfFile.open(filename);
        response.getOutputStream().write(getDatasetDDSFromNetCDFFile(ncFile,externalFileName,URLDecoder.decode(queryString,"UTF-8"),true));
      }else {
        
        streamFileToClient(response,fileNameFromPath);
      }

    } catch (IOException ioe) {
      DebugConsole.errprintln("trying to open " + filename, ioe);
    } finally { 
      if (null != ncFile) try {
        ncFile.close();
      } catch (IOException ioe) {
        DebugConsole.errprintln("trying to close " + filename, ioe);
      }
    }
  }

  private static void streamFileToClient(HttpServletResponse response,String fileNameFromPath) throws IOException {
    String filename=fileNameFromPath;       
    filename=fileNameFromPath;        
    DebugConsole.println("Streaming file "+filename);
    ServletOutputStream outputStream = response.getOutputStream();
    FileInputStream input = null;
    try{
    input = new FileInputStream(filename);
    }catch(FileNotFoundException e){
      response.setStatus(404);
      outputStream.println("404 Not found");
      return;
    }
    if(input!=null){
      try {
        response.setHeader("Content-Type", "application/netcdf");
        response.setHeader("Content-Length", String.valueOf(input.getChannel().size()));
        IOUtils.copy(input, outputStream);
        input.close();
      } catch (IOException e) {
        response.setStatus(404);
        outputStream.println("404 Unable to stream file.");
      }
    }
  };
  
}
