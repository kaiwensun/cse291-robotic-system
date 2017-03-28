import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Subprocess {
	
	private final String filename;
	private final File file;
	private final static String NL = System.getProperty("line.separator");
	private static final File filepath = new File(".");
	private Runtime rt = Runtime.getRuntime();
	private Process subP;
	private OutputStream out;
	private InputStream in;
	private Scanner sc;
	private int inBuffSize = 1024;
	
	public Subprocess(String subprocessFilename){
		this.filename = subprocessFilename;
		this.file = new File(subprocessFilename);
		init();
	}
	
	
	private boolean init(){
		if(!file.exists() || file.isDirectory()){
			System.err.println("File "+filename+" doesn't exist!");
			return false;
		}
		try {
			subP = rt.exec(filename,null,filepath);
		} catch (IOException e) {
			System.err.println("fail to run subprocess "+filename);
			return false;
		}
		out = subP.getOutputStream();
		try {
			out.flush();
		} catch (IOException e) {
			System.err.println("Fail to get and flush subprocess output stream");
		}
		in = subP.getInputStream();
		sc = new Scanner(in);
		return true;
	}
	
	public boolean send(String msg){
		try {
			out.write((msg+NL).getBytes());
			out.flush();
			return true;
		} catch (IOException | NullPointerException e) {
			System.err.println("Fail to send message "+ msg+" to "+this);
			return false;
		}
	}
	
	public String receive(){
		if(sc!=null){
			//return sc.next();
			byte[] buffer = new byte[inBuffSize];
			try {
				in.read(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new String(buffer);
		}else{
			return "Error: Scanner is null!";
		}
	}
	
	private static void test(){
		Scanner syssc = new Scanner(System.in);
		String f = syssc.next();
		Subprocess subprocess = new Subprocess(f);

		for(int i=0;i<10;i++){
			String cmd = syssc.next();
			subprocess.send(cmd);
			String rcv = subprocess.receive();
			System.out.println(rcv);
		}
		syssc.close();
	}
	
	public static void main(String[] argv){
		test();
	}
}
