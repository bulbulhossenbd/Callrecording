package com.pgizka.simplecallrecorder.contacts;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.main.MainActivity;
import com.pgizka.simplecallrecorder.recordings.RecordingDetailActivity;
import com.pgizka.simplecallrecorder.recordings.RecordingsDeleteAlertDialog;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;
import com.pgizka.simplecallrecorder.util.UpdateUserData;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ContactDetailActivityFragment extends Fragment implements ActionMode.Callback {

    ListView listView;
    ImageView mainImage;
    TextView displayNameText, phoneText, statusText;
    Button addContactButton, changeStatusButton;

    Uri uri;
    Cursor contactCursor;
    ContactDetailAdapterNew adapter;

    SharedPreferences systemPref;
    int mode;

    ActionMode actionMode;

    String phoneNumber;
    boolean recorded;

    static final int REQUEST_ADD_CONTACT = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_contact_detail, container, false);

        listView = (ListView) view.findViewById(R.id.contact_detail_list_view);
        mainImage = (ImageView) view.findViewById(R.id.contact_detail_main_image);
        displayNameText = (TextView) view.findViewById(R.id.contact_detail_display_name_text);
        phoneText = (TextView) view.findViewById(R.id.contact_detail_phone_text);
        statusText = (TextView) view.findViewById(R.id.contact_detail_status_text);
        addContactButton = (Button) view.findViewById(R.id.contact_detail_add_contact_button);
        changeStatusButton = (Button) view.findViewById(R.id.contact_detail_change_status_button);

        systemPref = getActivity().getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE, Context.MODE_PRIVATE);

        Intent intent = getActivity().getIntent();
        uri = intent.getData();
        contactCursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        contactCursor.moveToFirst();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClicked(view, position, id);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemLongClicked(view, position, id);
                return true;
            }
        });

        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeButton();
            }
        });

        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddContactButton();
            }
        });

        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMainImage();
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisplay();
        updateListView();
    }

    private void updateDisplay(){
        String displayName = contactCursor.getString(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
        phoneNumber = contactCursor.getString(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));
        String contactId = contactCursor.getString(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_CONTACT_ID));

        mainImage.setImageBitmap(Utils.getDisplayImage(getActivity(), contactId));

        mode = systemPref.getInt(PreferanceStrings.RECORDING_MODE, 0);
        if(mode == PreferanceStrings.RECORDING_MODE_EVERYTHING){
            int ignored = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_IGNORED));
            recorded = ignored == 0 ? true : false;
        } else {
            int rec = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_RECORDED));
            recorded = rec == 1 ? true : false;
        }
        if(recorded) {
            statusText.setText(R.string.contact_detail_status_recorded);
            changeStatusButton.setText(R.string.contact_detail_status_set_not_record);
        } else {
            statusText.setText(R.string.contact_detail_status_not_recorded);
            changeStatusButton.setText(R.string.contact_detail_status_set_record);
        }

        if(TextUtils.isEmpty(displayName)){
            displayNameText.setText(phoneNumber);
            phoneText.setVisibility(View.GONE);
            addContactButton.setVisibility(View.VISIBLE);
        } else {
            displayNameText.setText(displayName);
            phoneText.setText(phoneNumber);
            addContactButton.setVisibility(View.GONE);
            phoneText.setVisibility(View.VISIBLE);
        }
    }

    private void onListItemClicked(View view, int position, long id){
        if(actionMode == null) {
            Intent intent = new Intent(getActivity(), RecordingDetailActivity.class);
            intent.setData(RecorderContract.buildRecordItem(RecorderContract.getContentUri(
                    RecorderContract.PATH_RECORD_WITH_CONTACT), id));
            startActivity(intent);
        } else {
            boolean select = adapter.getItemSelected(position) ? false : true;
            adapter.setItemSelected(position, id, select);

            if(adapter.getSelectedCount() == 0){
                actionMode.finish();
            } else {
                setActionModeTitle();
            }

        }
    }

    private void onListItemLongClicked(View view, int position, long id){
        if(actionMode == null) {
            boolean select = adapter.getItemSelected(position) ? false : true;
            adapter.setItemSelected(position, id, select);

            ActionBarActivity activity = (ActionBarActivity) getActivity();
            actionMode = activity.startSupportActionMode(this);
            setActionModeTitle();
            ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void setActionModeTitle(){
        actionMode.setTitle(String.format(getString(R.string.selected), adapter.getSelectedCount()));
    }

    private void updateListView(){
        int id = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry._ID));
        String selection = RecorderContract.RecordEntry.COLUMN_CONTACT_KEY + " = ? ";
        String [] selectionArgs = {Integer.toString(id)};
        String order = RecorderContract.RecordEntry._ID + " DESC";
        Cursor recordCursor = getActivity().getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_RECORD),
                null, selection, selectionArgs, order);
        adapter = new ContactDetailAdapterNew(getActivity(), recordCursor);
        listView.setAdapter(adapter);
    }

    private void onChangeButton(){
        ContentValues contentValues = new ContentValues();
        if(mode == PreferanceStrings.RECORDING_MODE_EVERYTHING){
            if(recorded){
                contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, 1);
            } else {
                contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, 0);
            }
        } else if(mode == PreferanceStrings.RECORDING_MODE_ONLY_SELECTED){
            if(recorded){
                contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, 0);
            } else {
                contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, 1);
            }
        }
        getActivity().getContentResolver().update(uri, contentValues, null, null);

        recorded = recorded ? false : true;

        if(recorded) {
            statusText.setText(R.string.contact_detail_status_recorded);
            changeStatusButton.setText(R.string.contact_detail_status_set_not_record);
        } else {
            statusText.setText(R.string.contact_detail_status_not_recorded);
            changeStatusButton.setText(R.string.contact_detail_status_set_record);
        }
    }

    private void onAddContactButton(){

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);

        getActivity().startActivityForResult(intent, REQUEST_ADD_CONTACT);
    }

    private void onMainImage(){
        Intent intent = new Intent();
        intent.setAction(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
        intent.setData(Uri.fromParts("tel", phoneNumber, null));
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        getActivity().startActivity(intent);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_mode_delete:
                onDelete(false);
                return true;
        }

        return false;
    }


    private void onDelete(boolean delete){
        if(delete){
            ArrayList<Long> selectedIds = adapter.getSelectedIds();
            for(Long id : selectedIds){
                Uri uri = Uri.withAppendedPath(RecorderContract.getContentUri(RecorderContract.PATH_RECORD),
                        Long.toString(id));
                Cursor cursor = getActivity().getContentResolver().query(
                        uri, new String[]{RecorderContract.RecordEntry.COLUMN_PATH}, null, null, null);
                cursor.moveToFirst();
                String path = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_PATH));
                File file = new File(path);
                file.delete();
                getActivity().getContentResolver().delete(uri, null, null);
            }
            actionMode.finish();
            updateListView();
        } else {
            RecordingsDeleteAlertDialog alertDialog = new RecordingsDeleteAlertDialog();
            alertDialog.setCount(adapter.getSelectedCount());
            alertDialog.setOnDeleteListener(new RecordingsDeleteAlertDialog.OnDeleteListener() {
                @Override
                public void onUserClickedDelete() {
                    onDelete(true);
                }
            });
            alertDialog.show(getActivity().getSupportFragmentManager(), "TAG");
        }

    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter.clearSelected();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_contact_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent upIntent = new Intent(getActivity(), MainActivity.class);
                upIntent.putExtra("position", 2);
                NavUtils.navigateUpTo(getActivity(), upIntent);
                return true;
            case R.id.menu_contact_detail_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
                return true;
            case R.id.menu_contact_detail_message:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null)));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ADD_CONTACT){
            new UpdateUserData(getActivity()).execute();
        }

    }
}
