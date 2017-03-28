#include "mbed.h"

static InterruptIn echo(p7);
static DigitalOut trigger(p6);
static DigitalOut led(LED1);
 
static Timer sonarTimer;
static int distance = 0;

void sendSound(){
    trigger = 1;
    sonarTimer.reset();
    wait_us(10.0);
    trigger = 0;
}

void onRise() {
    sonarTimer.reset();
    sonarTimer.start();
}
void onFall() {
    sonarTimer.stop();
    distance = sonarTimer.read_us()/58.0;
    sendSound();
}
int main() {
     echo.rise(&onRise);
     echo.fall(&onFall);
     sendSound();
     while(1) {
         printf("\r%d cm\n",distance);
     }
 }