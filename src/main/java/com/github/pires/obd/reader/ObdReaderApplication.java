package com.github.pires.obd.reader;

import android.app.Application;
import com.github.pires.obd.reader.acra.ACRAPostSender;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 9/1/15
 * Time: 5:31 PM
 * developer STANIMIR MARINOV
 */
@ReportsCrashes(logcatFilterByPid = true)
public class ObdReaderApplication extends Application {

    @Override
    public void onCreate() {

        ACRA.init(this);
        super.onCreate();

        HashMap<String,String> ACRAData = new HashMap<String,String>();
        ACRAData.put("my_app_info", "ODB Reader");
        ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));
    }
}
