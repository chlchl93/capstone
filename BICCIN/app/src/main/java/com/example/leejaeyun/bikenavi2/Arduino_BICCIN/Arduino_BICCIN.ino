#include  <SoftwareSerial.h>

  //블루투스 통신
  SoftwareSerial BTSerial(11,12);

  //초음파센서1
  int distance1;
  int trig1 = 2;
  int echo1 = 3;
  int now1 = 0;
  int before1= 0;

  //초음파센서2
  int distance2;
  int trig2 = 4;
  int echo2 = 5;
  int now2 = 0;
  int before2= 0;

  //진동센서
  int piezoADC1;
  float piezoV1;
  //진동횟수측정
  int piezoADC2;
  float piezoV2;
   
  
  int redistance1;
  int redistance2;
  
  void setup()
  {
      Serial.begin(9600); //시리얼통신
      BTSerial.begin(9600); //블루투스통신

      //초음파센서1 핀설정
      pinMode(trig1,OUTPUT);
      pinMode(echo1,INPUT);
      
      //초음파센서2 핀설정
      pinMode(trig2,OUTPUT);
      pinMode(echo2,INPUT);
   }

   void loop()
  {
     //초음파센서1 거리측정,출력
     digitalWrite(trig1,HIGH);
     delayMicroseconds(10);   
     digitalWrite(trig1,LOW);
     distance1=pulseIn(echo1,HIGH) * 17 / 1000;
     Serial.print("D1:");
     Serial.print(distance1);
     Serial.println("cm");

     //초음파센서2 거리측정,출력
     digitalWrite(trig2,HIGH);
     delayMicroseconds(10);   
     digitalWrite(trig2,LOW);
     distance2=pulseIn(echo2,HIGH) * 17 / 1000;
     Serial.print("D2:");
     Serial.print(distance2);
     Serial.println("cm");
     
     //진동센서 진동값측정,출력(0~10)
     piezoADC1 = analogRead(0);
     piezoV1 = piezoADC1 / 1023.0 * 10.0;
     Serial.println(piezoV1);

     //현재 거리값
     now1 = distance1;   
     now2 = distance2;

     //사고감지(측정된 거리값이 이전에 측정한 값과 차이가 200이상 발생할시 사고로 판단)
     if((before1 != 0) && ( 200  <= (now1-before1)) && ( 200  <= (now2-before2)) )
     {
      delay(10000); //10초 대기후에 다시 거리를 재서, 거리가 여전히 큰값이면 사고 발생
      
      digitalWrite(trig1,HIGH);
      delayMicroseconds(10);   //10 마이크로 초 동안
      digitalWrite(trig1,LOW);
      redistance1=pulseIn(echo1,HIGH) * 17 / 1000;

      digitalWrite(trig2,HIGH);
      delayMicroseconds(10);   //10 마이크로 초 동안
      digitalWrite(trig2,LOW);
      redistance2=pulseIn(echo2,HIGH) * 17 / 1000;
      
      if((now1 - redistance1)<20 && (now2 - redistance2)<20)//재측정과 직전값의 차이가 20이하이면 사고로 감지
      {
         Serial.println("accidentoccur");
         BTSerial.println("accidentoccur");
       }
       
      }

      if(piezoV1 >7.0)//진동값이 1~10중에서 7이상일 경우
      {
        int count = 0;
        int i = 0;
        delay(1000);
        for(i =0 ; i<=10 ; i++){
             piezoADC2 = analogRead(0);
             piezoV2 = piezoADC2 / 1023.0 * 10.0;
             Serial.println(piezoV2);
             if((piezoV1-piezoV2)<1 && (piezoV1-piezoV2)>-1) //진동값이 7이상인 값일 경우 +1
              count++;
             delay(1000);
        }
        if(count >= 4){ //4번이상 높은 값이 측정될경우 노면상태 update
        Serial.println("R");
         BTSerial.println("R");
        }
      }
      
      //거리값을 이전값으로 전환
      before1 = now1;
      before2 = now2;
   
      delay(1000);
 }
