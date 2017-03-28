#include "motor.h"
#include "mbed.h"
Motor::Motor(Serial* input, Serial* output,PinName blue,PinName pink,PinName yellow,PinName orange):blue(blue),
pink(pink),
yellow(yellow),
orange(orange),
waitTime(0),
direction(1),
input(input),
output(output),
speed(0.0),
speedStep(1.0),
maxSpeed(10)
{
    rectifyWaitTime();
    rectifyDirection();
}
Motor::~Motor(){
    //nothing to do.
}
void Motor::actionOnce(){
    static unsigned int phase = 0;
    if(input!=NULL && input->readable()){
        char c = execInput();
        if(output!=NULL)
            output->printf("\rspeed: %.5f, waitTime: %.5f, pressed: %c\n",speed,waitTime,c);
    }
    
    switch(phase%8){
        case 0:orange = 1; yellow = 0; pink = 0; blue=1;break;
        case 1:orange = 1; yellow = 0; pink = 0; blue=0;break;
        case 2:orange = 1; yellow = 1; pink = 0; blue=0;break;
        case 3:orange = 0; yellow = 1; pink = 0; blue=0;break;
        case 4:orange = 0; yellow = 1; pink = 1; blue=0;break;
        case 5:orange = 0; yellow = 0; pink = 1; blue=0;break;
        case 6:orange = 0; yellow = 0; pink = 1; blue=1;break;
        case 7:orange = 0; yellow = 0; pink = 0; blue=1;break;
    }
    phase+=direction;
    wait_ms(waitTime*1000);    
}

double Motor::getWaitTime(){
    return waitTime;
}

double Motor::getSpeed(){
    return speed;
}

void Motor::increaseSpeed(){
    speed += speedStep;
    rectifyDirection();
    rectifyWaitTime();
}

void Motor::decreaseSpeed(){
    speed -= speedStep;
    rectifyDirection();
    rectifyWaitTime();
}

void Motor::setSpeed(float speed){
    this->speed = (float)speed;
    rectifyDirection();
    rectifyWaitTime();
}

void Motor::rectifyDirection(){
    if(speed>0){
        direction = 1;
    }
    else if(speed==0){
        direction = 0;
    }
    else{
        direction = -1;
    }
}
void Motor::rectifyWaitTime(){
    float absSpeed = 0;
    if(speed>0){
        absSpeed = speed;   
    }
    else if(speed<0){
        absSpeed = -speed;
    }
    if(absSpeed>maxSpeed){
        speed = speed>0?maxSpeed:-maxSpeed;
        absSpeed = maxSpeed;
    }
    waitTime = 0.003*maxSpeed/10-absSpeed*0.0002;
    if(waitTime<0.001){
        waitTime = 0.001;
    }
}
char Motor::execInput(){
    char c = input->getc();
    switch(c){
        case 'i': increaseSpeed();break;
        case 'd': decreaseSpeed();break;
        case 's': setSpeed(0);break;
    }
    if(c>='0' && c<='9'){
        int n = c-'0';
        if(1<=n && n<=5)
            setSpeed((n-6)*2);
        else if(n==0)
            setSpeed(5*2);
        else
            setSpeed((n-5)*2);
    }
    return c;
}
void Motor::setMaxSpeed(int maxSpeed){
    this->maxSpeed = maxSpeed;
}
void Motor::dragDownVoltage(){
    orange = 0; yellow = 0; pink = 0; blue=0;
}