
public class Kobuki {
	private Subprocess kobukiCppProgram;
	private double linear_velocity;
	private double angular_velocity;
	
	
	public Kobuki(String cppFilename){
		kobukiCppProgram = new Subprocess(cppFilename);
	}
	public void setBaseControl(final double linear_velocity, final double angular_velocity){
		this.linear_velocity = linear_velocity;
		this.angular_velocity = angular_velocity;
		kobukiCppProgram.send("speed="+this.linear_velocity);
if(angular_velocity==0){
try{	Thread.sleep(100);}
catch(Exception e){}
}
		kobukiCppProgram.send("angle="+this.angular_velocity);
	}
	public void sendBaseControlCommand(){
		kobukiCppProgram.send("cmd=exec");
	}
	public void shutdown(){
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
