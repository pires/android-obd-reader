package pt.lighthouselabs.obd.reader.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pt.lighthouselabs.obd.reader.R;
import pt.lighthouselabs.obd.reader.activity.MainActivity;

/**
 * Created by admin on 12/24/14
 */
public class MyFragmentDialog extends DialogFragment
{
    private static final String NO_BLUETOOTH = "Sorry, your device doesn't support Bluetooth.";
    private static final String BLUETOOTH_IS_OFF = "Bluetooth is disabled, will use Mock service instead";
    private static final String ERR_ORIENTATION_SENSOR = "Oops, your device doesn't have orientation sensor :(";

    private int mMode;


    public static MyFragmentDialog newInstance(int mode)
    {
        MyFragmentDialog f = new MyFragmentDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("mode", mode);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mMode = getArguments().getInt("mode");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);

        TextView tvMessage = (TextView) v.findViewById(R.id.tvMessage);

        switch (mMode)
        {
            case MainActivity.NO_BLUETOOTH_ID:
                tvMessage.setText(NO_BLUETOOTH);
                break;

            case MainActivity.BLUETOOTH_DISABLED:
                tvMessage.setText(BLUETOOTH_IS_OFF);
                break;

            case MainActivity.NO_ORIENTATION_SENSOR:
                tvMessage.setText(ERR_ORIENTATION_SENSOR);
                break;
        }

        Button btnClose = (Button) v.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                getDialog().dismiss();
            }
        });

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getDialog().setTitle("Warning!");
    }
}
