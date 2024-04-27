#pragma once

#include <Firebase_ESP_Client.h>
#include <String>
#include <queue>
#include <ESP32Time.h>
#include <time.h>

#define MAX_QUEUE_SIZE 255

class Database{
private:
  String API_KEY;
  String DATABASE_URL;
  bool COMMAND;
  String serverIP;
  bool signupOK = false;
  float timedShutoffTime = 10; //Calculated by the app (how much time the plug should be ON for in mins)
  template<typename current_readings, typename time_stamp >
  struct dataPacket{
    current_readings first;
    time_stamp second;
  };

std::queue<dataPacket<float,String>> dataQueue; // Queue setup
  
public: 
enum RES {
    OK = 0, //OK
    NC = 1, //No Connection (To Database)
    BD = 2, //Bad Data
    NN = 3, //No Network (WiFi)
    END = 4 //
  };
  enum CMD {
    OFF = 0,
    ON = 1,
    TIME = 2 //Setting the timed shutoff
  };
  // s1 and s2 listen to any change in a value of the database
  FirebaseData fbda;//Handle data when there is a change in its datapaths
  FirebaseData fbda_s1_command;
  FirebaseData fbda_s2_ip; 
  FirebaseAuth auth; //For Authentication
  FirebaseConfig config; //DataBase configuration
  FirebaseJson JsonQueue;
  unsigned long sendDataPrevMillis = 0;

  // Methods
  Database();
  Database(String api_key, String database_url);
  RES setDatabaseListener();
  RES sendInitialization();
  RES setupDatabase();
  RES sendCommand(bool data);
  RES sendCurrentReading(float data);
  RES checkWifiStatus();
  //Internally used by other method -> setupDatabase() & getters & setters
  void setSignupOK(bool value);
  bool getSignupOK();
  void setCommand(bool command);
  CMD getCommand();
  void setIP(String ip);
  float getTimeoutDeltaH() { return 10.0f; }; // Replace with data from the app
  void addDataToQueue(float currentReading,String timeStamp,int y);
  void sendQueue();
  RES sendDataQueue(String jsonString);
  String getIP();
  ~Database();
};