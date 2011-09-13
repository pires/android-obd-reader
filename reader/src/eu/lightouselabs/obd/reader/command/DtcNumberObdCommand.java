package org.obdreader.command;

public class DtcNumberObdCommand extends ObdCommand {

	private int codeCount = -1;
	private boolean milOn = false;
	public DtcNumberObdCommand() {
		super("0101","DTC Status","","");
	}
	public DtcNumberObdCommand(DtcNumberObdCommand other) {
		super(other);
	}
	public String formatResult() {
		String res = super.formatResult();
		String byte1 = res.substring(4,6);
		int mil = Integer.parseInt(byte1,16);
		String result = "MIL is off, ";
		if ((mil & 0x80) == 1) {
			milOn = true;
			result = "MIL is on, ";
		}
		codeCount = mil & 0x7f;
		result += codeCount + " codes";
		return result;
	}
	public int getCodeCount() {
		return codeCount;
	}
	public boolean getMilOn() {
		return milOn;
	}
}
