package com.patrick.easypanel.views;

import android.preference.PreferenceManager;
import android.util.AttributeSet;
import com.patrick.easypanel.*;

import android.content.Context;
import android.preference.Preference;

import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

//setSummary会触发notifyChanged()，导致该Preference的View重绘
public class SeekBarPreference extends Preference{

	private SeekBar m_seekBar;
	private int m_speed;
	private Context m_context;
	private TextView m_summary;
    public int getSpeed() {
		return m_speed;
	}

	public SeekBarPreference(Context context) {
		super(context);
        m_context = context;
		this.setWidgetLayoutResource(R.layout.speed_seekbar);
	}
	public boolean isPersistent() {
		return false;
	}
    public void setProgress(int i){
        m_speed = i;
        if (m_seekBar != null)
        {
            m_seekBar.setProgress(i);
        }
    }
    public void setSummaryText(String s)
    {
        if (m_summary != null){
            m_summary.setText(s);
        }
    }
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
        m_summary = (TextView) view.findViewById(android.R.id.summary);
		m_seekBar = (SeekBar) view.findViewById(R.id.speedSeek);
		m_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (m_speed == i)
                    return;
                m_speed = i;
                m_summary.setText(String.valueOf(m_speed));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PreferenceManager.getDefaultSharedPreferences(m_context).edit()
                        .putInt(m_context.getString(R.string.pref_sensitivity),m_speed).commit();
            }
        });
        m_seekBar.setProgress(m_speed);
        m_summary.setText(String.valueOf(m_speed));

	}
    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
        this.setWidgetLayoutResource(R.layout.speed_seekbar);
    }
}
