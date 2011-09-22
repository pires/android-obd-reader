package eu.lighthouselabs.obd.reader.command;

static import eu.lighthouselabs.obd.reader.config.ObdConfig.RPM;

public class EngineRPMObdCommand extends IntObdCommand {

	public EngineRPMObdCommand() {
		super("010C", ObdConfig.RPM, "RPM", "RPM");
	}

	public EngineRPMObdCommand(EngineRPMObdCommand other) {
		super(other);
	}

	@Override
	public String formatResult() {
		String res = getResult();
		String[] ress = res.split("\r");
		res = ress[0].replace(" ", "");
		if ("NODATA".equals(res)) {
			return "NODATA";
		}
		byte b1 = Byte.parseByte(res.substring(4, 6));
		byte b2 = Byte.parseByte(res.substring(6, 8));
		intValue = ((b1 << 8) | b2) / 4;
		return String.format("%d %s", intValue, resType);
	}
}