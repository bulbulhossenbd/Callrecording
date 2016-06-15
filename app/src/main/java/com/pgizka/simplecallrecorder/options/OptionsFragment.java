package com.pgizka.simplecallrecorder.options;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;


public class OptionsFragment extends Fragment {
    static final String TAG = OptionsFragment.class.getSimpleName();

    ToggleButton toggleButton;
    Spinner spinner;
    ListView listView;
    TextView infoText, emptyText;
    Button addButton;

    SharedPreferences systemPref;

    boolean isRecordingEnabled;

    int recordingMode;

    OptionsListAdapter optionsListAdapter;

    SelectContactDialogFragment dialogFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        toggleButton = (ToggleButton) view.findViewById(R.id.options_toggle_button);
        spinner = (Spinner) view.findViewById(R.id.options_spinner);
        listView = (ListView) view.findViewById(R.id.options_list_view);
        addButton = (Button) view.findViewById(R.id.options_add_button);
        infoText = (TextView) view.findViewById(R.id.options_info_text);
        emptyText = (TextView) view.findViewById(R.id.options_empty_text);

        listView.setEmptyView(emptyText);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.options_mode_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        spinner.setAdapter(adapter);

        systemPref = getActivity().getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE,
                Context.MODE_PRIVATE);
        isRecordingEnabled = systemPref.getBoolean(PreferanceStrings.RECORDING_ENABLED, false);
        recordingMode = systemPref.getInt(PreferanceStrings.RECORDING_MODE, 0);

        if(isRecordingEnabled){
            toggleButton.setChecked(true);
        } else {
            toggleButton.setChecked(false);
        }

        if(recordingMode == PreferanceStrings.RECORDING_MODE_EVERYTHING){
            spinner.setSelection(0);
            infoText.setText(R.string.options_exceptions);
            addButton.setText(R.string.options_add_exceptions_button);
        } else {
            spinner.setSelection(1);
            infoText.setText(R.string.options_recorded);
            addButton.setText(R.string.options_add_recorded_button);
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleButtonClick();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSpinnerClicked(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddButtonClicked();
            }
        });

        optionsListAdapter = new OptionsListAdapter(getActivity(), null, new OptionsListAdapter.OnDeleteButtonClickListener() {
            @Override
            public void onClick(int id) {
                onDelete(id);
            }
        }, listView);
        listView.setAdapter(optionsListAdapter);
        onSelectMode();

        return view;
    }

    private void onDelete(int id){
        String selection = RecorderContract.RecordEntry.COLUMN_CONTACT_KEY + " = ?";
        String [] selectionArgs = {Integer.toString(id)};
        Cursor recordigsCursor = getActivity().getContentResolver().query(
                RecorderContract.getContentUri(RecorderContract.PATH_RECORD),
                null, selection, selectionArgs, null);
        Cursor contactCursor = getActivity().getContentResolver().query(
                RecorderContract.buildRecordItem(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT), id),
                null, null, null, null);
        contactCursor.moveToFirst();
        int ignored = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_IGNORED));
        int recorded = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_RECORDED));
        boolean canDelete = (recordingMode == PreferanceStrings.RECORDING_MODE_EVERYTHING && recorded == 0 )
                            || (recordingMode == PreferanceStrings.RECORDING_MODE_ONLY_SELECTED && ignored == 0);

        if(canDelete && recordigsCursor.getCount() == 0){
            Uri uri = Uri.withAppendedPath(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT), String.valueOf(id));
            getActivity().getContentResolver().delete(uri, null, null);
            Log.d(TAG, "contact deleted");
            onSelectMode();
            return;
        }

        ContentValues contentValues = new ContentValues();
        if(recordingMode == PreferanceStrings.RECORDING_MODE_EVERYTHING){
            contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, false);
        } else {
            contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, false);
        }
        Uri uri = RecorderContract.buildRecordItem(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT), id);
        getActivity().getContentResolver().update(uri, contentValues, null, null);
        onSelectMode();
    }


    public void onToggleButtonClick(){
        SharedPreferences.Editor editor = systemPref.edit();
        if(toggleButton.isChecked()){
            editor.putBoolean(PreferanceStrings.RECORDING_ENABLED, true);
            isRecordingEnabled = true;
        } else {
            editor.putBoolean(PreferanceStrings.RECORDING_ENABLED, false);
            isRecordingEnabled = false;
        }
        editor.commit();
    }

    public void onSpinnerClicked(int position){
        SharedPreferences.Editor editor = systemPref.edit();
        if(position == 0) {
            editor.putInt(PreferanceStrings.RECORDING_MODE, PreferanceStrings.RECORDING_MODE_EVERYTHING);
            recordingMode = PreferanceStrings.RECORDING_MODE_EVERYTHING;
            infoText.setText(R.string.options_exceptions);
            addButton.setText(R.string.options_add_exceptions_button);
            emptyText.setText(R.string.options_empty_everything_text);
        } else {
            editor.putInt(PreferanceStrings.RECORDING_MODE, PreferanceStrings.RECORDING_MODE_ONLY_SELECTED);
            recordingMode = PreferanceStrings.RECORDING_MODE_ONLY_SELECTED;
            infoText.setText(R.string.options_recorded);
            addButton.setText(R.string.options_add_recorded_button);
            emptyText.setText(R.string.options_empty_selected_text);
        }
        editor.commit();
        onSelectMode();
    }

    public void onSelectMode(){
        String selection = null;
        String[] selectionArgs = {Integer.toString(1)};
        Cursor cursor;
        Uri uri = RecorderContract.getContentUri(RecorderContract.PATH_CONTACT);
        if(recordingMode == PreferanceStrings.RECORDING_MODE_EVERYTHING){
            selection = RecorderContract.ContactEntry.COLUMN_IGNORED + " = ?";
        } else {
            selection = RecorderContract.ContactEntry.COLUMN_RECORDED + " =?";
        }
        cursor = getActivity().getContentResolver().query(uri, null, selection, selectionArgs, null);
        optionsListAdapter.changeCursor(cursor);


    }

    private void onAddButtonClicked(){
        dialogFragment = new SelectContactDialogFragment();
        dialogFragment.show(getActivity().getSupportFragmentManager(), "Select contact");
        dialogFragment.setListener(new SelectContactDialogFragment.OnDismisListener() {
            @Override
            public void onDismis() {
                onSelectMode();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
