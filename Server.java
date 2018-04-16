import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class Server implements Runnable{

	public static void main(String[] args) throws SocketException, UnknownHostException{
		new Thread(new Server() ).start();
	}

	
	private static final String IMG_PATH = "/server/images/";   //Run run on linux
	
	private DatagramSocket serverSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivedPacket;
	private byte[] in;
	private byte[] out;
	private boolean looping;
	
	private InetAddress IPAddress;
	private int port;
	
	public Server() throws SocketException, UnknownHostException{
		serverSocket = new DatagramSocket(10000);
		System.out.println( "\n" + InetAddress.getLocalHost() + "\n");
	}
	
	private void stopServer(){
		serverSocket.disconnect();
		serverSocket.close();
		in = new byte[1024];
		out = new byte[1024];
	}
	
	public void run(){
		
		try{
			looping = true;
			while(looping){
				in = new byte[1024];
				out = new byte[1024];
				
				receivedPacket = new DatagramPacket(in, in.length);
				serverSocket.receive(receivedPacket);
				System.out.print("Message Received: ");
			
				String message = new String( receivedPacket.getData() );
				System.out.println(message);
				message = message.trim();
				
				if( message.equals(".stop") ){
				
					looping = false;
					message = "<STOPPING SERVER>";
				
				}else if( message.equals(".disconnect") ){
				
					message = ".stop";
				
				}else if( message.equals(".getTime") ){
				
					message = getTimeFormated();
				
				}else if( message.equals(".getPic") ){
					
					message = ".getName";
					out = message.getBytes();
			
					IPAddress = receivedPacket.getAddress();
					port = receivedPacket.getPort();
			
					sendPacket = new DatagramPacket(out, out.length, IPAddress, port);
					serverSocket.send(sendPacket);
					System.out.println("Message Sent: " + message);
					
					in = new byte[1024];
				
					receivedPacket = new DatagramPacket(in, in.length);
					serverSocket.receive(receivedPacket);
					System.out.print("Message Received: ");
			
					message = new String( receivedPacket.getData() );
					System.out.println(message);
					message = message.trim();
					
					IPAddress = receivedPacket.getAddress();
					port = receivedPacket.getPort();
					
					message = sendPic(message, IPAddress, port);
					
				}else{
				
					message = message.toUpperCase();
				
				}
				
				out = message.getBytes();
			
				IPAddress = receivedPacket.getAddress();
				port = receivedPacket.getPort();
			
				sendPacket = new DatagramPacket(out, out.length, IPAddress, port);
				serverSocket.send(sendPacket);
				System.out.println("Message Sent: " + message);
			}
		
			stopServer();
			
		}catch(Exception e){
			System.out.println( e.getLocalizedMessage() );
		}
		
	}
	
	
	private static String getTimeFormated(){
		
		
		long currentTime = (System.currentTimeMillis()/1000); //Seconds passed
		
		String time = "" + (currentTime%60);
		if ( time.length() != 2 ) time = "0" + time; 
		
		String formatedTime = ":" + time;
		
		
		
		currentTime /= 60; //Minutes passed
		
		time = "" + (currentTime%60);
		if ( time.length() != 2 ) time = "0" + time;
		
		formatedTime = ":" + time + formatedTime;
		
		
		
		currentTime /= 60; //Hours passed
		if( currentTime/(365*24) %2 == 0 ) currentTime++;
		
		time = "" + (currentTime%24);
		if ( time.length() != 2 ) time = "0" + time;
		
		formatedTime = time + formatedTime;
		
		
		
		return formatedTime;
	}
	
	private String sendPic(String path, InetAddress IPAddress, int port) throws Exception{
	
		out = new byte[1024];
		BufferedImage img;
		try{
			img = ImageIO.read( new File( IMG_PATH + path + ".png" ) );
		}catch(Exception error){
			return "Could not find file!";
		}
		
		int width = img.getWidth();
		int height = img.getHeight();
		int size = width*height;
		String sizeMessage = size + " " + width + " " + height;
		sizeMessage = sizeMessage.trim();
		out = sizeMessage.getBytes();
		
		DatagramPacket sendPacket = new DatagramPacket(out, out.length, IPAddress, port);
		serverSocket.send(sendPacket);
		System.out.println( "Sent Size of Picture: " + sizeMessage );
		
		in = new byte[1024];
		receivedPacket = new DatagramPacket(in, in.length);
		
		out = new byte[width]; //Alpha RGB
		byte[] red = new byte[width]; //Red RGB
		byte[] green = new byte[width]; //Green
		byte[] blue = new byte[width]; //Blue
		
		int color;
		
		try{
			for(int i = 0; i<height; i++){
				for(int j = 0; j<width; j++){
					color = img.getRGB(j,i);
					out[j] = (byte)( color>>24);
					red[j] = (byte)( (color>>16)&255);
					green[j] = (byte)( (color>>8)&255);
					blue[j] = (byte)( color&255);
				}
				
				do{
					serverSocket.receive(receivedPacket);
					/*Wait to make sure previous package
					is received before sending the next*/
				}while(!receivedPacket.getAddress().equals(IPAddress));
				
				sendPacket = new DatagramPacket(out, out.length, IPAddress, port);
				serverSocket.send(sendPacket);
				
				do{
					serverSocket.receive(receivedPacket);
					/*Wait to make sure previous package
					is received before sending the next*/
				}while(!receivedPacket.getAddress().equals(IPAddress));
		
				sendPacket = new DatagramPacket(red, red.length, IPAddress, port);
				serverSocket.send(sendPacket);
		
				do{
					serverSocket.receive(receivedPacket);
					/*Wait to make sure previous package
					is received before sending the next*/
				}while(!receivedPacket.getAddress().equals(IPAddress));
		
				sendPacket = new DatagramPacket(green, green.length, IPAddress, port);
				serverSocket.send(sendPacket);
		
				do{
					serverSocket.receive(receivedPacket);
					/*Wait to make sure previous package
					is received before sending the next*/
				}while(!receivedPacket.getAddress().equals(IPAddress));
		
				sendPacket = new DatagramPacket(blue, blue.length, IPAddress, port);
				serverSocket.send(sendPacket);
				
			}
		}catch(Exception error){
			return error.getMessage();
		}
		
		
		return "<Sent Picture> \"" + path + "\"";
	}
}
