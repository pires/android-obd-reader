/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package pt.lighthouselabs.obd.reader.net;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for OBD readings.
 */
public class ObdReading {
  private double latitude, longitude;
  private long timestamp;
  private String vin;
  private Map<String, String> readings;

  public ObdReading(){
    readings = new HashMap<String, String>();
  }

  public ObdReading(double latitude, double longitude, long timestamp, String vin, Map<String, String> readings) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.timestamp = timestamp;
    this.vin = vin;
    this.readings = readings;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getVin() {
    return vin;
  }

  public void setVin(String vin) {
    this.vin = vin;
  }

  public Map<String, String> getReadings() {
    return readings;
  }

  public void setReadings(Map<String, String> readings) {
    this.readings = readings;
  }

}