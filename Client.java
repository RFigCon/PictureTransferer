import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class Client implements Runnable{
	
	public static void main(String[] args) throws SocketException, UnknownHostException{
		
		new Thread(new Client()).start();
	}
	
	private final String IMAGE_PATH = "C:\\ServerImages\\";
	private final int SERVER_PORT = 10000;
	
	private BufferedReader inFromUser;
	private DatagramSocket clientSocket;
	private InetAddress ipAddress;
	private DatagramPacket out;
	private DatagramPacket in;
	private String message;
	
	private byte[] outData;
    private byte[] inData;
	
	public Client() throws SocketException, UnknownHostException{
		clientSocket = new DatagramSocket();
		//ipAddress = InetAddress.getByName("93.108.50.227");
		//ipAddress = InetAddress.getByName("10.10.39.70");
		ipAddress = InetAddress.getByName("192.168.1.73");
		inFromUser = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void run(){
		while(true){
			try {
				
				inData = new byte[1024];
				outData = new byte[1024];
				
				System.out.print("Client> ");
				message = inFromUser.readLine();
				
				outData = message.getBytes();
				
				out = new DatagramPacket(outData, outData.length, ipAddress, SERVER_PORT);
				clientSocket.send(out);
				
				in = new DatagramPacket(inData, inData.length);
				clientSocket.receive(in);
				
				message = new String(in.getData());
				message = message.trim();
				
				
				if( message.equals(".getName") ){
					
					receivePic();
					
				}
				
				System.out.println("Server> " + message);
				
			}catch (Exception e){
				System.out.println("Exception thrown: \"" + e.getLocalizedMessage()+"\"");
				System.out.println("Exception thrown: \"" + e.getMessage()+"\"");
				e.printStackTrace();
			}
		}
	}
	
	private void receivePic() throws Exception{
		
		System.out.println("Server> Insert name:");
					
		inData = new byte[1024];
		outData = new byte[1024];
					
		System.out.print("Client> ");
		message = inFromUser.readLine();
		message = message.trim();
		outData = message.getBytes();
		
		out = new DatagramPacket(outData, outData.length, ipAddress, SERVER_PORT);
		clientSocket.send(out);
			
		in = new DatagramPacket(inData, inData.length);
		clientSocket.receive(in);
		
		String s = new String(in.getData());
		s = s.trim();
		if(s.equals("Could not find file!")){
			message = "Could not find file!";
			return;
		}
		
		System.out.println("Server> Picture size is: " + s );
		
		String[] dimen = s.split(" ");
		int size = Integer.parseInt(dimen[0]);
		int width = Integer.parseInt(dimen[1]);
		int height = Integer.parseInt(dimen[2]);
		
		inData = new byte[width]; //Alpha
		byte[] red = new byte[width];
		byte[] green = new byte[width];
		byte[] blue = new byte[width];
		
		BufferedImage img = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );
		
		int a, r, g, b;
		int color;
		
		outData = new byte[1024];
		out = new DatagramPacket(outData, outData.length, ipAddress, SERVER_PORT);
		
		for(int i = 0; i<height; i++){
			
			clientSocket.send(out);//Notify server that the package was received
			
			do{
				in = new DatagramPacket(inData, width);
				clientSocket.receive(in);
			}while(!in.getAddress().equals(ipAddress));
			
			clientSocket.send(out); //Notify server that the package was received
			
			do{
				in = new DatagramPacket(red, width);
				clientSocket.receive(in);
			}while(!in.getAddress().equals(ipAddress));
			
			clientSocket.send(out); //Notify server that the package was received
			
			do{
				in = new DatagramPacket(green, width);
				clientSocket.receive(in);
			}while(!in.getAddress().equals(ipAddress));
			
			clientSocket.send(out); //Notify server that the package was received
			
			do{
				in = new DatagramPacket(blue, width);
				clientSocket.receive(in);
			}while(!in.getAddress().equals(ipAddress));
			
			for(int j = 0; j<width; j++){
				
				a = ((int)inData[j]);
				r = ((int)red[j]);
				g = ((int)green[j]);
				b = (int)blue[j];
				
				if(a<0) a = 256+a;
				if(r<0) a = 256+r;
				if(g<0) a = 256+g;
				if(b<0) a = 256+b;
				
				a = a<<24;
				r = r<<16;
				g = g<<8;
				
				color = a+r+g+b;
				img.setRGB(j,i,color);
				
			}
			
			System.out.print("\r" + (int)( (float)i/(float)height * 100) + "%" );
		}
		
		System.out.println("\r100%");
		
		ImageIO.write( img, "png", new File(IMAGE_PATH + message + ".png") );
		
		inData = new byte[1024];
		in = new DatagramPacket(inData, inData.length);
		clientSocket.receive(in);

		s = new String(in.getData());
		s = s.trim();
		System.out.println("Client> " + s);
		
	}
	
}