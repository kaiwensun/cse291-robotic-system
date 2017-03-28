#include "Sonar.h"
Sonar::Sonar(){
    distance=-1;
    sonarTimmer.reset();
    sonarTimmer.start();
    while (echo==2) {};
    sonarTimmer.stop();
    correction = sonarTimmer.read_us();
    printf("Approximate software overhead timer delay is %d uS\n\r",correction);
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

