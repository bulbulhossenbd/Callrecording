package com.pgizka.simplecallrecorder.recordings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.pgizka.simplecallrecorder.R;

/**
 * Created by Paweł on 2015-07-25.
 */
public class RecordingsDeleteAlertDialog extends DialogFragment {
    OnDeleteListener onDeleteListener;
    int count = 1;

    public interface OnDeleteListener{
        public void onUserClickedDelete();
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String title = getString(R.string.delete_alert_title) +
                String.format(getResources().getQuantityString(R.plurals.contact_recording_number, count), count) +
                "?";
        builder.setTitle(title)
                .setMessage(R.string.delete_alert_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_delete_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(onDeleteListener != null){
                            onDeleteListener.onUserClickedDelete();
                        }
                    }
                });

        return builder.create();
    }
}
