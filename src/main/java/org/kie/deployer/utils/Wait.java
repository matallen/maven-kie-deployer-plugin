package org.kie.deployer.utils;

public class Wait{
  public static boolean For(int timeoutInSeconds, int intervalInSeconds, ToHappen toHappen) {
    long start=System.currentTimeMillis();
    long end=start+(timeoutInSeconds*1000);
    boolean timeout=false;
    while(!toHappen.hasHappened() && !timeout){
      try{
        Thread.sleep((intervalInSeconds*1000));
      }catch(InterruptedException ignor){}
      //System.out.println("[Wait] - waiting... ["+((end-System.currentTimeMillis())/1000)+"s]");
//      System.out.print(".");
      timeout=System.currentTimeMillis()>end;
      if (timeout) System.err.println("timed out waiting.");
    }
    System.out.println("");
    return !timeout;
  }
  public static boolean For(int timeoutInSeconds, ToHappen toHappen) {
    return For(timeoutInSeconds, 1, toHappen);
  }
}