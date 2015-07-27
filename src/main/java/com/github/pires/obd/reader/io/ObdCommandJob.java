/*
 * TODO put header
 */
package com.github.pires.obd.reader.io;

import com.github.pires.obd.commands.ObdCommand;

/**
 * This class represents a job that ObdGatewayService will have to execute and
 * maintain until the job is finished. It is, thereby, the application
 * representation of an ObdCommand instance plus a state that will be
 * interpreted and manipulated by ObdGatewayService.
 */
public class ObdCommandJob {

    private Long _id;
    private ObdCommand _command;
    private ObdCommandJobState _state;

    /**
     * Default ctor.
     *
     * @param command the ObCommand to encapsulate.
     */
    public ObdCommandJob(ObdCommand command) {
        _command = command;
        _state = ObdCommandJobState.NEW;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    public ObdCommand getCommand() {
        return _command;
    }

    /**
     * @return job current state.
     */
    public ObdCommandJobState getState() {
        return _state;
    }

    /**
     * Sets a new job state.
     *
     * @param state the new job state.
     */
    public void setState(ObdCommandJobState state) {
        _state = state;
    }

    /**
     * The state of the command.
     */
    public enum ObdCommandJobState {
        NEW,
        RUNNING,
        FINISHED,
        EXECUTION_ERROR,
        QUEUE_ERROR,
        NOT_SUPPORTED
    }

}
