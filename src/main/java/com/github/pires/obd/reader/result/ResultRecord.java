package com.github.pires.obd.reader.result;


public class ResultRecord {

  public String id;
  public String name;
  public String result;
  public String unit;

  public ResultRecord(String id, String name, String result, String unit) {
    this.id = id;
    this.name = name;
    this.result = result;
    this.unit = unit;
  }

  public void Update(String result) {
    this.result = result;
  }

}
