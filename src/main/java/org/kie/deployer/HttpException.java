package org.kie.deployer;

import java.io.IOException;

public class HttpException extends IOException {

  public HttpException(String message, Throwable t){
    super(message,t);
  }
  public HttpException(String message){
    super(message);
  }
  public HttpException(Throwable t){
    super(t);
  }
}
