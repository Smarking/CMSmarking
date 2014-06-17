
package com.xlb.testautokill;

import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        PackageManager pm = getPackageManager();
        ComponentName autoKillComp= new ComponentName(getPackageName(),
                AutoKillService.class.getName());
        int enable = pm.getComponentEnabledSetting(autoKillComp);
        Log.d("AutoKillService", " onResume enable: " + enable);

    }
    @Override
    public void onClick(View v) {
        
        PackageManager pm = getPackageManager();
        ComponentName autoKillComp= new ComponentName(getPackageName(),
                AutoKillService.class.getName());
        int enable = pm.getComponentEnabledSetting(autoKillComp);
        Log.d("AutoKillService", " before enable: " + enable);
        if (enable == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            pm.setComponentEnabledSetting(autoKillComp, 
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(autoKillComp, 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
        enable = pm.getComponentEnabledSetting(autoKillComp);
        Log.d("AutoKillService", " able enable: " + enable);
//        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//            System.exit(0);
    }
}
