package com.github.pires.obd.reader.config;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedObdCommand;
import com.github.pires.obd.commands.control.CommandControlModuleVoltageObdCommand;
import com.github.pires.obd.commands.control.CommandEquivRatioObdCommand;
import com.github.pires.obd.commands.control.DistanceTraveledSinceCodesClearedObdCommand;
import com.github.pires.obd.commands.control.DistanceTraveledWithMILOnObdCommand;
import com.github.pires.obd.commands.control.DtcNumberObdCommand;
import com.github.pires.obd.commands.control.TimingAdvanceObdCommand;
import com.github.pires.obd.commands.control.TroubleCodesObdCommand;
import com.github.pires.obd.commands.control.VinObdCommand;
import com.github.pires.obd.commands.engine.EngineLoadObdCommand;
import com.github.pires.obd.commands.engine.EngineOilTempObdCommand;
import com.github.pires.obd.commands.engine.EngineRPMObdCommand;
import com.github.pires.obd.commands.engine.EngineRuntimeObdCommand;
import com.github.pires.obd.commands.engine.MassAirFlowObdCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionObdCommand;
import com.github.pires.obd.commands.fuel.FindFuelTypeObdCommand;
import com.github.pires.obd.commands.fuel.FuelAirCommanded;
import com.github.pires.obd.commands.fuel.FuelAirWidebandCommanded;
import com.github.pires.obd.commands.fuel.FuelConsumptionRateObdCommand;
import com.github.pires.obd.commands.fuel.FuelEconomyObdCommand;
import com.github.pires.obd.commands.fuel.FuelLevelObdCommand;
import com.github.pires.obd.commands.fuel.FuelTrimObdCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureObdCommand;
import com.github.pires.obd.commands.pressure.FuelPressureObdCommand;
import com.github.pires.obd.commands.pressure.FuelRailPressureObdCommand;
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
        cmds.add(new CommandControlModuleVoltageObdCommand());
        cmds.add(new CommandEquivRatioObdCommand());
        cmds.add(new DistanceTraveledSinceCodesClearedObdCommand());
        cmds.add(new DistanceTraveledWithMILOnObdCommand());
        cmds.add(new DtcNumberObdCommand());
        cmds.add(new TimingAdvanceObdCommand());
        cmds.add(new TroubleCodesObdCommand());
        cmds.add(new VinObdCommand());

        // Engine
        cmds.add(new EngineLoadObdCommand());
        cmds.add(new EngineRPMObdCommand());
        cmds.add(new EngineRuntimeObdCommand());
        cmds.add(new MassAirFlowObdCommand());
        cmds.add(new ThrottlePositionObdCommand());

        // Fuel
        cmds.add(new FindFuelTypeObdCommand());
        cmds.add(new FuelConsumptionRateObdCommand());
        // cmds.add(new AverageFuelEconomyObdCommand());
        cmds.add(new FuelEconomyObdCommand());
        cmds.add(new FuelLevelObdCommand());
        // cmds.add(new FuelEconomyMAPObdCommand());
        // cmds.add(new FuelEconomyCommandedMAPObdCommand());
        cmds.add(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_1));
        cmds.add(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_2));
        cmds.add(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_1));
        cmds.add(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_2));
        cmds.add(new FuelAirCommanded());
        cmds.add(new FuelAirWidebandCommanded());
        cmds.add(new EngineOilTempObdCommand());

        // Pressure
        cmds.add(new BarometricPressureObdCommand());
        cmds.add(new FuelPressureObdCommand());
        cmds.add(new FuelRailPressureObdCommand());
        cmds.add(new IntakeManifoldPressureObdCommand());

        // Temperature
        cmds.add(new AirIntakeTemperatureObdCommand());
        cmds.add(new AmbientAirTemperatureObdCommand());
        cmds.add(new EngineCoolantTemperatureObdCommand());

        // Misc
        cmds.add(new SpeedObdCommand());


        return cmds;
    }

}
