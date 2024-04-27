#include "HardwareManager.h"

HardwareManager::HardwareManager()
{
  on = true;
  current = 0.0f;
  shutoffTime = -1.0f;
  pinMode(ACS712_PIN, INPUT);
}

  bool HardwareManager::allowCurrentFlow(bool a)
  {
    if (isTimedShutoff)
    {
      a = false;
      //Relay stuff here
      Serial.println("Switched off based on timer");
      on = false;
      //timedShutoff.disable(); //No periodic-ness, oneshot timer only
      return true;
    }
    on = a;
    //Replace with relay stuff
    Serial.print("Plug is swicthed: ");
    Serial.println(a ? "On" : "Off");
    return true;
  }

  void HardwareManager::setTimedShutoff(long millisecs)
  {
    shutoffTime = millisecs;
  }

  void HardwareManager::updateTimedShutoff()
  {
    if (millis() >= shutoffTime && !isTimedShutoff)
    {
      isTimedShutoff = true;
      allowCurrentFlow(false);
    }
  }

  void HardwareManager::updateSensor()
  {
    time = micros();
    if (time - last_sample >= SAMPLE_TIME)
    {
      sample = analogRead(ACS712_PIN);
      if(low_sample > sample)
        low_sample = sample;
      sample_count++;
      last_sample = micros();
    }
    if(sample_count > 100)
    {
      float volts = low_sample * (3.3/4096); //Converting the low sample into a voltage
      float valley =  2.4 - volts; // Removing the 2.4V offset induced by the current sensor
      float current_peak = valley / 0.66; // Sensor voltage to current transfer function
      float rms_current = current_peak * (1/sqrt(2)); // Get RMS current
      float ppk_current = current_peak*2;
      xResult.fRMS = rms_current;
      xResult.fPPK = ppk_current;
      low_sample = 1000000;
      sample_count = 0;

      #ifdef OUTPUT_SAMPLE_DATA_DEBUG
      Serial.print("Low Sample: "); Serial.println(low_sample);
      Serial.print("Volts: "); Serial.println(volts);
      Serial.print("Valley: "); Serial.println(valley);
      Serial.print("Current peak: "); Serial.println(current_peak);
      Serial.print("RMS Current: "); Serial.println(rms_current); Serial.println();
      #endif
    }
  }

    HardwareManager::xRead HardwareManager::getResult()
  {
    return xResult;
  }