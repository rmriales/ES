#include <IRremote.h>
#include <string.h>

IRsend irsend;

boolean enable = false;
int motionPin = 2;
int IRPin = 11;
int relay_pin = 8;
long unsigned int timeOut = (1 * 60000); //will be used to count to 1 minutes.
long unsigned int lowTime; // used to get time motion detector has been low
boolean EquipOn = false;
boolean locked = true;
boolean takeTime;


void TVPowerOn(unsigned long code) {
    irsend.sendNEC(code, 32);
}

void Lighton() {
  digitalWrite(relay_pin, LOW);
}

void Lightoff() {
  digitalWrite(relay_pin, HIGH);
}



void setup() {
  // put your setup code here, to run once:
  pinMode(IRPin, OUTPUT);
  pinMode(motionPin, INPUT);
  pinMode(relay_pin, OUTPUT);
  digitalWrite(relay_pin, HIGH);
  Serial.begin(9600); //start serial port at 115200 bits per second
  digitalWrite(motionPin, LOW);
  //give the sensor some time to calibrate
  //Serial.println("calibrating sensor: ");
  for (int i = 29; i >= 0; i--) {
   //Serial.println(i);
   delay(1000);
  }
  //Serial.println(" done");
  //Serial.println("SENSOR ACTIVE");
  delay(50);
}


void loop() {
  if (enable == true) {
    //Make app to change these settings/check statuses.
    if (digitalRead(motionPin) == HIGH) {
      if (EquipOn == false) {
        //Serial.println("Turning On!");
        TVPowerOn(0x20DF10EF);
        Lighton();
        EquipOn = true;
      }
      if (locked) {
        locked = false;
      }
      takeTime = true;
    }
    if (digitalRead(motionPin) == LOW) {

      if (takeTime) {
        lowTime = millis();
        takeTime = false;
      }

      if (!locked && millis() - lowTime > timeOut) {
        locked = true;
        TVPowerOn(0x20DF10EF);
        Lightoff();
      }
    }
  }

  if (Serial.available() > 0) {
    char module = Serial.read();
    char val;
    delay(100);
    if (Serial.available() > 0) {
      val = Serial.read();
    }
    switch (module) {
      case 't': timeOut = (int(val) - 48) * 60000; /*Serial.println(timeOut);*/ break; //timeout
      case 'l': if (val == '0') { //lights on/off
          Lightoff(); //Serial.println(val);
        }
        else {
          Lighton(); //Serial.println(val);
        } break;
      case 'e': //enable
        if (val == '0'){
          enable = false; //Serial.println("disabled");
        }else{
          enable = true; //Serial.println("enabled");
        }break;
      case 'u': TVPowerOn(0x20DFA25D); break; //up button
      case 'd': TVPowerOn(0x20DF629D); break; //down button
      case 'f': TVPowerOn(0x20DFE21D); break; //left button on remote
      case 'r': TVPowerOn(0x20DF12ED); break; //right button
      case 'v': TVPowerOn(0x20DFB44B); break; //vizio button
      case 'o': TVPowerOn(0x20DF22DD); break; //ok button
      case 'p': TVPowerOn(0x20DF10EF); break; //power button
      case 'b': TVPowerOn(0x20DF52AD); break; //back button
      case 'x': TVPowerOn(0x20DF926D); break; //exit button
      case 'a': TVPowerOn(0x20DF57A8); break; //Amazon Video
      case 'n': TVPowerOn(0x20DFD728); break; //Netflix
      case 'i': TVPowerOn(0x20DFF40B); break; //InputButton
    }
  }
}


