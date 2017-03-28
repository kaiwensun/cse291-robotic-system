#ifndef _SONAR_H_
#define _SONAR_H_

#include "mbed.h"

static DigitalOut trigger(p6);
static DigitalIn  echo(p7);
static Timer sonarTimmer;

class Sonar{
private:
    int distance;
    int correction;
public:
    Sonar();
    int measureDistance();  //measure distance and return it
    int getLastDistance();  //get distance without measuring

};

#endif

