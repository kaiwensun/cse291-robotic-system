import java.awt.geom.Point2D;
import java.util.Stack;

public class TurtleBotHighlevel {
	
	private Kobuki kobuki;
	private Camera camera;
	private double linear_velocity = 0.0;
	private double angular_velocity = 0.0;
	final private double fixed_angular_amount = Math.PI/2;
	private Point2D location;
	private Stack<MotionRecord> motionRecords;
	private Timer timer;
	
	/**
	 * Constructor
	 * @param kobukiCppFilename is the filename of the cpp drive program. For
	 * example, cppFilename="./kobuki_driver_demo_initialisation"
	 * @param cameraFilename is the filename of the camera driver.
	 */
	public TurtleBotHighlevel(String kobukiCppFilename, String cameraFilename){
		kobuki = new Kobuki(kobukiCppFilename);
		System.setProperty("user.dir","/home/pi/orbbec-test/OpenNI2/Bin/Arm-Release");
		//camera = new Camera(cameraFilename,"/home/pi/orbbec-test/OpenNI2/Bin/Arm-Release"); 
try{
		camera = new Camera(cameraFilename,".");
}catch(Exception e){
e.printStackTrace();
}
		location = new Point2D.Double();
		motionRecords = new Stack<>();
		timer = new Timer();
		resetLocation();
		timer = new Timer();
	}
	
	/**
	 * drive the turtle bot at a given speed. angular velocity will remains unchanged.
	 * @param distance linear velocity in m/s
	 */
	public synchronized void drive(double linear_velocity){
		if(timer.isTiming()){
			timer.end();
			motionRecords.push(new LAMotionRecord(this.linear_velocity, this.angular_velocity, timer.getDuration()));
		}
		this.angular_velocity = 0;//temp
		this.linear_velocity = linear_velocity;
		kobuki.setBaseControl(this.linear_velocity, this.angular_velocity);
		kobuki.sendBaseControlCommand();
		timer.start();
	}
	
	/**
	 * drive the turtle bot at a given angular velocity. linear velocity will remains unchanged.
	 * @param angular_velocity angular velocity in rad/s.
	 */
	public synchronized void turn(double rad){
		if(timer.isTiming()){
			timer.end();
			motionRecords.push(new LAMotionRecord(this.linear_velocity, this.angular_velocity, timer.getDuration()));
		}
		stop();
		this.angular_velocity = 0;
		turnAngleRad(rad);
		motionRecords.push(new RadMotionRecord(rad));
	}
	
	/**
	 * turn left at a fixed angular velocity.
	 */
	public void turnLeft(){
		turn(fixed_angular_amount);
	}
	
	/**
	 * turn right at a fixed angular velocity.
	 */
	public void turnRight(){
		turn(-fixed_angular_amount);
	}
	
	/**
	 * Get core sensor data.
	 * @return core sensor data. The CoreSensorData can be directed printed using System.out.
	 * Or you can convert it to string by using coreSensorData.toString().
	 */
	public CoreSensorData readSensorData(){
		return kobuki.getCoreSensorData();
	}
	
	/**
	 * Reset the starting location (for use with the goback/return command)
	 */
	public void resetLocation(){
		stop();
		location.setLocation(0.0, 0.0);
		motionRecords.clear();
		timer.end();
		timer.start();
	}
	
	/**
	 * Return to the starting position before you started executing any commands.
	 */
	public synchronized void goBack(){
		stop();
		
		//go back
		while(!motionRecords.isEmpty()){
			MotionRecord mr = motionRecords.pop();
			if(mr instanceof LAMotionRecord){
				LAMotionRecord lamr = (LAMotionRecord)mr;
				kobuki.setBaseControl(-lamr.linear_v, -lamr.angular_v);
				kobuki.sendBaseControlCommand();
				try {
					Thread.sleep(lamr.duration_millisec);
				} catch (InterruptedException e) {
					break;
				};
			}else if(mr instanceof RadMotionRecord){
				RadMotionRecord rmr = (RadMotionRecord)mr;
				turnAngleRad(-rmr.rad);
			}
		}
		
		//stop at original location and reset.
		stop();
		resetLocation();
	}
	
	private void stop(){	//thread unsafe
		kobuki.setBaseControl(0, 0);
		kobuki.sendBaseControlCommand();
		if(timer.isTiming()){
			timer.end();
			motionRecords.push(new LAMotionRecord(this.linear_velocity, this.angular_velocity, timer.getDuration()));
		}
		this.linear_velocity = 0.0;
		this.angular_velocity = 0.0;
	}
	
	private double getHeading(){
		return kobuki.getHeading();
	}
	
	/**
	 * Go to a target position with respect to the current position. Can avoid obstacle
	 * @param x target position in meters. x-axis points to the right of the turtle box.
	 * @param y target positioin in meters. y-axis points to the front of the turtle box.
	 */
	public void smartGoTo(double x, double y){
		stop();
		
		//turn
		double rad = Math.atan2(y, x)-Math.PI/2;
		turn(rad);
		stop();
		
		//move straight
		double distance = Math.sqrt(x*x+y*y);
		drive(0.1);
		final int minObstDist = 30;//cm
		int direct = 0;//0:forward, -1:right, 1:left
		for(int i=0;i<(int)(distance*100);i++){
System.err.println("i="+i+",distance="+distance);
			int obstDist = getObstacleDistance();
			if(obstDist<minObstDist){
				//turn left 90 degrees
				stop();
				turn(Math.PI/2);
				direct = 1;
System.err.println("finished turning left");
				obstDist = getObstacleDistance();
				if(obstDist<minObstDist){
System.err.println("turnning backward");
					//if obstacle is on the left, turn 180 degrees
					direct = -1;
					turn(-Math.PI);
				}
				for(int j=0;j<3;j++){
					drive(0.1);
					try {
						if(j%2==0)
							Thread.sleep((long)(2*minObstDist*100));
						else
							Thread.sleep((long)(4*minObstDist*100));
					} catch (InterruptedException e) {
					}
					stop();
					switch(j){
					case 0:
					case 1:
						turn(-direct*Math.PI/2);break;
					case 2:
						turn(direct*Math.PI/2);break;
					default:
						break;
					}
					stop();
				}
				drive(0.1);
				distance-=4*minObstDist*0.01;
			}
			
			try {
				Thread.sleep((long)(100));
			} catch (InterruptedException e) {
			}
		}
		stop();
	}
	
	//private int fakeDistClk = 1;
	private int getObstacleDistance(){
		
try{
		int res = camera.getDistance()/10;
		return res;
		/*
		 fakeDistClk++;
System.err.println("fakeDistClk="+fakeDistClk);
		if(fakeDistClk%50==0 || fakeDistClk%50==1){
			return 20;
		}else{
			return 100;
		}
		*/
}catch(Exception e){
e.printStackTrace();
}
return -2;
	}
	
	/**
	 * Go to a target position with respect to the current position.
	 * @param x target position in meters. x-axis points to the right of the turtle box.
	 * @param y target positioin in meters. y-axis points to the front of the turtle box.
	 */
	public void goTo(double x, double y){
		stop();
		
		//turn
		double rad = Math.atan2(y, x)-Math.PI/2;
		turn(rad);
		stop();
		
		//move straight
		double distance = Math.sqrt(x*x+y*y);
		drive(0.05);
		try {
			Thread.sleep((long)(1000*20*distance));
		} catch (InterruptedException e) {
		}
		stop();
	}

	private void turnAngleRad(double rad){
		stop();
		double front = kobuki.getHeading();
		double target = rectifyAngle(front+rad);
		double curr = front;
		double diff = rectifyAngle(curr-target);
		final double maxTurnSpeed = 0.2;
		while(Math.abs(diff)>0.02){
			if(diff>0){
				//kobuki.setBaseControl(this.linear_velocity, -Math.max(Math.abs(diff)/1.5,maxTurnSpeed));
				kobuki.setBaseControl(this.linear_velocity ,-maxTurnSpeed);
				kobuki.sendBaseControlCommand();
			}else{
				//kobuki.setBaseControl(this.linear_velocity, Math.max(Math.abs(diff)/1.5,maxTurnSpeed));
				kobuki.setBaseControl(this.linear_velocity, maxTurnSpeed);
				kobuki.sendBaseControlCommand();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			curr = getHeading();
			diff = rectifyAngle(curr-target);
		}
		stop();
	}
	
	/**
	 * convert rad to range -PI to +PI
	 * @param rad input rad
	 * @return rad in -PI to +PI
	 */
	private double rectifyAngle(double rad){
		while(rad<Math.PI){
			rad+=Math.PI*2;
		}
		while(rad>Math.PI){
			rad-=Math.PI*2;
		}
		return rad;
	}

	public CoreSensorData getCoreSensorData(){
		return kobuki.getCoreSensorData();
	}

	public void close(){
		kobuki.shutdown();
	}

	/**
	 * This is a simple sampe on how you should use these high-level APIs
	 */
	public static void main(String[] argv){
System.err.println("start program");
		TurtleBotHighlevel tbh = new TurtleBotHighlevel("./demo_kobuki_initialisation","../orbbec-test/OpenNI2/Bin/Arm-Release/run");
		tbh.turn(Math.PI);
		tbh.resetLocation();
		tbh.drive(0.05);
		CoreSensorData data = null;
		while(true){
		try{
			data = tbh.getCoreSensorData();
			if(data.bumper!=0){
				break;
			}
			Thread.sleep(50);
		}catch(Exception e){};
		}
		tbh.stop();
		data = tbh.getCoreSensorData();
		System.out.println(data);
System.err.println("going back");
		tbh.goBack();
		tbh.close();
	}
}


abstract class MotionRecord{
}
class LAMotionRecord extends MotionRecord{
	final double linear_v;
	final double angular_v;
	final long duration_millisec;
	public LAMotionRecord(double linear_v, double angular_v,long duration_millisec){
		this.linear_v = linear_v;
		this.angular_v = angular_v;
		this.duration_millisec = duration_millisec;
	}
}
class RadMotionRecord extends MotionRecord{
	final double rad;
	public RadMotionRecord(double rad){
		this.rad = rad;
	}
}
class Timer {
	private long startTime;
	private long endTime;
	private boolean isTiming;
	public Timer(){
		startTime = Long.MIN_VALUE;
		endTime = Long.MIN_VALUE;
		isTiming = false;
	}
	public synchronized boolean isTiming(){
		return isTiming;
	}
	public synchronized void start(){
		if(!isTiming){
			startTime = System.currentTimeMillis();
			isTiming = true;
		}
	}
	public synchronized void end(){
		if(isTiming){
			endTime = System.currentTimeMillis();
			isTiming = false;
		}
	}
	
	/**
	 * Get duration.
	 * @return duration in milliseconds.
	 */
	public synchronized long getDuration(){
		return endTime-startTime;
	}
	
	/**
	 * Get duration.
	 * @return duration in seconds.
	 * @return
	 */
	public synchronized double getDurationSec(){
		return (endTime-startTime)/1000.0;
	}
}


