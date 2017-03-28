
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * A socket wrapper to send and receive serializable objects.
 * @author Kaiwen Sun
 *
 */
public class Postman {
	private Socket socket;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	/**
	 * Constructor.
	 * @param socket socket
	 * @throws IOException fail to open I/O stream.
	 */
	public Postman(Socket socket) throws IOException{
		this.socket = socket;
		init();
	}
	
	/**
	 * Initialize I/O stream on socket.
	 * @throws IOException fail to init
	 */
	private void init() throws IOException{
		 outStream = new ObjectOutputStream(socket.getOutputStream());
		 //outStream.flush();
		 InputStream inputStream = socket.getInputStream();
		 inStream = new ObjectInputStream(inputStream);
	}

	/**
	 * Receive serializable object. 
	 * @return received serializable object.
	 * @throws ClassNotFoundException fail to receive
	 * @throws IOException fail to receive
	 */
	public Object recv() throws ClassNotFoundException, IOException{
		synchronized (inStream) {
			return inStream.readObject();	
		}
	}
	
	/**
	 * Send serializable object.
	 * @param obj serializable object to be sent
	 * @throws IOException fail to send
	 */
	public  void send(Object obj) throws IOException{
		synchronized (outStream) {
			outStream.writeObject(obj);
			outStream.flush();
			outStream.reset();
		}
	}
	
	/**
	 * Close the I/O stream and socket. The postman should not be used anymore after being closed.
	 */
	public void close(){
		if(inStream!=null)
			try {
				inStream.close();
				inStream = null;
			} catch (IOException e) {
			}
		if(outStream!=null)
			try {
				outStream.close();
				outStream = null;
			} catch (IOException e) {
			}
		if(socket!=null)
			try {
				System.out.println("Postman at "+socket+" closed.");
				socket.close();
				socket = null;
			} catch (IOException e) {
			}
	}

	/**
	 * Convert the postman to a string containing its socket information.
	 */
	@Override
	public String toString(){
		try{
			return "postman at "+socket.toString();
		}
		catch(NullPointerException e){
			return "postman at unknown socket";
		}
	}
}
