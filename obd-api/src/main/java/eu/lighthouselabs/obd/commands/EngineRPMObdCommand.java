/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO
 * 
 * put description
 */
public class EngineRPMObdCommand extends OBDCommand {

	/**
	 * Default ctor.
	 */
	public EngineRPMObdCommand() {
		super("01 0C");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public EngineRPMObdCommand(EngineRPMObdCommand other) {
		super(other);
	}

	/**
	 * @return the engine RPM per minute
	 */
	@Override
	public String getFormattedResult() {
		String res = getResult();
		int value = 0;

		if (!"NODATA".equals(res)) {
			// ignore first two bytes [01 0C] of the response
			byte b1 = Byte.parseByte(res.substring(4, 6));
			byte b2 = Byte.parseByte(res.substring(6, 8));
			value = ((b1 << 8) | b2) / 4;
		}

		return String.format("%d", value);
	}
}