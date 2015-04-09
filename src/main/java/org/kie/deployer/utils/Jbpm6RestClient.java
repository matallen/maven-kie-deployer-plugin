package org.kie.deployer.utils;

//import static com.jayway.restassured.RestAssured.given;
//import com.jayway.restassured.response.Response;
import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.NUMBER;
import static javax.xml.xpath.XPathConstants.STRING;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.logging.Log;
import org.kie.deployer.HttpException;
import org.kie.deployer.model.DeploymentUnit;
import org.kie.deployer.model.GAV;
import org.kie.deployer.model.Model;
import org.kie.deployer.utils.Http.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Jbpm6RestClient {
  private String serverUri;
  private String username;
  private String password;
  private boolean debug;
  private Log log;
  private final Map<GAV, DeploymentUnit> deploymentsFound=new HashMap<GAV, DeploymentUnit>();
  
  public Jbpm6RestClient(String serverUri, String username, String password, boolean debug, Log log){
    this.log=log;
    this.serverUri=serverUri.endsWith("/")?serverUri:serverUri+"/";
    this.username=username;
    this.password=password;
    this.debug=debug;
  }
  
  public void deployKJar(GAV gav, String strategy, String kBaseName, String kSessionName) throws HttpException{
    actionKJar(gav, strategy, kBaseName, kSessionName, "deploy");
  }
  public void undeployKJar(GAV gav, String strategy, String kBaseName, String kSessionName) throws HttpException{
    actionKJar(gav, strategy, kBaseName, kSessionName, "undeploy");
  }
  
  private void actionKJar(GAV gav, String strategy, String kBaseName, String kSessionName, String action) throws HttpException{
    String uri="rest/deployment/"+gav.getGroupId()+":"+gav.getArtifactId()+":"+gav.getVersion()+(kBaseName!=null?":"+kBaseName:"")+(kSessionName!=null?":"+kSessionName:"")+"/"+action+"?strategy="+strategy;
    if (debug) log.debug("[KJar] "+action+" request  -> POST "+uri);
    
    Response response=new Http(serverUri).username(username).password(password).post(uri, null);
    String responseString=response.asString();
    if (debug) log.debug("[KJar] "+action+" response -> "+responseString);
    
    if (response.getStatusCode()!=202)
      throw new HttpException("[KJar] ERROR: Failed to POST to "+uri+" - http status code = "+ response.getStatusCode() +"; response content:\n "+ responseString);
  }
  
  public boolean deploymentExists(GAV gav, String strategy) throws HttpException{
    return null!=getDeployment(gav, strategy);
  }
  
  /** returns null if not found */
  public DeploymentUnit getDeployment(GAV gav, String strategy) throws HttpException{
    if (deploymentsFound.containsKey(gav)) return deploymentsFound.get(gav);
    for(DeploymentUnit d:getDeployments()){
      if (new GAV(d.getGroupId(), d.getArtifactId(), d.getVersion()).equals(gav) && strategy.equals(d.getStrategy())){
        deploymentsFound.put(gav,d);
        return d;
      }
    }
    return null;
  }
  public List<DeploymentUnit> getDeployments() throws HttpException {
    try {
      String uri="rest/deployment";
      if (debug) log.debug("[KJar] getDeployments -> GET "+uri);
      
      Response response=new Http(serverUri).username(username).password(password).get(uri);
      
      if (response.getStatusCode()!=200 && response.getStatusCode()!=202)
        throw new HttpException("Failed to GET to "+uri+" - http status code = "+ response.getStatusCode() +"; response content = "+ response.asString());

      String responseString=response.asString();
      if (debug) log.debug("[KJar] getDeployments() response = "+responseString);
      Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(responseString.getBytes()));
      XPath xpath=XPathFactory.newInstance().newXPath();
      List<DeploymentUnit> result=new ArrayList<DeploymentUnit>();
      Double count=(Double) xpath.evaluate("count(/deployment-unit-list/deployment-unit)",doc,NUMBER);
      for (int i=1; i <= count; i++) {
        Node node=(Node) xpath.evaluate("/deployment-unit-list/deployment-unit[" + i + "]",doc,NODE);
        String groupId=(String) xpath.evaluate("groupId",node,STRING);
        String artifactId=(String) xpath.evaluate("artifactId",node,STRING);
        String version=(String) xpath.evaluate("version",node,STRING);
        String strategy=(String) xpath.evaluate("strategy",node,STRING);
        String status=(String) xpath.evaluate("status",node,STRING);
        result.add(new DeploymentUnit(groupId,artifactId,version,strategy,status));
      }
      return result;
    }catch(Exception e){
      throw new HttpException(e.getMessage(), e);
    }
  }

  public void deployModel(File file, Model m) {
    if (debug) log.debug("[Model] "+file.getName()+" :: Deploying...");
    String resource="/maven2/"+m.getGroupId().replace('.', '/')+"/"+m.getArtifactId()+"/"+m.getArtifactId()+"-"+m.getVersion()+"."+m.getType();
    Response response=new Http(serverUri).username(username).password(password).post(resource, file);
    if (debug) log.debug("[Model] "+file.getName()+" :: Done (statusCode == "+response.getStatusCode()+")");
  }

}
