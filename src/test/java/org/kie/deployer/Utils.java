package org.kie.deployer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {


  public static byte[] readFully(InputStream is) throws IOException {
    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    byte[] buffer=new byte[1024];
    int length=0;
    while ((length=is.read(buffer))!=-1) {
      System.out.println("BUF = \n"+new String(buffer));
      baos.write(buffer, 0, length);
    }
    return baos.toByteArray();
  }
  public static String asString(InputStream is) throws IOException {
    String line;
    StringBuffer sb=new StringBuffer();
    BufferedReader reader=new BufferedReader(new InputStreamReader(is));
    while (!((line=reader.readLine()).equals(""))){
      sb.append(line).append("\n");
    }
//    while (!((line=reader.readLine()).equals(""))){
//      sb.append(line).append("\n");
//    }
//    is.close();
    return sb.toString();
    
//    return IOUtils.toString(is);
//    return new String(readFully(is));
  }
}
