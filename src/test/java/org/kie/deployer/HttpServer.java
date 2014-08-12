package org.kie.deployer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {
  private ExecutorService executors = Executors.newFixedThreadPool(1);
  private Thread acceptorThread;
  private HttpHandler httpHandler;
  private HttpClient client;
  private int port;
  
  public static HttpServer create(int port) {
    return new HttpServer(port);
  }
  
  class HttpClient{
    int port;
    public HttpClient(int port){
      this.port=port;
    }
    public void send(String msg) throws IOException{
      Socket clientSocket=new Socket("localhost", 16080);
      clientSocket.getOutputStream().write(msg.getBytes());
      clientSocket.close();
    }
  }
  
  public HttpClient createClient(){
    return client;
  }
  
  private HttpServer(int port){
    client=new HttpClient(port);
    this.port=port;
  }
  
  public void start(){
    try{
      final ServerSocket server=new ServerSocket(port);
      acceptorThread=new Thread(new Runnable() {
        @Override public void run() {
          try{
            while(true){
              final Socket s=server.accept();
              executors.execute(new Runnable() {
                @Override public void run() {
                  try{
                    HttpExchange exchange=new HttpExchange(s.getInputStream(), s.getOutputStream());
                    httpHandler.handle(exchange);
                  }catch(IOException e){
                    e.printStackTrace();
                  }
              }});
            }
          }catch(IOException e){
            e.printStackTrace();
          }
      }});
      acceptorThread.start();
      
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  @SuppressWarnings("deprecation")
  public void shutdown(){
    try{
      Thread.sleep(100);
      acceptorThread.stop();
      executors.shutdown();
      executors.awaitTermination(5, TimeUnit.SECONDS);
    }catch(InterruptedException e){
      throw new RuntimeException(e);
    }
  }
  
  public void setHttpHandler(HttpHandler httpHandler) {
    this.httpHandler=httpHandler;
  }
}
