#include "WifiNetwork.h"

WifiNetwork::WifiNetwork(){}
WifiNetwork::WifiNetwork(String ssid, String mac_address, String password){
  this -> SSID = ssid;
  this -> MacAddress = mac_address;
  this -> PASSWORD = password;
  this -> IP = WiFi.localIP();
}
bool WifiNetwork::establishConnection(){
  WiFi.begin(this -> SSID,this -> PASSWORD);
  Serial.println("Connecting to Wi-Fi");
  while(WiFi.status() != WL_CONNECTED){
  Serial.println(".");
  }
  Serial.println("Wi-Fi Connected to: ");
  Serial.println(this -> SSID);
  Serial.println(WiFi.localIP());
  return true;
}

WifiNetwork::~WifiNetwork(){ 
  
}