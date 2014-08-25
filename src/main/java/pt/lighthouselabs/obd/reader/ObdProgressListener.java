package pt.lighthouselabs.obd.reader;

import pt.lighthouselabs.obd.reader.io.ObdCommandJob;

/**
 * TODO put description
 */
public interface ObdProgressListener {

	void stateUpdate(final ObdCommandJob job);
	
}