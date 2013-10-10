package ASyncRunner;

public abstract class ASyncRunner extends MyRunnable{
  
 
  public ASyncRunner() {
    if(isStarted()){
      fail("Already started ASyncRunner ");
      return;
    }
   
    
  }

  
  public abstract boolean hasFailed();
  
  public abstract void fail(String message);
  public abstract void execute();
  

  public void run() {
   
    try {
      execute();
    } catch (Exception e) {
      fail("Exception in ASyncRunner: "+e.getMessage());
    }
    isCompleted = true;
  }
}
