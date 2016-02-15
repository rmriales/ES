#include <IRremote.h>

IRsend irsend;

int motionPin = 2;
int IRPin = 11;
int relay_pin = 8;
long unsigned int timeOut = (1*60000); //will be used to count to 1 minutes.
long unsigned int lowTime; // used to get time motion detector has been low
boolean EquipOn = false;
boolean locked = true;
boolean takeTime;
void setup() {
  // put your setup code here, to run once:
pinMode(IRPin, OUTPUT);
pinMode(motionPin, INPUT);
Serial.begin(9600); //start serial port at 115200 bits per second
digitalWrite(motionPin, LOW);
  //give the sensor some time to calibrate
  Serial.print("calibrating sensor ");
    for(int i = 0; i < 60;i++){
      Serial.print(".");
      delay(1000);
      }
    Serial.println(" done");
    Serial.println("SENSOR ACTIVE");
    delay(50);
  }


void loop() {
  //Make app to change these settings/check statuses.
if(digitalRead(motionPin) == HIGH){
  Serial.println("Motion Detected!");
  if(EquipOn == false){
    Serial.println("Turning On!");
    TVPowerOn();
    Lighton();
    EquipOn = true;
  }
  if(locked){
    locked = false;
  }
  takeTime = true;
}
if(digitalRead(motionPin) == LOW){

 if(takeTime){
  lowTime = millis();
  takeTime = false;
 }
  
  if(!locked && millis() - lowTime > timeOut){
    locked = true;
    TVPowerOn();
    Lightoff();
  }
}
 
while(Serial.available() > 0) {
  static char input[16];    //used to hold the sent number
  static uint8_t i;         //used to index the input matrix
  char c = Serial.read();  //reads each char at the input

    if(c != '\r' && i < 15){ //assuming "Carriage Return" is chosen in the Serial monitor as the line ending character
      input[i++] = c;
    }
    
    else{ //if input is full or Serial is fully read
      input[i] = '\0';
      i = 0;    
      timeOut = atoi(input);
      Serial.println(timeOut);
      break;
    }
  }
}

void TVPowerOn(){
  for (int i = 0; i < 3; i++) {
    irsend.sendNEC(0x20DF10EF, 32);
    delay(40);
  }
}

void Lighton(){
  digitalWrite(relay_pin, LOW);
}

void Lightoff(){
  digitalWrite(relay_pin, HIGH);
}


