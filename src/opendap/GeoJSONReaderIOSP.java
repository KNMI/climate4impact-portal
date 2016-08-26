package opendap;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.text.ParseException;
import java.util.List;

import tools.Debug;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.ma2.StructureDataIterator;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.ParsedSectionSpec;
import ucar.nc2.Structure;
import ucar.nc2.Variable;
import ucar.nc2.iosp.IOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

/**
 * GeoJSONReaderIOSP
 * 
 * This class provides an IOServiceProvider for GeoJSON.
 * It reads the GeoJSON into a CDM object, in a similar way as done in the ADAGUCServer. 
 * The ADAGUCServer is able to visualize GeoJSON over OpenDAP by serving GeoJSON with this IOSP.
 * 
 * The ADAGUCServer IOSP in C++ is here: https://dev.knmi.nl/projects/adagucserver/repository/entry/CCDFDataModel/CCDFGeoJSONIO.cpp
 *
 * @author Maarten Plieger, KNMI, 2016-08
 */
public class GeoJSONReaderIOSP implements IOServiceProvider {


  /**
   * Is this a geojson? Check it by reading the first character, should be a { token.
   */
  public boolean isValidFile(RandomAccessFile raf) throws IOException {
    raf.seek(0);
    byte[] b = new byte[1];
    raf.read(b);
    String got = new String(b);
    boolean isGeoJSON =  got.equals("{");
    if(isGeoJSON){
      Debug.println("This is GeoJson");
    }
    return isGeoJSON;
  }

  private ArrayChar.D1 jsoncontentArray;

  public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {
    int n;

    try {
      n = readAllData(raf);
    } catch (ParseException e) {
      e.printStackTrace();
      throw new IOException("bad data");
    }
    raf.close();

    Dimension jsonDim = new Dimension("jsoncontent", n, true);
    ncfile.addDimension(null, jsonDim);

    Variable jsonVar = new Variable(ncfile, null, null, "jsoncontent");
    jsonVar.setDimensions("jsoncontent");
    jsonVar.setDataType(DataType.CHAR);

    ncfile.addAttribute(null,new Attribute("ADAGUC_GEOJSON", ""));
    jsonVar.addAttribute(new Attribute("ADAGUC_BASENAME", "geojson.geojson"));
    ncfile.addVariable(null, jsonVar);
    ncfile.addAttribute(null, new Attribute("Conventions", "CF-1.6"));
    ncfile.addAttribute(null, new Attribute("history", "Provided by GeoJSONReader iosp"));
    ncfile.finish();
  }

  int readAllData(RandomAccessFile raf) throws IOException, NumberFormatException, ParseException {
    Debug.println("Read all");

    java.text.SimpleDateFormat isoDateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    isoDateTimeFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

    raf.seek(0);
    int dimensionSize = (int) raf.length()+1;
    byte[] document = new byte[(int) raf.length()];
    raf.readFully(document);

    int[] shape = new int[]{dimensionSize};
    jsoncontentArray = (ArrayChar.D1) Array.factory(DataType.CHAR, shape);


    for (int i = 0; i < dimensionSize-1; i++) {
      jsoncontentArray.setChar(i, (char) document[i]);
    }

    jsoncontentArray.setChar(dimensionSize-1, (char) 0);
    return dimensionSize;
  }

  @Override
  public Array readData(Variable arg0, Section arg1) throws IOException,
  InvalidRangeException {
    Debug.println("readData: "+ arg1.getOrigin(0)+ " " +arg1.getShape(0));
    return jsoncontentArray.section(arg1.getOrigin(), arg1.getShape(),arg1.getStride());
  }

  public Array readData(Variable v2, List<?> section) throws IOException, InvalidRangeException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public Array readNestedData(Variable v2, List<?> section) throws IOException, InvalidRangeException {
    return null;
  }

  public void close() throws IOException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean syncExtend() throws IOException {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean sync() throws IOException {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void setSpecial(Object special) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public String toStringDebug(Object o) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getDetailInfo() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getFileTypeDescription() {
    return null;
  }

  @Override
  public String getFileTypeId() {
    return null;
  }

  @Override
  public String getFileTypeVersion() {
    return null;
  }

  @Override
  public StructureDataIterator getStructureIterator(Structure arg0, int arg1)
      throws IOException {
    return null;
  }

  @Override
  public void reacquire() throws IOException {
  }


  @Override
  public Array readSection(ParsedSectionSpec arg0) throws IOException,
  InvalidRangeException {
    return null;
  }

  @Override
  public long readToByteChannel(Variable arg0, Section arg1,
      WritableByteChannel arg2) throws IOException, InvalidRangeException {
    return 0;
  }

  @Override
  public long readToOutputStream(Variable arg0, Section arg1, OutputStream arg2)
      throws IOException, InvalidRangeException {
    return 0;
  }

  @Override
  public void release() throws IOException {

  }

  @Override
  public Object sendIospMessage(Object arg0) {
    return null;
  }

  @Override
  public long streamToByteChannel(Variable arg0, Section arg1,
      WritableByteChannel arg2) throws IOException, InvalidRangeException {
    return 0;
  }


  public static void main(String args[]) throws IOException, IllegalAccessException, InstantiationException {
    NetcdfFile.registerIOProvider(GeoJSONReaderIOSP.class);
    NetcdfFile ncfile = NetcdfFile.open("/home/c4m/impactspace/ceda.ac.uk.openid.Maarten.Plieger/data//NUTS_2010_L0.geojson");
    System.out.println("ncfile = \n" + ncfile);
  }


}
