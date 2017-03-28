#include "mbed.h"
#include "motor.h"

int main() {
    Serial input = Serial(USBTX, USBRX);
    Serial output = Serial(USBTX, USBRX);
    output.printf("%s","\rUsage:\n\
\r  i for increase speed\n\
\r  d for decrease speed\n\
\r  s for stop\n\
\r  1234567890 are used to set speed\n\
");
    Motor motor(&input, &output);
    while(1){
        motor.actionOnce();
    }
}

