package com.pgizka.simplecallrecorder.options;


import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.contacts.ContactDetailAdapter;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;
import com.pgizka.simplecallrecorder.util.UpdateUserData;
import com.pgizka.simplecallrecorder.util.Utils;

/**
 * Created by Paweł on 2015-07-23.
 */
public class SelectContactDialogFragment extends DialogFragment {
    static final String TAG = SelectContactDialogFragment.class.getSimpleName();

    public static final int REQUEST_PICK_CONTACT = 1000;

    SharedPreferences systemPref;
    int recordingMode;

    OnDismisListener listener;

    public interface OnDismisListener{
        public void onDismis();
    }

    public void setListener(OnDismisListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.options_select_contact_dialog, null, false);

        ListView listView = (ListView) view.findViewById(R.id.options_dialog_list_view);
        systemPref = getActivity().getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE, Context.MODE_PRIVATE);
        recordingMode = systemPref.getInt(PreferanceStrings.RECORDING_MODE, 0);
        String selection = null;
        if(recordingMode == PreferanceStrings.RECORDING_MODE_EVERYTHING){
            selection = RecorderContract.ContactEntry.COLUMN_IGNORED + " = ?";
        } else {
            selection = RecorderContract.ContactEntry.COLUMN_RECORDED + " = ?";
        }
        String [] selectionArgs = {Integer.toString(0)};
        Uri uri = RecorderContract.getContentUri(RecorderContract.PATH_CONTACT);
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, selectionArgs, null);
        DialogListAdapter listAdapter = new DialogListAdapter(getActivity(), cursor);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClicked(id);
            }
        });

        Button addNumberButton = (Button) view.findViewById(R.id.options_dialog_add_number_button);
        Button addContactButton = (Button) view.findViewById(R.id.options_dialog_add_contact_button);

        addNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddNumberButton();
            }
        });
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddContactButton();
            }
        });

        builder.setView(view);

        return builder.create();
    }

    private void onItemClicked(long id){
        Uri uri = RecorderContract.buildRecordItem(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT), id);
        ContentValues contentValues = new ContentValues();
        if(recordingMode == PreferanceStrings.RECORDING_MODE_EVERYTHING) {
            contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, 1);
        } else {
            contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, 1);
        }
        getActivity().getContentResolver().update(uri, contentValues, null, null);
        dismiss();
        if(listener != null){
            listener.onDismis();
        }
    }

    private void onAddNumberButton(){
        OptionsAddNumberDialogFragment optionsAddNumberDialogFragment = new OptionsAddNumberDialogFragment();
        optionsAddNumberDialogFragment.show(getActivity().getSupportFragmentManager(), "add number");
        optionsAddNumberDialogFragment.setOnAddNumberListener(new OptionsAddNumberDialogFragment.OnAddNumberInterface() {
            @Override
            public void onAddNumber(String number) {
                addContact(number);
            }
        });

    }

    private void onAddContactButton(){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SelectContactDialogFragment.REQUEST_PICK_CONTACT){
            onPickContactResult(resultCode, data);
        }
    }

    public void onPickContactResult(int resultCode, Intent data){
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "on pick contact result code is not ok");
            return;
        }

        Uri contactData = data.getData();
        Cursor cursor =  getActivity().managedQuery(contactData, null, null, null, null);
        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            Cursor phones = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                    null, null);
            phones.moveToFirst();
            String phoneNumber = phones.getString(phones.getColumnIndex("data1"));
            addContact(phoneNumber);

        }

    }

    private void addContact(String phoneNumber){
        Log.d(TAG, "phone number is " + phoneNumber);
        String normalizedPhoneNumber = Utils.normalizePhoneNumber(phoneNumber);
        String selection = RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " = ?";
        String [] selectionArgs = {normalizedPhoneNumber};

        Cursor contactCursor = getActivity().getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                null, selection, selectionArgs, null);

        boolean ignored, recorded;
        if(recordingMode == PreferanceStrings.RECORDING_MODE_EVERYTHING){
            ignored = true;
            recorded = false;
        } else {
            ignored = false;
            recorded = true;
        }

        int contactId = -1;
        if(contactCursor.getCount() == 0){
            ContentValues contentValues = new ContentValues();
            contentValues.put(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER, normalizedPhoneNumber);
            contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, ignored);
            contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, recorded);
            Uri uri = getActivity().getContentResolver().insert(
                    RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                    contentValues);
            Log.d(TAG, "Last path segment is: " + uri.getLastPathSegment());
            //contactId = Integer.parseInt(uri.getLastPathSegment());
        } else {
            contactCursor.moveToFirst();
            contactId = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry._ID));
        }

        if(contactId != -1){
            ContentValues contentValues = new ContentValues();
            if (recordingMode == PreferanceStrings.RECORDING_MODE_EVERYTHING) {
                contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, true);
            } else {
                contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, true);
            }
            Uri uri = RecorderContract.buildRecordItem(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT), contactId);
            getActivity().getContentResolver().update(uri, contentValues, null, null);
        }
        new UpdateUserData(getActivity()).execute();

        if(listener != null){
            listener.onDismis();
        }
        dismiss();
    }

    private class DialogListAdapter extends CursorAdapter{

        public DialogListAdapter(Context context, Cursor c) {
            super(context, c);
        }

        public class ViewHolder{
            TextView displayName, phoneText;

            public ViewHolder(View view){
                displayName = (TextView) view.findViewById(R.id.options_dialog_display_name_text);
                phoneText = (TextView) view.findViewById(R.id.options_dialog_phone_text);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.options_dialog_item, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            String displayName = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
            String phoneNumber = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));

            if(TextUtils.isEmpty(displayName)){
                viewHolder.displayName.setText(phoneNumber);
                viewHolder.phoneText.setText("");
            } else {
                viewHolder.displayName.setText(displayName);
                viewHolder.phoneText.setText(phoneNumber);
            }
        }
    }

}
