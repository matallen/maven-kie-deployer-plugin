package org.kie.deployer;

import org.kie.deployer.model.Deployable;


public interface Configuration {
  public String getServerUri();
  public String getUsername();
  public String getPassword();
  public Integer getTimeoutInSeconds();
  public boolean isImmediate();
  public boolean isDebug();
  public Deployable[] getDeployables();
}
