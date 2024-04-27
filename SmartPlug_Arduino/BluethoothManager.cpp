#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <iostream>
#include <string.h>
#include "WifiNetwork.h"
#include "DatabaseHelper.h"

#define SERVICE_UUID  "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID_TX "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define DEBUG_MAC "A5:E2:57:43:F1"

class BluetoothManager
{
public:
  String SSID;
  String PW;
  String IP;
  BLECharacteristic *pCharacteristic;
  BLEServer *pServer;
  byte buff[128];
  bool deviceConnected = false;
  WifiNetwork * network;

  //Hmm today I will use nested classes
  //^ clueless
  class MyServerCallbacks: public BLEServerCallbacks{
    public:
    BluetoothManager * owner;
    MyServerCallbacks(BluetoothManager * own) { owner = own; }
    void onConnect(BLEServer* pServer){
      owner->deviceConnected = true;
    }

    void onDisconnect(BLEServer* pServer){
      owner->deviceConnected = false;
    }
  };

  BluetoothManager() {};

  void Connect(String, String);

class MyCharacteristicCallbacks : public BLECharacteristicCallbacks{
  //Nested classes moment
  public:
    BluetoothManager * owner;
    WifiNetwork * owner_network;
    MyCharacteristicCallbacks(BluetoothManager * own, WifiNetwork * nw) {owner = own; owner_network = nw; };
  //Unused?
  void onRead(BLECharacteristic *pCharacteristic) {
    pCharacteristic -> setValue("Test Communication.");
  }
  void onWrite(BLECharacteristic *pCharacteristic){
    uint8_t* value = pCharacteristic -> getData();
    int len = pCharacteristic -> getLength();
    

    if (len > 0){
      Serial.println("Received value: ");
      Serial.println((char)value[0]);

      //UNAME (ASCII CHAR BYTES) + EOT + PW (ASCII CHAR BYTES)
      for(int i = 0; i < len; i++){
        Serial.print(value[i], HEX);
        Serial.print(" ");
      }
      Serial.println();

    int EOTPos = -1;
    int EOTPos2 = -1;

    
    //Split data into SSID and password based on EOT character
      for (int i = 0; i < len; i++){
        if (value[i] == 0x04)
        {
          EOTPos = i;
          break;
        }
      }
      Serial.println(EOTPos);
      Serial.println(len);

    //Error checking
    if (EOTPos >= len || EOTPos < 0)
    {
      Serial.println("Bad Formed Data");
      return;
    }

      for (int i = 0; i < EOTPos; i++)
      {
        owner->SSID += (char)value[i];
      }
      for (int i = EOTPos + 1; i < len; i++)
      {
        owner->PW += (char)value[i];
      }

      Serial.print("Received: ");
      Serial.println(owner->SSID + ", " + owner->PW);

      //Attempt to connect here
      Connect(owner->SSID, owner->PW);

      pCharacteristic -> setValue("OK");
    }
  }
  void Connect(String SSID, String pass)
  {
    owner_network = new WifiNetwork(SSID, DEBUG_MAC, pass);
    owner_network -> establishConnection();
  }
};



void InitBluetooth()
{
  BLEDevice::init("Team7_390_SmartPlug");
  
  //Create BLE Server
  pServer = BLEDevice::createServer();
  pServer -> setCallbacks(new MyServerCallbacks(this));

  //Create BLE Service
  BLEService *pService = pServer -> createService(SERVICE_UUID);

  //Create BLE Characteristic
  pCharacteristic = pService -> createCharacteristic(CHARACTERISTIC_UUID_TX, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
  pCharacteristic -> setCallbacks (new MyCharacteristicCallbacks(this, network));

  //Needed to notify
  pCharacteristic -> addDescriptor(new BLE2902());

  //starting the service
  pService -> start();

  //start advertising
  pServer -> getAdvertising() -> start();
  Serial.println("Starting device advertising");
}

};