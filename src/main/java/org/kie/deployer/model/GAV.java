package org.kie.deployer.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class GAV {
  protected String groupId;
  protected String artifactId;
  protected String version;
  
  public String toString(){
    return groupId+":"+artifactId+":"+version;
//    return ReflectionToStringBuilder.toString(this);
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this,new String[] {});
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this,obj,new String[] {});
  }

  public GAV(String groupId, String artifactId, String version) {
    super();
    this.groupId=groupId;
    this.artifactId=artifactId;
    this.version=version;
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
}
