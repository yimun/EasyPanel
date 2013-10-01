package com.patrick.easypanel.logic;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;

import android.accessibilityservice.AccessibilityService;
import android.app.*;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.*;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.internal.util.*;
import com.patrick.easypanel.FlipService;
import com.patrick.easypanel.R;
import com.patrick.easypanel.receivers.EPDeviceAdminReceiver;

public class Toolkit {

	public Toolkit() {}
	public static Point getCenterPoint(Context context)
	{
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point outSize = new Point(wm.getDefaultDisplay().getWidth(),wm.getDefaultDisplay().getHeight()/2);
		return outSize;
	}

    public static void quickSms(Context context)
    {
        Uri uri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
	public static void lockScreen(Context context , Class<?> cls)
	{
		DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		//manager.is
		ComponentName name = new ComponentName(context, cls);
		if (manager.isAdminActive(name))
		{
			manager.lockNow();
		}
		else
		{
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN); 
	        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, name); 
	        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "一键锁屏"); 
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(intent); 
		}
	}
	
	public static void randomWallpaper(Context context, String wallDir)
	{
        if (wallDir.length() == 0)
        {
            postToast(context, "未设置壁纸路径");
            return;
        }
		WallpaperManager wall = WallpaperManager.getInstance(context);
		File dir = new File(wallDir.trim());
		Log.d("randomWallpaper", "dir: "+wallDir.trim());
		if (dir.isDirectory())
		{
			Log.d("randomWallpaper", "isDirectory true");
			ArrayList<String> lst = new ArrayList<String>();
			for (File f : dir.listFiles())
			{
				lst.add(f.getAbsolutePath());
			}
			Random r = new Random();
			r.setSeed(System.currentTimeMillis());
			String path = lst.get(r.nextInt(lst.size()));
			Log.d("path", path);
			Bitmap b = BitmapFactory.decodeFile(path);
			try {
				if (b!=null)
				{
					Log.d("set", "before");
					wall.setBitmap(b);
					Log.d("set", "after");
					b.recycle();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	public static void pressHome(Context context)
	{
		 Intent i= new Intent(Intent.ACTION_MAIN);
		 i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 i.addCategory(Intent.CATEGORY_HOME);
		 
		 context.startActivity(i);
	}
	
	public static void showRecentTask(final Context context)
	{
        context.sendBroadcast(new Intent("meizu.intent.double_home_key"));


	}

	
	/*
	 * 打开播放器
	 */
	public static void openPlayer(Context context) {
		try{
			Intent intent=new Intent();
			ComponentName cName=new ComponentName("com.android.music","com.android.music.MusicBrowserActivity");
			intent.setComponent(cName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}catch(Exception e)
		{
			try{
				Intent intent=new Intent();
				ComponentName cName=new ComponentName("com.android.music","com.android.music.fragment.ListBrowserActivity");
				intent.setComponent(cName);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}catch(Exception e2)
			{
				postToast(context, "音乐程序不兼容，你只能打开播放器后再用我控制了……");
			}
		}
	}
	/*
	 * 暂停播放
	 */
	public static void pauseMusic(Context context) {  
	    Intent intent = new Intent();  
	    intent.setAction("com.android.music.musicservicecommand.pause");  
	    intent.putExtra("command", "pause");  
	    context.sendBroadcast(intent);  
	} 
	/*
	 * 播放暂停音乐
	 */
	public static  void playMusic(Context context) {
		boolean running = false;
		ActivityManager a = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo r : a.getRunningServices(Integer.MAX_VALUE))
		{
			if (r.service.getClassName().equals("com.android.music.MediaPlaybackService"))
			{
				running = true;
				break;
			}
			Log.d("svcs", r.service.getClassName());
		}
		if (!running)
		{
			Intent intent=new Intent();
			ComponentName cName=new ComponentName("com.android.music","com.android.music.MediaPlaybackService");
			intent.setComponent(cName);
			context.startService(intent);
			
			try {
				Thread.sleep(400);
				Intent it = new Intent();  
				it.setAction("com.android.music.musicservicecommand.togglepause");  
				it.putExtra("command", "togglepause");  
				context.sendBroadcast(it); 
				context.sendBroadcast(it); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			openPlayer(context);
//			try {
//				Thread.sleep(1500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		else{
			Intent intent = new Intent();  
			intent.setAction("com.android.music.musicservicecommand.togglepause");  
			intent.putExtra("command", "togglepause");  
			context.sendBroadcast(intent); 
		}
	}
	/*
	 * 下一曲
	 */
	public static void nextMusic(Context context) {
		Intent intent = new Intent();  
		intent.setAction("com.android.music.musicservicecommand.next");  
		intent.putExtra("command", "next");  
		context.sendBroadcast(intent); 
	}
	/*
	 * 上一曲
	 */
	public static void prevMusic(Context context) {
		Intent intent = new Intent();  
		intent.setAction("com.android.music.musicservicecommand.previous");  
		intent.putExtra("command", "previous");  
		context.sendBroadcast(intent); 
	}
	
	@SuppressWarnings("unused")
	private boolean isNetWorkEnabled(Context context) {
		ConnectivityManager connMan=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo=connMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo.getState()==State.CONNECTED) {
			return true;
		} 
		return false;
	}
	/*
	 * 判断是否处于飞行模式
	 */
	private static boolean isAirplaneEnabled(Context context) {
		//处于飞行模式返回1
		int modeIndex=Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON,0);
		if(modeIndex==1){
			return true;
		}
		return false;
	}
	/*
	 * 设置飞行模式     status为true打开飞行模式，为false关闭飞行模式
	 */
	private static void setAirplaneMode(Context context,boolean status){
		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, status?1:0);    
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);    
         intent.putExtra("Sponsor", "Sodino");   
         intent.putExtra("state", status);    
         context.sendBroadcast(intent); 
         /*
 		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, status?1:0);  
         Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);  
         context.sendBroadcast(intent); 
         */
	}
	 /*
	  * WIFI开关
	  */
	public static void changeWIFIStatus(Context context) {
		if(isStillAirMode(context)){
			return;
		}	
		WifiManager wifiManager=(WifiManager)context.getSystemService(Service.WIFI_SERVICE);
		int status=wifiManager.getWifiState();
		if (status==wifiManager.WIFI_STATE_UNKNOWN) {
			postToast(context,"未知网卡状态");
		}
		else if (status==wifiManager.WIFI_STATE_DISABLED) {
			wifiManager.setWifiEnabled(true);
			postToast(context, "正打开Wifi，请稍等");
		}
		else {
			wifiManager.setWifiEnabled(false);
			postToast(context, "Wifi已断开");
		}
	}
	static Handler h = null;
	public static void setHandler(Handler handler)
	{
		h = handler;
	}
	static void postToast(final Context context,final String text){
		
		h.post(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
	/*
	 * 经过对话选择，判断是否仍处于飞行模式
	 */
	private static boolean isStillAirMode(final Context context) {
		if(isAirplaneEnabled(context))
		{
			new AlertDialog.Builder(context)
			.setTitle("正处于\'飞行模式\'")
			.setMessage("进入网设置关闭\'飞行模式\'?")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					setAirplaneMode(context, false);
				   // context.startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
				}
			})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			})
			.create()
			.show();
		}
		return isAirplaneEnabled(context);
	}
	
	public static void changeNetworkStatus(Context context) throws Exception {
		if(isStillAirMode(context)){
			return;
		}
		ConnectivityManager connMan=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		//connMan.s
		NetworkInfo networkInfo=connMan.getNetworkInfo(connMan.TYPE_MOBILE);
		Class<?> conClass=connMan.getClass();
		Class<?>[] argClasses=new Class[1];
		argClasses[0]=boolean.class;
		Method method=conClass.getMethod("setMobileDataEnabled", argClasses);
		if(networkInfo.getState()==State.UNKNOWN)
		{
			method.invoke(connMan, true);
			postToast(context, "连接中。。。");
		}
		else {
			//networkInfo
			if(networkInfo.getState()==State.CONNECTED)
			{
				method.invoke(connMan, false);
				postToast(context, "连接断开");
			}
			else if(networkInfo.getState()==State.DISCONNECTED) {
				method.invoke(connMan, true);
				postToast(context, "连接中。。。");
			}
		}
	}
	
	public static void toggleMute(Context context)
	{
		AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if (audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
		{
			audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			postToast(context,"震动模式");
		}
		else
		{
			audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			postToast(context,"正常模式");
		}
	}
    public static int getStatusHeight(Activity activity){
        int statusHeight = 0;
        Rect localRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight){
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = activity.getResources().getDimensionPixelSize(i5);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    public static void gotoBluetoothSetting(Context context)
    {
        Intent i = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }


    public static void gotoDisplaySetting(Context context)
    {
        Intent i = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void gotoWifiSetting(Context context)
    {
        Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void gotoSoundSetting(Context context)
    {
        Intent i = new Intent(Settings.ACTION_SOUND_SETTINGS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void gotoAccessibilitySetting(Context context)
    {
        Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void gotoNetworkSetting(Context context){
        Intent i = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void searchInBaidu(final Context context)
    {
        h.post(new Runnable() {
            @Override
            public void run() {
                final String prefix = "http://www.baidu.com/s?wd=";
                //context.setTheme(android.R.style.Theme_DeviceDefault_Light);
                final AlertDialog dialog = new AlertDialog.Builder(context).create();
                dialog.setTitle("请输入关键字");
                dialog.setMessage("将打开浏览器进行百度搜索");
                final EditText text = new EditText(context);
                //text.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                //text.setHeight(150);
                dialog.setView(text);

                dialog.setButton(DialogInterface.BUTTON_POSITIVE,"搜索",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String keyword = text.getText().toString();
                        String url = prefix + keyword;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        dialog.dismiss();
                        if (keyword.length() > 0)
                            context.startActivity(intent);
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
            }
        });
    }

    public static void killBackProcesses(final Context context){
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = context.getPackageManager();
        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        final long preAvalMemo = memoryInfo.availMem;
        for(ActivityManager.RunningTaskInfo info : am.getRunningTasks(Integer.MAX_VALUE))
        {
            if (info.baseActivity.getPackageName().equals(context.getPackageName()))
                continue;
            am.killBackgroundProcesses(info.baseActivity.getPackageName());
        }
        for(RunningServiceInfo info : am.getRunningServices(Integer.MAX_VALUE))
        {
            if (info.service.getPackageName().equals(context.getPackageName()))
                continue;
            am.killBackgroundProcesses(info.service.getPackageName());
        }
        for (RecentTaskInfo info :am.getRecentTasks(Integer.MAX_VALUE, 0))
        {
            try{
                if (info.origActivity.getPackageName().equals(context.getPackageName()))
                    continue;
                am.killBackgroundProcesses(info.origActivity.getPackageName());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()){
            try {
                ApplicationInfo applicationInfo = pm
                        .getApplicationInfo(info.processName, PackageManager.GET_META_DATA);
                if (applicationInfo.packageName.equals(context.getPackageName()))
                    continue;
                am.killBackgroundProcesses(applicationInfo.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                am.getMemoryInfo(memoryInfo);
                long diffMemo = memoryInfo.availMem - preAvalMemo;
                String diff= android.text.format.Formatter.formatFileSize(context, diffMemo > 0 ? diffMemo : 0);
                String nowMem = android.text.format.Formatter.formatFileSize(context, memoryInfo.availMem);
                postToast(context, "共清理内存" + diff +"，当前可用内存" + nowMem);
            }
        },1000);
    }
    private static Drawable getDrawable(Context context,int id)
    {
        return context.getResources().getDrawable(id);
    }


    public static ArrayList<FunctionItem> QuickAccessItems(Context context)
    {
        ArrayList<FunctionItem> _quickAccessItems = new ArrayList<FunctionItem>();
        _quickAccessItems.add(new FunctionItem("音乐控制：播放、暂停", getDrawable(context, R.drawable.play)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.playMusic(context);
            }
        });

        _quickAccessItems.add(new FunctionItem("音乐控制：上一曲", getDrawable(context, R.drawable.prev)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.prevMusic(context);
            }
        });

        _quickAccessItems.add(new FunctionItem("音乐控制：下一曲", getDrawable(context, R.drawable.next)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.nextMusic(context);
            }
        });

        _quickAccessItems.add(new FunctionItem("WiFi开关", getDrawable(context, R.drawable.wifi)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.changeWIFIStatus(context);
            }
        });

        _quickAccessItems.add(new FunctionItem("手机网络开关", getDrawable(context, R.drawable.network)) {
            @Override
            protected void invokeFunc(Context context) {
                try {
                    Toolkit.changeNetworkStatus(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        _quickAccessItems.add(new FunctionItem("切换铃声、震动模式", getDrawable(context, R.drawable.mute_concuss)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.toggleMute(context);
            }
        });

        _quickAccessItems.add(new FunctionItem("快速发短信", getDrawable(context, R.drawable.quicksms)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.quickSms(context);
            }
        });
        _quickAccessItems.add(new FunctionItem("随机更换壁纸", getDrawable(context, R.drawable.wallpaper)) {
            @Override
            protected void invokeFunc(Context context) {
                String wallDir = PreferenceManager.getDefaultSharedPreferences(context).getString("wallpaperpath", "");
                Toolkit.randomWallpaper(context, wallDir);
            }
        });

        _quickAccessItems.add(new FunctionItem("单击Home键", getDrawable(context, R.drawable.home)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.pressHome(context);
            }
        });

        _quickAccessItems.add(new FunctionItem("快速锁屏", getDrawable(context, R.drawable.lock)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.lockScreen(context, EPDeviceAdminReceiver.class);
            }
        });

        _quickAccessItems.add(new FunctionItem("双击Home键(魅族手机)", getDrawable(context, R.drawable.task)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.showRecentTask(context);
            }
        });

        _quickAccessItems.add(new FunctionItem("暂时关闭悬浮窗", getDrawable(context, R.drawable.disappear)) {
            @Override
            protected void invokeFunc(Context context) {
                Intent it = new Intent();
                it.setClass(context, FlipService.class);
                it.putExtra(FlipService.COMMAND, FlipService.ACT_Remove);
                context.startService(it);
                postToast(context, "悬浮窗将在下次解锁屏幕时恢复");
            }
        });

        _quickAccessItems.add(new FunctionItem("百度搜索", getDrawable(context, R.drawable.search)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.searchInBaidu(context);
            }
        });

        _quickAccessItems.add(new FunctionItem("清理内存", getDrawable(context, R.drawable.kill)) {
            @Override
            protected void invokeFunc(Context context) {
                killBackProcesses(context);
            }
        });
        return _quickAccessItems;
    }

    public static ArrayList<FunctionItem> SettingItems(Context context)
    {
        ArrayList<FunctionItem> _settingItems = new ArrayList<FunctionItem>();

        _settingItems.add(new FunctionItem("显示设置", getDrawable(context, R.drawable.set_display)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.gotoDisplaySetting(context);
            }
        });

        _settingItems.add(new FunctionItem("声音设置", getDrawable(context, R.drawable.set_mute)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.gotoSoundSetting(context);
            }
        });

        _settingItems.add(new FunctionItem("WiFi设置", getDrawable(context, R.drawable.set_wifi)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.gotoWifiSetting(context);
            }
        });

        _settingItems.add(new FunctionItem("网络设置", getDrawable(context, R.drawable.set_network)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.gotoNetworkSetting(context);
            }
        });

        _settingItems.add(new FunctionItem("蓝牙设置", getDrawable(context, R.drawable.set_bluetooth)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.gotoBluetoothSetting(context);
            }
        });

        _settingItems.add(new FunctionItem("辅助功能设置", getDrawable(context, R.drawable.set_accessibility)) {
            @Override
            protected void invokeFunc(Context context) {
                Toolkit.gotoAccessibilitySetting(context);
            }
        });
        return _settingItems;
    }


    public static ArrayList<FunctionItem> AllApplicationItems(Context context){
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(infos, new ResolveInfo.DisplayNameComparator(pm));

        ArrayList<FunctionItem> apps = new ArrayList<FunctionItem>();

        for (final ResolveInfo info : infos)
        {
            final String packageName = info.activityInfo.packageName;
            final String className = info.activityInfo.name;
            apps.add(new FunctionItem((String) info.loadLabel(pm), info.loadIcon(pm)) {
                @Override
                protected void invokeFunc(Context context) {
                    try{
                        Intent i = new Intent();
                        i.setComponent(new ComponentName(packageName,className));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                        i.setAction(Intent.ACTION_MAIN);
                        context.startActivity(i);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }

        return apps;
    }

    public static final String curItemPath = "epFunc.dat";
    public static ArrayList<FunctionItem> getCurrentItems(Context context){
        try {
            ObjectInputStream ois = new ObjectInputStream(context.openFileInput(curItemPath));
            ArrayList<FunctionItem> items = (ArrayList<FunctionItem>)ois.readObject();
            if (items == null)
                throw new Exception();
            return items;
        } catch (Exception e) {//程序首次运行
            e.printStackTrace();

            ArrayList<FunctionItem> defaultItems = new ArrayList<FunctionItem>();
            defaultItems.add(QuickAccessItems(context).get(0)); //播放、暂停
            defaultItems.add(QuickAccessItems(context).get(12)); //百度搜索
            defaultItems.add(QuickAccessItems(context).get(10)); //最近任务
            defaultItems.add(QuickAccessItems(context).get(5));  //静音震动
            defaultItems.add(QuickAccessItems(context).get(8));  //单击HOME

            defaultItems.add(QuickAccessItems(context).get(2));  //下一曲
            defaultItems.add(QuickAccessItems(context).get(1));  //上一曲
            defaultItems.add(QuickAccessItems(context).get(6));  //发短信
            defaultItems.add(new FunctionItem("Easy Panel", getDrawable(context, R.drawable.ic_launcher)) {
                @Override
                protected void invokeFunc(Context context) {
                    Intent i = new Intent();
                    i.setComponent(new ComponentName("com.patrick.easypanel","com.patrick.easypanel.EPSetting"));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    i.setAction(Intent.ACTION_MAIN);
                    context.startActivity(i);
                }
            });
            defaultItems.add(SettingItems(context).get(0));      //显示
            defaultItems.add(QuickAccessItems(context).get(7));  //随机换壁纸
            defaultItems.add(QuickAccessItems(context).get(3));  //Wifi
            defaultItems.add(QuickAccessItems(context).get(4));  //NetWork
            defaultItems.add(QuickAccessItems(context).get(11)); //Disappear
            defaultItems.add(QuickAccessItems(context).get(9));  //Lock

            return defaultItems;

        }
    }
};
