package org.obdreader.io;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ObdReaderServiceConnection implements ServiceConnection {

	private ObdReaderService service = null;
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.service = ((ObdReaderService.ObdReaderServiceBinder)service).getService();
	}
	public void onServiceDisconnected(ComponentName name) {
		service = null;
	}
	public ObdReaderService getService() {
		return service;
	}
	public boolean isRunning() {
		if (service == null) {
			return false;
		}
		return service.isRunning();
	}
}
