package com.patrick.easypanel;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.os.Build;
import com.patrick.easypanel.logic.FunctionItem;
import com.patrick.easypanel.logic.Toolkit;
import com.patrick.easypanel.receivers.EPDeviceAdminReceiver;
import com.patrick.easypanel.views.FlipWindow;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Toast;

import static com.patrick.easypanel.R.drawable.*;

public class FlipService extends Service {

    public static ArrayList<FunctionItem> currentItems;
    public static ArrayList<Bitmap> currentBitmaps;

	public static final String COMMAND = "command";
	public static final String ACT_Start = "start";
	public static final String ACT_Remove = "remove";
	public static final String ACT_ResetFunc = "resetfunc";
	public static final String ACT_BootCompleted = "afterboot";
	boolean isAdded;
	WindowManager m_wm;
	FlipWindow m_view;
	Context context;
	PackageManager m_pm;
	Handler handler;
	SharedPreferences m_shared;
	List<ResolveInfo> infos;
	public FlipService() {
		context = this;
		
	}
	private void tryAddView(Context context) {
		if (!isAdded)
		{
			m_view = new FlipWindow(getApplicationContext(),m_wm);
			m_wm.addView(m_view, m_view.getDefaultParams());
			m_wm.addView(m_view.getTouchView(), m_view.getTouchLayoutParams());
			setViewFunc();
			isAdded = true;
		}
	}
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
            if (intent == null)
                return;
			if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
				tryAddView(context);
			}
            else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())){
                if (isAdded)
                    m_view.updateTouchView();
            }
		}
	};
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
        int sysVer = Build.VERSION.SDK_INT; 
        if (sysVer > 14)
        {
            this.setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        }
		m_wm = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		m_pm = this.getPackageManager();
		m_shared = PreferenceManager.getDefaultSharedPreferences(this);
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);  
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER); 
        infos = m_pm.queryIntentActivities(mainIntent, 0);//PackageManager.0);

		isAdded = false;

        currentItems = Toolkit.getCurrentItems(context);
        refreshBitmap();

		final IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
	    registerReceiver(receiver, filter);


        Intent epIntent = new Intent(getApplicationContext(), EPSetting.class);
        epIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, epIntent, 0);
        Notification notification = new Notification(R.drawable.ic_launcher, "Easy Panel已启动", System.currentTimeMillis());
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notification.setLatestEventInfo(getApplicationContext(),"Easy Panel", "点击进入Easy Panel设置", pendingIntent);

        this.startForeground(1, notification);

	}

    public static void refreshBitmap() {
        currentBitmaps = new ArrayList<Bitmap>();
        for (FunctionItem item : currentItems)
        {
            currentBitmaps.add(item.getIcon());
        }
    }

    private void setViewFunc() {
		handler = new Handler();
		Toolkit.setHandler(handler);
		m_view.setOnSelectedListener(new FlipWindow.OnSelectedListener() {	
			@Override
			public void onSelect(final int which) {
				new Thread(){
                    public void run(){
                        try {
                            Thread.sleep(200);//延时，把主动权给绘图线程
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (which >= 0)
                            currentItems.get(which).start(context);
                    }
                }.start();
			}

		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null )
			return 0;
		String command = intent.getExtras().getString(COMMAND);
		if (command.equals(ACT_Start))
		{
			tryAddView(this); 
		}else if (command.equals(ACT_BootCompleted))
		{
			try {
				Thread.sleep(1000 * 10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tryAddView(this);
		}
		else if (command.equals(ACT_Remove))
		{
			tryRemoveView();    
		}else if (command.equals(ACT_ResetFunc))
		{
			setViewFunc();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	private void tryRemoveView() {
		if (isAdded)
		{
			m_wm.removeView(m_view.getTouchView());
			m_wm.removeView(m_view);
			isAdded = false;
		}
	}

	@Override
	public void onDestroy() {
		tryRemoveView();
		unregisterReceiver(receiver);
        receiver = null;
        System.gc();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
