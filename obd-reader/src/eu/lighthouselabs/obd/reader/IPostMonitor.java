/*
 * TODO put header 
 */
package eu.lighthouselabs.obd.reader;

/**
 * TODO put description
 */
public interface IPostMonitor {

	void setListener(IPostListener callback);
	boolean isRunning();
	
}