#include <SoftwareSerial.h>
#include <Thread.h>
#define PPR_ON "a"
#define PPR_OFF "b"
#define FSR_ON "c"
#define FSR_OFF "d"
#define TMP_ON "e"
#define TMP_OFF "f"

Thread fsrThread = Thread(); // For measuring Pressure data in other thread

//  Mosfet
int MOSFET_C1 = 6;
int MOSFET_C2 = 7;

//  UNO board
int INB = 8;
int INA = 9;
//  Analog read  
int PRESSURE = A0; 
int TEMPERATURE = A1;

bool isFSROn = false;
bool isTMPOn = false;

int MAX_TMP;

SoftwareSerial BTSerial(2, 3); // (RX, TX)

void setup() {
  BTSerial.begin(9600); 
  Serial.begin(9600);   
  pinMode(INA,OUTPUT); 
  pinMode(INB,OUTPUT); 
  pinMode(MOSFET_C1, OUTPUT);
  pinMode(MOSFET_C2, OUTPUT);
  fsrThread.onRun(readPressure);
  fsrThread.enabled = false;
  fsrThread.setInterval(1000);
}

void loop() {

  // char to String data received from Android
  String response = "";
  while(BTSerial.available()){
    char tempChar = BTSerial.read();
    if(tempChar != '\n')
    response.concat(tempChar);
  }
  if(response.length()>1){
    String resInt = response.substring(1);
    response = response.substring(0,1);
    MAX_TMP = resInt.toInt();
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
    isFSROn= true;
  }else if(response.equals(FSR_OFF)){
    Serial.println(response);
    isFSROn= false;
  }else if(response.equals(TMP_ON)){
    Serial.println(response);
    isTMPOn = true;
  }else if(response.equals(TMP_OFF)){
    Serial.println(response);
    MAX_TMP = 0;
    isTMPOn = false;
  }
  
  if(isTMPOn){
    readTmp();
  }
  
  if(isFSROn){
    fsrThread.enabled = true;
    if(fsrThread.shouldRun())
    fsrThread.run();
  }else{
    fsrThread.enabled = false;
  }
}

// Runnable for fsrThread
void readPressure(){
  int fsrData = analogRead(PRESSURE);
  Serial.println("Pressure : "+ String(fsrData));
  BTSerial.println(fsrData);
}

void readTmp(){
  float analogTmp = (float)analogRead(TEMPERATURE)*5/1024;
  float currTmp = 100*(analogTmp-0.5);
  if(currTmp < MAX_TMP){
    digitalWrite(MOSFET_C1,HIGH);
    digitalWrite(MOSFET_C2,HIGH);
  }else{
    digitalWrite(MOSFET_C1,LOW);
    digitalWrite(MOSFET_C2,LOW);
  }
}

