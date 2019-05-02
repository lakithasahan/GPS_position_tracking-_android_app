package com.ltronic.find;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GPS_service extends Service implements LocationListener {


    private LocationListener listener;
    private LocationManager locationManager;
    private double lati;
    private double longi=0;
    private SmsManager smsManager = SmsManager.getDefault();
    private String mprovider;
    private int done=0;
    private Handler handle;
    @Nullable
    public String data_received=null;
    private int flag=0;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private int done_flag=0;

    @Override
    public void onCreate() {


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        mprovider = locationManager.getBestProvider(criteria, false);

      //  Toast.makeText(getBaseContext(), "gps service running", Toast.LENGTH_SHORT).show();
        handle=new Handler();
        handle.postDelayed(new Runnable() {

            @Override
            public void run() {


                    if (mprovider != null && !mprovider.equals("")) {
                        if (ActivityCompat.checkSelfPermission(GPS_service.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(GPS_service.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Location location = locationManager.getLastKnownLocation(mprovider);
                        locationManager.requestLocationUpdates(mprovider, 100, 1, GPS_service.this);

                        if (location != null&&done==0)
                            onLocationChanged(location);
                        else
                            Toast.makeText(getBaseContext(), "No Location Provider Found Check Your Code", Toast.LENGTH_SHORT).show();
                    }


                handle.postDelayed(this,1000);
            }
        }, 1000);



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handle.removeCallbacksAndMessages(null);
        flag=1;
        this.stopSelf();

    }




    @Override
    public void onLocationChanged(@NonNull Location location) {
        done=1;
        longi = location.getLongitude();
        lati = location.getLatitude();
        Double x = new Double(longi);
        Double y = new Double((lati));
        String data = y + "," + x;


        String file = "number.txt";

        File myFile = new File(getFilesDir(), "number.txt");
        if (myFile.exists()&&done_flag==0) {
            String number = readFromFile(GPS_service.this, file);
            if (number != null) {
                done_flag=1;
                //Toast.makeText(getBaseContext(), data, Toast.LENGTH_SHORT).show();
                smsManager.sendTextMessage(number, null, "Find#" + data, null, null);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
              smsManager.sendTextMessage(number, null, "Find app", null, null);
                myFile.delete();
                handle.removeCallbacksAndMessages(null);
                this.stopSelf();
                // GPS_service.this.stopSelf();
            }
        }
        Log.i("SMSactivityddsff",  "number check has satisfied");
        File emergency = new File(getFilesDir(), "emergencynumber.txt");
        if (emergency.exists()&&done_flag==0) {
            String number = readFromFile(GPS_service.this, "emergencynumber.txt");
            if (number != null) {
                done_flag=1;
                Toast.makeText(getBaseContext(), data, Toast.LENGTH_SHORT).show();
                smsManager.sendTextMessage(number, null, "Emergency#" + data, null, null);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
             smsManager.sendTextMessage(number, null, "Find app", null, null);
               emergency.delete();

                handle.removeCallbacksAndMessages(null);
                this.stopSelf();
                // GPS_service.this.stopSelf();
            }
        }
        done=0;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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
                Toast.makeText(this,ret,Toast.LENGTH_LONG).show();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


}
