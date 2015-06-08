package com.github.pires.obd.reader.config;

import java.util.ArrayList;

import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.control.CommandEquivRatioObdCommand;
import pt.lighthouselabs.obd.commands.control.DtcNumberObdCommand;
import pt.lighthouselabs.obd.commands.control.TimingAdvanceObdCommand;
import pt.lighthouselabs.obd.commands.control.TroubleCodesObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineLoadObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRuntimeObdCommand;
import pt.lighthouselabs.obd.commands.engine.MassAirFlowObdCommand;
import pt.lighthouselabs.obd.commands.engine.ThrottlePositionObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FindFuelTypeObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelEconomyObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelLevelObdCommand;
import pt.lighthouselabs.obd.commands.fuel.FuelTrimObdCommand;
import pt.lighthouselabs.obd.commands.pressure.BarometricPressureObdCommand;
import pt.lighthouselabs.obd.commands.pressure.FuelPressureObdCommand;
import pt.lighthouselabs.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AirIntakeTemperatureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import pt.lighthouselabs.obd.enums.FuelTrim;

/**
 * TODO put description
 */
public final class ObdConfig {

  public static ArrayList<ObdCommand> getCommands() {
    ArrayList<ObdCommand> cmds = new ArrayList<>();

    // Control
    cmds.add(new CommandEquivRatioObdCommand());
    cmds.add(new DtcNumberObdCommand());
    cmds.add(new TimingAdvanceObdCommand());
    cmds.add(new TroubleCodesObdCommand(0));

    // Engine
    cmds.add(new EngineLoadObdCommand());
    cmds.add(new EngineRPMObdCommand());
    cmds.add(new EngineRuntimeObdCommand());
    cmds.add(new MassAirFlowObdCommand());

    // Fuel
    // cmds.add(new AverageFuelEconomyObdCommand());
    cmds.add(new FuelEconomyObdCommand());
    // cmds.add(new FuelEconomyMAPObdCommand());
    // cmds.add(new FuelEconomyCommandedMAPObdCommand());
    cmds.add(new FindFuelTypeObdCommand());
    cmds.add(new FuelLevelObdCommand());
    cmds.add(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_1));
    cmds.add(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_2));
    cmds.add(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_1));
    cmds.add(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_2));

    // Pressure
    cmds.add(new BarometricPressureObdCommand());
    cmds.add(new FuelPressureObdCommand());
    cmds.add(new IntakeManifoldPressureObdCommand());

    // Temperature
    cmds.add(new AirIntakeTemperatureObdCommand());
    cmds.add(new AmbientAirTemperatureObdCommand());
    cmds.add(new EngineCoolantTemperatureObdCommand());

    // Misc
    cmds.add(new SpeedObdCommand());
    cmds.add(new ThrottlePositionObdCommand());

    return cmds;
  }

}
