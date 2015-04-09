package org.kie.deployer.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

public class Http {
  private String serverUri;
  private String username;
  private String password;
  
  public Http(String serverUri){
    this.serverUri=serverUri;
  }
  public Http username(String username){
    this.username=username; return this;
  }
  public Http password(String password){
    this.password=password; return this;
  }
  
  public class Response{
    public int responseCode;
    public String content;
    public Response(int responseCode, String content){
      this.responseCode=responseCode;
      this.content=content;
    }
    public String asString(){
      return content;
    }
    public int getStatusCode(){
      return responseCode;
    }
  }
  
  
  private static String toString(java.io.InputStream is) {
    if (is==null) return "";
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
  
  public Response get(String url) throws IOException{
    HttpURLConnection cnn=(HttpURLConnection) new URL(serverUri+url).openConnection();
    cnn.setDoOutput(false);
    cnn.setRequestMethod("GET");
    if (username!=null && password!=null){
      cnn.setRequestProperty("Authorization", "Basic "+Base64.encodeBase64String((username+":"+password).getBytes()));
    }
    return new Response(cnn.getResponseCode(), toString(cnn.getInputStream()));
  }
  
  public Response post(String uri, Object obj) {
    try{
      HttpURLConnection connection=(HttpURLConnection) new URL(serverUri+uri).openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      if (username!=null && password!=null){
        connection.setRequestProperty("Authorization", "Basic "+Base64.encodeBase64String((username+":"+password).getBytes()));
      }
      MultipartEntity multipartEntity=new MultipartEntity(HttpMultipartMode.STRICT);
      
      if (null!=obj){
        if (obj instanceof File){
          multipartEntity.addPart("file", new FileBody((File)obj));
//          System.out.println("[Model] "+((File)obj).getName()+" :: Http POST to -> "+serverUri+uri);
        }else if (obj instanceof String){
          multipartEntity.addPart("file", new StringBody((String)obj));
        }
      }
      connection.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
      OutputStream out=connection.getOutputStream();
      try {
        multipartEntity.writeTo(out);
      } finally {
        out.close();
      }
      return new Response(connection.getResponseCode(), toString(connection.getInputStream()));
    }catch(IOException e){
      e.printStackTrace();
      return null;
    }
  }
}
