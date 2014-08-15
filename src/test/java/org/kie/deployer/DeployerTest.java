package org.kie.deployer;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DeployerTest {
  private final int port=getAvailablePortStartingAt(System.getProperty("port")!=null?Integer.parseInt(System.getProperty("port")):16080);
  private static final String deployJobSubmittedReply="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><deployment-job-result><operation>DEPLOY</operation><deploymentUnit><groupId>org.jboss.quickstarts.brms6</groupId><artifactId>business-rules</artifactId><version>6.0.0-SNAPSHOT</version><strategy>PER_PROCESS_INSTANCE</strategy><status>DEPLOYING</status></deploymentUnit><success>true</success><explanation>Deployment (deploy) job submitted successfully.</explanation></deployment-job-result>";
  private static final String emptyDeploymentsReply="<deployment-unit-list></deployment-unit-list>";
  private static final String deployedDeploymentsReply=
  "<deployment-unit-list>                             \n"+
  "  <deployment-unit>                                \n"+
  "    <groupId>org.jboss.quickstarts.brms6</groupId> \n"+
  "    <artifactId>business-rules</artifactId>        \n"+
  "    <version>6.0.0-SNAPSHOT</version>              \n"+
  "    <strategy>PER_PROCESS_INSTANCE</strategy>      \n"+
  "    <status>deployed</status>                      \n"+
  "  </deployment-unit>                               \n"+
  "</deployment-unit-list>                              ";
  private static String deploymentsReply=emptyDeploymentsReply;
  
  
  boolean isAliveReply=false;
  int deploymentsReplyCount=0;
  boolean deploymentSubmittedReply=false;
  
  private int getAvailablePortStartingAt(int start){
    System.out.println("Checking for available ports");
    boolean portTaken=true;
    int port=start;
    while (portTaken){
      try {
        ServerSocket serverSocket=new ServerSocket(port++);
        serverSocket.close();
        System.out.println("Port available: "+(port-1));
        portTaken=false;
      } catch (IOException e) {
        System.out.println("Port already bound: "+(port-1));
        portTaken=true;
      } catch (Exception e) {
        System.out.println("XXXXX");
      }
    }
    return port-1;
  }
  
  @Before
  public void reset(){
    isAliveReply=false;
    deploymentsReplyCount=0;
    deploymentSubmittedReply=false;
  }
  
  @Test
  public void test() throws Exception {
    HttpServer server=HttpServer.create(port);
    server.setHttpHandler(new HttpHandler() {
      @Override public void handle(HttpExchange httpExchange) {
        try{
          String request=httpExchange.asString();
          System.err.println("REQUEST:\n"+request);
          
          if (httpExchange.getMethod().equals("GET") && httpExchange.getUri().matches("^/business-central/$")){
            httpExchange.replyWith(200, "anything");
            isAliveReply=true;
          }else if (httpExchange.getMethod().equals("GET") && httpExchange.getUri().contains("rest/deployment")){
            httpExchange.replyWith(200, deploymentsReply);
            deploymentsReplyCount=deploymentsReplyCount+1;
          }else if (httpExchange.getMethod().equals("POST") &&httpExchange.getUri().contains("rest/deployment/org.jboss.quickstarts.brms6:business-rules:6.0.0-SNAPSHOT/deploy?strategy=PER_PROCESS_INSTANCE")){
            httpExchange.replyWith(202, deployJobSubmittedReply);
            deploymentsReply=deployedDeploymentsReply;
            deploymentSubmittedReply=true;
          }
          
        }catch(Exception e){
          e.printStackTrace();
        }
    }});
    server.start();
    
    ProcessDeployer6Mojo mojo=ProcessDeployer6Mojo.testInstance("http://127.0.0.1:"+port+"/business-central/");
    mojo.execute();
    
    // deployer is asynchronous so we have to wait for it to do its work
    long timeout=System.currentTimeMillis()+(System.getProperty("timeout")!=null?Long.parseLong(System.getProperty("timeout")):10000);
    while(mojo.getResultStatus()<0 && System.currentTimeMillis()<timeout){
      Thread.sleep(1000l);
    }
    
    Assert.assertEquals(0, mojo.getResultStatus());
    Assert.assertTrue(isAliveReply);
    Assert.assertTrue(deploymentsReplyCount>=2);
    Assert.assertTrue(deploymentSubmittedReply);
    
    server.shutdown();
  }

}
