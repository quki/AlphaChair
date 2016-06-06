#include <SoftwareSerial.h>
#include <Thread.h>
#define PPR_ON "a"
#define PPR_OFF "b"
#define FSR_ON "c"
#define FSR_OFF "d"
#define TMP_ON "e"
#define TMP_OFF "f"

Thread fsrThread = Thread(); // For measuring Pressure data in other thread

//  Mosfet channel
int MOSFET_C1 = 10;
int MOSFET_C2 = 7;
//  UNO board
int INB = 8;
int INA = 9;
//  Analog read  
int PRESSURE_0 = A0; 
int PRESSURE_1 = A1; 
int PRESSURE_2 = A2; 
int PRESSURE_3 = A3; 

bool isTMPOn = false;

SoftwareSerial BTSerial(2, 3); // (RX, TX)

void setup() {
  BTSerial.begin(9600); 
  Serial.begin(9600);   
  pinMode(INA,OUTPUT); 
  pinMode(INB,OUTPUT); 
  pinMode(MOSFET_C1, OUTPUT);
  pinMode(MOSFET_C2, OUTPUT);
  digitalWrite(MOSFET_C1,LOW);
  fsrThread.onRun(readPressure);
  fsrThread.enabled = false;
  fsrThread.setInterval(1000);
}

void loop() {

  // char to String data received from Android
  String response = "";
  while(BTSerial.available()){
    char tempChar = BTSerial.read();
    if(tempChar == '\n'){
      break;
    }
    response.concat(tempChar);
  }
  
  if(response.equals(PPR_ON)){
    Serial.println(response);
    digitalWrite(INA,HIGH);
    digitalWrite(INB,LOW);
  }else if(response.equals(PPR_OFF)){
    Serial.println(response);
    digitalWrite(INA,LOW);
    digitalWrite(INB,LOW);
  }else if(response.equals(FSR_ON)){
    Serial.println(response);
    fsrThread.enabled = true;
  }else if(response.equals(FSR_OFF)){
    Serial.println(response);
    fsrThread.enabled = false;
  }else if(response.equals(TMP_ON)){
    Serial.println(response);
    isTMPOn = true;
  }else if(response.equals(TMP_OFF)){
    Serial.println(response);
    digitalWrite(MOSFET_C1,LOW);
    isTMPOn = false;
  }

  if(isTMPOn)
    readTmp();
  
  if(fsrThread.shouldRun())
    fsrThread.run();
}

// Runnable for fsrThread
void readPressure(){
  int frontRight = analogRead(PRESSURE_0)*5;
  int frontLeft = analogRead(PRESSURE_1)*5;
  int backRight = analogRead(PRESSURE_2)*5;
  int backLeft = analogRead(PRESSURE_3)*5;
  Serial.println("0 : "+ String(frontRight));
  Serial.println("1 : "+ String(frontLeft));
  Serial.println("2 : "+ String(backRight));
  Serial.println("3 : "+ String(backLeft));

  // send 'posture msg' to android
  if(frontRight>2000){
    BTSerial.println("fr");
  }
  if(frontLeft>2000){
    BTSerial.println("fl");
  }
  if(backRight>2000){
    BTSerial.println("br");
  }
  if(backLeft>2000){
    BTSerial.println("bl");
  }
}

void readTmp(){
    digitalWrite(MOSFET_C1,HIGH);
    Serial.println("temperature on"));  
}

