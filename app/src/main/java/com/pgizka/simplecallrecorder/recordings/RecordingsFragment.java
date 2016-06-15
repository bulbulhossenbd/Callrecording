package com.pgizka.simplecallrecorder.recordings;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;

import java.io.File;
import java.util.ArrayList;


public class RecordingsFragment extends Fragment implements ActionMode.Callback {
    static  final String TAG = RecordingsFragment.class.getSimpleName();

    ListView listView;
    TextView emptyText;
    RecordingsAdapter recordingsAdapter;

    ActionMode actionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recordings, container, false);

        listView = (ListView) view.findViewById(R.id.recordings_list_viev);
        emptyText = (TextView) view.findViewById(R.id.recordings_empty_text);
        listView.setEmptyView(emptyText);

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



        return view;
    }

    private void onListItemClicked(View view, int position, long id){
        if(actionMode == null) {
            Intent intent = new Intent(getActivity(), RecordingDetailActivity.class);
            intent.setData(RecorderContract.buildRecordItem(RecorderContract.getContentUri(
                    RecorderContract.PATH_RECORD_WITH_CONTACT), id));
            startActivity(intent);
        } else {
            boolean select = recordingsAdapter.getItemSelected(position) ? false : true;
            recordingsAdapter.setItemSelected(position, id, select);

            if(recordingsAdapter.getSelectedCount() == 0){
                actionMode.finish();
            } else {
                setActionModeTitle();
            }

        }
    }

    private void onListItemLongClicked(View view, int position, long id){
        if(actionMode == null) {
            boolean select = recordingsAdapter.getItemSelected(position) ? false : true;
            recordingsAdapter.setItemSelected(position, id, select);

            ActionBarActivity activity = (ActionBarActivity) getActivity();
            actionMode = activity.startSupportActionMode(this);
            setActionModeTitle();
            ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void setActionModeTitle(){
        actionMode.setTitle(String.format(getString(R.string.selected), recordingsAdapter.getSelectedCount()));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateListView();
    }

    private void updateListView(){
        String order = RecorderContract.RecordEntry.TABLE_NAME + "." + RecorderContract.RecordEntry._ID + " DESC";
        Cursor cursor = getActivity().getContentResolver().query(
                RecorderContract.getContentUri(RecorderContract.PATH_RECORD_WITH_CONTACT),
                null, null, null, order);

        recordingsAdapter = new RecordingsAdapter(getActivity(), cursor);
        listView.setAdapter(recordingsAdapter);

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
            ArrayList<Long> selectedIds = recordingsAdapter.getSelectedIds();
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
            alertDialog.setCount(recordingsAdapter.getSelectedCount());
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
        recordingsAdapter.clearSelected();
    }
}
