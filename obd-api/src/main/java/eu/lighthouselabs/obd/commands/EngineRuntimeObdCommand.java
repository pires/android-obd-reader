/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO put description
 */
public class EngineRuntimeObdCommand extends OBDCommand {

	public EngineRuntimeObdCommand() {
		super("01 1F");
	}

	public EngineRuntimeObdCommand(EngineRuntimeObdCommand other) {
		super(other);
	}

	@Override
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [01 0C] of the response
			byte b1 = Byte.parseByte(res.substring(4, 6));
			byte b2 = Byte.parseByte(res.substring(6, 8));
			int value = ((b1 << 8) | b2);
			
			// determine time
			String hh = String.format("%02d", value / 3600);
			String mm = String.format("%02d", (value % 3600) / 60);
			String ss = String.format("%02d", value % 60);
			res = String.format("%s:%s:%s", hh, mm, ss);
		}

		return res;
	}
}