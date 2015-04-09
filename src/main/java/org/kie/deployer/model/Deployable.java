package org.kie.deployer.model;

public class Deployable {
    protected String groupId;
    protected String artifactId;
    protected String version;
    protected String strategy;
    protected String kBaseName;
    protected String kSessionName;

    public String getGroupId() {
        return groupId;
    }
    public String getArtifactId() {
        return artifactId;
    }
    public String getVersion() {
        return version;
    }
    public String getStrategy() {
        return strategy;
    }
    public String getkBaseName() {
      return kBaseName;
    }
    public String getkSessionName() {
      return kSessionName;
    }
    public Deployable(String groupId, String artifactId, String version, String strategy) {
        this.groupId=groupId;
        this.artifactId=artifactId;
        this.version=version;
        this.strategy=strategy;
    }
    public Deployable(){}
    
    @Override
    public String toString() {
//      return groupId+":"+artifactId+":"+version+":"+strategy+":"+kBaseName+":"kSessionName;
      return "Deployable [groupId="+groupId+", artifactId="+artifactId+", version="+version+", strategy="+strategy+", kBaseName="+kBaseName+", kSessionName="
          +kSessionName+"]";
    }
}
