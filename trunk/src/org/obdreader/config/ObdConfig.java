package org.obdreader.config;

import java.util.ArrayList;

import org.obdreader.command.AirIntakeTempObdCommand;
import org.obdreader.command.CommandEquivRatioObdCommand;
import org.obdreader.command.DtcNumberObdCommand;
import org.obdreader.command.EngineRPMObdCommand;
import org.obdreader.command.EngineRunTimeObdCommand;
import org.obdreader.command.FuelEconomyCommandedMAPObdCommand;
import org.obdreader.command.FuelEconomyMAPObdCommand;
import org.obdreader.command.FuelEconomyObdCommand;
import org.obdreader.command.FuelPressureObdCommand;
import org.obdreader.command.FuelTrimObdCommand;
import org.obdreader.command.IntakeManifoldPressureObdCommand;
import org.obdreader.command.MassAirFlowObdCommand;
import org.obdreader.command.ObdCommand;
import org.obdreader.command.PressureObdCommand;
import org.obdreader.command.SpeedObdCommand;
import org.obdreader.command.TempObdCommand;
import org.obdreader.command.ThrottleObdCommand;
import org.obdreader.command.TroubleCodesObdCommand;

public class ObdConfig {

	public static ArrayList<ObdCommand> getCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.add(new AirIntakeTempObdCommand());
		cmds.add(new IntakeManifoldPressureObdCommand());
		cmds.add(new PressureObdCommand("0133","Barometric Press","kPa","atm"));
		cmds.add(new TempObdCommand("0146","Ambient Air Temp","C","F"));
		cmds.add(new SpeedObdCommand());
		cmds.add(new ThrottleObdCommand());
		cmds.add(new EngineRPMObdCommand());
		cmds.add(new FuelPressureObdCommand());
		cmds.add(new TempObdCommand("0105","Coolant Temp","C","F"));
		cmds.add(new ThrottleObdCommand("0104","Engine Load","%"));
		cmds.add(new MassAirFlowObdCommand());
		cmds.add(new FuelEconomyObdCommand());
		cmds.add(new FuelEconomyMAPObdCommand());
		cmds.add(new FuelEconomyCommandedMAPObdCommand());
		cmds.add(new FuelTrimObdCommand());
		cmds.add(new FuelTrimObdCommand("0106","Short Term Fuel Trim","%"));
		cmds.add(new EngineRunTimeObdCommand());
		cmds.add(new CommandEquivRatioObdCommand());
		return cmds;
	}
	public static ArrayList<ObdCommand> getStaticCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.add(new DtcNumberObdCommand());
		cmds.add(new TroubleCodesObdCommand("03","Trouble Codes","",""));
		cmds.add(new ObdCommand("04","Reset Codes","",""));
		cmds.add(new ObdCommand("atz","Serial Reset atz","",""));
		cmds.add(new ObdCommand("ate0","Serial Echo Off ate0","",""));
		cmds.add(new ObdCommand("ate1","Serial Echo On ate1","",""));
		cmds.add(new ObdCommand("atsp0","Reset Protocol astp0","",""));
		cmds.add(new ObdCommand("atspa2","Reset Protocol atspa2","",""));
		return cmds;
	}
	public static ArrayList<ObdCommand> getAllCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.addAll(getStaticCommands());
		cmds.addAll(getCommands());
		return cmds;
	}
}
