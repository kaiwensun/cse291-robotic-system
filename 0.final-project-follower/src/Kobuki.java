import java.util.LinkedList;
import java.util.Queue;

public class Kobuki {
	private Subprocess kobukiCppProgram;
	private double linear_velocity = Double.NEGATIVE_INFINITY;
	private double angular_velocity = Double.NEGATIVE_INFINITY;;
	private Thread cmdSender;
	private volatile boolean isRunning;
//	BlockingQueue<double[]> commands = new LinkedBlockingQueue<>(2);
	Queue<double[]> commands = new LinkedList<>();
	public Kobuki(String cppFilename){
		kobukiCppProgram = new Subprocess(cppFilename);
		isRunning = true;
		cmdSender = new Thread(){
			@Override
			public void run(){
				try {
					double[] command = null;
					while(isRunning){
						Thread.sleep(50);
						synchronized (commands) {
							if(commands.isEmpty())
								continue;
							command = commands.poll();
						}
						if(Math.abs(linear_velocity-command[0])>0.02){
							linear_velocity = command[0];
							kobukiCppProgram.send("speed="+linear_velocity);
						}
						if(Math.abs(angular_velocity-command[1])>0.01){
							angular_velocity = command[1];
							kobukiCppProgram.send("angle="+angular_velocity);
						}
					}
				} catch (InterruptedException e) {
					System.err.println("Kobuki command sender thread stopped");
				}
			}
		
		};
		cmdSender.start();
	}
	public void setBaseControl(final double linear_velocity, final double angular_velocity){
		synchronized (commands) {
			while(commands.size()>1){
				commands.remove();
			}
			commands.add(new double[]{linear_velocity, angular_velocity});
		}
	}
	public void sendBaseControlCommand(){
		kobukiCppProgram.send("cmd=exec");
	}
	public void shutdown(){
		isRunning = false;
		cmdSender.interrupt();
		kobukiCppProgram.send("cmd=quit");
	}
	public CoreSensorData getCoreSensorData(){
		kobukiCppProgram.send("cmd=data");
		String sensorDataStr = kobukiCppProgram.receive();
		return new CoreSensorData(sensorDataStr);
	}
	public double getHeading(){
		kobukiCppProgram.send("cmd=heading");
		String rcv = kobukiCppProgram.receive();
		if(rcv.startsWith("heading=")){
			return Double.parseDouble(rcv.substring(8));
		}else{
			return Double.NaN;
		}
	}
}
