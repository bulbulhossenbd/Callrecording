package com.pgizka.simplecallrecorder.recordings;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.contacts.ContactDetailActivity;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;


public class RecordingDetailFragment extends Fragment {
    static final String TAG = RecordingDetailFragment.class.getSimpleName();

    ImageView mainImage, callImage, playPauseImage;
    TextView dateText, durationText, sizeText, playDurationText, playPositionText;
    Button deleteButton, shareButton, callButton, messageButton;
    SeekBar seekBar;
    EditText notesEditText;

    Uri uri;
    Cursor cursor;
    String audioFilePath, phoneNumber, notesInitial;

    Handler handler = new Handler();
    MediaPlayer mediaPlayer;
    boolean isPlaybackOn = false, isPlayerUpdate = false, isUpdateProgressBar = false;
    int progressSeted, currentPosition = 0;

    public RecordingDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recording_detail, container, false);

        mainImage = (ImageView) view.findViewById(R.id.recording_detail_main_image);
        callImage = (ImageView) view.findViewById(R.id.recording_detail_call_image);
        playPauseImage = (ImageView) view.findViewById(R.id.recording_detail_play_image);
        dateText = (TextView) view.findViewById(R.id.recording_detail_date_text);
        durationText = (TextView) view.findViewById(R.id.recording_detail_duration_text);
        sizeText = (TextView) view.findViewById(R.id.recording_detail_size_text);
        playDurationText = (TextView) view.findViewById(R.id.recording_detail_play_duration_text);
        playPositionText = (TextView) view.findViewById(R.id.recording_detail_play_position_text);
        deleteButton= (Button) view.findViewById(R.id.recording_detail_delete_button);
        shareButton = (Button) view.findViewById(R.id.recording_detail_share_button);
        //callButton = (Button) view.findViewById(R.id.recording_detail_call_button);
        //messageButton = (Button) view.findViewById(R.id.recording_detail_message_button);
        seekBar = (SeekBar) view.findViewById(R.id.recording_detail_seek_bar);
        notesEditText = (EditText) view.findViewById(R.id.recording_detail_notes_edit_text);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDelete(true);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShare();
            }
        });

        playPauseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayPauseImage();
            }
        });

        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMainImage();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    progressSeted = progress;
                    playPositionText.setText(Utils.formatDuration(mediaPlayer.getDuration()*progress/100/1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUpdateProgressBar = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUpdateProgressBar = true;
                setPlayPosition(progressSeted);
            }
        });

        uri = getActivity().getIntent().getData();

        setHasOptionsMenu(true);

        if(savedInstanceState != null){
            currentPosition = savedInstanceState.getInt("playPosition", 0);
            isPlaybackOn = savedInstanceState.getBoolean("playOn", false);
            Log.d(TAG, "saved state is not null current position " + currentPosition);
        } else {
            Log.d(TAG, "state is null");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        setUpScreen();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(currentPosition > 0){
            mediaPlayer.seekTo(currentPosition);
            if(isPlaybackOn) {
                play();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(isPlaybackOn) {
            pause();
        }

        String notesCurrent = notesEditText.getText().toString();
        if((notesInitial == null && !TextUtils.isEmpty(notesCurrent))
                || (notesInitial != null && notesCurrent != null && !notesInitial.equals(notesCurrent))){
            int id = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry._ID));
            Uri uri = Uri.withAppendedPath(RecorderContract.getContentUri(RecorderContract.PATH_RECORD),
                    String.valueOf(id));
            ContentValues contentValues = new ContentValues();
            contentValues.put(RecorderContract.RecordEntry.COLUMN_NOTES, notesCurrent);
            getActivity().getContentResolver().update(uri, contentValues, null, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "on save state");
        currentPosition = mediaPlayer.getCurrentPosition();
        if(currentPosition > 0){
            Log.d(TAG, "current position is " + currentPosition);
            outState.putInt("playPosition", currentPosition);
            outState.putBoolean("playOn", isPlaybackOn);
        }
        super.onSaveInstanceState(outState);
    }

    private void setUpScreen(){
        cursor.moveToFirst();

        phoneNumber = cursor.getString(cursor.getColumnIndex(
                RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));
        audioFilePath = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_PATH));
        int duration = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_LENGTH));
        int type = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_TYPE));
        long date = cursor.getLong(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_DATE));
        String displayName = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
        String contactId = cursor.getString(cursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_CONTACT_ID));
        notesInitial = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_NOTES));
        int recordingSource = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_SOURCE));
        int recordingError = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_SOURCE_ERROR));

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if(displayName != null && !TextUtils.isEmpty(displayName)) {
            actionBar.setTitle(displayName);
        } else {
            actionBar.setTitle(phoneNumber);
        }


        if(contactId != null && !TextUtils.isEmpty(contactId)) {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getActivity().getContentResolver(), uri);
            if(input != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                mainImage.setImageBitmap(bitmap);
            }
        }

        if(type == RecorderContract.RecordEntry.TYPE_INCOMING){
            callImage.setImageResource(R.drawable.call_arrow_incoming);
        } else {
            callImage.setImageResource(R.drawable.call_arrow_outgoing);
        }

        String dateFormated = new SimpleDateFormat("HH:mm dd MMM").format(date);
        dateText.setText(dateFormated);

        String durationFormated = Utils.formatDuration(duration);
        durationText.setText(durationFormated);
        playDurationText.setText(durationFormated);

        File file = new File(audioFilePath);
        int size = (int) (file.length() / 1024);
        sizeText.setText(size + "KB");

        if(notesInitial != null) {
            notesEditText.setText(notesInitial);
        }

        if(recordingError == 1){
            String[] source = getResources().getStringArray(R.array.settings_recording_source);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.recording_detail_alert_title)
                    .setMessage(String.format(getString(R.string.recording_detail_alert_message),
                            source[recordingSource], source[recordingSource]))
                    .setPositiveButton("Ok", null);
            builder.create().show();

            ContentValues contentValues = new ContentValues();
            contentValues.put(RecorderContract.RecordEntry.COLUMN_SOURCE, RecorderContract.RecordEntry.SOURCE_MIC);
            contentValues.put(RecorderContract.RecordEntry.COLUMN_SOURCE_ERROR, 0);
            String id = String.valueOf(cursor.getInt(0));
            Uri uri = Uri.withAppendedPath(RecorderContract.getContentUri(RecorderContract.PATH_RECORD), id);
            getActivity().getContentResolver().update(uri, contentValues, null, null);
        }

    }

    private void onPlayPauseImage(){
        if(isPlaybackOn){
            pause();
        } else {
            play();
        }
    }

    Runnable playerRun = new Runnable() {
        @Override
        public void run() {
            playerUpdate();
        }
    };

    private void playerUpdate(){
        int progress = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();
        Log.d(TAG, "progress is " + progress);
        if(isUpdateProgressBar) {
            seekBar.setProgress(progress);
        }
        playPositionText.setText(Utils.formatDuration(mediaPlayer.getCurrentPosition() / 1000));
        if(progress >= 99){
            playPauseImage.setImageResource(R.drawable.ic_action_play);
            isPlaybackOn = false;
            isPlayerUpdate = false;
            seekBar.setProgress(0);
            playPositionText.setText(Utils.formatDuration(0));
        } else if(isPlayerUpdate) {
            handler.postDelayed(playerRun, 1000);
        }
    }

    private void setPlayPosition(int progress){
        mediaPlayer.seekTo(mediaPlayer.getDuration() * progress / 100);
    }

    private void play(){
        mediaPlayer.start();
        isPlaybackOn = true;
        isPlayerUpdate = true;
        isUpdateProgressBar = true;
        playerUpdate();
        playPauseImage.setImageResource(R.drawable.ic_action_pause);
    }

    private void pause(){
        mediaPlayer.pause();
        playPauseImage.setImageResource(R.drawable.ic_action_play);
        isPlaybackOn = false;
        isPlayerUpdate = false;
    }

    private void onDelete(boolean showAlertDialog){
        if(showAlertDialog){
            RecordingsDeleteAlertDialog dialog = new RecordingsDeleteAlertDialog();
            dialog.setCount(1);
            dialog.setOnDeleteListener(new RecordingsDeleteAlertDialog.OnDeleteListener() {
                @Override
                public void onUserClickedDelete() {
                    onDelete(false);
                }
            });
            dialog.show(getActivity().getSupportFragmentManager(), "on delete alert");
        } else {
            int contactId = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_CONTACT_KEY));
            int id = cursor.getInt(cursor.getColumnIndex(/*RecorderContract.RecordEntry.TABLE_NAME + "."
                    + */RecorderContract.RecordEntry._ID));

            String path = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_PATH));
            File file = new File(path);
            file.delete();

            Uri uri = Uri.withAppendedPath(RecorderContract.getContentUri(RecorderContract.PATH_RECORD),
                    Integer.toString(id));

            getActivity().getContentResolver().delete(uri, null, null);

            getActivity().finish();
        }
    }

    private void onShare(){
        String audioPath = cursor.getString(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_PATH));

        Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setDataAndType(Uri.fromFile(new File(audioPath)), "audio/*");

        Uri uri = Uri.parse(audioPath);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(intent);
    }

    private void onMainImage(){
        Intent intent = new Intent();
        intent.setAction(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
        intent.setData(Uri.fromParts("tel", phoneNumber, null));
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
        getActivity().startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_recording_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                cursor.moveToFirst();
                int id = cursor.getInt(cursor.getColumnIndex(RecorderContract.RecordEntry.COLUMN_CONTACT_KEY));
                Uri uri = RecorderContract.buildRecordItem(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT), id);
                Intent upIntent = new Intent(getActivity(), ContactDetailActivity.class);
                upIntent.setData(uri);
                NavUtils.navigateUpTo(getActivity(), upIntent);
                return true;
            case R.id.menu_recording_detail_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
                return true;
            case R.id.menu_recording_detail_message:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null)));
                return true;
        }


        return super.onOptionsItemSelected(item);
    }



}
