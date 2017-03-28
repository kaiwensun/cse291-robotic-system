#include "mbed.h"
void readAcc(int* data);
float toGravity(int value);
I2C i2c(p9,p10);

int acc_dev_addr = 0x32; //accelerometer device read address
int acc_reg_addr = 0xA8;   //starting address of 6 accelerometer registers
    
int main(void){
    
    char initwrite[2] = {0x20,0x27};
    i2c.frequency(100000);
    i2c.write(acc_dev_addr|0,initwrite,2);
    
    printf("\n\rtest start\n\r");
    int data[3] = {-1,-1,-1};
    while(1){
        readAcc(data);
        printf("\racc=[x=%.2fm/s^2, y=%.2fm/s^2, z=%.2fm/s^2]\n\r",toGravity(data[0]),toGravity(data[1]),toGravity(data[2]));
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

inline float toGravity(int value){
    return value*9.8/16832;
}