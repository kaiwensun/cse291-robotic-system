#include "mbed.h"

DigitalOut myled1(LED1);
DigitalOut myled2(LED2);
DigitalOut myled3(LED3);
DigitalOut myled4(LED4);

int main() {
    int i=0;
    DigitalOut* myled = &myled1;
    while(1) {
        switch(i++%4){
            case 0:myled = &myled1;break;
            case 1:myled = &myled2;break;
            case 2:myled = &myled3;break;
            case 3:myled = &myled4;break;
        }
        *myled = 1;
        wait(0.25);
        *myled = 0;
    }
}

