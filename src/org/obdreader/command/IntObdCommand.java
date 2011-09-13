package org.obdreader.command;

public class IntObdCommand extends ObdCommand {

	protected int intValue = -9999;
	public IntObdCommand(String cmd, String desc, String resType, String impType) {
		super(cmd, desc, resType, impType);
	}
	public IntObdCommand(IntObdCommand other) {
		super(other);
	}
	public String formatResult() {
		String res = super.formatResult();
		if ("NODATA".equals(res)) {
			return "NODATA";
		}
		String byteStr = res.substring(4,6);
		int b = Integer.parseInt(byteStr,16);
		intValue = transform(b);
		if (isImperial()) {
			return String.format("%s %s", Integer.toString(getImperialInt()), impType); 
		} else {
			return String.format("%s %s", Integer.toString(intValue), resType);
		}
	}
	protected int transform(int b) {
		return b;
	}
	public int getInt() {
		return intValue;
	}
	public int getImperialInt() {
		return intValue;
	}
}
