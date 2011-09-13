package org.obdreader.command;

public class CommandEquivRatioObdCommand extends ObdCommand {

	private double ratio = 1.0;
	public CommandEquivRatioObdCommand() {
		super("0144","Command Equivalence Ratio","","");
	}
	public CommandEquivRatioObdCommand(String cmd, String desc, String resType, String impType) {
		super(cmd, desc, resType, impType);
	}
	public CommandEquivRatioObdCommand(CommandEquivRatioObdCommand other) {
		super(other);
	}
	public String formatResult() {
		String res = super.formatResult();
		if ("NODATA".equals(res)) {
			return "NODATA";
		}
		try {
			String A = res.substring(4,6);
			String B = res.substring(6,8);
			int a = Integer.parseInt(A,16);
			int b = Integer.parseInt(B,16);
			ratio = ((a*256)+b)*0.0000305;
		} catch (Exception e) {
			setError(e);
		}
		return String.format("%.2f", ratio);
	}
	double getRatio() {
		return ratio;
	}
}
