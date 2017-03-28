import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;  
  
public class Client {  

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		Scanner sc = new Scanner(System.in);
		//String ip = "127.0.0.1";
		String ip = "192.168.43.126";
		int port = 4567;
	    Postman postman = new Postman(new Socket(ip, port));
	    
	    Thread sender = new Thread(){
	    	@Override
	    	public void run(){
	    		String userInput = sc.nextLine();
	    		while(!userInput.equals("exit")){
	    			try {
						postman.send(userInput);
					} catch (IOException e) {
						e.printStackTrace();
					}
	    			userInput = sc.nextLine();
	    		}
	    		sc.close();
	    		postman.close();
	    	}
	    };
	    Thread receiver = new Thread(){
	    	@Override
	    	public void run(){
	    		while(true){
		    		Object result;
					try {
						result = postman.recv();
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
					if(result instanceof String){
						System.out.print(result);
					}
	    		}
	    	}
	    };
	    sender.start();
	    receiver.start();
    }    
}    