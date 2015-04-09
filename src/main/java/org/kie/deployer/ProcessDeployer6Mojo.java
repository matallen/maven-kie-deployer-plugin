package org.kie.deployer;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.kie.deployer.model.Deployable;
import org.kie.deployer.model.GAV;
import org.kie.deployer.model.Model;
import org.kie.deployer.utils.Http;
import org.kie.deployer.utils.Http.Response;
import org.kie.deployer.utils.Jbpm6RestClient;
import org.kie.deployer.utils.ToHappen;
import org.kie.deployer.utils.Utils;
import org.kie.deployer.utils.Wait;


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
  /** @parameter property="models" @since 1.0.0 **/
  private Model[] models;
//  /** @parameter property="action" @since 1.0.0 **/
//  private String action="deploy";
  

  
  /**
   * Used to look up Artifacts in the remote repository.
   * @parameter expression=
   *  "${component.org.apache.maven.artifact.factory.ArtifactFactory}"
   * @required
   * @readonly
   */
  protected ArtifactFactory factory;
  /**
   * Used to look up Artifacts in the remote repository.
   * @parameter expression=
   *  "${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
   * @required
   * @readonly
   */
  protected ArtifactResolver artifactResolver;
  /**
   * List of Remote Repositories used by the resolver
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected List remoteRepositories;
  /**
   * Location of the local repository.
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected ArtifactRepository localRepository;
  
  public String getServerUri() { return serverUri; }
  public String getUsername() { return username; }
  public String getPassword() { return password; }
  public Integer getTimeoutInSeconds(){ return Integer.parseInt(timeoutInSeconds); }
  public boolean isDebug() { return "true".equalsIgnoreCase(debug); }
  public boolean isImmediate() { return "true".equalsIgnoreCase(immediate); }
  public Deployable[] getDeployables(){ return deployables; }
  public Model[] getModels(){ return models; }
  
  private Jbpm6RestClient client;
  
  public static ProcessDeployer6Mojo testInstance(String uri){
    ProcessDeployer6Mojo m=new ProcessDeployer6Mojo();
    m.debug="true";
    m.immediate="false";
    m.serverUri=uri;
    m.username="admin";
    m.password="admin";
    m.deployables=new Deployable[]{new Deployable("org.jboss.quickstarts.brms6","business-rules","6.0.0-SNAPSHOT","PER_PROCESS_INSTANCE")};
    m.models=new Model[]{new Model("commons-lang","commons-lang","2.6", "jar")};
    return m;
  }
  
  private void checkParameters(){
    boolean noDeployables=getDeployables()==null || getDeployables().length<=0;
    boolean noModels=getModels()==null || getModels().length<=0;
    if (noDeployables && noModels){
      throw new RuntimeException("Please provide a <deployables> and/or <models> section in the plugin configuration");
    }
    
    if (getDeployables()!=null){
      for(Deployable d:getDeployables()){
      	if (d.getStrategy()!=null && !d.getStrategy().matches("^(SINGLETON|PER_REQUEST|PER_PROCESS_INSTANCE)$"))
      		throw new RuntimeException("deployable strategy must be SINGLETON, PER_REQUEST or PER_PROCESS_INSTANCE. Found "+d.getStrategy()+" for "+d.getGroupId()+":"+d.getArtifactId()+"");
      }
    }
    
    if (null==client)
      client=new Jbpm6RestClient(getServerUri().endsWith("/")?getServerUri():getServerUri()+"/",username,password, debug!=null&&debug.equalsIgnoreCase("true"), getLog());
    if (isDebug()) getLog().debug(Utils.printPrivateVariables("*** DEBUG PARAMETERS *** ", this));
  }
  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    checkParameters();
System.out.println("debug = "+isDebug());
    if (!isImmediate()) {
      if (isDebug()) getLog().debug("polling mode enabled");
      Runnable runnable=new Runnable() {
        int count=0;
        @Override public void run() {
          if (isDebug()) getLog().debug("started thread to upload...");
          Wait.For(getTimeoutInSeconds(),5,new ToHappen() {
        	@Override public boolean hasHappened() {
            try {
//              int statusCode=RestAssured.given().auth().preemptive().basic(getUsername(),getPassword()).when().get(getServerUri()).getStatusCode();
              Response response=new Http(getServerUri()).username(getUsername()).password(getPassword()).get("");
              int statusCode=response.getStatusCode();
              if (statusCode!=200)
          		  throw new RuntimeException("Guvnor down... - returned HTTP "+statusCode);
          	  if (isDebug()) getLog().debug("Guvnor is up!");
              uploadDeployables();
              uploadModels();
              return true;
            } catch (Exception e) {
              if (count>1)
                getLog().debug("WARNING: "+e.getClass().getName()+":"+e.getMessage());
              count=count+1;
              return false;
            }
        	}});
        }
      };
      new Thread(runnable).start();
    } else {
      if (isDebug()) getLog().debug("immediate mode");
      uploadDeployables();
      uploadModels();
    }
  }

  
  public File resolve(String groupId, String artifactId, String version, String type) {
    try {
      Artifact pomArtifact = this.factory.createArtifact(groupId, artifactId, version, "", type);
      artifactResolver.resolve(pomArtifact, this.remoteRepositories, this.localRepository);
      
      if (isDebug()) getLog().debug("Resolved Artifact GAV to ["+pomArtifact.getFile()+"]");
      return pomArtifact.getFile();
      
    } catch (ArtifactResolutionException e) {
      getLog().error("can't resolve parent pom", e);
    } catch (ArtifactNotFoundException e) {
      getLog().error("can't resolve parent pom", e);
    } catch (Exception e){
      getLog().error(e.getMessage(), e);
    }
    throw new RuntimeException("What is this?");
}
  
  protected void uploadModels() throws MojoExecutionException, MojoFailureException {
    if (isDebug()) getLog().debug("attempting model deployment...");
    
    for(final Model m:getModels()){
      File file=resolve(m.getGroupId(), m.getArtifactId(), m.getVersion(), m.getType());
//      getLog().info("[Model] ["+m+"] Deploying...");
      client.deployModel(file, m);
      getLog().info("[Model] ["+m+"] Confirmed. Model has been deployed successfully on BPMS Server.");
    }
  }
  
  protected void uploadDeployables() throws MojoExecutionException, MojoFailureException {
    try {
      if (isDebug()) getLog().debug("attempting kjar deployment...");
      
      for(final Deployable d:getDeployables()){
        final GAV gav=new GAV(d.getGroupId(), d.getArtifactId(), d.getVersion());
        if (!client.deploymentExists(gav, d.getStrategy())){
      	  if (isDebug()) getLog().debug("[KJar]  ["+gav+"] doesnt exist on BPM Server. Deploying now...");
      	  String kBaseName=d.getkBaseName();
          String kSessionName=d.getkSessionName();
          client.deployKJar(gav, d.getStrategy(), kBaseName, kSessionName);
          getLog().info("[KJar]  ["+gav+"; strategy="+d.getStrategy()+"] deploy request made. Confirming deployment on BPMS Server...");
          boolean success=Wait.For(getTimeoutInSeconds(),1,new ToHappen() {
            @Override public boolean hasHappened() {
              try{
                return client.deploymentExists(gav, d.getStrategy());
              }catch(HttpException sink){}
              return false;
          }});
          
          if (success){
            getLog().info("[KJar]  ["+gav+"; strategy="+d.getStrategy()+"] Confirmed. GAV is deployed on BPM Server");
            resultStatus=0;
          }else{
            getLog().error("[KJar]  ["+gav+"; strategy="+d.getStrategy()+"] After deployment (and "+getTimeoutInSeconds()+"s timeout) GAV was NOT found on BPM Server!");
            resultStatus=1;
          }
        }else{
          getLog().info("[KJar]  ["+gav+"; strategy="+d.getStrategy()+"] Deployment already exists");
          resultStatus=2;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      resultStatus=3;
      throw new MojoExecutionException("Unable to upload rules", e);
    }
  }
  
  private int resultStatus=-1;
  public int getResultStatus(){
    return resultStatus;
  }

}
