package com.ltronic.find;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Registration extends AppCompatActivity {
    private int flag=0;
    //private static final Object TODO ="";
    @Nullable
    private String Registration_Reply = null;
    @Nullable
    private String member_name = null;
    public int auth_flag = 0;
    @Nullable
    private String[] dataf=null;
    @Nullable
    private MyReceiver myReceiver;
    @Nullable
    private String data = null;
    private int registration_done = -1;
    private static Registration inst;
    public String priveous_registration_reply;
    private EditText namee;
    private EditText numbere;
    @Nullable
    private String number_pic;
    private Handler handle;
    private int pin_flag = 0;
    // public  EditText pin;
    public int member_count = 0;

    public static Registration instance() {
        return inst;
    }

    private int time = 0;
    ProgressDialog dialog;
    private SmsManager smsManager = SmsManager.getDefault();

    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    @Nullable
    private Uri uriContact;
    private String contactID;     // contacts unique ID
    @NonNull
    String[] appPermissions = {Manifest.permission.READ_CONTACTS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        namee = findViewById(R.id.editText);
        numbere = findViewById(R.id.editText2);
        File myFile = new File(getFilesDir(), "user.txt");
        if (myFile.exists()) {
          TextView pin = findViewById(R.id.textView16);
            String profile_data = readFromFile(Registration.this);
            String[] data = profile_data.trim().split(",");


          pin.setText("User Name -"+data[0]);
          pin.setEnabled(false);
            pin_flag = 1;
        }
        final SmsManager smsManager = SmsManager.getDefault();
        writeToFile2(Registration.this);
        final Button done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                data = namee.getText().toString().trim() + "," + numbere.getText().toString().trim();
                member_name = namee.getText().toString().trim();


                File myFile = new File(getFilesDir(), "user.txt");
                if (myFile.exists()) {

                    if(data!=null&&data.length()>1) {
                        String profile_data = readFromFile(Registration.this);
                        String[] data = profile_data.trim().split(",");
                        //String phone_number="+61416598921";
                        String phone_number = data[1];

                        Toast.makeText(Registration.this, "SMS will be send to the added member mobile number,Waiting 1 min for reply", Toast.LENGTH_LONG).show();

                        smsManager.sendTextMessage(numbere.getText().toString().trim(), null, "FindR#Registration," + phone_number+","+data[0], null, null);
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                       smsManager.sendTextMessage(numbere.getText().toString().trim(), null, "done" + phone_number, null, null);

                        registration_done = 0;
                    }else{
                        Toast.makeText(Registration.this, "Please enter new registration details.", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(Registration.this, "Please setup the Profile first..! ", Toast.LENGTH_SHORT).show();
                }
            }
        });


        final Button setup_profile = findViewById(R.id.setup_p);
        File myFile1 = new File(getFilesDir(), "user.txt");
        if (myFile1.exists()) {
        setup_profile.setText("SHOW");
        }
        setup_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Registration.this, profile_registration.class));
            }
        });


        final Button contact = findViewById(R.id.contact);
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);

            }
        });





        handle = new Handler();
        handle.postDelayed(new Runnable() {

            @Override
            public void run() {
                if(flag==0) {
                    auth_check();
                }

                handle.postDelayed(this, 2000);
            }
        }, 2000);


    }


    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            //   Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            retrieveContactName();
            retrieveContactNumber();
            retrieveContactPhoto();

        }
    }

    private String getMyPhoneNO() {
        TelephonyManager tMgr = (TelephonyManager)Registration.this.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String mPhoneNumber = tMgr.getLine1Number();
        //String yourNumber = mTelephonyMgr.getLine1Number();
        return mPhoneNumber;
    }
    private void retrieveContactPhoto() {

        Bitmap photo;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                saveToInternalStorage(photo);
                inputStream.close();
                //  ImageView imageView = (ImageView) findViewById(R.id.img_contact);
                // imageView.setImageBitmap(photo);
            }else{
                Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.profile);
                saveToInternalStorage(largeIcon);
               // inputStream.close();
            }

            assert inputStream != null;


        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    private void saveToInternalStorage(@NonNull Bitmap bitmapImage){
        //  ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        // File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(getFilesDir().getAbsolutePath(),number_pic+".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private void retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        //  Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();
        numbere.setText(contactNumber);
        //Log.d(TAG, "Contact Phone Number: " + contactNumber);
    }

    private void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();
        namee.setText(contactName);
        number_pic=contactName;
        // Log.d(TAG, "Contact Name: " + contactName);

    }



    private void auth_check(){

        if (time >61) {
            Toast.makeText(Registration.this, "Member failed to respond to text message sent", Toast.LENGTH_LONG).show();
            File myFile = new File(getFilesDir(),"registragionflag.txt");
            if(myFile.exists())
                myFile.delete();
            time=0;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finish();
        } else if (Registration_Reply!=null&&time<=61) {
            dataf=Registration_Reply.trim().split(",");
            if (Registration_Reply.equals("yes") || Registration_Reply.equals("Yes")) {
                Toast.makeText(Registration.this, "Registration successful!", Toast.LENGTH_LONG).show();
                writeToFile(data, Registration.this);

                File myFile = new File(getFilesDir(),"registragionflag.txt");
                if(myFile.exists())
                    myFile.delete();

                time = 0;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();

            }else if(dataf.length>1&&flag==0) {

                if (dataf[0].equals("Registration")) {
                    flag=1;


                    AlertDialog.Builder builder = new AlertDialog.Builder(Registration.this);
                    builder.setMessage("Accept the connection request from-"+dataf[2]+"? "+dataf[1])
                            .setCancelable(true)
                            .setPositiveButton("Yes,I am", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                  //  String[] dataf=Registration_Reply.trim().split(",");
                                    String connect_user_data=dataf[2].trim()+","+dataf[1].trim();
                                    writeToFile3(connect_user_data, dataf[2].trim(),Registration.this);
                                    smsManager.sendTextMessage(dataf[1], null, "R#yes", null, null);
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    smsManager.sendTextMessage(dataf[1], null, "Done", null, null);
                                    flag=0;
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();





                    File myFile = new File(getFilesDir(), "registragionflag.txt");
                    if (myFile.exists())
                        myFile.delete();
                    Registration_Reply = null;
                }
            }
        }



        time++;

    }


    @NonNull
    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("user.txt");

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
               // Toast.makeText(this,ret,Toast.LENGTH_LONG).show();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


    private void writeToFile(String data, @NonNull Context context) {
        if(member_name!=null) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(member_name + ".txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    private void writeToFile3(String data, @Nullable String name , @NonNull Context context) {
        if(name!=null) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name + ".txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }



    private void writeToFile2(@NonNull Context context) {
        if("04081994" !=null) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput( "registragionflag.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write("04081994");
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    public void onResume() {

        super.onResume();

        myReceiver = new Registration.MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SMScheck_service.REGISTRATION);
        registerReceiver(myReceiver, intentFilter);
        writeToFile2(Registration.this);
    }

    @Override
    public void onStart() {
        super.onStart();

        myReceiver = new Registration.MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SMScheck_service.REGISTRATION);

        try {
            if (myReceiver != null) {
                this.unregisterReceiver(myReceiver);
            }
        } catch (IllegalArgumentException e) {
            //  Log.i(TAG,"epicReciver is already unregistered");
            myReceiver = null;
        }
        inst = this;

    }



    public  void onDestroy() {

        super.onDestroy();
        handle.removeCallbacksAndMessages(null);
        File myFile = new File(getFilesDir(),"registragionflag.txt");

        if(myFile.exists())
            myFile.delete();
        // Log.i("SMSactivityddsff",  "deleted");

        try {
            if (myReceiver != null) {
                this.unregisterReceiver(myReceiver);
            }
        } catch (IllegalArgumentException e) {
            //  Log.i(TAG,"epicReciver is already unregistered");
            myReceiver = null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        File myFile = new File(getFilesDir(),"registragionflag.txt");
        if(myFile.exists())
            myFile.delete();

        try {
            if (myReceiver != null) {
                this.unregisterReceiver(myReceiver);
            }
        } catch (IllegalArgumentException e) {
            //  Log.i(TAG,"epicReciver is already unregistered");
            myReceiver = null;
        }
        //  Log.i("SMSactivityddsff",  "deleted");
    }
    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, @NonNull Intent arg1) {

            String registration_message_reply = arg1.getStringExtra("REGISTRATION");
            // Toast.makeText(Registration.this,  registration_message_reply.trim(), Toast.LENGTH_SHORT).show();
            Registration_Reply = registration_message_reply.trim();

        }

    }





}
