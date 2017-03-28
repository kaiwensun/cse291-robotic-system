#ifndef _USE_SONAR_INT
#include "sonar_noint.h"
Sonar::Sonar(PinName echo, PinName trigger):echo(echo),trigger(trigger){
    distance=-1;
    sonarTimmer.reset();
    sonarTimmer.start();
    while (echo==2) {};
    sonarTimmer.stop();
    correction = sonarTimmer.read_us();
}
int Sonar::measureDistance(){
    trigger = 1;
    sonarTimmer.reset();
    wait_us(10.0);
    trigger = 0;

    while (echo==0) {};
    sonarTimmer.start();

    while (echo==1) {};
    sonarTimmer.stop();

    distance = (sonarTimmer.read_us()-correction)/58.0;
    return distance;
}
int Sonar::getLastDistance(){
    return distance;
}
#endif