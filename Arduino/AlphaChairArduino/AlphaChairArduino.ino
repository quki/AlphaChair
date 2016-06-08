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

int count_0 = 0;
int count_1 = 0;
int count_2 = 0;
int count_3 = 0;
int count_4 = 0;
int count_5 = 0;   

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
  
  int frontRight = analogRead(PRESSURE_0)*5;
  int frontLeft = analogRead(PRESSURE_1)*5;
  int backRight = analogRead(PRESSURE_2)*5;
  int backLeft = analogRead(PRESSURE_3)*5;
  
  Serial.println("frontRight : "+ String(frontRight));
  Serial.println("frontLeft : "+ String(frontLeft));
  Serial.println("backRight : "+ String(backRight));
  Serial.println("backLeft : "+ String(backLeft));

    // send 'posture msg' to android
    sendPostureMsg(frontRight,frontLeft,backRight,backLeft);
}

void readTmp(){
    digitalWrite(MOSFET_C1,HIGH);
    Serial.println("temperature on");  
}
void sendPostureMsg(int frontRight,int frontLeft, int backRight, int backLeft){

    if(frontRight>=10 || frontLeft>=10 || backRight>=10 || backLeft>=10){
      // 왼쪽으로 기울임 : 0
    if(frontRight<100 && backRight<100){
        count_0++;
        if(count_0 >= 5){
          BTSerial.println("몸을 왼쪽으로 기울이지 마세요!");
          count_0 = 0;
        }
        
      // 다리꼬기 : 오른쪽 다리 올리기 : 1  
      }else if(frontRight <100){
        count_1++;
        if(count_1 >= 5){
          BTSerial.println("오른쪽 다리를 꼬지마세요!");
          count_1 = 0;
        }
      }else{
          count_0 = 0;
          count_1 = 0;
      }

    // 오른쪽으로 기울임 : 2
    if(frontLeft<100 && backLeft<100){
        count_2++;
        if(count_2 >= 5){
          BTSerial.println("몸을 오른쪽으로 기울이지 마세요!");
          count_2 = 0;
        }
        
      // 다리꼬기 : 왼쪽 다리 올리기 : 3  
      }else if(frontLeft <100){
        count_3++;
        if(count_3 >= 5){
          BTSerial.println("왼쪽다리를 꼬지마세요!");
          count_3 = 0;
        }
      }else{
          count_2 = 0;
          count_3 = 0;
      }
      
    // 발끝 들기 : 4
    if(frontRight<100  && frontLeft<100){
        count_4++;
        if(count_4 >= 5){
          BTSerial.println("발끝을 들지 마세요!");
          count_4 = 0;
        }
    }else{
        count_4 = 0;
    }

    // 엎드리기 && 눕기 : 5
    if(backRight<100  && backLeft<100){
        count_5++;
        if(count_5 >= 5){
          BTSerial.println("엎드리기거나 눕지 마세요!");
          count_5 = 0;
        }
    }else{
        count_5 = 0;
    }
  }
}
  

