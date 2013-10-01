package com.patrick.easypanel.views;

import com.patrick.easypanel.R;
import com.patrick.easypanel.R.id;
import com.patrick.easypanel.R.layout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class IconListPreference extends ListPreference {

	ImageView m_iv;
	Drawable m_icon;
	public IconListPreference(Context context) {
		super(context);
		setWidgetLayoutResource(R.layout.app_icon);
	}

	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	    //if (m_iv == null)
	    m_iv = (ImageView) view.findViewById(R.id.iv);
	    m_iv.setBackgroundColor(Color.TRANSPARENT);
	    if (m_icon != null)
	    	m_iv.setImageDrawable(m_icon);
	}
	@Override
	public boolean isPersistent() {
		return false;
	}
	
	public void setWidgetIcon(Drawable icon)
	{
		m_icon = icon;
	}
	public IconListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		//setWidgetLayoutResource(R.layout.app_icon);
		// TODO Auto-generated constructor stub
	}

}
