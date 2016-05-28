#include <SoftwareSerial.h>
#define PPR_ON 1
#define PPR_OFF 2
#define FSR_ON 3
#define FSR_OFF 4
#define TMP_UP 5
#define TMP_DOWN 6
int INA = 9;  
int INB = 8;
int LED = 5; // LED를 5번핀에 연결합니다.
int PRESSURE = A0;
int TEMPERATURE = A1;
int MOSFET = 7 ;

bool isFSROn = false;
bool isTMPOn = false;
float currTmp;
int MAX_TEMP;
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
        case PPR_ON :{
          Serial.println(response);
          digitalWrite(INA,HIGH);
          digitalWrite(INB,LOW); 
          break;
        }
        case PPR_OFF :{
          Serial.println(response);
          digitalWrite(INA,LOW);
          digitalWrite(INB,LOW); 
          break;
        }
        case FSR_ON :{
          Serial.println(response);
          isFSROn= true;
          break;
        }
        case FSR_OFF :{
          Serial.println(response);
          isFSROn = false;
          break;
        }
        case TMP_UP :{
          Serial.println(response);
          MAX_TEMP = response;
          isTMPOn = true;
          break;
        }
        case TMP_DOWN :{
          Serial.println(response);
          isTMPOn = false;
          break;
        }
      }
      
  }
  if(isTMPOn){
    temperature(MAX_TEMP);
  }
  
  if(isFSROn){
    int fsrData = analogRead(PRESSURE);
    Serial.println("Temperature : "+ String(fsrData));
    BTSerial.println(fsrData);
    delay(1000);
  }
}

void temperature(int MAX_TEMP){
  float analogTmp = (float)analogRead(TEMPERATURE)*5/1024;
  currTmp = 100*(analogTmp-0.5);
  if(currTmp < MAX_TEMP){
    digitalWrite(MOSFET,HIGH);
  }else{
    digitalWrite(MOSFET,LOW);
  }
}

