#include "mbed.h"
#ifndef _SONAR_INT_H_
#define _SONAR_INT_H_


static Timer sonarTimer;
static volatile int distance;
static int correction;
static volatile bool distReady;

class Sonar2{
private:
    InterruptIn echo;
    DigitalOut trigger;


public:
    Sonar2(PinName echo, PinName trigger);
    ~Sonar2();
    void sendSound();
    int getDistance();
    bool distIsReady();
    void disableReady();
    
private:
    //static void onRise();
    //static void onFall();
};
void onRise();  //call back can't be member func
void onFall();  //call back can't be member func
    
#endif