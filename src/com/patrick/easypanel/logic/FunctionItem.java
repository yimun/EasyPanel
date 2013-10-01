package com.patrick.easypanel.logic;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.patrick.easypanel.FlipService;
import com.patrick.easypanel.R;
import com.patrick.easypanel.receivers.EPDeviceAdminReceiver;

public abstract class FunctionItem implements Serializable{

	//public Drawable icon;
    private byte[] bitmapBytes;
	public String description;
	public int id;
	protected void invokeFunc(Context context){}
	public void start(Context context)
	{
		invokeFunc(context);
	}
	public FunctionItem(String description, Drawable icon) {
        this.description = description;
        ByteArrayOutputStream baops = new ByteArrayOutputStream();
        ((BitmapDrawable)icon).getBitmap().compress(Bitmap.CompressFormat.PNG, 0, baops);
        bitmapBytes = baops.toByteArray();
	}

    public Bitmap getIcon()
    {
        return BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
    }
}
