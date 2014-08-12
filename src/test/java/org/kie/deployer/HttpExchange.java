package org.kie.deployer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HttpExchange {
  private InputStream is;
  private OutputStream os;
  private String payload;
  public HttpExchange(InputStream inputStream, OutputStream outputStream) {
    this.is=inputStream;
    this.os=outputStream;
    try{
      this.payload=Utils.asString(is);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  public String getMethod(){
    return payload.substring(0, payload.indexOf("\n")).split(" ")[0];
  }
  public String getUri(){
    return payload.substring(0, payload.indexOf("\n")).split(" ")[1];
  }
  public void replyWith(int statusCode, String string) throws IOException{
    String response=
    "HTTP/1.1 "+statusCode+" OK\n"+
    "Content-Type: text/xml; charset=utf-8\n"+
    "Content-Length: %s\n"+
    "\n"+
    ""+string;
    os.write(String.format(response, string.length()).getBytes());
    os.close();
  }
  public String asString() {
    return payload;
  }
}
