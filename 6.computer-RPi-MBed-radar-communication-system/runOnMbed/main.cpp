#define _USE_SONAR_INT

#include "mbed.h"
#include "motor.h"
#include "compass.h"
#include "Hexi_OLED_SSD1351.h"
#define big_angle 90
#define small_angle 15

#ifdef _USE_SONAR_INT
#include "sonar_int.h"
#else
#include "sonar_noint.h"
#endif


Serial pc(USBTX, USBRX);
bool enMotor = true;
bool enSonar = true;
bool enCompass = true;
bool scan = false;
bool scanStart = false;
bool gotoHeading = false;
bool scanHeading = false;
bool displayData = false;

int main() {

    //declare motor
    Motor motor(NULL,NULL,p26,p25,p24,p23);
    float speed = 0.1;  //should be a positive value.
    motor.setMaxSpeed(20);
    motor.setSpeed(speed);
    
    
    //declare screen
    SSD1351 oled(p5,p7,p12,p10,p9,p8);  
    oled_text_properties_t textProperties = {0};
    oled.GetTextProperties(&textProperties);  
    //Fills the screen with solid black     
    oled.FillScreen(COLOR_BLACK);
              
    // Set text properties to white and center aligned for the dynamic text
    textProperties.fontColor = COLOR_RED;
    textProperties.alignParam = OLED_TEXT_ALIGN_CENTER;
    oled.SetTextProperties(&textProperties); 

    
    //declare sonar
#ifdef _USE_SONAR_INT
    Sonar2 sonar(p14,p13);
#else
    Sonar sonar(p29,p30);
#endif

    //declare compass
    Compass compass(p28,p27);
    int i=-1;
    //compass.calibrate(true);
    compass.setScale(262,37,212,1000);
    compass.setBias(376,-1155,490);
    float heading_sum = 0;
    const int heading_cnt = 5;
    float cntr_heading = 0;
    for(int c=0;c<heading_cnt;c++){
                heading_sum += compass.getHeading(true);
                wait(0.1);
    }
    cntr_heading = heading_sum/heading_cnt;
    float heading = cntr_heading;
    printf("\rThe current heading is: %f\n", heading); 
    int scan_heading = 0;
    int temp_cntr = cntr_heading;
    int scan_angle = big_angle;
    
    // frequency of devices
    const int freq = 30;
    float cur_heading = 0;
    int dist = 0;
    
    // for user input
    char buffer[100] = {0};
    int buffer_index = 0;
    int num = 0;
    

    while(true){
        i++;
        //read the input string (char)
        if(pc.readable()){
            char c = pc.getc();
            printf("%c",c);
            if(c != ';'){
                buffer[buffer_index] = c;
                ++buffer_index;
            }
            else{
                buffer[buffer_index] = '\0';
                char* str = buffer;
                if(strncmp(str,"scan",4) == 0){
                    scan = true;
                    scanHeading = false;
                    cntr_heading = temp_cntr;
                    scan_angle = big_angle;
                    printf("\rstart scanning...\n");
                }
                else if(strncmp(str,"stop",4) == 0){
                    printf("\rstop scanning!\n"); 
                    scan = false; 
                }
                else if(strncmp(str,"display",7) == 0){
                    printf("\rrea dy to display data...\n"); 
                    displayData = true; 
                }
                else if(strncmp(str,"heading=",8)==0){
                    num = atoi(str+8);
                    if(num>0 || num<360){
                        printf("\rgo to heading %d...\n",num);
                        gotoHeading = true;
                        scan_heading = num;
                    }
                    else printf("\rinput heading error\n");
                }
                else{
                    printf("\rinput error\n");
                }       
                buffer_index = 0;
                num = 0;
                printf("\r\n");
            }
                    
        }
            
        //move to heading and start scanning
        if(gotoHeading){
            //move to heading
           // if(heading > (scan_heading-2) && heading < (scan_heading+2)){
                printf("\rarrive the heading...\n");
                gotoHeading = false;
                wait(2);
                printf("\rstart scanning in designated area...\n");
                scanHeading = true; // set "start scanning"
                temp_cntr = cntr_heading;
                cntr_heading = scan_heading;
                scan_angle = small_angle;
           // }
        }
            
            //display data
        if(displayData){
            int len = oled.getCurrentIndex();
            int** pointsData = oled.getPointsData();
            for(int i = 0; i < len; ++i){
            printf("\rThe point is: x=%d, y=%d\n",**(pointsData+i)-48,*(*(pointsData+i)+1)-20);
            }
            if(!len) printf("\rNo data to display!\n"); 
            displayData = false;
        }
        
        if(enSonar && scan){
            
            
#ifdef _USE_SONAR_INT
            //sonar_int
            if((i%freq==0 || sonar.distIsReady())){
                motor.dragDownVoltage();
                if(sonar.distIsReady()){
                    dist = sonar.getDistance();
                    //printf("\rdist = %d\n",dist);
                    sonar.disableReady();
                }else{
                    sonar.sendSound();
                }
            }
#else
            //sonar_noint
            if((i%freq==0)){
                motor.dragDownVoltage();
                dist = sonar.measureDistance();
                for(int trysonar=0;trysonar<5 && dist==0;trysonar++){
                    dist = sonar.measureDistance();
                }
                printf("\rdist = %d\n",dist);
            }
#endif
        }
        
        if((enCompass && i%freq==0) && scan){
            motor.dragDownVoltage();
            heading = compass.getHeading(true);
            cur_heading = heading - cntr_heading;
            if(cur_heading<-180){
                cur_heading+=360;
            }else if(cur_heading>180){
                cur_heading-=360;
            }
            //printf("\rheading-cntr_heading=%3.5f, heading=%3.5f\n",cur_heading,heading);
            //printf("\rdist = %d\n",dist);
            oled.DrawCurrentRadar(48,20,40,dist,heading,180-(cur_heading+90));
            
        }
        
        
        if((enCompass && i%freq==0) && scan){  //do not move motor when sonar works, to save energy.
            //printf("\rDeciding change of motor direction\n");
            float motor_heading = compass.getHeading(true);
            if(motor_heading-cntr_heading<-180){
                motor_heading+=360;
            }else if(motor_heading-cntr_heading>180){
                motor_heading-=360;
            }
            int static lastMotorSpeed = 0;
            lastMotorSpeed = motor.getSpeed();
            int ra = (int)(motor_heading-cntr_heading+360)%360;
            int la = (int)(cntr_heading-motor_heading+360)%360;
            if(ra>scan_angle && ra<180){
                motor.setSpeed(speed);
                if(motor.getSpeed()!=lastMotorSpeed)
                    oled.clearPointsData();
            }
            else if(la>scan_angle && la<180){
                motor.setSpeed(-speed);
                if(motor.getSpeed()!=lastMotorSpeed)
                    oled.clearPointsData();
            }
        }
        
        
        if((enMotor && i%freq!=0) && scan){
            motor.actionOnce();
         }
         wait(0.01);
    }
}