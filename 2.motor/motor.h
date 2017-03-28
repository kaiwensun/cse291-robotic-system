#ifndef _MOTOR_H_
#define _MOTOR_H_

#include "mbed.h"

static DigitalOut blue(p26);
static DigitalOut pink(p25);
static DigitalOut yellow(p24);
static DigitalOut orange(p23);
    
class Motor
{
private:
    double waitTime;
    int direction;
    Serial* input;
    Serial* output;
    float speed;
    const float speedStep;
    int voltSequence[8][4];
public:
    Motor(Serial* input, Serial* output);
    void actionOnce();
    
    double getWaitTime();
    double getSpeed();
    
    void increaseSpeed();
    void decreaseSpeed();
    void setSpeed(int speed);
    
private:
    void rectifyDirection();
    void rectifyWaitTime();
    virtual char execInput();
};

#endif

