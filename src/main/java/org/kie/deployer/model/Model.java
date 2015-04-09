package org.kie.deployer.model;

public class Model extends GAV{
  protected String type="jar";
  
  public Model(String groupId, String artifactId, String version, String type) {
    super(groupId, artifactId, version);
    this.type=type;
  }
  
  public Model(String groupId, String artifactId, String version) {
    super(groupId, artifactId, version);
  }
  
  public Model(){
    super("","","");
  }
  
  public String getType(){
    return type;
  }
  
  public String toString(){
    return super.toString()+":"+type;
  }
}
