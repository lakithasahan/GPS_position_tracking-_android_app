package com.ltronic.find;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class SMScheck_service extends Service {
    private Handler h;
    private Runnable r;


    @Nullable
    private String messageboady;
    @Nullable
    private String number;
    @Nullable
    private String prevous_message;
    private String[] appPermissions={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS,Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.PROCESS_OUTGOING_CALLS,Manifest.permission.CALL_PHONE,Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE};
    private int counter = 0;
    public int done=0;
    private static final int RequestPermissionCode = 1;
    private static SMScheck_service inst;
    public int count=0;
    final static String MY_ACTION = "MY_ACTION";

    final static String REGISTRATION = "REGISTRATION";
    SmsManager smsManager = SmsManager.getDefault();
    private AudioManager myAudioManager;
    private int done_flag=0;

    @Nullable
    private String file_name=null;
    private int cordinates_received=0;
    public int registration_mode=0;
    private int number_check=0;
    private boolean permision_flag=false;

    public int mainactivity_start=0;
    public static SMScheck_service instance() {
        return inst;
    }


    public SMScheck_service() {

            final Handler handle = new Handler();
            handle.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (done_flag == 0) {
                        refreshSmsInbox();
                    }
                    handle.postDelayed(this, 50);
                }
            }, 50);



    }






    private void refreshSmsInbox() {
        done_flag=1;
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        String str = "SMS From: " + smsInboxCursor.getString(indexAddress) + "\n" + smsInboxCursor.getString(indexBody) + "\n";
        number = smsInboxCursor.getString(indexAddress).trim();
        messageboady = smsInboxCursor.getString(indexBody).trim();

        number = number.replaceAll("[^\\p{ASCII}]", "");
        messageboady = messageboady.replaceAll("[^\\p{ASCII}]", "");
        number=number.replace(" ","").trim();
        messageboady=messageboady.replace(" ","").trim();

        if (!messageboady.equals(prevous_message)) {


            prevous_message=messageboady;
            String a = "Start";
            String b = "Stop";

            String path = getFilesDir().getAbsolutePath();
            final File file = new File(path);
            File[] list = file.listFiles();
            int count = 0;
            for (File f : list) {
                file_name = f.getName();
                file_name = file_name.replaceAll("[^\\p{ASCII}]", "");
              //  Log.i("filestored_number", file_name);
                if (file_name.endsWith(".txt")) {


                    if (file_name.equals("registragionflag.txt")) {
                        Log.i("inboxreceivedbumber", "Registration_mode");
                        number_check = 1;
                        break;
                    }
                    if ((!file_name.equals("number.txt")) || (!file_name.equals("pin.txt"))) {

                        String txt = readFromFile(SMScheck_service.this, file_name.trim()).trim();
                        String[] array = txt.split(",");

                        if (array.length > 1) {
                            Log.i("filestored_number", array[1]);
                            array[1]=array[1].replace(" ","");

                            //  Toast.makeText(this,number, Toast.LENGTH_LONG).show();
                            if (array[1].trim().equals(number) && number_check == 0) {
                                number_check = 1;
                                break;

                            } else {
                                number_check = 0;
                            }
                        }


                    }
                }


            }
            Log.i("inboxreceivedbumber", number);


            if (number_check == 1) {

                String[] message = messageboady.trim().split("#");
                Log.i("SMSactivityddsff-----", message[0]);
                if (message != null && message.length > 1) {


                    String id1 = "Find";
                    String id2 = "R";
                    String id3 = "W";
                    String id4 = "S";
                    String id5 = "G";
                    String id6 = "Emergency";
                    String id7="FindR";

                    if (message[0].trim().equals(id1)) {

                        //Toast.makeText(getApplicationContext(), "GPS data received", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setAction(MY_ACTION);
                        intent.putExtra("DATAPASSED", message[1].trim());
                        sendBroadcast(intent);
                    }

                    else if ((message[0].trim().equals(id2))) {
                        //  Toast.makeText(getApplicationContext(), message[1].trim(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setAction(REGISTRATION);
                        intent.putExtra("REGISTRATION", message[1].trim());
                        sendBroadcast(intent);
                        Log.i("inboxreceivedbumber", "sd'kifsjfdfd[safsidjfsodfsidfisdidsjoisdfisdisdjfi");

                    }else if((message[0].trim().equals(id7))){
                        Intent intent = new Intent();
                        intent.setAction(REGISTRATION);
                        intent.putExtra("REGISTRATION", message[1].trim());
                        sendBroadcast(intent);
                    }


                    else if ((message[0].trim().equals(id6))) {
                        //  startActivityForResult(new Intent(android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);

                        myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        myAudioManager.setStreamVolume(AudioManager.STREAM_RING, myAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                        // myAudioManager.setMode(AudioManager.MODE_IN_CALL);
                        //  if (!myAudioManager.isSpeakerphoneOn())
                        //    myAudioManager.setSpeakerphoneOn(true);
                        // myAudioManager.setMode(AudioManager.MODE_NORMAL);

                    } else if ((message[0].trim().equals(id3)) && (message[1].trim().equals("Start"))) {

                       // Toast.makeText(getApplicationContext(), "start!", Toast.LENGTH_SHORT).show();
                        writeToFile(number.trim(), SMScheck_service.this);
                        cordinates_received = 0;
                        startService(new Intent(this, GPS_service.class));
                    } else if ((message[0].trim().equals(id3)) && (message[1].trim().equals("Stop"))) {

                       // Toast.makeText(getApplicationContext(), "stop!", Toast.LENGTH_SHORT).show();
                        // smsManager.sendTextMessage(number.trim(), null, "Find app", null, null);
                        stopService(new Intent(this, GPS_service.class));
                    }



                }


                number_check = 0;

            }
            number=null;
            messageboady=null;


        }
        done_flag=0;
    }




    @NonNull
    private String readFromFile(Context context, String file_name) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(file_name);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString().trim();
                //  Toast.makeText(this,ret,Toast.LENGTH_LONG).show();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private Notification updateNotification() {
        counter++;
        String info ="Find app Background service";

        Context context = getApplicationContext();

        PendingIntent action = PendingIntent.getActivity(context,
                0, new Intent(context, MainActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT); // Flag indicating that if the described PendingIntent already exists, the current one should be canceled before generating a new one.

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String CHANNEL_ID = "alex_channel";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AlexChannel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alex channel description");
            manager.createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        }
        else
        {
            builder = new NotificationCompat.Builder(context);
        }

        return builder.setContentIntent(action)
                .setSmallIcon(R.drawable.placeholder)
                .setContentTitle("Find App")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText("Background service")
                .setContentIntent(action)
                .setOngoing(true).build();
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {

        if (intent.getAction().contains("start")) {

            startForeground(101, updateNotification());


        } else {
           // h.removeCallbacks(r);
            //stopForeground(true);
            //stopSelf();
        }

        return Service.START_STICKY;
    }





    public void deleteSMS(){
        ContentResolver cr=getContentResolver();
        Uri url=Uri.parse("content://sms/");
        int num_deleted=cr.delete(url, null, null);
        Toast.makeText(this, num_deleted+" items are deleted.", Toast.LENGTH_SHORT).show();

    }

    private void writeToFile(String data, Context context) {

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("number.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

        super.onDestroy();

        //   startService(new Intent(this, MainActivity.class));
        // Log.d(TAG, "FirstService destroyed");
    }


}
