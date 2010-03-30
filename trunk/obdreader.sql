
DROP TABLE IF EXISTS `obd_data`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `obd_data` (
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `air_temp` double default NULL,
  `intake_temp` double default NULL,
  `intake_press` double default NULL,
  `bar_press` double default NULL,
  `throttle_pos` double default NULL,
  `gspd` double default NULL,
  `vspd` double default NULL,
  `gtime` double default NULL,
  `otime` double default NULL,
  `vehicle` varchar(32) default NULL,
  `fuel_econ` double default NULL,
  `maf` double default NULL,
  `stft` double default NULL,
  `ltft` double default NULL,
  `run_time` double default NULL
) ENGINE=innodb DEFAULT CHARSET=latin1;
