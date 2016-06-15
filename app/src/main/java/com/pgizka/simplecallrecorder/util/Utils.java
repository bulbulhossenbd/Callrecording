package com.pgizka.simplecallrecorder.util;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.pgizka.simplecallrecorder.R;

import java.io.InputStream;
import java.text.SimpleDateFormat;

/**
 * Created by Paweł on 2015-07-17.
 */
public class Utils {

    /**Returns formated duration which goes like 0:03:23
     *
     * @param duration number of seconds
     * @return formated String
     */
    public static String formatDuration(int duration){
        int hours = 0;
        int minutes = duration/60;
        int seconds = duration%60;
        if(minutes >= 60){
            hours = minutes/60;
            minutes %= 60;
        }
        String formatedMinutes;
        if(minutes < 10){
            formatedMinutes = "0" + minutes;
        } else {
            formatedMinutes = Integer.toString(minutes);
        }
        String formatedSeconds;
        if(seconds < 10){
            formatedSeconds = "0" + seconds;
        } else {
            formatedSeconds = Integer.toString(seconds);
        }

        String durationFormated = hours + ":" + formatedMinutes + ":" + formatedSeconds;

        return durationFormated;
    }

    public static String formatTime(long time){
        SimpleDateFormat format = new SimpleDateFormat("kk:mm");
        return format.format(time);
    }

    public static String formatDate(long time){
        SimpleDateFormat format = new SimpleDateFormat("EEEEE dd MMMMM");
        return format.format(time);
    }

    public static String normalizePhoneNumber(String phoneNumber){
        String normalizedPhoneNumber;
        if(phoneNumber.charAt(0) == '+'){
            normalizedPhoneNumber = phoneNumber.substring(3);
        } else {
            normalizedPhoneNumber = phoneNumber;
        }
        return normalizedPhoneNumber;
    }

    public static Bitmap getDisplayImage(Context context, String contactId) {
        Bitmap bitmap = null;
        if (contactId != null && !TextUtils.isEmpty(contactId)) {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
            if (input != null) {
                bitmap = BitmapFactory.decodeStream(input);
            } else {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.defult_contact_image);
            }
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.defult_contact_image);
        }
        return bitmap;
    }



}
