/*
isr4pi.c
D. Thiebaut
based on isr.c from the WiringPi library, authored by Gordon Henderson
https://github.com/WiringPi/WiringPi/blob/master/examples/isr.c

Compile as follows:

    arm-buildroot-linux-gnueabi-gcc -o out.elf test.c -lwiringPi -lpthread

Run as follows:

    sudo ./isr4pi

 */
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
//#include "./wiringPi/wiringPi.h"
#include <wiringPi.h>

// Use GPIO Pin 17, which is Pin 0 for wiringPi library
// Use GPIO Pin 4, which is Pin 7 for wiringPi library

#define BUTTON_PIN 7


// the event counter 
volatile int eventCounter = 0;

// -------------------------------------------------------------------------
// myInterrupt:  called every time an event occurs
void myInterrupt(void) {
   eventCounter++;
}


// -------------------------------------------------------------------------
// main
int main(void) {
  // sets up the wiringPi library
  if (wiringPiSetup() < 0) {
      fprintf (stderr, "Unable to setup wiringPi: %s\n", strerror (errno));
      return 1;
  }

  // set Pin 17/0 generate an interrupt on high-to-low transitions
  // and attach myInterrupt() to the interrupt
  if ( wiringPiISR(BUTTON_PIN, INT_EDGE_FALLING, &myInterrupt) < 0 ) {
      fprintf (stderr, "Unable to setup ISR: %s\n", strerror (errno));
      return 1;
  }

  // display counter value every second.
  while ( 1 ) {
    printf( "%d\n", eventCounter );
    //eventCounter = 0;
    delay( 1000 ); // wait 1 second
  }

  return 0;
}
