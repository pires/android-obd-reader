package com.github.pires.obd.reader.config;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedObdCommand;
import com.github.pires.obd.commands.control.CommandEquivRatioObdCommand;
import com.github.pires.obd.commands.control.DtcNumberObdCommand;
import com.github.pires.obd.commands.control.TimingAdvanceObdCommand;
import com.github.pires.obd.commands.control.TroubleCodesObdCommand;
import com.github.pires.obd.commands.engine.EngineLoadObdCommand;
import com.github.pires.obd.commands.engine.EngineRPMObdCommand;
import com.github.pires.obd.commands.engine.EngineRuntimeObdCommand;
import com.github.pires.obd.commands.engine.MassAirFlowObdCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionObdCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeObdCommand;
import com.github.pires.obd.commands.fuel.FuelEconomyObdCommand;
import com.github.pires.obd.commands.fuel.FuelLevelObdCommand;
import com.github.pires.obd.commands.fuel.FuelTrimObdCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureObdCommand;
import com.github.pires.obd.commands.pressure.FuelPressureObdCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureObdCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import com.github.pires.obd.enums.FuelTrim;

import java.util.ArrayList;

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
    cmds.add(new TroubleCodesObdCommand());

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
