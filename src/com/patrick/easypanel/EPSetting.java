package com.patrick.easypanel;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Environment;
import android.preference.*;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.patrick.easypanel.OpenDialog.OnFileSelectedListener;
import com.patrick.easypanel.logic.FunctionItem;
import com.patrick.easypanel.logic.Toolkit;
import com.patrick.easypanel.receivers.EPDeviceAdminReceiver;
import com.patrick.easypanel.views.FlipWindow;
import com.patrick.easypanel.views.*;


import android.os.Bundle;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class EPSetting extends PreferenceActivity {

	SharedPreferences m_shared;
	PackageManager pm ;
	List<ResolveInfo> infos;
	int index;
    ArrayList<CharSequence> names;

    Context m_context;
    Preference wallpaper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_context = this;
        setTheme(android.R.style.Theme_DeviceDefault_Light);
        PreferenceManager.setDefaultValues(this, R.xml.ep_settings, false);
        addPreferencesFromResource(R.xml.ep_settings);
        m_shared = PreferenceManager.getDefaultSharedPreferences(this);
        int statusBarHeight = Toolkit.getStatusHeight(this);
        m_shared.edit().putInt("statusBarHeight",statusBarHeight).commit();
        initPreferences();

        ArrayList<CharSequence> classes;
	}

    private void initPreferences() {
        initWallPreference();
        initInterfacePreference();
        initSensitivityPreference();
        initOthers();
        tryGetDeviceAdmin();

        Preference preference = this.findPreference(getResString(R.string.pref_function_settings));
        Intent intent = new Intent();
        intent.setClass(this, FunctionSetting.class);
        preference.setIntent(intent);


        initBackupPreferences();
    }

    private void initBackupPreferences() {
        final String confPath = Environment.getExternalStorageDirectory().getPath() + "/Android/Easy Panel/";
        final String sharedPrefPath = confPath + "pref.dat";
        new File(confPath).mkdirs();
        Preference backup = this.findPreference(getResString(R.string.pref_backup));
        Preference restore = this.findPreference(getResString(R.string.pref_restore));
        assert backup != null;
        backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog dialog = new AlertDialog.Builder(EPSetting.this)
                        .setTitle("备份设置").setMessage("备份设置可能会覆盖上一次的备份，确定备份吗？")
                        .create();
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        try {
                            File file = new File(confPath + Toolkit.curItemPath);
                            FileOutputStream fos = new FileOutputStream(file);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(FlipService.currentItems);
                            oos.flush();
                            oos.close();

                            file = new File(sharedPrefPath);
                            oos = new ObjectOutputStream(new FileOutputStream(file));
                            oos.writeObject(m_shared.getAll());
                            oos.flush();
                            oos.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return false;
            }
        });

        restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog dialog = new AlertDialog.Builder(EPSetting.this)
                        .setTitle("恢复设置").setMessage("恢复设置将覆盖当前的设置，确定恢复吗？")
                        .create();
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File file = new File(confPath + Toolkit.curItemPath);
                        if (!file.exists())
                        {
                            new AlertDialog.Builder(EPSetting.this)
                                    .setTitle("提示").setMessage("备份文件未找到，无法完成设置恢复").create().show();
                            return;
                        }
                        try {
                            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                            ArrayList<FunctionItem> items = (ArrayList<FunctionItem>) ois.readObject();
                            if (items != null){
                                FlipService.currentItems = items;
                                FlipService.refreshBitmap();
                            }


                            file = new File(sharedPrefPath);
                            ois = new ObjectInputStream(new FileInputStream(file));
                            Map<String, ?> prefs = (Map<String, ?>) ois.readObject();
                            if (prefs != null)
                            {
                                m_shared.edit().clear().commit();
                                for (Map.Entry<String ,?> entry : prefs.entrySet())
                                {
                                    Object value = entry.getValue();
                                    String key = entry.getKey();
                                    if (value instanceof Boolean){
                                        m_shared.edit().putBoolean(key, (Boolean) value).commit();
                                    }
                                    else if (value instanceof String){
                                        m_shared.edit().putString(key, (String) value).commit();
                                    }
                                    else if (value instanceof Integer){
                                        m_shared.edit().putInt(key, (Integer) value).commit();
                                    }
                                    else if (value instanceof Float){
                                        m_shared.edit().putFloat(key, (Float) value).commit();
                                    }

                                }
                                EPSetting.this.getPreferenceScreen().removeAll();
                                EPSetting.this.addPreferencesFromResource(R.xml.ep_settings);
                                initPreferences();
                                Intent it = new Intent();
                                it.setClass(m_context, FlipService.class);
                                it.putExtra(FlipService.COMMAND, FlipService.ACT_Remove);
                                startService(it);
                                it = new Intent();
                                it.setClass(m_context, FlipService.class);
                                it.putExtra(FlipService.COMMAND, FlipService.ACT_Start);
                                startService(it);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return false;
            }
        });
    }

    private void tryGetDeviceAdmin() {
		DevicePolicyManager dpmanager = (DevicePolicyManager) m_context.getSystemService(Context.DEVICE_POLICY_SERVICE);

		ComponentName name = new ComponentName(m_context, EPDeviceAdminReceiver.class);
		if (!dpmanager.isAdminActive(name))
		{
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
	        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, name);
	        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "点击激活后，重新打开程序即可");

	        startActivityForResult(intent, RESULT_OK);
	        this.finish();
	        //startActivity(intent);
		}
		else{
			startFlipService();
			resetFlipView();//刷新一下图标，主要用于读取默认快捷方式
		}
	}

	private void startFlipService() {
		Intent it = new Intent();
		it.setClass(getApplicationContext(), FlipService.class);
		it.putExtra(FlipService.COMMAND, FlipService.ACT_Start);
		startService(it);
	}

	private void initOthers() {
		Preference quit = this.findPreference(getResString(R.string.pref_quit));
		quit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent it = new Intent();
				it.setClass(m_context, FlipService.class);
				it.putExtra(FlipService.COMMAND, FlipService.ACT_Remove);
				stopService(it);
				finish();
                ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                am.killBackgroundProcesses(getPackageName());
                System.exit(0);
				return false;
			}
		});
		Preference about = this.findPreference(getResString(R.string.pref_about));
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog al = new AlertDialog.Builder(m_context).create();
				al.setTitle("关于Easy Panel");
				al.setMessage("Easy Panel v1.0 \n Author:姜东亚、邓建平 SSDUT");
				al.show();
				return false;
			}
		});
	}

	void resetFlipView()
	{
		Intent it = new Intent();
		it.setClass(this, FlipService.class);
		it.putExtra(FlipService.COMMAND, FlipService.ACT_ResetFunc);
		startService(it);
	}

	private void initWallPreference() {
		wallpaper = this.findPreference(getResString(R.string.pref_wallpaperpath));
		wallpaper.setSummary(m_shared.getString(wallpaper.getKey(), "未设置"));
		wallpaper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				OpenDialog dialog = new OpenDialog(m_context);
				//dialog.
				dialog.setOnFileSelected(new OnFileSelectedListener() {
					@Override
					public void onSelected(String path, String fileName) {
						String real = path.substring(5);
						wallpaper.setSummary(real);
						m_shared.edit().putString(wallpaper.getKey(), real).apply();
					}
				});
				dialog.Show();
				return false;
			}
		});
	}



    WindowManager wm;
    Point centerPoint;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(this.openFileOutput(Toolkit.curItemPath, MODE_PRIVATE));
            oos.writeObject(FlipService.currentItems);
            oos.flush();
            oos.close();
            oos = null;
            this.finish();
            for (int i = 0; i< 10 ;++i){
                System.gc();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initInterfacePreference(){
        wm = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        Preference vertPreference = this.findPreference(getResString(R.string.pref_interfacesettings_vertical));
        Preference horiPreference = this.findPreference(getResString(R.string.pref_interfacesettings_horizonal));
        assert vertPreference != null;
        Preference.OnPreferenceClickListener sizeListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                Intent intent = new Intent();
                intent.setClass(m_context, FlipService.class);
                intent.putExtra(FlipService.COMMAND, FlipService.ACT_Remove);
                EPSetting.this.startService(intent);//关掉悬浮窗

                final View view = new View(EPSetting.this) {
                    Paint paint = new Paint();

                    @Override
                    protected void onDraw(Canvas canvas) {
                        boolean isVertical = preference.getKey().equals(getResString(R.string.pref_interfacesettings_vertical));
                        float sizePer = m_shared.getFloat(getResString(isVertical
                                ? R.string.pref_size_per_vert
                                : R.string.pref_size_per_horiz), 1f);
                        float positon = m_shared.getFloat(getResString(isVertical
                                ? R.string.pref_positonY_per_vert
                                : R.string.pref_positonY_per_horiz), 0.5f);
                        centerPoint = Toolkit.getCenterPoint(EPSetting.this);
                        if (!isVertical)
                        {//横屏处理
                            centerPoint.x /=2.0;
                            centerPoint.y *=2.0;
                        }
                        //菜单大小,用比例计算
                        float innerCirR, midiumCirR, outterCirR;
                        if (isVertical) {//竖屏
                            midiumCirR = centerPoint.x / 3 * 1.2f;
                            centerPoint.y = (int) (centerPoint.y * 2 * positon);//半圈的位置
                        } else {
                            float width = centerPoint.x * 2 * 10 / 16;
                            midiumCirR = width / 3 * 1.2f;
                            centerPoint.x = (int) (centerPoint.x * 2 * positon);//半圈的位置
                            centerPoint.y -= Toolkit.getStatusHeight(EPSetting.this);
                            centerPoint.x -= Toolkit.getStatusHeight(EPSetting.this);
                        }
                        midiumCirR *= sizePer;//设置扇形大小
                        innerCirR = midiumCirR / 4;//内外圈的大小
                        outterCirR = innerCirR * 6f;

                        paint.setColor(getResources().getColor(R.color.cadetblue));
                        paint.setAntiAlias(true);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(3f);
                        canvas.drawCircle(centerPoint.x, centerPoint.y, midiumCirR, paint);
                        canvas.drawCircle(centerPoint.x, centerPoint.y, outterCirR, paint);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.GRAY & 0x77FFFFFF);
                        canvas.drawCircle(centerPoint.x, centerPoint.y, innerCirR, paint);
                    }
                };
                final boolean isVertical = preference.getKey().equals(getResString(R.string.pref_interfacesettings_vertical));

                //创建调整大小的Dialog
                AlertDialog dialog = new AlertDialog.Builder(EPSetting.this)
                        .setTitle("界面设置")
                        .create();//dialog初始化
                View content = View.inflate(EPSetting.this, R.layout.dialog_size_position, null);
                dialog.setView(content);//设置View
                //初始化两个SeekBar
                final SeekBar sbSize = (SeekBar) content.findViewById(R.id.sbSize);
                sbSize.setMax(120);
                float sizePer = m_shared.getFloat(getResString(isVertical
                        ? R.string.pref_size_per_vert
                        : R.string.pref_size_per_horiz), 1f);
                sbSize.setProgress((int) (sizePer * 100));
                sbSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        m_shared.edit().putFloat(getResString(isVertical
                                ? R.string.pref_size_per_vert
                                : R.string.pref_size_per_horiz), i / 100f).commit();
                        view.invalidate();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
                final SeekBar sbPos = (SeekBar) content.findViewById(R.id.sbPos);
                float positon = m_shared.getFloat(getResString(isVertical
                        ? R.string.pref_positonY_per_vert
                        : R.string.pref_positonY_per_horiz), 0.5f);
                sbPos.setProgress((int) (positon * 100));
                sbPos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        m_shared.edit().putFloat(getResString(isVertical
                                ? R.string.pref_positonY_per_vert
                                : R.string.pref_positonY_per_horiz), i / 100f).commit();
                        view.invalidate();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                //重置按钮
                Button btnReset = (Button) content.findViewById(R.id.btnReset);
                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sbSize.setProgress(100);
                        sbPos.setProgress(50);
                    }
                });
                //恢复悬浮窗
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        Intent intent = new Intent();
                        intent.setClass(m_context, FlipService.class);
                        intent.putExtra(FlipService.COMMAND, FlipService.ACT_Start);
                        EPSetting.this.startService(intent);
                        wm.removeView(view);
                    }
                });
                dialog.show();
                wm.addView(view, FlipWindow.getDefaultParams());
                return false;
            }
        };
        vertPreference.setOnPreferenceClickListener(sizeListener);
        horiPreference.setOnPreferenceClickListener(sizeListener);
    }

    private void initSensitivityPreference()
    {
        SeekBarPreference sensiPreference = (SeekBarPreference)this.findPreference(getResString(R.string.pref_sensitivity));
        int sensitivity = m_shared.getInt(getResString(R.string.pref_sensitivity), -1);
        if (sensitivity == -1)
        {
            m_shared.edit().putInt(getResString(R.string.pref_sensitivity), 100).commit();
        }
        assert sensiPreference != null;
        sensitivity = m_shared.getInt(getResString(R.string.pref_sensitivity), -1);
        sensiPreference.setSummaryText(String.valueOf(sensitivity));
        sensiPreference.setProgress(sensitivity);
    }
    private String getResString(int id)
    {
        return getResources().getString(id);
    }

}
