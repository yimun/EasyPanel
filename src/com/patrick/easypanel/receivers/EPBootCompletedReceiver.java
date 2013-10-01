package com.patrick.easypanel.receivers;

import com.patrick.easypanel.FlipService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class EPBootCompletedReceiver extends BroadcastReceiver {

	public EPBootCompletedReceiver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent == null )
			return;
		if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
			return;
		boolean autostart = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autostart", true);
		if (autostart)
		{
			Intent it = new Intent();
			it.setClass(context, FlipService.class);
			it.putExtra(FlipService.COMMAND, FlipService.ACT_BootCompleted);
			context.startService(it);
		}
		
	}

}
