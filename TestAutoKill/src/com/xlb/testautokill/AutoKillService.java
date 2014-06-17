package com.xlb.testautokill;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AutoKillService extends AccessibilityService {

    public static final String TAG = "AutoKillService";

    private static final String PAGE_CLASS_NAME = 
            "com.android.settings.applications.InstalledAppDetailsTop";

    private static final String PAGE_CLASS_NAME2 = "com.android.settings.SubSettings";

    private static final String ALERT_CLASS_NAME = "android.app.AlertDialog";

    private static final int STOP_BTN_INDEX = 2;

    private static final int BTN_STOP = 1;
    private static final int BTN_OK = 2;

    private boolean mIsStoped = false;

    private void returnActivity() {
        performGlobalAction(GLOBAL_ACTION_BACK);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log( "======= onAccessibilityEvent ======== event:" + event);
        Log( "className: " + event.getClassName());
        Log( " event type: " + event.getEventType());

        if (PAGE_CLASS_NAME2.equals(event.getClassName())) {
            if (performBtnClick(event, BTN_STOP)) {
                Log( " perform BTN_STOP  " );
                mIsStoped = true;
            } else {
//                returnActivity();
            }

        } else if (ALERT_CLASS_NAME.equals(event.getClassName())) {
            if (mIsStoped && performBtnClick(event, BTN_OK)) {
                mIsStoped = false;
                Log(" perform BTN OK  " );
                returnActivity();
            }
        }
    }

    private boolean performBtnClick(AccessibilityEvent event, int btn ) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return false;
        }
        AccessibilityNodeInfo node = getButtonNodeInfo(source, btn);
        if (node == null) {
            return false;
        }
        Log("btn text" + node.getText() + ", enabled:" + node.isEnabled());
        if (node.isEnabled() ) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            node.recycle();
            return true;
        } else {
            node.recycle();
            return false;
        }
    }

    // …Ó∂»’“button
    private AccessibilityNodeInfo getButtonNodeInfo(AccessibilityNodeInfo source, int btn) {
        AccessibilityNodeInfo current = source;
        int childNum = current.getChildCount();

        Log("childNum" + childNum );
        for ( int i = 0 ;i <childNum; i++ ) {
            AccessibilityNodeInfo child  = current.getChild(i);
            Log(" index: " + i + " child:" + child + ", child num:" + child.getChildCount() );
            if (child.getClassName().toString().endsWith("Button")) {
                if (btn == BTN_OK) {
                    if (i+1 < childNum) {
                        AccessibilityNodeInfo secondBtn  = current.getChild(i + 1);
                        if (secondBtn.getClassName().toString().endsWith("Button")) {
                            child.recycle();
                            return secondBtn;
                        }
                    }
                }
                return child;
            } else if (child.getChildCount() > STOP_BTN_INDEX) {
                return getButtonNodeInfo(child, btn);
            }

            // NOTE: Recycle the infos.
            child.recycle();
        }
        return null;
    }

    private void Log(String msg) {
        Log.d(TAG,msg);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log("onServiceConnected");
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        Log("onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {
        Log("onInterrupt");
    }

}
