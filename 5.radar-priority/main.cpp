#define _USE_SONAR_INT

#include "mbed.h"
#include "motor.h"
#ifdef _USE_SONAR_INT
#include "sonar_int.h"
#else
#include "sonar_noint.h"
#endif
#include "screen.h"

#include "compass.h"
#include <queue>
using namespace std::queue
enum TaskName{CHANGEDIR, DIST,DIRANDDRAW,MOTOR};
//priority level: 1,2,2,3
bool enMotor = true;
bool enSonar = true;
bool enCompass = true;

queue que[3];


void addMotorTask();
void addChangeDirTask();
void addDistTask();
void addDirAndDrawTask();

//declare motor
Motor motor(NULL,NULL,p26,p25,p24,p23);
//declare sonar
#ifdef _USE_SONAR_INT
Sonar2 sonar(p14,p13);
#else
Sonar sonar(p29,p30);
#endif

//declare compass
Compass compass(p28,p27);
//declare screen
SSD1351 oled(p5,p7,p12,p10,p9,p8);  


int main() {

    float speed = 0.1;  //should be a positive value.
    motor.setMaxSpeed(20);
    motor.setSpeed(speed);
    
    
    
    oled_text_properties_t textProperties = {0};
    oled.GetTextProperties(&textProperties);  
    //Fills the screen with solid black     
    oled.FillScreen(COLOR_BLACK);
              
    // Set text properties to white and center aligned for the dynamic text
    textProperties.fontColor = COLOR_RED;
    textProperties.alignParam = OLED_TEXT_ALIGN_CENTER;
    oled.SetTextProperties(&textProperties); 

    
    int i=-1;
    //compass.calibrate(true);
    compass.setScale(283,116,307,1000);
    compass.setBias(336,-1361,451);
    float heading_sum = 0;
    const int heading_cnt = 5;
    
    for(int c=0;c<heading_cnt;c++){
        heading_sum += compass.getHeading(true);
        wait(0.1);
    }
    const float cntr_heading = heading_sum/heading_cnt;
    
    
    // frequency of devices
    const int freq = 150;
    float heading = cntr_heading;
    int dist = 0;
    int screenClearCnt = 0;
    int screenDrawCnt = 0;
    
    Ticker ticker_changeDir;
    ticker_changeDir.attach(&addChangeDirTask,1.5);
    Ticker ticker_dirAndDraw;
    ticker_dirAndDraw.attach(&addDirAndDrawTask,1.5);
    Ticker ticker_dist;
    ticker_dist.attach(&addDistTask,1.5);
    Ticker ticker_motor;
    ticker_motor.attach(&addMotorTask,0.01);

    while(true){
        int taskId = -1;
        for(int i=0;i<3;i++){
            if(!queue[i].empty()){
                taskId = queue[i].pop();
                break;
            }
        }
        switch(taskId){
            case CHANGEDIR:changeDirTask();break;
            case DIST:distTask();break;
            case DIRANDDRAW:dirAndDrawTask();break;
            case MOTOR:motorTask();
            default:wait(0.01);
        }
        wait(0.01);
    }
}

void addMotorTask(){
    queue[2].push(MOTOR);
}
void addChangeDirTask(){
    queue[0].push(CHANGEDIR);
}
void addDistTask(){
    queue[1].push(DIST);
}
void addDirAndDrawTask(){
    queue[1].push(DIRANDDRAW);
}

void distTask(){
#ifdef _USE_SONAR_INT
            //sonar_int
            if(i%freq==0 || sonar.distIsReady()){
                motor.dragDownVoltage();
                if(sonar.distIsReady()){
                    dist = sonar.getDistance();
                    printf("\rdist = %d\n",dist);
                    sonar.disableReady();
                }else{
                    sonar.sendSound();
                }
            }
#else
            //sonar_noint
            if(i%freq==0){
                motor.dragDownVoltage();
                dist = sonar.measureDistance();
                for(int trysonar=0;trysonar<5 && dist==0;trysonar++){
                    dist = sonar.measureDistance();
                }
                printf("\rdist = %d\n",dist);
            }
#endif
}

void motorTask(){
    motor.actionOnce();
}
void changeDirTask(){
    printf("\rDeciding change of motor direction\n");
    float motor_heading = compass.getHeading(true);
    if(motor_heading-cntr_heading<-180){
        motor_heading+=360;
    }else if(motor_heading-cntr_heading>180){
        motor_heading-=360;
    }
    int ra = (int)(motor_heading-cntr_heading+360)%360;
    int la = (int)(cntr_heading-motor_heading+360)%360;
    int static lastMotorSpeed = 0;
    lastMotorSpeed = motor.getSpeed();
    if(ra>90 && ra<180){
        motor.setSpeed(speed);
        if(motor.getSpeed()!=lastMotorSpeed)
            oled.clearPointsData();
    }else if(la>90 && la<180){
        motor.setSpeed(-speed);
        if(motor.getSpeed()!=lastMotorSpeed)
            oled.clearPointsData();
    }
}
void dirAndDrawTask(){
    motor.dragDownVoltage();
    heading = compass.getHeading(true);
    float cur_heading = heading - cntr_heading;
    if(cur_heading<-180){
        cur_heading+=360;
    }else if(cur_heading>180){
        cur_heading-=360;
    }
    printf("\rheading-cntr_heading=%3.5f, heading=%3.5f\n",cur_heading,heading);
    printf("\rdist = %d\n",dist);
    oled.DrawCurrentRadar(48,20,40,dist,heading,180-(cur_heading+90));
}