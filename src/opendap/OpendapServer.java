package opendap;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import tools.DateFunctions;
import tools.Debug;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Tiny OpenDAP server used for the basket from climate4impact
 * 
 * @author Maarten Plieger
 * 2016-08-15
 */

public class OpendapServer {
  /**
   * Set to true to see debug info
   * @author plieger
   *
   */
  class Debugger{
    static final boolean DebugOpendap = false;
  }
  
  /**
   * Handles the opendap server requests. Give it a netcdf filename to serve, an external filename, a opendap name and a servlet request/response object.
   * @param localNetCDFFileLocation - NetCDF file location to open.
   * @param externalFileName - Filename which is used when a file stream/download request is requested.
   * @param baseName - The opendap name, eg the file basename without das/dds/dods suffix but with nc
   * @param request
   * @param response
   * @throws IOException
   */
  public static void handleOpenDapReqeuests(String localNetCDFFileLocation,String baseName,HttpServletRequest request,HttpServletResponse response) throws IOException{
    long startTime = 0;

    if(Debugger.DebugOpendap){
      startTime = DateFunctions.getCurrentDateInMillis();
      Debug.println("handleOpenDapReqeuests received.");
      Debug.println("localNetCDFFileLocation:"+localNetCDFFileLocation);
      Debug.println("opendapNameFromPath:    "+baseName);
    }
    String queryString = request.getQueryString();
    if(queryString == null)queryString="";
    String path = request.getPathInfo();
    if(path==null)return;
    NetcdfFile ncFile = null;
    try {
      if(path.endsWith(".dds")){
        response.setContentType("text/plain");
        ncFile = NetcdfFile.open(localNetCDFFileLocation);
        response.getOutputStream().write(getDatasetDDSFromNetCDFFile(ncFile,baseName,URLDecoder.decode(queryString,"UTF-8"),false));
      }else if(path.endsWith(".das")){
        response.setContentType("text/plain");
        ncFile = NetcdfFile.open(localNetCDFFileLocation);
        response.getOutputStream().print(getDASFromNetCDFFile(ncFile).toString());
      }else if(path.endsWith(".dods")){
        response.setContentType("application/octet");
        ncFile = NetcdfFile.open(localNetCDFFileLocation);
        response.getOutputStream().write(getDatasetDDSFromNetCDFFile(ncFile,baseName,URLDecoder.decode(queryString,"UTF-8"),true));
      }else {
        streamFileToClient(response,localNetCDFFileLocation);
      }

    } catch (IOException ioe) {
      Debug.errprintln("Error opening: " + localNetCDFFileLocation);
      Debug.printStackTrace(ioe);
      response.getOutputStream().print("Error opening: " + localNetCDFFileLocation);
      response.setStatus(404);
    } finally { 
      if (null != ncFile) try {
        ncFile.close();
      } catch (IOException ioe) {
        Debug.println("Error closing: " + localNetCDFFileLocation);
      }
    }
    
    if(Debugger.DebugOpendap){
      Debug.println("/handleOpenDapReqeuests done, took "+(DateFunctions.getCurrentDateInMillis()-startTime)+" ms.");
    }
    
  };
  
  
  public static long getUnsignedInt(int x) {
    return x & 0x00000000ffffffffL;
  }

  static class DimInfo{
    int start[] = null;
    int count[] = null;
    int stride[] = null;
    int size = 0;
    boolean useStartStopStride = false;
    List<Dimension> dims = null;

    /**
     * Parses query string and handles subsetting requests. Fills out dimension object with found parameters.
     * @param variable The NetCDF variable
     * @param variableDodsQuery The query
     */
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

          /*Is start+count given or only count?*/
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

  
  enum CDMTypes {String,Byte,UInt16,Int16,UInt32,Int32,Float32,Float64 };

  private static CDMTypes ncTypeToCDMType(String CDMType){
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
      Debug.errprintln("Unknown type "+CDMType);
    }
    return null;
  }

  private static String CDMTypeToString(CDMTypes cdmType){
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

  /**
   * Gets the variable name from the NetCDF variable
   * @param variable
   * @return
   */
  private static String getVariableName(Variable variable) {
    String varName = variable.getFullName();
    return varName.replace("\\.","_dot_");
  }

  /**
   * Gets the NetCDF variable by its name
   * @param ncfile
   * @param varName
   * @return
   */
  private static Variable getVariable(NetcdfFile ncfile, String varName) {
    varName = varName.replace("_dot_", "\\.");
    return ncfile.findVariable(varName);
  }
  
  /**
   * Returns the data description for this variable
   * @param variable
   * @param ncfile
   * @param variableDodsQuery
   * @return
   */
  private static StringBuilder getDDSForVariable(Variable variable,NetcdfFile ncfile,String variableDodsQuery) {
    StringBuilder ddsResult = new StringBuilder();
    if(variable.getRank() == 0){
      ddsResult.append("    "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+getVariableName(variable));
    }else{
      ddsResult.append("    "+CDMTypeToString(ncTypeToCDMType(variable.getDataType().toString()))+" "+getVariableName(variable));
      DimInfo dimInfo = new DimInfo();
      dimInfo.parse(variable,variableDodsQuery);
      for(int i=0;i<dimInfo.dims.size();i++){
        ddsResult.append("["+dimInfo.dims.get(i).getFullName()+" = "+dimInfo.count[i]+"]");
      }
    }
    ddsResult.append(";\n");
    return ddsResult;
  };

  /**
   * Writes a an integer to the ByteArrayOutputStream
   * @param out
   * @param v
   * @throws IOException
   */
  private final static void writeInt(ByteArrayOutputStream out,int v) throws IOException {
    out.write((v >>> 24) & 0xFF);
    out.write((v >>> 16) & 0xFF);
    out.write((v >>>  8) & 0xFF);
    out.write((v >>>  0) & 0xFF);
  };

  /**
   * Writes NC_STRING arrays from the NetCDF variable to the ByteArrayOutputStream
   * @param bos
   * @param variable
   * @param dimInfo
   * @param varName
   * @throws IOException
   * @throws InvalidRangeException
   */
  private static void writeVariableDataString(ByteArrayOutputStream bos, Variable variable, DimInfo dimInfo,String varName) throws IOException, InvalidRangeException{
    if(Debugger.DebugOpendap)
      Debug.println("Normal read "+varName+" start:"+dimInfo.start[0] +" count:"+dimInfo.count[0]);
    Array a = variable.read(dimInfo.start, dimInfo.count);
    int varSize = dimInfo.size;
    writeInt(bos,varSize);
    for(int e= 0;e<a.getSize();e++){
      byte[] theString = a.getObject(e).toString().getBytes();
      int strLength = theString.length;
      writeInt(bos,strLength+1);
      bos.write(theString);
      bos.write(0);
      strLength++;
      int dapLength = ((int)((strLength/4.0+0.9)))*4;
      for(int i=strLength;i<dapLength;i++){
        bos.write(0);
      }
    }
  };
  
  /**
   * Writes scalar variable data to the ByteArrayOutputStream
   * @param bos
   * @param variable
   * @param dimInfo
   * @param varName
   * @throws IOException
   */
  private static void writeVariableScalar(ByteArrayOutputStream bos,
      Variable variable, DimInfo dimInfo, String varName) throws IOException {
    if(Debugger.DebugOpendap)Debug.println("Scalar");
    CDMTypes type = ncTypeToCDMType(variable.getDataType().toString());
    if(type != CDMTypes.String){
      if(type == CDMTypes.Byte ){
        bos.write(variable.readScalarByte());
        bos.write(variable.readScalarByte());
        bos.write(variable.readScalarByte());
        bos.write(variable.readScalarByte());
      }
      if(type == CDMTypes.Int16||type == CDMTypes.UInt16){
        writeInt(bos,variable.readScalarShort());
      }
      if(type == CDMTypes.Int32||type == CDMTypes.UInt32){
        if(Debugger.DebugOpendap)Debug.println("Writing scalar int32");
        writeInt(bos,variable.readScalarInt());
      }
      if(type == CDMTypes.Float32){
        if(Debugger.DebugOpendap)Debug.println("Writing scalar float32");
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeFloat(variable.readScalarFloat());
        dos.flush();
      }
      if(type == CDMTypes.Float64){
        if(Debugger.DebugOpendap)Debug.println("Writing scalar double");
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeDouble(variable.readScalarDouble());
        dos.flush();
      }
    }else{
      writeInt(bos,1);
      writeInt(bos,0);
    }
  };

  /**
   * Writes variable data arrays to the ByteArrayOutputStream
   * @param bos
   * @param variable
   * @param dimInfo
   * @param varName
   * @throws IOException
   * @throws InvalidRangeException
   */
  private static void writeVariableData(ByteArrayOutputStream bos, Variable variable, DimInfo dimInfo,String varName) throws IOException, InvalidRangeException{
    CDMTypes type = ncTypeToCDMType(variable.getDataType().toString());
    if(type == CDMTypes.String){
      writeVariableDataString(bos,variable,dimInfo,varName);
      return;
    }
    int varSize = dimInfo.size;
    writeInt(bos,varSize);
    writeInt(bos,varSize);
    
    try {
      if(Debugger.DebugOpendap)Debug.println("Normal read "+varName+" start:"+dimInfo.start[0] +" count:"+ dimInfo.count[0]);
      ByteBuffer byteBuffer = variable.read(dimInfo.start, dimInfo.count).getDataAsByteBuffer();
      int elsize = 1;
      if(type == CDMTypes.Byte)elsize=1;
      if(type == CDMTypes.UInt16)elsize=2;
      if(type == CDMTypes.Int16)elsize=2;
      if(type == CDMTypes.UInt32)elsize=4;
      if(type == CDMTypes.Int32)elsize=4;
      if(type == CDMTypes.Float32)elsize=4;
      if(type == CDMTypes.Float64)elsize=8;
      if(Debugger.DebugOpendap){
        Debug.println("ElSize = "+elsize);
      }
      
      int outSize = varSize*elsize;
      if(type == CDMTypes.Int16 || type == CDMTypes.UInt16){
        outSize*=2;
      }
      
      byte[] b = byteBuffer.array();
      byte[] c = new byte[outSize];

      if(type==CDMTypes.Byte||type == CDMTypes.Int32||type == CDMTypes.UInt32||type == CDMTypes.Float32||type == CDMTypes.Float64){
        for(int g=0;g<b.length;g++){
          c[g]=b[g];
        }
      }
      
      if(type==CDMTypes.UInt16||type == CDMTypes.Int16){
        for(int g=0;g<b.length;g=g+2){
          c[g*2+0]=0;
          c[g*2+1]=0;
          c[g*2+2]=b[g+0];
          c[g*2+3]=b[g+1];
        }
      }
      
      bos.write(c);
      c=null;

      int dapLength = ((int)((outSize/4.0+0.9)))*4;
      if(Debugger.DebugOpendap)Debug.println(varName+" numbytes: "+outSize+ " dapLength "+dapLength);
      for(int i=outSize;i<dapLength;i++){
        bos.write(0);
        if(Debugger.DebugOpendap)Debug.println("X");
      }
    } catch (InvalidRangeException e) {
      e.printStackTrace();
    }

  };

  /**
   * Returns the complete data description document for this NetCDF file, filtered by the queryString
   * @param ncfile
   * @param fileName
   * @param queryString
   * @param includeData
   * @return
   * @throws IOException
   */
  private static byte[] getDatasetDDSFromNetCDFFile(NetcdfFile ncfile,String fileName,String queryString, boolean includeData) throws IOException {
    if(queryString == null){
      queryString = "";
    }
    if(queryString.equals("null")) queryString = "";
    StringBuilder ddsResult = new StringBuilder();
    if(Debugger.DebugOpendap)Debug.println("getDatasetDODSFromNetCDFFile: ["+queryString+"]");

    String[] varNames = null;
    if(queryString.length() != 0){
      varNames = queryString.split(",");
    }else{
      List<Variable> var = ncfile.getVariables();
      for(int j=0;j<var.size();j++){
        varNames = tools.Tools.appendString(varNames, getVariableName(var.get(j)));
      }
    }

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
          variableDodsQuery = varNames[j];
        }
      }
      Variable var = getVariable(ncfile,varName);
      if(var == null){
        Debug.println("Variable "+varName+" not found");
        return null;
      }
      ddsResult.append(getDDSForVariable(var,ncfile,variableDodsQuery));
      dimInfo[j] = new DimInfo();
      dimInfo[j].parse(var,variableDodsQuery);
    }
    ddsResult.append("} "+fileName+";\n");

    /*This was the data description document*/
    if(includeData == false){
      return ddsResult.toString().getBytes();
    }
    /*This will be the data part */
    ddsResult.append("\nData:\n");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bos.write(ddsResult.toString().getBytes());
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
      
      Variable variable = getVariable(ncfile,varName);
      if(variable == null){
        Debug.errprintln("Variable "+varName+" not found.");
        return null;
      }
      if(variable.isScalar()==false){
        try {
          writeVariableData(bos,variable,dimInfo[j],varName);
        } catch (InvalidRangeException e) {
          Debug.errprintln("Unable to write data for variable "+varName+".");
          e.printStackTrace();
          return null;
        }
      }else{
        writeVariableScalar(bos,variable,dimInfo[j],varName);
      }
    }
    return bos.toByteArray();
  };
 
  /**
   * Returns the NetCDF attribute as Stringbuilder object for the data attribute service document.
   * @param attr
   * @return
   */
  private static StringBuilder getAttribute(Attribute attr){
    DataType type = attr.getDataType();
    String attrName = attr.getFullNameEscaped();
    attrName = attrName.replaceAll("\\[","_");
    attrName = attrName.replaceAll("\\]","_");
    StringBuilder attrStr = new StringBuilder(("        "+CDMTypeToString(ncTypeToCDMType(type.toString()))+" "+attrName)+" ");
    boolean foundType = false;
    if(type == DataType.STRING){
      String s = attr.getStringValue();
      if(s == null) s = "";
      s = s.replaceAll(";", "");
      s = s.replaceAll("\"", "\\\\\"");
      byte b[]= s.getBytes();
      /*Valid tokens should be in this range, otherwise replace with exclamation mark.*/
      for(int j=0;j<b.length;j++){
        if(b[j]!=13&&b[j]!=10)if(b[j]<32||b[j]>126)b[j]='!';
      }
      attrStr.append("\"");
      attrStr.append(new String(b));
      attrStr.append("\";\n");
      foundType = true;
    }else{
      foundType = true;
      StringBuilder attrValue = new StringBuilder();
      Array vals = attr.getValues();
      if(vals.isUnsigned()==false){
        for(int j=0;j<vals.getSize();j++){
          if(attrValue.length()>0)attrValue.append(",");
          switch(type){
          case BYTE:
            attrValue.append(vals.getByte(j));
            break;
          case CHAR:
            attrValue.append(vals.getChar(j));
            break;
          case SHORT:
            attrValue.append(vals.getShort(j));
            break;
          case INT:
            attrValue.append(vals.getInt(j));
            break;
          case LONG:
            attrValue.append(vals.getLong(j));
            break;
          case FLOAT:
            attrValue.append(vals.getFloat(j));
            break;
          case DOUBLE:
            attrValue.append(vals.getDouble(j));
            break;
          default:
            foundType = false;
            break;
          }
        }
      }else{
        for(int j=0;j<vals.getSize();j++){
          if(attrValue.length()>0)attrValue.append(",");
          switch(type){
          case BYTE:
            attrValue.append(""+getUnsignedInt(vals.getByte(j)));
            break;
          case CHAR:
            attrValue.append(""+getUnsignedInt(vals.getChar(j)));
            break;
          case SHORT:
            attrValue.append(""+getUnsignedInt(vals.getShort(j)));
            break;
          case INT:
            attrValue.append(""+getUnsignedInt(vals.getInt(j)));
            break;
          case LONG:
            attrValue.append(""+getUnsignedInt((int) vals.getLong(j)));
            break;

          default:
            foundType = false;
            break;
          }
        }
       // Debug.errprintln("Unsigned stuffs found!:"+attrName+":"+attrValue);
      }
      attrStr.append(attrValue);
      attrStr.append(";\n");
      if(Debugger.DebugOpendap)Debug.print(attrName+":"+attrStr);
    }
    if(foundType == false){
      Debug.errprintln("Attribute type not known: "+type.toString());
    }
    return attrStr;
  };

  /**
   * Returns the data atrribute service document.
   * @param ncfile
   * @return
   */
  private static StringBuilder getDASFromNetCDFFile(NetcdfFile ncfile) {
    if(Debugger.DebugOpendap)Debug.println("getDatasetDASFromNetCDFFile: ");
    List<Variable> var = ncfile.getVariables();
    StringBuilder ddsResult = new StringBuilder();
    ddsResult.append("Attributes {\n");
    for(int j=0;j<var.size();j++){
      ddsResult.append("    "+getVariableName(var.get(j))+" {\n");
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
    return ddsResult;
  };

  

  
  
  private static void streamFileToClient(HttpServletResponse response,String filename) throws IOException {
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
