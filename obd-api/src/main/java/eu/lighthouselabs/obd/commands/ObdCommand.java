package eu.lighthouselabs.obd.commands;


public abstract class ObdCommand extends ObdBaseCommand {
    /**
     * Default ctor to use
     *
     * @param command
     *            the command to send
     */
    public ObdCommand(String command) {
        super(command);
    }

    /**
     * Copy ctor.
     *
     * @param other
     *            the ObdCommand to copy.
     */
    public ObdCommand(ObdCommand other) {
        this(other.cmd);
    }

    @Override
    protected void fillBuffer() {
        // clear buffer
        buffer.clear();

        // read string each two chars
        int begin = 0;
        int end = 2;
        while (end <= rawData.length()) {
            String temp = "0x" + rawData.substring(begin, end);
            buffer.add(Integer.decode(temp));
            begin = end;
            end += 2;
        }
    }
}
