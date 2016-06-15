package com.pgizka.simplecallrecorder.servicerecording;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.main.MainActivity;
import com.pgizka.simplecallrecorder.recordings.RecordingDetailActivity;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.UpdateUserData;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.text.SimpleDateFormat;


public class RecordingService extends Service {
    static final String TAG = RecordingService.class.getSimpleName();
    static final int NOTIFICATION_RECORDING = 0;
    static final int NOTIFICATION_CALL_RECORDED = 1;

    MediaRecorder recorder, recorderMic;
    File audiofile, audioFileMic;
    boolean recordstarted = false, recorderThrewException = false;

    Bundle bundle;
    String state;

    SharedPreferences systemPref;
    SharedPreferences userPref;

    String phoneNumber;
    boolean wasIncoming = false;
    String fileName, fileNameMic;
    long startedTime;
    int recordingSource;

    NotificationManager notificationManager;
    boolean updateNotification = false;
    Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        recorder = new MediaRecorder();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "on created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processCall(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void processCall(Intent intent){

        if(intent == null){
            Log.d(TAG, "intent is null");
            Toast.makeText(this, "intent is null", Toast.LENGTH_SHORT).show();
            return;
        }

        systemPref = getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE, Context.MODE_PRIVATE);
        userPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean recordingEnabled = systemPref.getBoolean(PreferanceStrings.RECORDING_ENABLED, false);
        if(!recordingEnabled){
            Log.d(TAG, "recording is not enabled");
            return;
        }


        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG, "outgoing,ringing:" + phoneNumber);
            //Toast.makeText(this, "outgoing,ringing:" + phoneNumber, Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "it wasnt outgoing call");
        }

        bundle = intent.getExtras();
        if(bundle == null){
            Log.wtf(TAG, "Bundle is null");
            return;
        }

        state = bundle.getString(TelephonyManager.EXTRA_STATE);
        if(state == null){
            Log.d(TAG, "state is null");
            return;
        } else {
            Log.d(TAG, "state is not null");
        }

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            wasIncoming = true;
            phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            //Toast.makeText(this, "Icoming ", Toast.LENGTH_LONG).show();
        }

        phoneNumber = Utils.normalizePhoneNumber(phoneNumber);

        //Check wheater to record
        int recordingMode = systemPref.getInt(PreferanceStrings.RECORDING_MODE, 0);
        String selectionContact = RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " = ?";
        String[] selectionContactArgs = {phoneNumber};

        Cursor contactCursor = getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                null, selectionContact, selectionContactArgs, null);

        int recorded = 0, ignored = 0;
        if(contactCursor.getCount() > 0){
            contactCursor.moveToFirst();
            recorded = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_RECORDED));
            ignored = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_IGNORED));
        }

        boolean canRecord = false;

        if(recordingMode == PreferanceStrings.RECORDING_MODE_ONLY_SELECTED){
            if(contactCursor.getCount() == 0) {
                Log.d(TAG, "recording mode is only selected, and given contact doesn't exist");
                canRecord = false;
            } else if(recorded == 1){
                Log.d(TAG, "recording mode is only selected, and given contact exist and is recorded");
                canRecord = true;
            }
        } else {
            if(contactCursor.getCount() == 0){
                canRecord = true;
            } else if(ignored == 0){
                canRecord = true;
            }
        }

        if(canRecord){
            Log.d(TAG, "can record time to start recordin");
        } else {
            Log.d(TAG, "can not record");
            return;
        }
        contactCursor.close();
        // end of checking whether to record


        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

            recordingSource = Integer.parseInt(userPref.getString(PreferanceStrings.USER_RECORDING_SOURCE, "0"));
            int recordingFormat = Integer.parseInt(userPref.getString(PreferanceStrings.USER_RECORDING_FORMAT, "0"));
            String recordingPath = userPref.getString(PreferanceStrings.USER_RECORDING_PATH, "/sdcard/simpleCallRecorder");
            //boolean turnOnPhone = userPref.getBoolean(PreferanceStrings.USER_TURN_ON_PHONE, false);
            boolean increaseVolume = userPref.getBoolean(PreferanceStrings.USER_TURN_UP_VOLUME, true);

            recorder = new MediaRecorder();

            String audioSufix = null;

            switch (recordingSource){
                case 0:
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    break;
                case 1:
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);
                    break;
                case 2:
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK);
                    break;
                case 3:
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    break;
            }

            if(recordingSource >= 0 && recordingSource <= 2){
                recorderMic = new MediaRecorder();
                recorderMic.setAudioSource(MediaRecorder.AudioSource.MIC);
            } else {
                recorderMic = null;
            }

            switch (recordingFormat){
                case 0:
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    audioSufix = ".amr";
                    if(recorderMic != null){
                        recorderMic.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                        recorderMic.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    }
                    break;
                case 1:
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    audioSufix = ".3gp";
                    if(recorderMic != null){
                        recorderMic.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        recorderMic.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    }
                    break;
                case 2:
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    audioSufix = ".mp4";
                    if(recorderMic != null){
                        recorderMic.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        recorderMic.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    }
                    break;
            }



            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            if(increaseVolume){
                Log.d(TAG, "volume increaded");
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
            }
            /*
            if(turnOnPhone){
                Log.d(TAG, "turn speaker on");
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(true);
            }*/

            Log.d(TAG, "Call started");

            //Toast.makeText(this, "Call started", Toast.LENGTH_LONG).show();
            startedTime = System.currentTimeMillis();
            showRecordingNotification();

            fileName = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(System.currentTimeMillis());

            if(wasIncoming){
                fileName += "_Incoming";
            }  else {
                fileName += "_Outgoing";
            }

            if(recorderMic != null){
                fileNameMic = fileName + "_MIC";
            }

            //File sampleDir = new File(Environment.getExternalStorageDirectory(), "SimpleCallRecorder");
            File sampleDir = new File(recordingPath);
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            Log.d(TAG, "file name is: " + fileName);

            audiofile = new File(sampleDir, fileName + audioSufix);
            if(recorderMic != null){
                audioFileMic = new File(sampleDir, fileNameMic + audioSufix);
            }

            recorder.setOutputFile(audiofile.getAbsolutePath());
            if(recorderMic != null){
                recorderMic.setOutputFile(audioFileMic.getAbsolutePath());
            }
            try {
                recorder.prepare();
                if(recorderMic != null){
                    recorderMic.prepare();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Toast.makeText(this, "Recorder threw exception", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Recorder threw exception", Toast.LENGTH_LONG).show();
            }
            try {
                recorder.start();
            } catch(Exception e){
                e.printStackTrace();
                recorderThrewException = true;
            }
            if(recorderMic != null){
                recorderMic.start();
            }
            recordstarted = true;
        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {

            if (recordstarted) {
                Log.d(TAG, "recordre stoped");
                if(!recorderThrewException) {
                    recorder.stop();
                }
                recorder.release();
                if(recorderMic != null){
                    recorderMic.stop();
                    recorderMic.release();
                }
                recordstarted = false;
            }

            int conversationDuration = (int) ((System.currentTimeMillis() - startedTime)/1000);

            String selection = RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " = ?";
            String [] selectionArgs = {phoneNumber};

            contactCursor = getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                    null, selection, selectionArgs, null);

            int contactId;
            if(contactCursor.getCount() == 0){
                ContentValues contentValues = new ContentValues();
                contentValues.put(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER, phoneNumber);
                contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, false);
                contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, false);
                Uri uri = getContentResolver().insert(
                        RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                        contentValues);
                Log.d(TAG, "Last path segment is: " + uri.getLastPathSegment());
                contactId = Integer.parseInt(uri.getLastPathSegment());
                new UpdateUserData(this).execute();
            } else {
                contactCursor.moveToFirst();
                contactId = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry._ID));
            }

            //check wheater recording from source other then mic was successful
            String path;
            int recordingVoiceError;
            if((recorderThrewException || audiofile.length() < 20)  && recorderMic != null){
                path = audioFileMic.getAbsolutePath();
                recordingVoiceError = 1;
                audiofile.delete();
                SharedPreferences.Editor editor = userPref.edit();
                editor.putString(PreferanceStrings.USER_RECORDING_SOURCE, "3");
                editor.commit();
            } else {
                path = audiofile.getAbsolutePath();
                recordingVoiceError = 0;
                if(recorderMic != null) {
                    audioFileMic.delete();
                }
            }
            recorderThrewException = false;

            ContentValues contentValues = new ContentValues();
            contentValues.put(RecorderContract.RecordEntry.COLUMN_CONTACT_KEY, contactId);
            contentValues.put(RecorderContract.RecordEntry.COLUMN_DATE, startedTime);
            contentValues.put(RecorderContract.RecordEntry.COLUMN_LENGTH, conversationDuration);
            contentValues.put(RecorderContract.RecordEntry.COLUMN_PATH, path);
            contentValues.put(RecorderContract.RecordEntry.COLUMN_SOURCE, recordingSource);
            contentValues.put(RecorderContract.RecordEntry.COLUMN_SOURCE_ERROR, recordingVoiceError);
            if(wasIncoming) {
                contentValues.put(RecorderContract.RecordEntry.COLUMN_TYPE, RecorderContract.RecordEntry.TYPE_INCOMING);
            } else {
                contentValues.put(RecorderContract.RecordEntry.COLUMN_TYPE, RecorderContract.RecordEntry.TYPE_OUTGOING);
            }
            Uri uri = getContentResolver().insert(RecorderContract.getContentUri(RecorderContract.PATH_RECORD), contentValues);

            //Toast.makeText(this, "REJECT || DISCO", Toast.LENGTH_LONG).show();
            hideRecordingNotification(conversationDuration, uri);

            wasIncoming = false;
        }

    }

    private void showRecordingNotification(){
        boolean showNotifiaction = userPref.getBoolean(PreferanceStrings.USER_NOTIFICATION_DURING_CALL, true);
        if(showNotifiaction) {
            updateNotification = true;
            updateNotification();
        }
    }

    private void hideRecordingNotification(int duration, Uri uri){
        updateNotification = false;
        notificationManager.cancel("TAG", NOTIFICATION_RECORDING);

        boolean showNotification = userPref.getBoolean(PreferanceStrings.USER_NOTIFICATION_POST_CALL, true);
        if(!showNotification){
            return;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_action_call_dark)
                        .setContentTitle(getString(R.string.service_notification_call_recorded) + " " + phoneNumber)
                        .setContentText(Utils.formatDuration(duration))
                        .setAutoCancel(true);

        int id = Integer.parseInt(uri.getLastPathSegment());
        Intent intent = new Intent(this, RecordingDetailActivity.class);
        intent.setData(
                Uri.withAppendedPath(RecorderContract.getContentUri(RecorderContract.PATH_RECORD_WITH_CONTACT),
                        String.valueOf(id)));

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);

        taskStackBuilder.addParentStack(RecordingDetailActivity.class);
        taskStackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(2, PendingIntent.FLAG_ONE_SHOT);

        mBuilder.setContentIntent(pendingIntent);
        //NotificationManager notificationManager =
        //        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        notificationManager.notify("TAG", NOTIFICATION_CALL_RECORDED, mBuilder.build());
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateNotification();
        }
    };

    private void updateNotification(){
        if(updateNotification) {
            int currentDuration = (int) ((System.currentTimeMillis() - startedTime) / 1000);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_recording_icon)
                            .setContentTitle(getString(R.string.service_notification_recording) + " " +
                                    Utils.formatDuration(currentDuration))
                            .setContentText(phoneNumber);
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setContentIntent(pendingIntent);
            notificationManager.notify("TAG", NOTIFICATION_RECORDING, mBuilder.build());
        }
        if(updateNotification){
            handler.postDelayed(runnable, 1000);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
