package org.obdreader.command;

public class TroubleCodesObdCommand extends ObdCommand {

	protected final static char[] dtcLetters = {'P','C','B','U'};
	private StringBuffer codes = null;
	public TroubleCodesObdCommand() {
		super("03","Trouble Codes","","");
		codes = new StringBuffer();
	}
	public TroubleCodesObdCommand(String cmd, String desc, String resType, String impType) {
		super(cmd, desc, resType, impType);
		codes = new StringBuffer();
	}
	public TroubleCodesObdCommand(TroubleCodesObdCommand other) {
		super(other);
		codes = new StringBuffer();
	}
	public void run() {
		DtcNumberObdCommand numCmd = new DtcNumberObdCommand();
		numCmd.setInputStream(in);
		numCmd.setOutputStream(out);
		numCmd.start();
		try {
			numCmd.join();
		} catch (InterruptedException e) {
			setError(e);
		}
		int count = numCmd.getCodeCount();
		int dtcNum = (count+2)/3;
		for (int i = 0; i < dtcNum; i++) {
			sendCmd(cmd);
			String res = getResult();
			for (int j = 0; j < 3; j++) {
				String byte1 = res.substring(3+j*6,5+j*6);
				String byte2 = res.substring(6+j*6,8+j*6);
				int b1 = Integer.parseInt(byte1,16);
				int b2 = Integer.parseInt(byte2,16);
				int val = (b1 << 8) + b2;
				if (val == 0) {
					break;
				}
				String code = "P";
				if ((val&0xC000) > 14) {
					code = "C";
				}
				code += Integer.toString((val&0x3000)>>12);
				code += Integer.toString((val&0x0fff));
				codes.append(code);
				codes.append("\n");
			}
		}
	}
	public String formatResult() {
		String res = getResult();
		String[] ress = res.split("\r");
		for (String r:ress) {
			String k = r.replace("\r","");
			codes.append(k);
			codes.append("\n");
		}
		return codes.toString();
	}
}
