package eu.lighthouselabs.obd.reader.io;

import java.util.Map;

import eu.lighthouselabs.obd.reader.network.DataUploader;

public class ObdUploadThread extends Thread {

	private String urlString = null;
	private Map<String,String> dataMap = null;
	private ObdReaderService service = null;
	public ObdUploadThread(String urlString, ObdReaderService service, Map<String,String> dataMap) {
		this.urlString = urlString;
		this.dataMap = dataMap;
		this.service = service;
	}
	public void run () {
		try {
    		if (urlString != null && !"".equals(urlString)) {
    			DataUploader du = new DataUploader();
				du.uploadRecord(urlString,dataMap);
    		}
		} catch (Exception e) {
			service.notifyMessage("Error uploading data", e.getMessage(), ObdReaderService.OBD_SERVICE_ERROR_NOTIFY);
		}
	}
}
