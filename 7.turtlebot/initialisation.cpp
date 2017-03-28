/**
 * @file /kobuki_driver/src/test/initialisation.cpp
 *
 * @brief Demo program for kobuki initialisation.
 **/
/*****************************************************************************
** Includes
*****************************************************************************/

#include <iostream>
#include <kobuki_driver/kobuki.hpp>
#include <ecl/time.hpp>

class KobukiManager {
public:
  KobukiManager() {
    kobuki::Parameters parameters;
    // change the default device port from /dev/kobuki to /dev/ttyUSB0
    parameters.device_port = "/dev/ttyUSB0";
    // Other parameters are typically happy enough as defaults
    // namespaces all sigslot connection names under this value, only important if you want to
    parameters.sigslots_namespace = "/kobuki";
    // Most people will prefer to do their own velocity smoothing/acceleration limiting.
    // If you wish to utilise kobuki's minimal acceleration limiter, set to true
    parameters.enable_acceleration_limiter = false;
    // If your battery levels are showing significant variance from factory defaults, adjust thresholds.
    // This will affect the led on the front of the robot as well as when signals are emitted by the driver.
    parameters.battery_capacity = 16.5;
    parameters.battery_low = 14.0;
    parameters.battery_dangerous = 13.2;
	// Initializa the speed and angle
    speed = 0.0;
    angle = 0.0;

    // initialise - it will throw an exception if parameter validation or initialisation fails.
    try {
      kobuki.init(parameters);
    } catch ( ecl::StandardException &e ) {
      std::cout << e.what();
    }
  }
 
void exec(){
	kobuki.setBaseControl(speed, angle);
	kobuki.sendBaseControlCommand();
}

void setSpeed(float s){
	speed = s;
}

void setAngle(float a){
	angle = a;
}

void move(float speed, float angle){
	kobuki.setBaseControl(speed,angle);
	kobuki.sendBaseControlCommand();
}

double getHeading(){
	kobuki.lockDataAccess();
	//printf("I got the data\n");
	double heading = kobuki.getHeading();
	kobuki.unlockDataAccess();
	return heading;
}

kobuki::CoreSensors::Data getData(){
	kobuki.lockDataAccess();
	kobuki::CoreSensors::Data data = kobuki.getCoreSensorData();
	//printf("I got the data!\n");
	kobuki.unlockDataAccess();
	return data;
}

private:
  kobuki::Kobuki kobuki;
  float speed;
  float angle;
};

int main() {
        KobukiManager kobuki_manager;
	char buffer[100];
        while(1){
		std::cin>>buffer;
		char* str = buffer;
        	if(strncmp(str, "speed=", 6) == 0){
                	float speed = atof(str+6);
               		kobuki_manager.setSpeed(speed);
			//printf("The speed is set to: %f", speed);
                }
                else if(strncmp(str, "angle=", 6) == 0){
                	float angle = atof(str+6);
                	kobuki_manager.setAngle(angle);
	       		//printf("The angle is set to: %f", angle);
                }
		else if(strncmp(str, "cmd=", 4) == 0){
	     		if(strcmp(str+4, "exec") == 0){
				//printf("I'm starting to execute...");
				kobuki_manager.exec(); 
			}
			else if(strcmp(str+4, "quit") == 0){
				//printf("I'm quitting");
                        	return 0;
			}
			else if(strcmp(str+4,"data")==0){
				kobuki::CoreSensors::Data data = kobuki_manager.getData();
				printf("time_stamp=%u;bumper=%u;wheel_drop=%u;cliff=%u;left_encoder=%u;right_encoder=%u;left_pwm=%d;right_pwm=%d;buttons=%u;charger=%u;battery=%u;over_current=%u;",data.time_stamp,data.bumper,data.wheel_drop,data.cliff,data.left_encoder,data.right_encoder,data.left_pwm,data.right_pwm,data.buttons,data.charger,data.battery,data.over_current);
			}
			else if(strcmp(str+4,"heading")==0){
				double heading = kobuki_manager.getHeading();
				while(std::isnan(heading)){
					heading = kobuki_manager.getHeading();
				}
				printf("heading=%f",heading);
			}
			else{
				std::cerr << "Sorry, this command is not supported for now..." << std::endl;
			}
               	}
                else{
                        printf("\rinput error!\n");
               	}
        }
	return 0;
}
