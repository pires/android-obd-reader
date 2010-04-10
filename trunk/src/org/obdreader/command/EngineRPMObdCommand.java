package org.obdreader.command;

public class EngineRPMObdCommand extends IntObdCommand{

	public EngineRPMObdCommand() {
		super("010C","Engine RPM","RPM","RPM");
	}
	public EngineRPMObdCommand(EngineRPMObdCommand other) {
		super(other);
	}
	public String formatResult() {
		String res = getResult();
		String[] ress = res.split("\r");
		res = ress[0].replace(" ","");
		if ("NODATA".equals(res)) {
			return "NODATA";
		}
		String byteStrOne = res.substring(4,6);
		String byteStrTwo = res.substring(6,8);
		int a = Integer.parseInt(byteStrOne,16);
		int b = Integer.parseInt(byteStrTwo,16);
		intValue = transform(a,b);
		return String.format("%d %s", intValue, resType);
	}
	protected int transform(int a, int b) {
		return (int)((double)(a*256+b)/4.0);
	}
}
