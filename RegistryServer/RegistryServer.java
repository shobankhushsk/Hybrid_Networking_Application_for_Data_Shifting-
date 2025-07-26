import java.util.*;
import java.io.*;
import java.net.*;
class RegistryServer{
  static Hashtable table = new Hashtable();
  public static void main(String arg[])throws Exception{	
		ServerSocket serverSocket = new ServerSocket(9090);
        System.out.println("Server started...");
		do{
			Socket socket = serverSocket.accept();
			String host = socket.getInetAddress().getHostName();
            System.out.println("Client Connected: "+host);
		    
			table.put(host,socket);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            PrintStream out = new PrintStream(socket.getOutputStream());
   			//sendListToAll();			
			
			HandlerConn t  =  new HandlerConn(in,out,host);
			t.start();
		}while(true);	
  }// end of main

static class HandlerConn extends Thread{
	
	private PrintStream out;
	private DataInputStream in;
	private String host;
	
	HandlerConn(DataInputStream in,PrintStream out,String host){
		this.in=in;
		this.out=out;
		this.host=host;
	}
	
	public void run(){
	 try{
			 sendListToAll();
			 do{
		         String msg=in.readLine();// client will not send any message to registry service
				 System.out.println(msg);		
			 }while(true);	 
		  
	 }catch(Exception e){
		 removeHost(host);
		 sendListToAll();
	 }	
	}//end run
	
	 static void removeHost(String host){
		 table.remove(host);
	 }
	 
	
	
	static void sendListToAll(){
		String clientList=getClientList();
		Enumeration e=table.keys();
		while(e.hasMoreElements()){
			String hostName = (String)e.nextElement();
			Socket socket=(Socket)table.get(hostName);
			try{
					PrintStream out=new PrintStream(socket.getOutputStream());
					out.println(clientList);
				}catch(Exception exp){exp.printStackTrace();}
		}//end while
	}//end sendListToAll
	
	
	static String getClientList(){
		
		String clientList="";
		Enumeration e = table.keys();
		while(e.hasMoreElements()){
			clientList+=e.nextElement()+":";
		}
		return clientList;
	}
}// end class HandlerConn inner class
}// end of class main
