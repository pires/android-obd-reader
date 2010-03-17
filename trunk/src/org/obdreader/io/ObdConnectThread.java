package org.obdreader.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.obdreader.command.ObdCommand;
import org.obdreader.config.ObdConfig;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class ObdConnectThread extends Thread implements LocationListener {

	protected BluetoothDevice dev = null;
	protected BluetoothSocket sock = null;
	protected boolean stop = false;
	protected static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	protected InputStream in = null;
	protected OutputStream out = null;
	protected ArrayList<ObdCommand> cmds = null;
	protected Map<String,String> results = null;
	protected int updateCycle = 4000;
	protected ObdReaderService service = null;
	protected String uploadUrl = null;
	protected Location currentLocation = null;
	protected LocationManager locationManager = null;

	public ObdConnectThread(BluetoothDevice dev, LocationManager locationManager, final ObdReaderService service, String uploadUrl, int updateCycle) {
		this.dev = dev;
		this.cmds = ObdConfig.getCommands();
		this.updateCycle = updateCycle;
		this.service = service;
		this.uploadUrl = uploadUrl;
		this.locationManager = locationManager;
		if (locationManager != null) {
			this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this);
		}
		results = new HashMap<String,String>();
	}
	protected void startDevice() throws IOException, InterruptedException {
		sock = this.dev.createRfcommSocketToServiceRecord(MY_UUID);
        sock.connect();
        in = sock.getInputStream();
        out = sock.getOutputStream();
        while (!stop) {
        	ObdCommand echoOff = new ObdCommand("ate0", "echo off", "string");
        	String result = runCommand(echoOff).replace(" ","");
        	if (result != null && result.contains("OK")) {
        		break;
        	}
        	Thread.sleep(1500);
        }
	}
	public void run() {
        try {
        	startDevice();
            for (int i = 0; i < cmds.size(); i++) {
            	results.put(cmds.get(i).getDesc(),"");
            }
            for (int i = 0; !stop; i = ((i+1) % cmds.size())) {
            	if (i == 0) {
            		results.put("Obs Time", Long.toString(System.currentTimeMillis()/1000));
            		if (uploadUrl != null && !"".equals(uploadUrl)) {
            			new ObdUploadThread(uploadUrl, service, getResults()).start();
            		}
            		Thread.sleep(updateCycle);
            	}
            	ObdCommand cmd = cmds.get(i);
            	try {
	            	cmd = getCopy(cmd); //make a copy because thread can only run once
					String result = runCommand(cmd);
					results.put(cmd.getDesc(),result);
            	} catch (Exception e) {
            		results.put(cmd.getDesc(), "");
            		service.notifyMessage("Error running " + cmd.getDesc(), e.getMessage(), ObdReaderService.COMMAND_ERROR_NOTIFY);
            	}
			}
        } catch (IOException e) {
        	service.notifyMessage("Bluetooth Connection Error",e.getMessage(), ObdReaderService.CONNECT_ERROR_NOTIFY);
        } catch (Exception e) {
			service.notifyMessage(e.getMessage(), e.toString(), ObdReaderService.OBD_SERVICE_ERROR_NOTIFY);
		} finally {
			close();
		}
	}
	public static ObdCommand getCopy(ObdCommand cmd) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return cmd.getClass().getConstructor(cmd.getClass()).newInstance(cmd);
	}
	public String runCommand(ObdCommand cmd) throws InterruptedException {
		cmd.setInputStream(in);
		cmd.setOutputStream(out);
		cmd.start();
		while (!stop) {
			cmd.join(300);
			if (!cmd.isAlive()) {
				break;
			}
		}
		return cmd.formatResult();
	}
	public ArrayList<ObdCommand> getCmds() {
		return cmds;
	}
	public synchronized Map<String,String> getResults() {
		if (currentLocation != null) {
			double lat = currentLocation.getLatitude();
    		double lon = currentLocation.getLongitude();
    		int speed = (int) currentLocation.getSpeed();
    		long gtime = currentLocation.getTime()/1000;
    		results.put("Latitude", Double.toString(lat));
    		results.put("Longitude", Double.toString(lon));
    		results.put("GPS Speed", Integer.toString(speed));
    		results.put("GPS Time", Long.toString(gtime));
		}
		return results;
	}
	public void cancel() {
        stop = true;
	}
	public void close() {
		try {
			locationManager.removeUpdates(this);
		} catch (Exception e) {
		}
		try {
        	stop = true;
            sock.close();
        } catch (Exception e) { 
        }
	}
	public String getStackTrace(Exception e) {
		StringWriter strw = new StringWriter();
    	PrintWriter ptrw = new PrintWriter(strw);
    	e.printStackTrace(ptrw);
    	return strw.toString();
	}
	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
	}
	@Override
	public void onProviderDisabled(String provider) {
		service.notifyMessage("GPS Unavailable", "GPS_PROVIDER disabled, please enable gps in your settings.", ObdReaderService.OBD_SERVICE_ERROR_NOTIFY);
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
