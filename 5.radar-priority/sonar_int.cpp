#include "sonar_int.h"

Sonar2::Sonar2(PinName echo, PinName trigger):echo(echo),trigger(trigger){
    distance=0;
    distReady = false;
    correction = 0;
    sonarTimer.reset();
    sonarTimer.start();
    while (echo==2) {};
    sonarTimer.stop();
    correction = sonarTimer.read_us();
    this->echo.rise(&onRise);
    this->echo.fall(&onFall);
}
Sonar2::~Sonar2(){
    //nothing to do.
}
int Sonar2::getDistance(){
    return distance;
}
void Sonar2::sendSound(){
    distReady = false;
    trigger = 1;
    sonarTimer.reset();
    wait_us(10.0);
    trigger = 0;
}
bool Sonar2::distIsReady(){
    return distReady;
}
void Sonar2::disableReady(){
    distReady=false;
}
void onRise() {
    sonarTimer.reset();
    sonarTimer.start();
}
void onFall() {
    sonarTimer.stop();
    distance = (sonarTimer.read_us()-correction)/58.0;
    distReady = true;
}

