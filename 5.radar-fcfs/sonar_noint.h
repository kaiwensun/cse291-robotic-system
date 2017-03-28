#ifndef _USE_SONAR_INT

#ifndef _SONAR_NOINT_H_
#define _SONAR_NOINT_H_

#include "mbed.h"

class Sonar{
private:
    DigitalIn  echo;
    DigitalOut trigger;
    int distance;
    int correction;
    Timer sonarTimmer;
public:
    /**
     * Constructor
     * @param echo pin of echo
     * @param trigger pin of trigger
     */
    Sonar(PinName echo, PinName trigger);
    
    /**
     * Measure distance and return it.
     * @return newly measured distance in cm.
     */
    int measureDistance();
    
    /**
     * Get distance without measuring.
     * @return old value of distance in cm.
     */
    int getLastDistance();

};

#endif
#endif
