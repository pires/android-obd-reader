package com.github.pires.obd.reader.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by elagin on 19.03.15.
 */
public class ConfirmDialog {

    public static final int DIALOG_CONFIRM_DELETE_ID = 1;

    public static Dialog createDialog(final int id, Context context, final Listener listener) {
        String title;
        String message;

        switch (id) {
            case DIALOG_CONFIRM_DELETE_ID:
                title = "Delete trip record";
                message = "Are you sure?";
                return getDialog(id, title, message, context, listener);
            default:
                return null;
        }
    }

    public static Dialog getDialog(final int id, String title, String message, Context context, final Listener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onConfirmationDialogResponse(id, true);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onConfirmationDialogResponse(id, false);
            }
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public interface Listener {
        public void onConfirmationDialogResponse(int id, boolean confirmed);
    }
}
