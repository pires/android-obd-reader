package org.obdreader.command;

public class IntObdCommand extends ObdCommand {

	public IntObdCommand(String cmd, String desc, String resType) {
		super(cmd, desc, resType);
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
		return Integer.toString(transform(b)) + " " + resType;
	}
	protected int transform(int b) {
		return b;
	}
}
