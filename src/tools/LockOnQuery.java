package tools;

import java.util.Vector;

public class LockOnQuery {

  static Vector<String> busyURLs = new Vector<String>();
  
  /**
   * Locks on any query
   * @param query The query string to wait for
   * @param waitMillis If zero, the function blocks until the query has been released by another thread. If nonzero, the amount of millis is waited and then returns.
   * @return Zero when lock is released, One when lock is still there but timeout has expired.
   */
  static public int lock(String query,int waitMillis){
    if(query == null){
      return 2;
    }
    //Debug.println("lock "+query);
    synchronized(busyURLs){
      try {
        if(waitMillis <=0 ){
          while(busyURLs.contains(query)){
            busyURLs.wait();
          }
        }else{
          if(busyURLs.contains(query)){
            busyURLs.wait(waitMillis);
            Debug.println( "lock timeout on "+query);
            return 1;
          }
        }

      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      busyURLs.add(query); 
    }
    //Debug.println("unlock");
    return 0;
  }
  
  static public void release(String query){
    synchronized(busyURLs){
      busyURLs.remove(query);
      busyURLs.notifyAll();
    }
  }
}
