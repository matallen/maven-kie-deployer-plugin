package org.jboss.kie.jbpm;

import org.apache.http.HttpException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jboss.kie.jbpm.model.Deployable;
import org.jboss.kie.jbpm.model.GAV;
import org.jboss.kie.jbpm.utils.Jbpm6RestClient;
import org.jboss.kie.jbpm.utils.ToHappen;
import org.jboss.kie.jbpm.utils.Utils;
import org.jboss.kie.jbpm.utils.Wait;
import com.jayway.restassured.RestAssured;

/**
 * @goal deploy
 */
public class ProcessDeployer6Mojo extends AbstractMojo implements Configuration {
  /** @parameter property="serverUri" default-value="http://localhost:8080/business-central" @since 1.0.0 **/
  private String serverUri = "http://localhost:8080/business-central";
  /** @parameter property="username" default-value="admin" @since 1.0.0 **/
  private String username = "admin";
  /** @parameter property="password" default-value="admin" @since 1.0.0 **/
  private String password = "admin";
  /** @parameter property="debug" @since 1.0.0 **/
  private String debug="false";
  /** @parameter property="immediate" @since 1.0.0 **/
  private String immediate="false";
  /** @parameter property="timeoutInSeconds" @since 1.0.0 **/
  private String timeoutInSeconds="60";
  /** @parameter property="deployables" @since 1.0.0 **/
  private Deployable[] deployables;
//  /** @parameter property="action" @since 1.0.0 **/
//  private String action="deploy";
  
  public String getServerUri() { return serverUri; }
  public String getUsername() { return username; }
  public String getPassword() { return password; }
  public Integer getTimeoutInSeconds(){ return Integer.parseInt(timeoutInSeconds); }
  public boolean isDebug() { return "true".equalsIgnoreCase(debug); }
  public boolean isImmediate() { return "true".equalsIgnoreCase(immediate); }
  public Deployable[] getDeployables(){ return deployables; }
  
  private Jbpm6RestClient client;
  
  public static void main(String[] asd) throws Exception{
    ProcessDeployer6Mojo m=new ProcessDeployer6Mojo();
    m.debug="true";
    m.immediate="false";
    m.serverUri="http://localhost:8080/business-central/";
    m.username="admin";
    m.password="admin";
    //m.deployables=new Deployable[]{new Deployable("dl-customer-order-service","dl-customer-order-service","1.1","PER_PROCESS_INSTANCE")};
    m.deployables=new Deployable[]{new Deployable("org.jboss.quickstarts.brms6","business-rules","6.0.0-SNAPSHOT","PER_PROCESS_INSTANCE")};
//    m.action="deploy";
    m.execute();
  }
  
  private void checkParameters(){
    if (getDeployables()==null) throw new RuntimeException("Please provide <deployables> section in the plugin configuration");
    for(Deployable d:getDeployables()){
    	if (d.getStrategy()!=null && !d.getStrategy().matches("^(SINGLETON|PER_REQUEST|PER_PROCESS_INSTANCE)$"))
    		throw new RuntimeException("deployable strategy must be SINGLETON, PER_REQUEST or PER_PROCESS_INSTANCE. Found "+d.getStrategy()+" for "+d.getGroupId()+":"+d.getArtifactId()+"");
    }
    if (null==client) client=new Jbpm6RestClient(getServerUri().endsWith("/")?getServerUri():getServerUri()+"/",username,password);
    
    if (isDebug()) System.out.println(Utils.printPrivateVariables("*** DEBUG PARAMETERS *** ", this));
  }
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    checkParameters();

    if (!isImmediate()) {
      if (isDebug()) System.out.println("[KIE-DEPLOYER]    polling mode enabled");
      Runnable runnable=new Runnable() {
        @Override public void run() {
          if (isDebug()) System.out.println("[KIE-DEPLOYER]    started thread to upload...");
          Wait.For(getTimeoutInSeconds(),5,new ToHappen() {
        	@Override public boolean hasHappened() {
            try {
              int statusCode=RestAssured.given().auth().preemptive().basic(getUsername(),getPassword()).when().get(getServerUri()).getStatusCode();
          	  if (statusCode!=200)
          		  throw new RuntimeException("[KIE-DEPLOYER]    Guvnor down... - returned HTTP "+statusCode);
          	  if (isDebug()) System.out.println("[KIE-DEPLOYER]    Guvnor is up!");
              uploadKJar();
              return true;
            } catch (Exception e) {
//              if (isDebug()) System.out.print(".");
              System.out.println("[KIE-DEPLOYER] WARNING: "+e.getMessage());
              return false;
            }
        	}});
        }
      };
      new Thread(runnable).start();
    } else {
      if (isDebug()) System.out.println("immediate mode");
      uploadKJar();
    }
  }

  protected void uploadKJar() throws MojoExecutionException, MojoFailureException {
    try {
      if (isDebug()) System.out.println("[KIE-DEPLOYER]    attempting deployment...");
      
      for(final Deployable d:getDeployables()){
        final GAV gav=new GAV(d.getGroupId(), d.getArtifactId(), d.getVersion());
        if (!client.deploymentExists(gav, d.getStrategy())){
      	if (isDebug()) System.out.println("[KIE-DEPLOYER]    Deployment doesnt exist on BPM Server. Deploying GAV now ["+gav+"]");
      	  String kBaseName=d.getkBaseName();
          String kSessionName=d.getkSessionName();
          client.actionKJar(gav, d.getStrategy(), kBaseName, kSessionName, "deploy");
          System.out.print("[KIE-DEPLOYER]    checkingForDeployment:");
          boolean success=Wait.For(30,1,new ToHappen() {
            @Override public boolean hasHappened() {
              try{
                return client.deploymentExists(gav, d.getStrategy());
              }catch(HttpException sink){}
              return false;
          }});
          System.out.println();
          
          if (success){
            if (isDebug()) System.out.println("[KIE-DEPLOYER]    Confirmed GAV is deployed on BPM Server ["+gav+"; strategy="+d.getStrategy()+"]");
          }else
            if (isDebug()) System.err.println("[KIE-DEPLOYER]    After deployment GAV was NOT found on BPM Server! ["+gav+"; strategy="+d.getStrategy()+"]");
        }else
          if (isDebug()) System.out.println("[KIE-DEPLOYER]    Deployment already exists ["+gav+"; strategy="+d.getStrategy()+"]");
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoExecutionException("[KIE-DEPLOYER] Unable to upload rules", e);
    }
  }

}
