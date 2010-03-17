package org.obdreader.config;

import java.util.ArrayList;

import org.obdreader.command.DtcNumberObdCommand;
import org.obdreader.command.EngineRPMObdCommand;
import org.obdreader.command.FuelPressureObdCommand;
import org.obdreader.command.IntObdCommand;
import org.obdreader.command.ObdCommand;
import org.obdreader.command.TempObdCommand;
import org.obdreader.command.ThrottleObdCommand;
import org.obdreader.command.TroubleCodesObdCommand;

public class ObdConfig {

	public static ArrayList<ObdCommand> getCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.add(new TempObdCommand("010F","Air Intake Temp","C"));
		cmds.add(new IntObdCommand("010B","Intake Manifold Press","kPa"));
		cmds.add(new IntObdCommand("0133","Barometric Press","kPa"));
		cmds.add(new TempObdCommand("0146","Ambient Air Temp","C"));
		cmds.add(new IntObdCommand("010D","Vehicle Speed","km/h"));
		cmds.add(new ThrottleObdCommand("0111","Throttle Position","%"));
		cmds.add(new EngineRPMObdCommand("010C","Engine RPM","RPM"));
		cmds.add(new FuelPressureObdCommand("010A","Fuel Press","kPa"));
		cmds.add(new TempObdCommand("0105","Coolant Temp","C"));
		cmds.add(new ThrottleObdCommand("0104","Engine Load","%"));
		return cmds;
	}
	public static ArrayList<ObdCommand> getStaticCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.add(new DtcNumberObdCommand("0101","Trouble Code Status",""));
		cmds.add(new TroubleCodesObdCommand("03","Trouble Codes",""));
		cmds.add(new ObdCommand("04","Reset Codes",""));
		return cmds;
	}
	public static ArrayList<ObdCommand> getAllCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.addAll(getStaticCommands());
		cmds.addAll(getCommands());
		return cmds;
	}
}
