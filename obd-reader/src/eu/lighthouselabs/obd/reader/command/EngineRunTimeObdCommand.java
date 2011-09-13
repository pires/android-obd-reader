package eu.lighthouselabs.obd.reader.command;

import eu.lighthouselabs.obd.reader.config.ObdConfig;



public class EngineRunTimeObdCommand extends ObdCommand {

	public EngineRunTimeObdCommand() {
		super("011F",ObdConfig.RUN_TIME,"","");
	}
	public EngineRunTimeObdCommand(EngineRunTimeObdCommand other) {
		super(other);
	}
	@Override
	public String formatResult() {
		String res = super.formatResult();
		if ("NODATA".equals(res)) {
			return "NODATA";
		}
		String A = res.substring(4,6);
		String B = res.substring(6,8);
		int a = Integer.parseInt(A,16);
		int b = Integer.parseInt(B,16);
		int sec = (a*256)+b;
		String hh = String.format("%02d", sec/3600);
		String mm = String.format("%02d", (sec%3600)/60);
		String ss = String.format("%02d", sec%60);
		String time = String.format("%s:%s:%s", hh,mm,ss);
		return  time;
	}
}
