package tools;

import java.util.Vector;

public class LockOnQuery {

  static Vector<String> busyURLs = new Vector<String>();
  
  static public void lock(String query){
    synchronized(busyURLs){
      try {
        while(busyURLs.contains(query)){
          busyURLs.wait();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      busyURLs.add(query); 
    }
  }
  
  static public void release(String query){
    synchronized(busyURLs){
      busyURLs.remove(query);
      busyURLs.notifyAll();
    }
  }
}
