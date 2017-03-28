import java.net.Socket;

public class Camera {
	private Subprocess cameraProgram;
	private Postman postman;
	public Camera(String cameraFilename) throws Exception{
		//cameraProgram = new Subprocess(cameraFilename);
		postman = new Postman(new Socket("127.0.0.1",4568));
	}
	public Camera(String cameraFilename,String dir) throws Exception{
		//cameraProgram = new Subprocess(cameraFilename,dir);
		postman = new Postman(new Socket("127.0.0.1",4568));
	}
	public int getDistance() throws Exception{
		//cameraProgram.send("depth");
		postman.send("depth");
System.err.println("send depth");
		//String res = cameraProgram.receive();
		Object obj = postman.recv();
		String res = "";
		if(obj instanceof String)
			res = (String) obj;
System.err.println("Camera:depth="+res);
		return Integer.parseInt(res);
	}
	public boolean hasObstacle() throws Exception{
		//cameraProgram.send("obstacle");
		postman.send("obstacle");
		//String res = cameraProgram.receive();
		String res = "";
		Object obj = postman.recv();
		if(obj instanceof String)
			res = (String) obj;
		
System.err.println("obstacle="+res);
		return res.equals("1");
		
	}
}

