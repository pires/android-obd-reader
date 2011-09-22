package eu.lighthouselabs.obd.reader.command;

import java.io.IOException;

public class ObdMultiCommand extends ObdCommand {

	private String[] cmds;

	public ObdMultiCommand(String[] cmds, String desc, String resType, String impType) {
		super(null, desc, resType, impType);
		this.cmds = cmds;
	}
	public ObdMultiCommand(ObdMultiCommand other) {
		this(other.cmds, other.desc, other.resType, other.impType);
	}
	public void run() {
		buff.clear();
		for (int i = 0; i < cmds.length; i++) {
			String cmd = cmds[i];
			sendCmd(cmd);
			readResult();
		}
	}
	protected void readResult() {
		byte c = 0;
		try {
			while ((char)(c = (byte)in.read()) != '>') {
				buff.add(c);
			}
			buff.add((byte)'\n');
		} catch (IOException e) {
		}
	}
	public String getCmd() {
		String cmd = "";
		for (int i = 0; i < cmds.length; i++) {
			cmd += cmds[i];
			if (i+1 < cmds.length) {
				cmd += ",";
			}
		}
		return cmd;
	}
}
