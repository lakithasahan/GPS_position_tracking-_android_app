package com.ltronic.find;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

public class Callreceive_Broadcast extends BroadcastReceiver {
    @NonNull
    private String LAUNCHER_NUMBER = "1234567890";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        String phoneNubmer = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if (LAUNCHER_NUMBER.equals(phoneNubmer)) {
            setResultData(null);

         //   if (isLauncherIconVisible(context)) {

          //  } else {

                    PackageManager p =context.getPackageManager();
                    ComponentName componentName=new ComponentName(context,MainActivity.class);
                    // ComponentName componentName = new ComponentName(this, com.ltronic.find.MainActivity.class);
                    p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        //    }


        }

    }

  //  private boolean isLauncherIconVisible(Context context) {
  //      ComponentName componentName=new ComponentName(context,MainActivity.class);
  //      int enabledSetting = context.getPackageManager().getComponentEnabledSetting(componentName);
  //      return enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
  //  }

}
