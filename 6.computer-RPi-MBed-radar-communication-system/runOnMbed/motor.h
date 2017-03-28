#ifndef _MOTOR_H_
#define _MOTOR_H_

#include "mbed.h"

    
class Motor
{
private:
    DigitalOut blue;
    DigitalOut pink;
    DigitalOut yellow;
    DigitalOut orange;

    double waitTime;
    int direction;
    Serial* input;
    Serial* output;
    float speed;
    const float speedStep;
    int voltSequence[8][4];
    int maxSpeed;
public:
    /**
     * Constructor
     * @param input keyboard input. can be NULL.
     * @param output debug terminal output. can be NULL.
     * @param blue pin of blue wire
     * @param pink pin of pink wire
     * @param yellow pin of yellow wire
     * @param orange pin of orange wire
     */
    Motor(Serial* input, Serial* output,PinName blue,PinName pink,PinName yellow,PinName orange);
    
    /**
     * Destructor
     */
    ~Motor();
    
    /**
     * Let the motor move one step and stop. Can be called in a while loop so
     * that the motor will continuously move.
     */
    void actionOnce();
    
    /**
     * @return the wait time between 8 phases of motion sequence.
     */
    double getWaitTime();
    
    /**
     * @return current speed
     */
    double getSpeed();
    
    /**
     * increase speed by 1.0
     */
    void increaseSpeed();
    
    /**
     * decrease speed by 1.0
     */
    void decreaseSpeed();
    
    /**
     * set speed. can be negative or positive or 0. If the absolute value of
     * the speed exceeds the maximum, the maximum value will apply. 
     */
    void setSpeed(float speed);
    
    /**
     * Set maximum speed. By default is 10. If the maximum is too small or too
     * big, then the motor may not move at all.
     */
    void setMaxSpeed(int maxSpeed);
    
    /**
     * Set voltage on four wires of motor as 0. Sometime this is required due to
     * insufficient power supply. This will not affect the sequence of wire
     * voltage in the next phase.
     */
    void dragDownVoltage();
private:
    void rectifyDirection();
    void rectifyWaitTime();
    virtual char execInput();
};

#endif