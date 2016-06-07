#include <SoftwareSerial.h>
#include <Thread.h>
#include <QueueList.h>

QueueList <int> q;
int sum ;
int firstVal;

#define PPR_ON "a"
#define PPR_OFF "b"
#define FSR_ON "c"
#define FSR_OFF "d"
#define TMP_ON "e"
#define TMP_OFF "f"

Thread fsrThread = Thread(); // For measuring Pressure data in other thread

int countFSRThread = 0;
int count_0 = 0;
int count_1 = 0;
int count_2 = 0;
int count_3 = 0;    

int initPosture_0;
int initPosture_1;
int initPosture_2;
int initPosture_3;

float percentage = 0.25; 

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
    
    initPosture_0 = 0;
    initPosture_1 = 1;
    initPosture_2 = 2;
    initPosture_3 = 3;
    
    countFSRThread = 0;
    count_0 = 0;
    count_1 = 0;
    count_2 = 0;
    count_3 = 0;
    
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
  countFSRThread++;
  int frontRight = analogRead(PRESSURE_0)*5;
  int frontLeft = analogRead(PRESSURE_1)*5;
  int backRight = analogRead(PRESSURE_2)*5;
  int backLeft = analogRead(PRESSURE_3)*5;
  Serial.println("0 : "+ String(frontRight));
  Serial.println("1 : "+ String(frontLeft));
  Serial.println("2 : "+ String(backRight));
  Serial.println("3 : "+ String(backLeft));
  
  if(countFSRThread <= 5){
    initPosture_0 = frontRight;
    initPosture_1 = frontLeft;
    initPosture_2 = backRight;
    initPosture_3 = backLeft;
  }else{
    // 자세 측정 및 알람
    Serial.println("your initial post : "+String(initPosture_0));
    // send 'posture msg' to android
    if(frontRight>initPosture_0*(percentage+1)){
      count_0++;
      if(count_0 >= 5){
        BTSerial.println("fr");
        count_0 = 0;
      }
    }else{
      count_0 = 0;
    }  
  }
}

void readTmp(){
    digitalWrite(MOSFET_C1,HIGH);
    Serial.println("temperature on");  
}

