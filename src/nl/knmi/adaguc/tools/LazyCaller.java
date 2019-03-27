package nl.knmi.adaguc.tools;

import java.util.Vector;



public class LazyCaller {
  private class LazyObject{
    String name = null;
    long age = 0;
  }
  static private  Vector<LazyObject> lazyObjects = new Vector<LazyObject>();
  
  public boolean isCallable(String id,int timeOutMs){
    long currentAge = DateFunctions.getCurrentDateInMillis();
    LazyObject fileObj = getLazyObject(id);
    if(fileObj!=null){
      if(fileObj.age!=0&&fileObj.age+timeOutMs>currentAge){
        return false;
      }
    }else{
      fileObj = new LazyObject();
      lazyObjects.add(fileObj);
      fileObj.name = id;
    }
    fileObj.age= currentAge;
    return true;
  };
  
  static public void markDirty(String id){
    LazyObject fileObj = getLazyObject(id);
    if(fileObj!=null){
      fileObj.age=0;      
    }
  }
  
  static private LazyObject getLazyObject(String file){
    for(int j=0;j<lazyObjects.size();j++){
      if(lazyObjects.get(j).name.equals(file)){
        return lazyObjects.get(j);
      }
    }
    return null;
  }

  static LazyCaller lazyFileReaderWriter = null;
  public static synchronized LazyCaller getInstance() {
    if(lazyFileReaderWriter == null){
      Debug.println("Creating new instance");
      lazyFileReaderWriter = new LazyCaller();
    }
    return lazyFileReaderWriter;
  }  
  
    
  
  
}
