#include "mbed.h"
#include "math.h"
void readMag(int* data);
float toAngle(float y, float x);
I2C i2c(p9,p10);

int mag_dev_addr = 0x3C; // magnetometer device read address
int mag_reg_addr = 0x03;   //starting address of 6 magnetometer registers
float ang_y_x = 0.0;
float ang_z_y = 0.0;
float ang_x_z = 0.0;

int main(void){
    char initwrite1[4] = {0x00,0x10,0x40,0x00};
    i2c.frequency(100000);
    i2c.write(mag_dev_addr|0,initwrite1,4);
    
    printf("\n\rtest start\n\r");
    int data[3] = {-1,-1,-1};
    while(1){
        readMag(data);
        printf("\rang_y_x=[x=%.2fm/s^2, ang_z_y=%.2fm/s^2, ang_x_z=%.2fm/s^2] \n\r", ang_y_x, ang_z_y, ang_x_z);
        wait(1);
    }
}

void readMag(int* data){
    char data_rcv[6];
    char data_snd[1] = {(char)mag_reg_addr};
    int a = i2c.write(mag_dev_addr|0, data_snd, 1, true);
    int b = i2c.read(mag_dev_addr|1, data_rcv, 6);
    
    for(int i=0;i<3;i++){
        data[i]=data_rcv[i*2];
        data[i]<<=8;
        data[i]|=data_rcv[i*2+1];
        data[i]=~((short)(data[i]))+1;
    }
    ang_y_x = toAngle((float)data[2],(float)data[0]);
    ang_z_y = toAngle((float)data[1],(float)data[2]);
    ang_x_z = toAngle((float)data[0],(float)data[2]);
}

inline float toAngle(float y, float x){
    return atan2(y,x)/3.1415926*180;
}