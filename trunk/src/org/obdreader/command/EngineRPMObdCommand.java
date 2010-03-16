package org.obdreader.command;

public class EngineRPMObdCommand extends ObdCommand{

	public EngineRPMObdCommand(String cmd, String desc, String resType) {
		super(cmd, desc, resType);
	}
	public EngineRPMObdCommand(EngineRPMObdCommand other) {
		super(other);
	}
	public String formatResult() {
		String res = super.formatResult();
		if ("NODATA".equals(res)) {
			return "NODATA";
		}
		String byteStrOne = res.substring(4,6);
		String byteStrTwo = res.substring(6,8);
		int a = Integer.parseInt(byteStrOne,16);
		int b = Integer.parseInt(byteStrTwo,16);
		return Integer.toString(transform(a,b)) + " " + resType;
	}
	protected int transform(int a, int b) {
		return (int)((double)(a*256+b)/4.0);
	}
}
