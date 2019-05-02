package com.ltronic.find;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class profile_registration extends AppCompatActivity {
    private EditText name;
    private EditText number;
    private EditText pin;
    private int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_registration);

        name= findViewById(R.id.name);
        number= findViewById(R.id.phone_number);
        pin= findViewById(R.id.pin);
        File myFile = new File(getFilesDir(),"user.txt");
        if(myFile.exists())
          {flag=1;
           String profile_data= readFromFile(profile_registration.this);
           String[] data=profile_data.trim().split(",");
           name.setEnabled(false);
           number.setEnabled(false);
           pin.setEnabled(false);
           name.setText(data[0]);
           number.setText((data[1]));
        }else {
            flag=0;
        }
            final Button contact = findViewById(R.id.button);
            contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flag == 0) {

                        writeToFile1(pin.getText().toString().trim(), profile_registration.this);
                        String data = name.getText().toString().trim() + "," + number.getText().toString().trim();
                        writeToFile(data, profile_registration.this);
                        Toast.makeText(profile_registration.this, "Profile setup completed !", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(profile_registration.this, "Already profile has been set ", Toast.LENGTH_SHORT).show();
                        //finish();
                    }

                }
            });



    }





    private void writeToFile1(@Nullable String data, @NonNull Context context) {
        if(data!=null) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput( "pin.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }


    private void writeToFile(@Nullable String data, @NonNull Context context) {
        if(data!=null) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput( "user.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
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


}
