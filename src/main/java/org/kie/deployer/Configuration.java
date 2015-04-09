package org.kie.deployer;

import org.kie.deployer.model.Deployable;
import org.kie.deployer.model.Model;


public interface Configuration {
  public String getServerUri();
  public String getUsername();
  public String getPassword();
  public Integer getTimeoutInSeconds();
  public boolean isImmediate();
  public boolean isDebug();
  public Deployable[] getDeployables();
  public Model[] getModels();
}
