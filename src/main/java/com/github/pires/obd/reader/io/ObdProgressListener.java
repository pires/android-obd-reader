package com.github.pires.obd.reader.io;

import com.github.pires.obd.reader.io.ObdCommandJob;

/**
 * TODO put description
 */
public interface ObdProgressListener {

  void stateUpdate(final ObdCommandJob job);

}