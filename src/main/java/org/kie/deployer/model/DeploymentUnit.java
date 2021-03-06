package org.kie.deployer.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DeploymentUnit {
  private String groupId;
  private String artifactId;
  private String version;
  private String strategy;
  private String status;
  
  public DeploymentUnit(String groupId, String artifactId, String version, String strategy, String status) {
    super();
    this.groupId=groupId;
    this.artifactId=artifactId;
    this.version=version;
    this.strategy=strategy;
    this.status=status;
  }
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this,new String[] {});
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this,obj,new String[] {});
  }
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
  public String getStatus() {
    return status;
  }
  @Override
  public String toString() {
    return "DeploymentUnit [groupId="+groupId+", artifactId="+artifactId+", version="+version+", strategy="+strategy+", status="+status+"]";
  }
  
}
