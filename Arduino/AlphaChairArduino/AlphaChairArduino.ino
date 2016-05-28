#include <SoftwareSerial.h>
#define PPRON 1
#define PPROFF 2
#define FSRON 3
#define FSROFF 4
#define TMPUP 5
#define TMPDOWN 6
int INA = 9;  
int INB = 8;
int LED = 5; // LED를 5번핀에 연결합니다.
int PRESSURE = A0;
int TEMPERATURE = A1;
int MOSFET = 7 ;
bool isFSROn = false;
float Val;
float val;
SoftwareSerial BTSerial(2, 3);

void setup() {
  BTSerial.begin(9600); 
  Serial.begin(9600);   
  pinMode(INA,OUTPUT); 
  pinMode(INB,OUTPUT); 
  pinMode(MOSFET, OUTPUT);
}

void loop() {
  if(BTSerial.available()){
      byte response = BTSerial.read();
      switch(response){
        case PPRON :{
          Serial.println(response);
          digitalWrite(INA,HIGH);
          digitalWrite(INB,LOW); 
          break;
        }
        case PPROFF :{
          Serial.println(response);
          digitalWrite(INA,LOW);
          digitalWrite(INB,LOW); 
          break;
        }
        case FSRON :{
          Serial.println(response);
          isFSROn= true;
          break;
        }
        case FSROFF :{
          Serial.println(response);
          isFSROn = false;
          break;
        }
        case TMPUP :{
          Serial.println(response);
          break;
        }
        case TMPDOWN :{
          Serial.println(response);
          break;
        }
      }
      
  }
  if(isFSROn){
    int fsrData = analogRead(PRESSURE);
    Serial.println("Temperature : "+ String(fsrData));
    BTSerial.println(fsrData);
    delay(1000);
  }
}
