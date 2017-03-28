#include "compass.h"

Compass::Compass(PinName sda, PinName scl):i2c(sda,scl),
mag_dev_addr(0x3C),
mag_reg_addr(0x03),
ang_y_x(0.0),
ang_z_y(0.0),
ang_x_z(0.0)
{
    char initwrite[4] = {0x00,0x10,0x20,0x00};
    i2c.frequency(100000);
    i2c.write(mag_dev_addr|0,initwrite,4);
    
    /*
    scale_x = 307;
    scale_y = 51;
    scale_z = 327;
    scale = 1000;
    
    bias_x = 83;
    bias_y = -194;
    bias_z = 134;
    
    */
    scale_x = 508;
    scale_y = 504;
    scale_z = 553;
    scale = 1000;
    
    bias_x = 87;
    bias_y = -194;
    bias_z = -37;
    
 
    
}
void Compass::setScale(int x,int y,int z,int scale){
    scale_x = x;
    scale_y = y;
    scale_z = z;
    this->scale = scale;
}
void Compass::setBias(int x,int y,int z){
    bias_x = x;
    bias_y = y;
    bias_z = z;
}
float Compass::getHeading(bool update){
    int data[3] = {-1};
    get3DMagStrength(data, update);
    int x = data[0];
    int y = data[1];
    float heading = toAngle((float)y,float(x));
    return heading;
}
float Compass::toAngle(float y, float x){
     return atan2(y,x)*180/3.1415926+180;
}
void Compass::measureRawData(){
    char data_rcv[6];
    char data_snd[1] = {(char)mag_reg_addr};
    int a = i2c.write(mag_dev_addr|0, data_snd, 1, true);
    int b = i2c.read(mag_dev_addr|1, data_rcv, 6);
    
    int8_t* p = (int8_t*)raw_data;
    for(int i=0;i<3;i++){
        *p++ = data_rcv[i*2+1];
        *p++ = data_rcv[i*2];
    }
}
void Compass::get3DMagStrength(int* strength, bool update){
    if(update){
        measureRawData();
    }
    strength[0] = (raw_data[0]-bias_x)*scale/scale_x;
    strength[2] = (raw_data[1]-bias_y)*scale/scale_y;
    strength[1] = (raw_data[2]-bias_z)*scale/scale_z;
    
    strength[0] = strength[0];
    strength[1] = strength[1];
/*    if(strength[0] > 0) strength[0] = strength[0]*30/45;
    else strength[0] = strength[0]*35/25;
*/
}

void Compass::calibrate(bool update){
    const int N = 10000;
    printf("\n\rrotate the compass arbitraryly!\n");
    int mins[3] = {0x7fffffff,0x7fffffff,0x7fffffff};
    int maxs[3] = {0x80000000,0x80000000,0x80000000};
    printf("\n");
    for(int i=0;i<N;i++){
        if(i%20==0)
            printf("\r%d%%",i*100/N);
        measureRawData();
        for(int i=0;i<3;i++){
            mins[i]=mins[i]>raw_data[i]?raw_data[i]:mins[i];
            maxs[i]=maxs[i]<raw_data[i]?raw_data[i]:maxs[i];
        }
        wait(0.01);
    }
    printf("\n\r");
    printf("\rmax=[%d,%d,%d], min=[%d,%d,%d]\n",maxs[0],maxs[1],maxs[2],mins[0],mins[1],mins[2]);
    int tmpbx = (mins[0]+maxs[0])/2;
    int tmpby = (mins[1]+maxs[1])/2;
    int tmpbz = (mins[2]+maxs[2])/2;
    
    int tmpsx = (maxs[0]-mins[0])/2; 
    int tmpsy = (maxs[1]-mins[1])/2;
    int tmpsz = (maxs[2]-mins[2])/2;
    scale = 100;
    printf("\rbias_x=%d,bias_y=%d,bias_z=%d,scale_x=%d,scale_y=%d,scale_z=%d\n",tmpbx,tmpby,tmpbz,tmpsx,tmpsy,tmpsz);
    if(update){
        bias_x = tmpbx;
        bias_y = tmpby;
        bias_z = tmpbz;
        
        scale_x = tmpsx;
        scale_y = tmpsy;
        scale_z = tmpsz;
        scale = 100;
    }
}
/* //not used
double Compass::getMean(int val[], int N){
    int sum = 0;
    for(int i=0;i<N;i++){
        sum+=val[i];
    }
    return (double)sum/N;
}

double Compass::getStdev(int val[], int N, double mean){
    int sum = 0;
    for(int i=0;i<N;i++){
        sum+=(val[i]-mean)*(val[i]-mean);
    }
    return sqrt((double)sum/N);
} 
*/