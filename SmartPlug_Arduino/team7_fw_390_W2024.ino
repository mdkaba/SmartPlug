/////////////////////////////////////////////////////////////////////////
// Whats displayed on the serial monitor                               //
// By default - All communication protocol validations are displayed   //
//            - Either PASSED or FAILED                                //
/////////////////////////////////////////////////////////////////////////
//Defines SERVICE_UUID and CHARACTERISTIC_UUID_TX
#include "BluethoothManager.cpp"
#include "HardwareManager.h"
//REMEBER TO SET PARTITION TABLE (Tools > Partition Scheme) TO "Huge App 3Mb"

#define API_KEY "AIzaSyDuDqeggAiaI0wqPDWE7Le3EXLWdSKBFDw"
#define DATABASE_URL "https://team7-390-db-default-rtdb.firebaseio.com/"


#ifndef SERVICE_UUID
  #define SERVICE_UUID  "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#endif
#ifndef CHARACTERISTIC_UUID_TX
  #define CHARACTERISTIC_UUID_TX "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#endif

BluetoothManager bt;
HardwareManager plug;
Database database (API_KEY,DATABASE_URL);

bool cmd, prevCmd;
float hr;
long int ms;
ESP32Time rtc(3600);
int data_index = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  //Starts bluetooth with service UUID and characteristic UUID from above
  //Will auto-connect to WiFi when a valid packet is sent to the characteristic
  bt = BluetoothManager();
  bt.InitBluetooth();
  //Only after this point are we connected to the internet

  configTime(-5 * 3600, 0, "pool.ntp.org", "time.nist.gov");
  delay(1000);
  Serial.println("Connection status: " + String(bt.deviceConnected ? "Connected" : "Disconnected"));
};
bool databaseSetup = false;
void loop() {
  while(WiFi.status() != WL_CONNECTED){
  Serial.println(".");
  delay(300);
  }
  if(databaseSetup != true){
    database.setupDatabase();
    databaseSetup = true;
  }
  //Limited in function body by 1/425Hz (2353us)
  float dummyCurrent = random(0.00,10.00);
  
  plug.updateSensor();
  HardwareManager::xRead result = plug.getResult();
  //This will run like every 100us, but sendX() commands are limited to 1 per 5sec in the function body 
  
  // Real Current reading sending functions
  //database.sendCurrentReading(result.fRMS);
  //database.addDataToQueue(result.fRMS, rtc.getDateTime(true), data_index); 
  
  //TEST ONLY
  database.addDataToQueue(dummyCurrent, rtc.getDateTime(true), data_index); 
  database.sendCurrentReading(dummyCurrent);

  prevCmd = cmd;
  try
  {
    cmd = database.getCommand();
  }
  catch (Database::RES)
  {
    cmd = prevCmd;
  }
  
  // Real Current Values

  // switch (cmd)
  // {
  //   case Database::CMD::ON:
  //     plug.allowCurrentFlow(true);
  //     break;
  //   case Database::CMD::OFF:
  //     plug.allowCurrentFlow(false);
  //     break;
  //   case Database::CMD::TIME:
  //     plug.allowCurrentFlow(true);
  //     hr = database.getTimeoutDeltaH();
  //     ms = hr * pow(10, 6) * 3.6;
  //     plug.setTimedShutoff(millis() + ms);
  //     break;
  // }

  // TO USE WITH REAL CURRENT VALUES !!!!!!!
  // database.sendCurrentReading(result.fRMS);
  database.sendQueue();

  if (plug.isTimedShutoff)
  {
    plug.updateTimedShutoff();
  }
  
  delay(3000);
};




