#include "DatabaseHelper.h"

#include"addons/TokenHelper.h"
#include"addons/RTDBHelper.h"
//Global variable
Database::Database(){}
//Done in the setupSection
Database::Database(String api_key, String database_url){
  this -> API_KEY = api_key;
  this -> DATABASE_URL = database_url;
}
Database::RES Database::setupDatabase(){
  //Config section 
  bool signupOK = false;
    Serial.println(Firebase.authenticated());
    Firebase.authenticated();
    config.api_key = API_KEY;
    config.database_url = DATABASE_URL;
    if(Firebase.signUp(&config, &auth,"","")){
      Serial.println("Connect to Firebase estasblished!");
      setSignupOK(true);
    }else{
      //Signup error msg
      Serial.printf("%s\n", config.signer.signupError.message.c_str());
      return Database::NC;
    }
    sendInitialization();
    return Database::OK;
}
Database::RES Database::setDatabaseListener(){
  //beginStream return bool value wether it is accessible or not
  if(!Firebase.RTDB.beginStream(&fbda_s1_command, "/User-1/Command")){
    Serial.printf("Stream 1 beging error - Command stream: , %s\n",fbda_s1_command.errorReason().c_str());
    return Database::NC;
  }
  if(!Firebase.RTDB.beginStream(&fbda_s2_ip, "/User-1/IP")){
    Serial.printf("Stream 2 beging error - IP stream: , %s\n",fbda_s2_ip.errorReason().c_str());
    return Database::NC;
  }
  return Database::OK;
}
Database::RES Database::sendInitialization(){
  config.token_status_callback = tokenStatusCallback; // Call back function
  Firebase.begin((&config), &auth);
  Firebase.reconnectWiFi(true);
  if (Firebase.ready())
  {
    return Database::OK;
  }
  return Database::NC;
}
//Loop Methods
Database::RES Database::sendCommand(bool data){
  Serial.println(Firebase.ready());
  if(Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0)) //Tries to connect at the start then every 3sec
    sendDataPrevMillis = millis();  
    if(Firebase.RTDB.setBool(&fbda,"User-1/Command",data)){ // if "User-1/Command" does not exist, it will create it ,  
      Serial.println("PASSED");
      Serial.println();
      Serial.print(data);
      Serial.print(" - successfully saved to: " + fbda.dataPath()); //This return the location where it was stored
      Serial.println("TYPE: "+ fbda.dataType());
    }else{
      Serial.println(("FAILED: " + fbda.errorReason()));
      Database::NC;
    }
    Database::OK;
}

Database::RES Database::sendCurrentReading(float data){
  // Serial.println("Started Sending current Reading!");

  if(Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0)) //Tries to connect at the start then every 3sec
    // Serial.println("If Firebase.ready in send current reading!");
    sendDataPrevMillis = millis();  
    Serial.print("Value : ");
    Serial.println(data);
    if(Firebase.RTDB.setFloat(&fbda,"User-1/Current sensor reading",data)){ // if "User-1/Current sensor reading" does not exist, it will create it ,  
      Serial.println("PASSED");
      Serial.println();
      Serial.print(data);
      Serial.println(" - successfully saved to: " + fbda.dataPath()); //This return the location where it was stored
      Serial.println("TYPE: "+ fbda.dataType());
    }else{
      Serial.println(("FAILED: " + fbda.errorReason()));
      return Database::NC;
    }
    // Serial.println("Exiting Sending current Reading");
    return Database::OK;
}
Database::CMD Database::getCommand(){
   if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 15000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    if (Firebase.RTDB.getBool(&fbda_s1_command,"/User-1/Command")){
      COMMAND = fbda_s1_command.boolData();
      Serial.println("Successgul read from " + fbda_s1_command.dataPath() + ": " + COMMAND + "(" + fbda_s1_command.dataType() + ")");
      //At this point, the new state of the command, ON/OFF is now stored in the COMMAND variable in the DatabaseHelper object

      return (COMMAND == true) ? CMD::ON : CMD::OFF;
    }
    else
    {
      throw RES::NC;
    }
  }
}
String Database::getIP(){
if(Firebase.ready() && signupOK){
      if(!Firebase.RTDB.readStream(&fbda_s2_ip)){
        Serial.printf("Stream 2 - IP read error, %s\n\n", fbda_s2_ip.errorReason().c_str());
      }
      if(fbda_s2_ip.streamAvailable()){
        if(fbda_s2_ip.dataType() == "String"){
          serverIP = fbda_s1_command.stringData();
          Serial.println("Successgul read from " + fbda_s2_ip.dataPath() + ": " + serverIP + "(" + fbda_s2_ip.dataType() + ")");
          //At this point, the new state of the command, ON/OFF is now stored in the COMMAND variable in the DatabaseHelper object
          return serverIP;
        }
      }
  }
}
Database::RES Database::checkWifiStatus(){
//check if Firebase ready
  if(Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0)) //Tries to connect at the start then every 3sec
  sendDataPrevMillis = millis();
  return Database::OK;
}

void Database::addDataToQueue(float currentReading,String timeStamp,int y){
  if(dataQueue.size() >= MAX_QUEUE_SIZE){
    dataPacket<float, String> frontPair = dataQueue.front();
    dataQueue.pop();

    // Access elements of the dequeued pair
    float currentReadings = frontPair.first;
    String timeStamp = frontPair.second;

    //Newest data pushed
    dataQueue.push({currentReading,timeStamp});
    // Serial.println("Max reached, Queue!!");
  }else{
    dataQueue.push({currentReading,timeStamp});
    //Serial.println("Added added to Queue, still not full!!");
    delay(1000);
  }
}
//Sends the whole 255 Pair queue after being converted into a JSON object
void Database::sendQueue(){
  //Serial.println("sending data queue");
  //Converts queue into JSON
  char charArray[15];
  String dataString="";
  String value;
  while(!dataQueue.empty()){
    value = dtostrf(dataQueue.front().first,2,2,charArray);
    dataString += value +  " - " + dataQueue.front().second + " | ";
    dataQueue.pop();
  }
  Serial.println(dataString);
  delay(1000);
  sendDataQueue(dataString);

  delay(2000);

}

Database::RES Database::sendDataQueue(String jsonString){
  if(Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 5000 || sendDataPrevMillis == 0)) //Tries to connect at the start then every 3sec
    sendDataPrevMillis = millis();  
    if(Firebase.RTDB.setString(&fbda,"User-1/Readings & Time Stamps",jsonString)){ // if "User-1/Readings & Time Stamps" does not exist, it will create it ,  
      Serial.println("PASSED");
      Serial.println();
      Serial.println(jsonString);
      Serial.print(" - successfully saved to: " + fbda.dataPath()); //This return the location where it was stored
      Serial.println("TYPE: "+ fbda.dataType());
    }else{
      Serial.println(("FAILED: " + fbda.errorReason()));
      return Database::BD;
    }
    return Database::OK;
}

void Database:: setSignupOK(bool value){
  this -> signupOK = value;
}

bool Database:: getSignupOK(){
  return this->signupOK;
}
Database::~Database(){}