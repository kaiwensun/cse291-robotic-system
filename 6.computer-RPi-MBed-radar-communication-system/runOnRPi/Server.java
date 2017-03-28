import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;  
import java.net.Socket;
import java.util.Scanner;

import gnu.io.NoSuchPortException;

public class Server {
	public static void main(String[] argv){
		ServerSocket listener = null;
		int port = 0;
		try {
			port = 4567;
			listener = new ServerSocket(port);
			while(!Thread.interrupted()){
				Socket incoming = listener.accept();
				Thread thread = new Thread(){
					@Override
					public void run(){
						try {
							Service service = new Service(incoming);
							service.doTask();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
			}
		} catch (IOException e1) {
			System.err.println("Can't open port "+port);
			return;
		}
		catch(Throwable throwable){
			throwable.printStackTrace();
		}
		finally{
			if(listener!=null)
				try {
					listener.close();
				} catch (IOException e) {
				}
		}
	}
}

class Service{
	private Postman postman;
	Service(Socket socket) throws IOException{
		postman = new Postman(socket);
	}
	
	public void doTask(){
		MbedSerial mbedSerial;
		int i=0;
		while(true){
			try{
				mbedSerial = new MbedSerial("/dev/ttyACM"+(i++));
				break;
			} catch(NoSuchPortException e){
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if(i==4){
				return;
			}
		}
		new Thread( new Mbed2UserPipe(mbedSerial.getInputStream(), postman)).start();
        new Thread( new User2MbedPipe(mbedSerial.getOutputStream(),postman)).start();
	}
}


class Mbed2UserPipe implements Runnable {
    private InputStream mbed2PiIn;
    private Postman pi2UserOutPostman;
    public Mbed2UserPipe ( InputStream mbed2PiIn, Postman pi2UserOutPostman) {
    	this.mbed2PiIn = mbed2PiIn;
    	this.pi2UserOutPostman = pi2UserOutPostman;
    }
 
    @Override
    public void run() {
        byte[] buffer = new byte[ 10240 ];
        int len = -1;
System.err.println("mbed 2 user thread starts");
        try {
        while( ( len = this.mbed2PiIn.read( buffer ) ) > -1){
System.err.println("from pi 2 user: "+new String(buffer,0,len));
        	pi2UserOutPostman.send(new String( buffer, 0, len ));
        }
	    } catch( IOException e ) {
	        e.printStackTrace();
	    }
System.err.println("mbed 2 user thread ends");
    }
}
class User2MbedPipe implements Runnable {
    
    private OutputStream pi2MbedOut;
    private Postman user2PiPostman;
    public User2MbedPipe(OutputStream pi2MbedOut, Postman user2PiPostman){
    	this.pi2MbedOut = pi2MbedOut;
    	this.user2PiPostman = user2PiPostman;
    }
    
    public void run() {
        try {
            while(!Thread.interrupted()){
            	try {
					Object obj = user2PiPostman.recv();
					if(obj instanceof String){
System.err.println("from user 2 pi: "+obj);
						pi2MbedOut.write(((String) obj).getBytes());
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
            	
            }
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }
}
