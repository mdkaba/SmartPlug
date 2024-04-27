#pragma once
//#include <TimerEvent.h>
#define ACS712_PIN 32
#define SAMPLE_TIME 2353
#define MAX_V 4095
#define VC_P 0.0008056640625
// #define OUTPUT_SAMPLE_DATA_DEBUG
#include <Arduino.h>

class HardwareManager
{
  private:
  bool on = true; //device should start like a normal plug
  
  float current = 0.0f;
  float shutoffTime = 1.0f;
  long sample_total = 0;
  long last_sample = 0;
  int sample_count = 0;
  float sensitivity = 0.066; //set sensor voltage to current using 0.066V/A
  unsigned long time = 0;

  long low_sample = 1000000;
  long sample = 0;
  
  public:
  bool isTimedShutoff = false;

  struct xRead {
    float fRMS = 0.0f;
    float fPPK = 0.0f;
  };

  xRead xResult;

  HardwareManager();

  bool allowCurrentFlow(bool a);

  //Input is time when plug will switch off in millis
  void setTimedShutoff(long millisecs);

  void updateTimedShutoff();

  void updateSensor();

  xRead getResult();
};