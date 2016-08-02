package opendap;

import impactservice.AccessTokenStore;
import impactservice.AccessTokenStore.AccessTokenHasExpired;
import impactservice.AccessTokenStore.AccessTokenIsNotYetValid;
import impactservice.ImpactUser;
import impactservice.LoginManager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import tools.Debug;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

class Debugger{
  static boolean DebugOpendap = false;
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
        if(Debugger.DebugOpendap)Debug.println("dods subset for dim "+dims.get(j).getFullName()+" start: "+start[j]+" count "+count[j]);
      }
    }

    size = 1;
    for(int j=0;j<start.length;j++){
      size = size * count[j];
    }
  }

};

public class Opendap {

  enum CDMTypes {String,Byte,UInt16,Int16,UInt32,Int32,Float32,Float64 };

  public static CDMTypes ncTypeToCDMType(String CDMType){
    if(CDMType.equals("String"))return CDMTypes.String;
    if(CDMType.equals("byte"))return CDMTypes.Byte;
    if(CDMType.equals("char"))return CDMTypes.Byte;
    if(CDMType.equals("ushort"))return CDMTypes.UInt16;
    if(CDMType.equals("short"))return CDMTypes.Int16;
    if(CDMType.equals("uint"))return CDMTypes.UInt32;
    if(CDMType.equals("int"))return CDMTypes.Int32;
    if(CDMType.equals("float"))return CDMTypes.Float32;
    if(CDMType.equals("double"))return CDMTypes.Float64;
    if(Debugger.DebugOpendap){
      System.out.println(CDMType);
    }
    return null;
  }

  public static String CDMTypeToString(CDMTypes cdmType){
    if(Debugger.DebugOpendap){
      System.out.println(cdmType);
    }
    if(cdmType == CDMTypes.String)return "String";
    if(cdmType == CDMTypes.Byte)return "Byte";
    
    if(cdmType == CDMTypes.UInt16)return "UInt16";
    if(cdmType == CDMTypes.Int16)return "Int16";
    if(cdmType == CDMTypes.UInt32)return "UInt32";
    if(cdmType == CDMTypes.Int32)return "Int32";
    if(cdmType == CDMTypes.Float32)return "Float32";
    if(cdmType == CDMTypes.Float64)return "Float64";
    return null;
  }

  private static StringBuilder getDDSForVariable(Variable variable,NetcdfFile ncfile,String variableDodsQuery) {

    StringBuilder ddsResult = new StringBuilder();

    int rank = variable.getRank();
//    Debug.println(variable.getDODSName());
//    Debug.println(variable.getFullName());
//    Debug.println(variable.getDataType().toString());
//    Debug.println(variable.getDataType().name());
//    Debug.println(variable.getDataType().getPrimitiveClassType().getName());
    
    if(rank == 0){
      ddsResult.append("    "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+variable.getFullName());
    }
    if(rank == 1){
      ddsResult.append("    "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+variable.getFullName());
      Dimension dim = variable.getDimension(0);
      if(dim == null){
        Debug.errprintln("Dimension not found");
        return null;
      }
      ddsResult.append("["+dim.getFullName()+" = "+dim.getLength()+"]");  
    }
    if(rank>1){
     // if(variableDodsQuery.equals("")){
        /*if(1==2){
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
        }else{*/
          ddsResult.append("    "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+variable.getFullName());
          DimInfo dimInfo = new DimInfo();

          dimInfo.parse(variable,variableDodsQuery);
          for(int i=0;i<dimInfo.dims.size();i++){
            ddsResult.append("["+dimInfo.dims.get(i).getFullName()+" = "+dimInfo.count[i]+"]");
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
    
    StringBuilder ddsResult = new StringBuilder();
 
    if(Debugger.DebugOpendap)Debug.println("getDatasetDODSFromNetCDFFile: ["+queryString+"]");
    
    
    String[] varNames = null;
    
    if(queryString.length() != 0){
//      Debug.println("Selection");
      varNames = queryString.split(",");
    }else{
//      Debug.println("ALL");
      List<Variable> var = ncfile.getVariables();
      for(int j=0;j<var.size();j++){
//        Debug.println("Adding ["+var.get(j).getFullName()+"]");
        varNames = tools.Tools.appendString(varNames, var.get(j).getFullName());
      }
    }

  
    ddsResult.append("Dataset {\n");

    DimInfo[] dimInfo = new DimInfo[varNames.length];
    for(int j=0;j<varNames.length;j++){
      String variableDodsQuery = "";
      int subSetRequestP = varNames[j].indexOf(".");;
      String varName = varNames[j];
      //Debug.println("Processing "+varName);
      if(subSetRequestP!=-1){
        varName = varNames[j].substring(0, subSetRequestP);
        variableDodsQuery = varNames[j].substring(subSetRequestP+1);
      }else{
      
        int subsetRequest2 = varNames[j].indexOf("[");
        if(subsetRequest2!=-1){
         
          varName = varNames[j].substring(0, subsetRequest2);
          variableDodsQuery = varNames[j];//.substring(subsetRequest2+1);
         
        }
      }
      
      //DebugConsole.println("variable "+varName);
      Variable var = ncfile.findVariable(varName);
      if(var == null){
        Debug.println("Variable "+varName+" not found");
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
      }else{
        
        int subsetRequest2 = varNames[j].indexOf("[");
        if(subsetRequest2!=-1){
        
          varName = varNames[j].substring(0, subsetRequest2);
        
        }
      }
      Variable variable = ncfile.findVariable(varName);
      if(variable == null){
        Debug.errprintln("Variable "+varName+" not found.");
        return null;
      }
      int varSize = dimInfo[j].size;
      
      if(Debugger.DebugOpendap){
      //  DebugConsole.println(j+"): name: "+varName+" size: "+varSize+ " all: " + (int) variable.getSize()+" type: "+variable.getDataType().toString()+" scalar:"+variable.isScalar());
      }

      //boolean wasScalar = false;
      if(variable.isScalar()==false)
      {
        DataOutputStream dos = new DataOutputStream(bos);
     /*   if(wasScalar){
          dos.writeInt(1);
          dos.writeInt(0);
        }*/
        //dos.writeInt(1);
        //dos.writeInt(0);
        //}else{
        //dos.
        //bos.write(b)
        //os.writeInt(varSize);
       
        dos.writeInt(varSize);
        dos.writeInt(varSize);
        //dos.writeInt(varSize);
        dos.flush();
     
        //byte[] b = variable.read().getDataAsByteBuffer().array();


        byte[] b = null;
        try {
          //Debug.println("variable.getDataType().toString():"+variable.getDataType().toString());
          //DebugConsole.println("ByteBuffer "+byteBuffer.hasArray()+" - "+b.length);
          CDMTypes type = ncTypeToCDMType(variable.getDataType().toString());
//          if(type == CDMTypes.String){
//            for(int i=0;i<dimInfo[j].size;i++){
//              Debug.println("String "+i+" count/start:"+dimInfo[j].start[i]+"/"+dimInfo[j].count[i]);              
//            }
//          
//          }
////          
//          Array a = variable.read();
//          for(int i=0;i<a.getSize();i++){
//            Debug.println(i+"="+a.getByte(i));
//          }
//          
          if(Debugger.DebugOpendap)Debug.println("Normal read "+varName+" start:"+dimInfo[j].start[0] +" count:"+ dimInfo[j].count[0]);
          ByteBuffer byteBuffer = variable.read(dimInfo[j].start, dimInfo[j].count).getDataAsByteBuffer();
          
         
          
          
          
          int elsize = 1;
          if(type == CDMTypes.Byte)elsize=1;
          if(type == CDMTypes.UInt16)elsize=2;
          if(type == CDMTypes.UInt32)elsize=4;
          if(type == CDMTypes.Int16)elsize=2;
          if(type == CDMTypes.Int32)elsize=4;
          if(type == CDMTypes.Float32)elsize=4;
          if(type == CDMTypes.Float64)elsize=8;
          if(Debugger.DebugOpendap){
            Debug.println("ElSize = "+elsize);
          }
          //if(b.length!=varSize){
            //DebugConsole.errprintln(""+b.length/elsize+" and "+varSize);
          //}

          //DebugConsole.println("ByteBuffer "+byteBuffer.hasArray()+" - "+b.length+" elsize="+elsize);
          byte[] c = new byte[elsize];
          
          //DataOutputStream dos2 = new DataOutputStream(bos);
          b = byteBuffer.array();

          for(int g=0;g<b.length;g=g+elsize){
            for(int i=0;i<elsize;i++){
             // c[i]=b[((elsize-1)-i)+g];
              c[i]=b[i+g];
             //Debug.println(""+c[i]);
            }
            //dos2.writeFloat(0);
            bos.write(c);
            
          }
          c=null;
          bos.flush();
        } catch (InvalidRangeException e) {
          e.printStackTrace();
        }
        int dapLength = ((int)((b.length/4.0+0.9)))*4;
        if(Debugger.DebugOpendap)Debug.println(varName+" numbytes: "+b.length+ " dapLength "+dapLength);

       
        for(int i=b.length;i<dapLength;i++){
          dos.writeByte(0);
          if(Debugger.DebugOpendap)Debug.println("X");
        }
     //  wasScalar = false;
      }else{
        if(Debugger.DebugOpendap)Debug.println("Scalar");
        //Debug.println("Scalar size: "+variable.getSize());
        DataOutputStream dos = new DataOutputStream(bos);
        CDMTypes type = ncTypeToCDMType(variable.getDataType().toString());
        if(type != CDMTypes.String){
          if(type == CDMTypes.Byte ){
            //Debug.println("Writing scalar byte");
            bos.write(variable.readScalarByte());
            bos.write(variable.readScalarByte());
            bos.write(variable.readScalarByte());
            bos.write(variable.readScalarByte());
            
            //dos.writeDouble(variable.readScalarByte());
          }
          if(type == CDMTypes.Int16||type == CDMTypes.UInt16){
            dos.writeInt(variable.readScalarShort());
          }
          if(type == CDMTypes.Int32||type == CDMTypes.UInt32){
            if(Debugger.DebugOpendap)Debug.println("Writing scalar int32");
            //dos.writeDouble(variable.readScalarInt());
            dos.writeInt(variable.readScalarInt());
//            bos.write(0xFF);
//            bos.write(0xFF);
//            bos.write(0x80);
//            bos.write(0x01);
          }
          if(type == CDMTypes.Float32){
            if(Debugger.DebugOpendap)Debug.println("Writing scalar float32");
            dos.writeFloat(variable.readScalarFloat());
            
          }
          if(type == CDMTypes.Float64){
            
            if(Debugger.DebugOpendap)Debug.println("Writing scalar double");
            dos.writeDouble(variable.readScalarDouble());
          }
          
          //bos.write(0xFF);
          //bos.write(0xFF);
          //bos.write(0x80);
          //bos.write(0x01);
        }else{
          
          dos.writeInt(1);
          dos.writeInt(0);
          dos.flush();
        }
        
      }
      
    }
 
    //return dos.g
    return bos.toByteArray();
  }

  private static String getAttribute(Attribute attr){
    DataType type = attr.getDataType();
    String attrName = attr.getFullNameEscaped();
    attrName = attrName.replaceAll("\\[","_");
    attrName = attrName.replaceAll("\\]","_");
    String attrStr = ("        "+CDMTypeToString(ncTypeToCDMType(type.toString()))+" "+attrName)+" ";
    //+" \""+attr.getStringValue()+"\";\n");
    boolean foundType = false;
    if(type == DataType.STRING){
      //attrStr+="\""+attr.getStringValue()+"\";\n";
      String s = attr.getStringValue();
      if(s == null) s = "";
      s = s.replaceAll(";", "");
      s = s.replaceAll("\"", "\\\\\"");
      byte b[]= s.getBytes();
      
      //Valid tokens should be in this range, otherwise replace with exclamation mark.
      for(int j=0;j<b.length;j++){
        if(b[j]!=13&&b[j]!=10)if(b[j]<32||b[j]>126)b[j]='!';
      }
      s= new String(b);
      //Debug.println(attrName+":"+s);
      attrStr+="\""+s+"\";\n";
      foundType = true;
    }else{
        String attrValue = "";
        Array vals = attr.getValues();
        
        for(int j=0;j<vals.getSize();j++){
          if(attrValue.length()>0)attrValue+=",";
          if(type == DataType.BYTE){
            attrValue+=vals.getByte(j);
            foundType = true;
          }
          if(type == DataType.CHAR){
            attrValue+=vals.getChar(j);
            foundType = true;
          }
          if(type == DataType.SHORT){
            attrValue+=vals.getShort(j);
            foundType = true;
          }
          
          if(type == DataType.STRUCTURE){
            attrValue+="";
            foundType = true;
          }

          if(type == DataType.INT){
            attrValue+=vals.getInt(j);
            foundType = true;
          }
          if(type == DataType.LONG){
            attrValue+=vals.getLong(j);
          }
          if(type == DataType.FLOAT){
            attrValue+=vals.getFloat(j);
            foundType = true;
          }
          if(type == DataType.DOUBLE){
            attrValue+=vals.getDouble(j);
            foundType = true;
          }
          attrStr+=attrValue;
      }
      attrStr+=";\n";
    }

    if(foundType == false){
      Debug.errprintln("Attribute type not known: "+type.toString());
    }
    return attrStr;

  }

  public static String getDASFromNetCDFFile(NetcdfFile ncfile) {
    if(Debugger.DebugOpendap)Debug.println("getDatasetDASFromNetCDFFile: ");
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
    /* Three ways of authentication are possible:
     * 1) - browser session based
     * 2) - X509, for command line
     * 3) - Access token, for commandline/browsers
     * 
     * When an access token is provided, it is part of the path. 
     */
    
    
    //Get User ID from tokenstore 
    JSONObject token = null;
    try {
      token = AccessTokenStore.checkIfTokenIsValid(request);
    } catch (AccessTokenIsNotYetValid e1) {
    } catch (AccessTokenHasExpired e1) {
    }
    
    
    String queryString = request.getQueryString();
    if(queryString == null)queryString="";
    //Debug.println("queryString: =["+queryString+"]");
    String path = request.getPathInfo();
    if(path==null)return;
    //Debug.println("path: =["+path+"]");
    String filename = null;//"/home/c4m/Downloads/australia.nc";
    
    

    //Retrieve user ID from path

    boolean skipTokenFirst=false;
    if(token!=null){
      //Debug.println("Found token in URL");
      //Debug.println(token.toString());
      skipTokenFirst = true;//token is the first piece of the part.
    }
    String userIdFromPath = "";
    String cleanPath = "";//Complete string
    String[] pathParts = path.split("/");
    //Debug.println("Found parts in path:"+pathParts.length);
    int pathPartsIndex = 0;
    while(pathPartsIndex<pathParts.length){
      String pathParth = pathParts[pathPartsIndex];
      //Debug.println("Checking pathParth "+pathPartsIndex+"  = ["+pathParth +"]");
      if(pathParth.length()>0){
        if(skipTokenFirst){
          try {
            //Debug.println("pathParth   = ["+pathParth +"]");
            String tokenString = token.getString("token");
            //Debug.println("tokenString = ["+tokenString +"]");
            if(pathParth.equals(tokenString)){
              skipTokenFirst = false;
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
        else{
          if(pathParth.length()>0){
            if(userIdFromPath.length()==0){
              userIdFromPath = pathParth;
            }
            cleanPath+="/"+pathParth;
          }
        }
      }
      pathPartsIndex++;
    }
    
    token = null;
    
    //Debug.println("cleanPath: "+cleanPath);
    String fileNameFromPath = cleanPath.substring(cleanPath.lastIndexOf("/")+1);
    //Debug.println("fileNameFromPath:    "+fileNameFromPath);
    String opendapNameFromPath = fileNameFromPath.substring(0,fileNameFromPath.lastIndexOf("."));
    //Debug.println("opendapNameFromPath: "+opendapNameFromPath);
    
    //Debug.println("userIdFromPath:      "+userIdFromPath);
    String filePath = cleanPath.substring(userIdFromPath.length()+1);
    filePath = filePath.substring(0,filePath.lastIndexOf("/"));
    //Debug.println("filePath        : "+filePath);
    try {
      //Debug.println("user:");
      ImpactUser user =  LoginManager.getUser(request);
      //Debug.println("user:"+user);
      
      
      fileNameFromPath = user.getDataDir()+"/"+filePath+"/"+fileNameFromPath;
      //Debug.println("fileNameFromPath: "+fileNameFromPath);
      
      filename = user.getDataDir()+"/"+filePath+"/"+opendapNameFromPath;
      
      
      //Debug.println("Local file name is "+filename);
      
      
      if(!userIdFromPath.startsWith(user.getInternalName())){
        Debug.println("Comparing "+user.getInternalName() + "==" + userIdFromPath+ " UNEQUAL");
        Debug.errprintln("403, Unauthorized: "+userIdFromPath+"!="+user.getInternalName());
        response.setStatus(403);
        response.getOutputStream().print("403 Forbidden (Wrong user id)");
        return;
      }
      //Debug.println("Comparing "+user.getInternalName() + "==" + userIdFromPath+ " OK");
    } catch (Exception e) {
      String message = "401 No user information provided: "+e.getMessage();
      response.setStatus(401);
      Debug.errprintln(message);
      response.getOutputStream().print(message);
      
      return;
      
     
    }

    String externalFileName = opendapNameFromPath;
    //String externalFileName = "IS-ENES/TESTSETS/"+opendapNameFromPath;
    

    NetcdfFile ncFile = null;
    try {

      
      if(path.endsWith(".dds")){
        response.setContentType("text/plain");
        ncFile = NetcdfFile.open(filename);
        response.getOutputStream().write(getDatasetDDSFromNetCDFFile(ncFile,externalFileName,URLDecoder.decode(queryString,"UTF-8"),false));
      }else if(path.endsWith(".das")){
        response.setContentType("text/plain");
        //Debug.println("OPEN");
        ncFile = NetcdfFile.open(filename);
        //Debug.println("START DAS");
        response.getOutputStream().print(getDASFromNetCDFFile(ncFile));
      }else if(path.endsWith(".dods")){
        response.setContentType("application/octet");
        ncFile = NetcdfFile.open(filename);
        response.getOutputStream().write(getDatasetDDSFromNetCDFFile(ncFile,externalFileName,URLDecoder.decode(queryString,"UTF-8"),true));
      }else {
        
        streamFileToClient(response,fileNameFromPath);
      }

    } catch (IOException ioe) {
      Debug.errprintln("Error opening: " + filename);
      Debug.printStackTrace(ioe);
      response.getOutputStream().print("Error opening: " + filename);
      response.setStatus(404);
    } finally { 
      if (null != ncFile) try {
        ncFile.close();
      } catch (IOException ioe) {
        Debug.println("Error closing: " + filename);
      }
    }
  }

  private static void streamFileToClient(HttpServletResponse response,String fileNameFromPath) throws IOException {
    String filename=fileNameFromPath;       
    filename=fileNameFromPath;        
    Debug.println("Streaming file "+filename);
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
