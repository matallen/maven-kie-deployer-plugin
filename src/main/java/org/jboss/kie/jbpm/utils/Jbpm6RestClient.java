package org.jboss.kie.jbpm.utils;

import static com.jayway.restassured.RestAssured.given;
import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.NUMBER;
import static javax.xml.xpath.XPathConstants.STRING;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpException;
import org.jboss.kie.jbpm.model.DeploymentUnit;
import org.jboss.kie.jbpm.model.GAV;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.jayway.restassured.response.Response;

public class Jbpm6RestClient {
  private String serverUri;
  private String username;
  private String password;
  private final Map<GAV, DeploymentUnit> deploymentsFound=new HashMap<GAV, DeploymentUnit>();
  
  public Jbpm6RestClient(String serverUri, String username, String password){
    this.serverUri=serverUri;
    this.username=username;
    this.password=password;
  }
  
  public void actionKJar(GAV gav, String strategy, String kBaseName, String kSessionName, String action) throws HttpException{
    String url=serverUri+"rest/deployment/"+gav.getGroupId()+":"+gav.getArtifactId()+":"+gav.getVersion()+(kBaseName!=null?":"+kBaseName:"")+(kSessionName!=null?":"+kSessionName:"")+"/"+action+"?strategy="+strategy;
    System.out.println("[KIE-DEPLOYER]    "+action+" request  -> POST "+url);
    Response response=given().redirects().follow(true).auth().preemptive().basic(username,password).when().post(url);
    String responseString=response.asString();
    System.out.println("[KIE-DEPLOYER]    "+action+" response -> "+responseString);
    
    if (response.getStatusCode()!=202)
      throw new HttpException("[KIE-DEPLOYER]    ERROR: Failed to POST to "+url+" - http status line = "+ response.getStatusLine() +"; response content:\n "+ responseString);
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
      String url=serverUri+"rest/deployment";
      
//      System.out.println("[KIE-DEPLOYER]    getDeployments -> GET "+url);
      System.out.print(".");
      
      Response response=given().redirects().follow(true).auth().preemptive().basic(username,password).when().get(url);
      if (response.getStatusCode()!=200 && response.getStatusCode()!=202)
        throw new HttpException("Failed to GET to "+url+" - http status line = "+ response.getStatusLine() +"; response content = "+ response.asString());

      String responseString=response.asString();
//      System.out.println("[KIE-DEPLOYER]    getDeployments() response = "+responseString);
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


}
