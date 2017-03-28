#include "mbed.h"
#include "math.h"

I2C i2c(p9,p10);

void readAcc(int* data);
void readMag(int* data);
float heading(float y, float x);
float acceleration(int value);

int acc_dev_addr = 0x32; //accelerometer device read address
int acc_reg_addr = 0xA8;   //starting address of 6 accelerometer registers
int mag_dev_addr = 0x3C; // magnetometer device read address
int mag_reg_addr = 0x03;   //starting address of 6 magnetometer registers

float ang_y_x = 0.0;
float ang_z_y = 0.0;
float ang_x_z = 0.0;

int main(void){
    char initwriteA[2] = {0x20,0x27};
    char initwriteM[4] = {0x00,0x10,0x40,0x00};
    
    i2c.frequency(100000);
    i2c.write(acc_dev_addr|0,initwriteA,2);
    i2c.write(mag_dev_addr|0,initwriteM,4);
    
    printf("\n\rtest start\n\r");
    int dataM[3] = {-1,-1,-1};
    int dataA[3] = {-1,-1,-1};
    
    while(1){
        readMag(dataM);
        printf("\rang_y_x=[x=%.2fdeg, ang_z_y=%.2fdeg, ang_x_z=%.2fdeg] \n\r", ang_y_x, ang_z_y, ang_x_z);
        wait(1);
        
        readAcc(dataA);
        printf("\racc=[x=%.2fm/s^2, y=%.2fm/s^2, z=%.2fm/s^2]\n\r",acceleration(dataA[0]),acceleration(dataA[1]),acceleration(dataA[2]));
        wait(0.1);
    }
}

void readAcc(int* data){
    char data_rcv[6];
    char data_snd[1] = {(char)acc_reg_addr};
    int a = i2c.write(acc_dev_addr|0, data_snd,1,true);
    int b = i2c.read(acc_dev_addr|1, data_rcv,6);
    
    for(int i=0;i<3;i++){
        data[i]=data_rcv[i*2+1];
        data[i]<<=8;
        data[i]|=data_rcv[i*2];
        data[i]=~((short)(data[i]))+1;
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
    ang_y_x = heading((float)data[2],(float)data[0]);
    ang_z_y = heading((float)data[1],(float)data[2]);
    ang_x_z = heading((float)data[0],(float)data[2]);
}

inline float heading(float y, float x){
    return atan2(y,x)/3.1415926*180;
}

inline float acceleration(int value){
    return value*9.8/16832;
}