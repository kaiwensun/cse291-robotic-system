#include "mbed.h"
#include "Sonar.h"
int main(){
    Sonar sonar;
    while(1){
        int dist = sonar.measureDistance();
        printf("\r%d cm\n",dist);
        wait(0.2);
    }
}

