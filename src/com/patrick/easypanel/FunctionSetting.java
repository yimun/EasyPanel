package com.patrick.easypanel;

import android.*;
import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import com.patrick.easypanel.logic.FunctionItem;
import com.patrick.easypanel.logic.Toolkit;
import com.patrick.easypanel.views.FlipWindow;
import com.patrick.easypanel.views.FunctionChooserDialog;

import java.util.ArrayList;

/**
 * Created by 东亚 on 13-6-5.
 */
public class FunctionSetting extends Activity {
    private ArrayList<FunctionItem> _AllAppItems;

    private ArrayList<FunctionItem> AllAppItems()
    {
        if (_AllAppItems == null)
        {
            _AllAppItems = Toolkit.AllApplicationItems(FunctionSetting.this);
        }
        return _AllAppItems;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_DeviceDefault_Light_NoActionBar);
        super.onCreate(savedInstanceState);

    }

    FlipWindow view;
    LinearLayout layout;
    @Override
    protected void onResume() {
        super.onResume();
        layout = new LinearLayout(FunctionSetting.this);
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;

        view = new FlipWindow(FunctionSetting.this, true);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(view,params);

        this.setContentView(layout, linearParams);

        view.setOnSelectedListener(new FlipWindow.OnSelectedListener() {
            @Override
            public void onSelect(final int which) {
                if (which <0 )
                    return;
                final FunctionChooserDialog.OnChooseListener listener = new FunctionChooserDialog.OnChooseListener() {
                    @Override
                    public void onChoose(FunctionChooserDialog dialog, FunctionItem functionItem) {
                        FlipService.currentItems.remove(which);
                        FlipService.currentItems.add(which, functionItem);
                        FlipService.currentBitmaps.remove(which);
                        FlipService.currentBitmaps.add(which, functionItem.getIcon());
                        view.refresh();
                        dialog.dismiss();
                    }
                };
                AlertDialog chooseDialog = new AlertDialog.Builder(FunctionSetting.this)
                        .setTitle("选择功能")
                        .setItems(new String[]{"快捷功能", "系统设置", "常用程序"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int i) {

                                String hint = new String[]{"选择快捷方式","选择设置","选择常用程序"}[i];
                                final FunctionChooserDialog dialog = new FunctionChooserDialog(FunctionSetting.this);
                                dialog.setTitle(hint);
                                dialog.setOnChooseLister(listener);
                                dialog.show();
                                new Thread() {
                                    public void run() {

                                        ArrayList<FunctionItem> items = null;
                                        switch (i) {

                                            case 0:
                                                items = Toolkit.QuickAccessItems(getApplicationContext());
                                                break;
                                            case 1:
                                                items = Toolkit.SettingItems(getApplicationContext());
                                                break;
                                            case 2:
                                                items = AllAppItems();
                                                break;
                                            default:
                                                break;
                                        }
                                        final ArrayList<FunctionItem> finalItems = items;
                                        view.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.notifyLoaded(finalItems);
                                            }
                                        });
                                    }
                                }.start();
                            }
                        }).create();
                chooseDialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        _AllAppItems = null;
        layout.removeView(view);
        layout = null;

        view.setVisibility(View.GONE);
        view = null;
        this.finish();
        for (int i = 0; i< 10 ;++i){
            System.gc();
        }
        super.onDestroy();
    }
}
