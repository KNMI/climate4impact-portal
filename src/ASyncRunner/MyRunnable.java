package ASyncRunner;

public abstract class MyRunnable implements Runnable {
  protected boolean isStarted = false;
  protected boolean isCompleted = false;
  
  public boolean isStarted(){
    return isStarted;     
  }
  public boolean isCompleted(){
    return isCompleted;     
  }
  
  public synchronized void start() throws Exception{
    if(isStarted){
      throw new Exception("Thread is already started");
    }
    isStarted=true;
    Thread t = new Thread(this);
    t.start();
  }
}
