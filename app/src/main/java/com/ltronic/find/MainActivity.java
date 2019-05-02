package com.ltronic.find;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //private final ComponentName componentName=new ComponentName(MainActivity.this,MainActivity.class);
    private int hide_flag=0;

    private MapView mapView;
    private GoogleMap gmap;

    private static MainActivity inst;
    private ProgressDialog progressDialog;

    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyBOT_0ZAF_czg8vYhbslBo3UArhhOsVqC8";
   private int flag=0;

    private static final int RequestPermissionCode = 1;
    static MainActivity instance;
    private SmsManager smsManager = SmsManager.getDefault();
    private String[] separated;
    @Nullable
    private MyReceiver myReceiver;
    private String nameg;
    private int positiong;
    @Nullable
    public String pin_number=null;
    @NonNull
    public String[] file_name=new String[5];
    private String locate_name;
    Context context;
    @NonNull
    private String[] appPermissions={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS,Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.PROCESS_OUTGOING_CALLS,Manifest.permission.CALL_PHONE,Manifest.permission.READ_CONTACTS,Manifest.permission.READ_PHONE_STATE};
    @Nullable
    private ArrayAdapter adapter;
    private ListView listView;
    private TextToSpeech mTTS;

    public static MainActivity instance() {
        return inst;
    }

    boolean flag_per=false;


    @Override
    public void onStart() {
        super.onStart();

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SMScheck_service.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);


        mapView.onStart();

        inst = this;

    }




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        final ImageButton locate= findViewById(R.id.locate);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Alert");
        progressDialog.setMessage("Please wait");


       flag_per=Check_Permission();
       refresh_list();

        if(flag_per==true) {

            if (!isMyServiceRunning()) {
                Intent startIntent = new Intent(this, SMScheck_service.class);
                startIntent.setAction("start");
                startService(startIntent);
            }




            mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = mTTS.setLanguage(Locale.ENGLISH);

                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "Language not supported");
                        }
                    } else {
                        Log.e("TTS", "Initialization failed");
                    }
                }
            });




            final ImageButton background_activity_close= findViewById(R.id.imageButton);

            background_activity_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showInputDialog();

                }
            });

            final ImageButton Emergency= findViewById(R.id.emergency);

            Emergency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (locate_name != null){
                        speak("Are you sure you are in a Emergency?");

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Are you sure you are in a Emergency?")
                                .setCancelable(true)
                                .setPositiveButton("Yes,I am", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        String txt = readFromFile(MainActivity.this, locate_name + ".txt").trim();
                                        String array[] = txt.split(",");
                                        writeToFile(array[1],MainActivity.this);
                                        startService(new Intent(MainActivity.this, GPS_service.class));
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setData(Uri.parse("tel:"+array[1]));
                                        startActivity(callIntent);
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();



                    }else {
                        Toast.makeText(MainActivity.this,"Please select a Emergency contact member",Toast.LENGTH_SHORT).show();
                    }



                }
            });

            final Button hide= findViewById(R.id.hide);

            hide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide_flag=1;
                    showInputDialog();
                    speak("By typing your 4 digit pin.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    speak("App icon can be hidden from the home screen.");
                }
            });



            final Button privacy= findViewById(R.id.privacy);

            privacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, privacy_policy.class));

                }
            });



            listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    locate_name= (String) listView.getItemAtPosition(position);
                    gmap.clear();
                   // Toast.makeText(MainActivity.this,"You selected : " + item,Toast.LENGTH_SHORT).show();
                }

            });

            listView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
                                                    @Override
                                                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                                        String name= (String) listView.getItemAtPosition(position);
                                                        nameg=name;
                                                        positiong=position;
                                                        String path = getFilesDir().getAbsolutePath();
                                                        Toast.makeText(MainActivity.this,"You selected : " + name,Toast.LENGTH_SHORT).show();


                                                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                                        alertDialog.setTitle("Warning!!");
                                                        alertDialog.setMessage("You wish to delete this member ?");
                                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "YES",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        File myFile = new File(getFilesDir(),nameg+".txt");
                                                                        File image_file = new File(getFilesDir(),nameg+".png");
                                                                        if(myFile.exists())
                                                                            myFile.delete();
                                                                        if(image_file.exists())
                                                                            image_file.delete();
                                                                        adapter.remove(positiong);
                                                                        adapter.notifyDataSetChanged();
                                                                        refresh_list();
                                                                    }
                                                                });
                                                        alertDialog.show();



                                                        return true;
                                                    }
                                                } );





            locate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (locate_name != null&&flag==0){
                        flag=1;

                        String txt = readFromFile(MainActivity.this, locate_name + ".txt").trim();
                    String array[] = txt.split(",");

                    Toast.makeText(MainActivity.this, "Locating -" + array[0], Toast.LENGTH_LONG).show();

                    speak("Locating -" + array[0]);
                    smsManager.sendTextMessage(array[1], null, "W#Start", null, null);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                     smsManager.sendTextMessage(array[1], null, "Receiver stop", null, null);


                }else{
                        Toast.makeText(MainActivity.this,"Please select a member.", Toast.LENGTH_LONG).show();
                        speak("Please select a member.");
                    }
                    flag=0;
                }
            });

            final Button register= findViewById(R.id.Registration);

           register.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   File myFile1 = new File(getFilesDir(), "user.txt");
                   if (myFile1.exists()) {

                       startActivity(new Intent(MainActivity.this, Registration.class));
                   }else{
                       startActivity(new Intent(MainActivity.this, profile_registration.class));
                   }

               }
           });


          //  startService(new Intent(this, SMScheck_service.class));


        }
        else{
            Toast.makeText(MainActivity.this,"Permission Not Granted ! ",Toast.LENGTH_LONG).show();
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);


        final Handler handle=new Handler();
        handle.postDelayed(new Runnable() {

            @Override
            public void run() {
                if(Check_Permission()) {

                    try {
                        Map_Refresh(gmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                handle.postDelayed(this,2000);
            }
        }, 2000);

    }


    private void speak(String message) {

        mTTS.setPitch(1f);
        mTTS.setSpeechRate(1f);

        mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = promptView.findViewById(R.id.edittext);
        final String pin_number=editText.getText().toString().trim();
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        File myFile = new File(getFilesDir(),"pin.txt");
                        if(myFile.exists()) {
                            String pin = readFromFile(MainActivity.this, "pin.txt").trim();
                            final String pin_number=editText.getText().toString().trim();
                            speak("Please follow the shown instruction to get back icon.");
                            if (pin_number != null&&hide_flag==0) {

                                if (isMyServiceRunning() && (pin.equals(pin_number.trim()))) {
                                    Toast.makeText(MainActivity.this,"Find App Deactivated",Toast.LENGTH_LONG).show();
                                    Intent stopIntent = new Intent(MainActivity.this, SMScheck_service.class);
                                    stopIntent.setAction("stop");
                                    stopService(stopIntent);
                                    //myFile.delete();
                                }
                            }
                            else if(hide_flag==1){
                                if (isLauncherIconVisible()) {
                                    fn_hideicon();

                                }else {
                                    fn_unhide();

                                }
                                hide_flag=0;
                            }

                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(@NonNull DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private boolean isLauncherIconVisible() {
        ComponentName componentName=new ComponentName(MainActivity.this,MainActivity.class);
        int enabledSetting = getPackageManager().getComponentEnabledSetting(componentName);
        return enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }

    private void fn_hideicon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important!");
        builder.setMessage("To launch the app again, dial phone number 1234567890");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ComponentName componentName=new ComponentName(MainActivity.this,MainActivity.class);
                getPackageManager().setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    private void fn_unhide() {
        PackageManager p = getPackageManager();
        ComponentName componentName=new ComponentName(MainActivity.this,MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
    private void refresh_list(){
        adapter=null;
        List<String> file_name = new ArrayList<>();
        String path = getFilesDir().getAbsolutePath();
        final File file=new File(path);
        File[] list = file.listFiles();
        int count = 0;
        for (File f: list){
            String name = f.getName();

            if (name.endsWith(".txt")){
                name=name.replace(".txt","");
                if(!name.trim().equals("pin")) {
                    if(!name.trim().equals("user")) {
                        file_name.add(name);
                        // Toast.makeText(MainActivity.this,"Locating"+ file_name[count],Toast.LENGTH_SHORT).show();
                    }
                }
            }// file_name[count]="Register New Member";
// count++;

            if(count==5){
                count=0;
            }
            // System.out.println("170 " + count);
        }




        adapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_view, file_name);
        listView = findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
       // file_name=null;

    }





    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);

    }

    private void writeToFile(String data, Context context) {

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("emergencynumber.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }

    private boolean Check_Permission(){


        List<String>List_Permission_Needed=new ArrayList<>();
        for (String per:appPermissions){
            if(ActivityCompat.checkSelfPermission(this,per)!=PackageManager.PERMISSION_GRANTED){
                List_Permission_Needed.add(per);
            }

        }

        if(!List_Permission_Needed.isEmpty()){
            ActivityCompat.requestPermissions(this,List_Permission_Needed.toArray(new String[List_Permission_Needed.size()]),RequestPermissionCode);
            return false;
        }


        return true;
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



    @Override
    public void onMapReady(GoogleMap googleMap) {

        gmap = googleMap;
        gmap.setMinZoomPreference(12);

        gmap.setIndoorEnabled(true);
        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        LatLng ny = new LatLng(40.7143528, -74.0059731);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));

    }

    private void Map_Refresh(GoogleMap googleMap) throws FileNotFoundException {
        if(separated!=null) {
            gmap = googleMap;
            gmap.setIndoorEnabled(true);
            UiSettings uiSettings = gmap.getUiSettings();
            uiSettings.setIndoorLevelPickerEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
            uiSettings.setCompassEnabled(true);
            uiSettings.setZoomControlsEnabled(true);
            gmap.setMinZoomPreference(12);
            LatLng location = new LatLng(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]));


            File f=new File(getFilesDir().getAbsolutePath(), locate_name+".png");


            if(f.exists()) {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                Log.i("filestored_number", "file existss");

              //  gmap.addMarker(new MarkerOptions()
               //         .position(location).icon(BitmapDescriptorFactory.fromBitmap(b))
               //         .title(locate_name));


                gmap.addMarker(new MarkerOptions()
                        .position(location).icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(MainActivity.this,b,"Manish")))).setTitle(locate_name);


            }else {
                gmap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(locate_name));
            }


            gmap.moveCamera(CameraUpdateFactory.newLatLng(location));

        }else{
            gmap.clear();
        }
}

    private static Bitmap createCustomMarker(Context context, Bitmap resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        CircleImageView markerImage = marker.findViewById(R.id.user_dp);
        markerImage.setImageBitmap(resource);
        TextView txt_name = marker.findViewById(R.id.name);
        txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, @NonNull Intent arg1) {

            String cordinaters = arg1.getStringExtra("DATAPASSED");
         Toast.makeText(MainActivity.this, cordinaters.trim(), Toast.LENGTH_SHORT).show();
            cordinaters=cordinaters.replace(" ","");
            separated = cordinaters.trim().split(",");

        }

    }


    //foreground service detector
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SMScheck_service.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }







    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        try {
            if (myReceiver != null) {
                this.unregisterReceiver(myReceiver);
            }
        } catch (IllegalArgumentException e) {
            //  Log.i(TAG,"epicReciver is already unregistered");
            myReceiver = null;
        }
    }
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    protected void onResume() {
        super.onResume();

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SMScheck_service.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
        mapView.onResume();
        refresh_list();
    }

    public void onDestroy() {

        super.onDestroy();

        try {
            if (myReceiver != null) {
                this.unregisterReceiver(myReceiver);
            }
        } catch (IllegalArgumentException e) {
          //  Log.i(TAG,"epicReciver is already unregistered");
            myReceiver = null;
        }



        //startService(new Intent(this, SMScheck_service.class));

    }

    public void onStop() {


        super.onStop();

        try {
            if (myReceiver != null) {
                this.unregisterReceiver(myReceiver);
            }
        } catch (IllegalArgumentException e) {
            //  Log.i(TAG,"epicReciver is already unregistered");
            myReceiver = null;
        }
    }


}







