package com.github.pires.obd.reader;

import android.app.Application;
import com.github.pires.obd.reader.acra.ACRAPostSender;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.HashMap;

@ReportsCrashes(logcatFilterByPid = true)
public class ObdReaderApplication extends Application {

    @Override
    public void onCreate() {

        ACRA.init(this);
        super.onCreate();

        HashMap<String,String> ACRAData = new HashMap<String,String>();
        ACRAData.put("my_app_info", "ODB Reader");
        ACRAData.put("EMAIL_SERVICE", "MAILGUN");
        ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));
    }
}
