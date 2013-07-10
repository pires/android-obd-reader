package eu.lighthouselabs.obd.commands.protocol;

import eu.lighthouselabs.obd.commands.ObdBaseCommand;

public abstract class ObdProtocolCommand extends ObdBaseCommand {
    /**
     * Default ctor to use
     *
     * @param command
     *            the command to send
     */    public ObdProtocolCommand(String command) {
        super(command);
    }
    /**
     * Copy ctor.
     *
     * @param other
     *            the ObdCommand to copy.
     */
    public ObdProtocolCommand(ObdProtocolCommand other) {
        this(other.cmd);
    }


    protected void fillBuffer() {
        // settings commands don't return a value appropriate to place into the buffer, so do nothing
    }
}
