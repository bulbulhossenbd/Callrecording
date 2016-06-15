package com.pgizka.simplecallrecorder.options;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.pgizka.simplecallrecorder.R;

/**
 * Created by Paweł on 2015-07-24.
 */
public class OptionsAddNumberDialogFragment extends DialogFragment {
    OnAddNumberInterface callback;

    public interface  OnAddNumberInterface{
        public void onAddNumber(String number);
    }

    public void setOnAddNumberListener(OnAddNumberInterface callback){
        this.callback = callback;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.options_dialog_add_number, null, false);
        final EditText phoneField = (EditText) view.findViewById(R.id.options_dialog_add_number_field);

        builder.setTitle(R.string.options_dialog_add_number_title)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(callback != null){
                            callback.onAddNumber(phoneField.getText().toString());
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .setView(view);


        return builder.create();
    }
}
