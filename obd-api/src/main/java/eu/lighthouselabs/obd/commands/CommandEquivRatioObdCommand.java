/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

/**
 * TODO put description Command Equivalence Ratio
 */
public class CommandEquivRatioObdCommand extends OBDCommand {

	/*
	 * TODO should this go inside getFormattedResult()?
	 */
	private double ratio = 1.0;

	/**
	 * Default ctor.
	 */
	public CommandEquivRatioObdCommand() {
		super("01 44");
	}

	/**
	 * Copy ctor.
	 * 
	 * @param other
	 */
	public CommandEquivRatioObdCommand(CommandEquivRatioObdCommand other) {
		super(other);
	}

	/**
	 * 
	 */
	@Override
	public String getFormattedResult() {
		String res = getResult();

		if (!"NODATA".equals(res)) {
			try {
				// ignore first two bytes [hh hh] of the response
				byte b1 = Byte.parseByte(res.substring(4, 6));
				byte b2 = Byte.parseByte(res.substring(6, 8));
				ratio = ((b1 << 8) | b2) * 0.0000305;
				res = String.format("%.2f", ratio);
			} catch (Exception e) {
				/*
				 * TODO this must be revised.
				 */
				return "NODATA";
			}
		}
		
		return res;
	}

	/**
	 * TODO is this needed?
	 * 
	 * @return
	 */
	@Deprecated
	double getRatio() {
		return ratio;
	}
}