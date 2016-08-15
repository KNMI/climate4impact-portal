package tools;

import java.io.IOException;
import java.util.Vector;



public class LazyCaller {
  private class LazyObject{
    String name = null;
    long age = 0;
    String data;
  }
  private static Vector<LazyObject> lazyObjects = new Vector<LazyObject>();
  
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
  
  public void markDirty(String id){
    LazyObject fileObj = getLazyObject(id);
    if(fileObj!=null){
      fileObj.age=0;      
    }
  }
  
  private static LazyObject getLazyObject(String file){
    for(int j=0;j<lazyObjects.size();j++){
      if(lazyObjects.get(j).name.equals(file)){
        return lazyObjects.get(j);
      }
    }
    return null;
  }
  
  public synchronized String readFile(String file) throws IOException{
    long currentAge =DateFunctions.getCurrentDateInMillis();
    LazyObject fileObj = getLazyObject(file);
    if(fileObj!=null){
      if(fileObj.age+1000>currentAge){
        return fileObj.data; 
      }
    }else{
      fileObj = new LazyObject();
      lazyObjects.add(fileObj);
      fileObj.name = file;
    }
    fileObj.age= currentAge;
    Debug.println("LazyRead needs update: "+file);
    
    fileObj.data = tools.Tools.readFile(fileObj.name);
    return fileObj.data;
  }
  
  public synchronized void writeFile(String file,String data) throws IOException{
    long currentAge =DateFunctions.getCurrentDateInMillis();
    LazyObject fileObj = getLazyObject(file);
    if(fileObj!=null){
//      if(fileObj.age+1000>currentAge){
//        fileObj.data = data;
//        return;
//      }
    }else{
      fileObj = new LazyObject();
      lazyObjects.add(fileObj);
      fileObj.name = file;
    }
    fileObj.age= currentAge;
    fileObj.data = data;
    //Debug.println("LazyWrite: writing data "+file);
    tools.Tools.writeFile(file,fileObj.data);
  }

  static LazyCaller lazyFileReaderWriter = null;
  public static LazyCaller getInstance() {
    if(lazyFileReaderWriter == null){
      Debug.println("Creating new instance");
      lazyFileReaderWriter = new LazyCaller();
    }
    return lazyFileReaderWriter;
  }  
  
    
  
  
}
