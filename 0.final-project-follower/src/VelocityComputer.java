/*
THIS CLASS IS A STABILIZING CONTROLLER: GIVEN THE RECTANGLE CENTER INDICES, OUTPUT THE L/R VELOCITY
*/
import java.util.*;
import org.opencv.core.Rect;

public class VelocityComputer implements VelocityComputerInterface{
	
	//define the controller parameter
	private float x_Kp;
	private float area_Kp;
	private int barWidth;
	private int areaThres;
	private float stopRatio;
	private float x_err;
	private float area_err;
	private float x_step;
	private float area_step;
	private float forwardSpeedLimit;
	
	public VelocityComputer(){
		x_Kp = 0;
		area_Kp = 0;
		barWidth = 20;
		areaThres = 1000;
		stopRatio = 0.1f;
		x_err = 0;
		area_err = 0;
		x_step = 0;
		area_step = 0;
		forwardSpeedLimit = 0.1f;
	}
	
	public float[] getVelocity(int imgWidth, int imgHeight, Rect roi){
		updateErr(imgWidth, imgHeight, roi);
		updateKp(imgWidth, imgHeight, roi);
		float[] velocity = new float[2];
		if(Math.abs(x_err) <= barWidth){
			velocity[1] = 0;
		}
		else {
			velocity[1] = -this.x_err*getXStep(imgWidth);
		}
		if(Math.abs(area_err) <= areaThres){
			velocity[0] = 0;
		}
		else{
			velocity[0] = -this.area_err*getAreaStep(imgWidth, imgHeight);	
		} 
		System.out.format("The linear velocity is: %.5f, The angular velocity is: %.5f",velocity[0],velocity[1]);
		return velocity;
	}
	
	private float getForwardSpeedLimit(){
		return this.forwardSpeedLimit;
	}
	
	public void setForwardSpeedLimit(float limit){
		this.forwardSpeedLimit = limit;
	}
	
	private float getAreaStep(int imgWidth, int imgHeight){
		float areaStep = 0;
		if(this.area_err <= 0){
			areaStep = this.forwardSpeedLimit/getRectAreaRef(imgWidth, imgHeight);
		}
		else{
			if(this.stopRatio >= 1){
				System.err.println("The ratio is too small!");
				areaStep = 100000;
			}
			else{
				areaStep = this.forwardSpeedLimit/(getRectAreaRef(imgWidth, imgHeight)*(1/this.stopRatio-1));	
			}
		}
		return areaStep;
	}
	
	private float getXStep(int imgWidth){
		return (float)1/imgWidth*2;
	}
	private void updateErr(int imgWidth, int imgHeight, Rect rect){
		int x_cur = computeCenterX(rect);
		int x_ref = computeCenterXRef(imgWidth);
		this.x_err = x_cur - x_ref;
		this.area_err = getRectArea(rect) - getRectAreaRef(imgWidth, imgHeight); 
	}
	
	private int computeCenterX(Rect rect){
		return rect.width/2+rect.x;
	}
	
	private int computeCenterXRef(int imgWidth){
		return imgWidth/2;
	} 
	
	private void updateKp(int imgWidth, int imgHeight, Rect rect){
		this.x_Kp = (float)1/imgWidth*2;
		this.area_Kp = (float)1/getRectAreaRef(imgWidth, imgHeight)*this.stopRatio;
	}
	
	private int getRectArea(Rect rect){
		return rect.width*rect.height;
	}
	
	private float getRectAreaRef(int imgWidth, int imgHeight){
		return (float)imgWidth*imgHeight*this.stopRatio;
	}
	
	public void setStopRatio(float ratio){
		this.stopRatio = ratio;
	}
	
	public static void main(String[] args){
		Rect roi = new Rect(30, 40, 150, 150);
		VelocityComputer VC = new VelocityComputer();
		VC.getVelocity(640,280,roi);
	}
	
}

