package ASyncRunner;

import java.util.Calendar;
import java.util.Vector;

import tools.Debug;

public class MyRunnableWaiter {
  private long startTimeInMillis;
  private String errorMessage = ""; 
  int maxThreadsRunning = 3;
  public void setMaxThreads(int numThreads){
    maxThreadsRunning=numThreads;
  }
  class JobPoller implements Runnable{
   
    private Vector<ASyncRunner> threads = new Vector<ASyncRunner>();
    private volatile boolean allCompleted=false;
  
    public void run() {
     
     
     
      try{
        boolean checkAllCompleted=true;
        //Determine number of running threads
        do{
          checkAllCompleted=true;
          int numThreadsRunning = 0;
          for(int j=0;j<threads.size();j++){
            if(!threads.get(j).isCompleted()){
              checkAllCompleted=false;
              if(threads.get(j).isStarted()){
                numThreadsRunning++;
               
              }else{
                if(numThreadsRunning<maxThreadsRunning){
                  threads.get(j).start();
                  numThreadsRunning++;
                }
              }
            }
          }
          Thread.sleep(10);
        }while(checkAllCompleted==false);
        allCompleted=true;
      }catch(Exception e){
        destroy();
      }
    }

    public void monitor(Vector<ASyncRunner> threads) {
      this.threads=threads;
    }
    public void add(ASyncRunner t) {
      threads.add(t);   

    }
    /**
     * 
     * @return 0 on success.
     */
    public int waitForCompletion(){
      try {
        while(allCompleted==false){
          Thread.sleep(10);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      int success = 0;
      for(int j=0;j<jobPoller.threads.size();j++){
        if(jobPoller.threads.get(j).hasFailed()){
          success=1;
          errorMessage+=jobPoller.threads.get(j).getErrorMessage()+"\n";
        }
      }
      return success;
    }
    public void destroy(){
      for(int j=0;j<jobPoller.threads.size();j++){
        jobPoller.threads.set(j, null);
      }
      jobPoller.threads.clear();
      jobPoller=null;
      jobPollerThread =null;
    }

    public MyRunnable get(int i) {
      return threads.get(i);
    }   
  }
  JobPoller jobPoller = new JobPoller();
  Thread jobPollerThread = null;

  

  public int waitForCompletion(){
    int retCode = jobPoller.waitForCompletion();
    Debug.println("Took "+(Calendar.getInstance().getTimeInMillis()-startTimeInMillis)+" ms");
    return retCode;
  }
    
  public void add(ASyncRunner t) {
    jobPoller.add(t);
    if(jobPollerThread == null){
      startTimeInMillis = Calendar.getInstance().getTimeInMillis();
      jobPollerThread = new Thread(jobPoller);
      jobPollerThread.start();
    }
  }

  public MyRunnable get(int i) {
    return jobPoller.get(i);
  }
  
  public void destroy(){
    jobPoller.destroy();
  }

  public String getErrorMessage() {
    return errorMessage;
  }   
}
