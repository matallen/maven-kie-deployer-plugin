package org.jboss.kie.jbpm.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Deployable {
    private String groupId;
    private String artifactId;
    private String version;
    private String strategy;

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

    public Deployable(String groupId, String artifactId, String version, String strategy) {
        this.groupId=groupId;
        this.artifactId=artifactId;
        this.version=version;
        this.strategy=strategy;
    }
    public Deployable(){}

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
