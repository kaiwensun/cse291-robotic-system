#ifndef _COMPASS_H_
#define _COMPASS_H_

#include "mbed.h"

class Compass{
private:

    I2C i2c;    
    const int8_t mag_dev_addr; // magnetometer device read address
    const int8_t mag_reg_addr; //starting address of 6 magnetometer registers
    int ang_y_x;
    int ang_z_y;
    int ang_x_z;
    
    int scale_x;
    int scale_y;
    int scale_z;
    int scale;
    
    float bias_x;
    float bias_y;
    float bias_z;
    
    /* WARNING: raw_data[] is in the order of XZY, not XYZ! */
    int16_t raw_data[3];
    
public:

    /**
     * Constructor
     * @param sda serial data pin
     * @param scl serial clock pin
     */
    Compass(PinName sda, PinName scl);
    
    /**
     * Get megnatic values.
     * @param data is a length-3 array which will be assigned by the xyz values.
     */
    void readAngles(float* angles);
    
    /**
     * Get orientation of heading.
     * @param update by default is true. if true, will actually measure. if false, will use old values.
     */
    float getHeading(bool update=true);
    
    /**
     * Get magnetic strengths in 3 directions.
     * @param strength is a length-3 array. will be assigned by the strength at X-Y-Z directions.
     * @param update by default is true. if true, will actually measure. if false, will use old values.
     */
    void get3DMagStrength(int* strength, bool update=true);
    
    /**
     * calibrate scales and bias of magnetic strengths. values will be printed
     * to stdio.
     * @param update by default is false. if true, will automatically adjust
     * scales and bias. If false, will only print values and not auto-adjust.
     */
    void calibrate(bool update=false);
    
    void setScale(int x,int y,int z,int scale=1000);
    void setBias(int x,int y,int z);

private:
    inline float toAngle(float y, float x);
    void measureRawData();
    /* not used
    double getMean(int val[], int N);
    double getStdev(int val[], int N, double mean);
    */
    
};

#endif