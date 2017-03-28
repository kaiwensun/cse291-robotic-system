# CSE291 Robotic Systems - Team KSK

## Abstraction for motor control
*motor_demo.cpp* is a sample code using the Motor class. In this demo,

* i for increase speed
* d for decrease speed
* s for stop
* 1234567890 are used to set speed
* Currently there are 20 levels of speed, plus a 0-speed level.

How to interperate inputs is determined by the virtual function `execInput()` in Motor.

