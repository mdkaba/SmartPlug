#pragma once

#include<WiFi.h>
#include<String>
class WifiNetwork{
  private:
    String SSID;
    String MacAddress;
    String PASSWORD;
    int IP;
  public:
    // Basic constructor
    WifiNetwork();
    WifiNetwork(String ssid, String mac_address, String password);
    
    // To establish a Wifi connection
    bool establishConnection();

    // Destructor
    ~WifiNetwork();
};