package com.github.pires.obd.reader.io;

public interface ObdProgressListener {

    void stateUpdate(final ObdCommandJob job);

}