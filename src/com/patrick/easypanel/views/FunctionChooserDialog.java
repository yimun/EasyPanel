package com.patrick.easypanel.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.patrick.easypanel.R;
import com.patrick.easypanel.logic.FunctionItem;

import java.util.ArrayList;

/**
 * Created by 东亚 on 13-6-3.
 */
public class FunctionChooserDialog extends AlertDialog {
	public interface OnChooseListener {
		public void onChoose(FunctionChooserDialog dialog,
				FunctionItem functionItem);
	}

	OnChooseListener listener;
	LayoutInflater inflater;
	ArrayList<FunctionItem> m_items;
	ListView listView;
	Context context;
	FunctionItemAdapter adapter;
	LinearLayout layout;

	public FunctionChooserDialog(Context context) {
		super(context);
		this.context = context;
		inflater = LayoutInflater.from(context);
		adapter = new FunctionItemAdapter();
		layout = new LinearLayout(context);
		ProgressBar progressBar = new ProgressBar(context);
		progressBar.setIndeterminate(true);
		layout.addView(progressBar, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		this.setView(layout);

	}

	public void notifyLoaded(ArrayList<FunctionItem> items) {
		m_items = items;
		listView = new ListView(context);
		listView.setAdapter(adapter);
		layout.removeAllViews();
		layout.addView(listView, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int i, long l) {
				if (listener != null) {
					listener.onChoose(FunctionChooserDialog.this,
							m_items.get(i));
				}
			}
		});

	}

	public void setOnChooseLister(OnChooseListener listener) {
		this.listener = listener;
	}

	class FunctionItemAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return m_items.size();
		}

		@Override
		public Object getItem(int i) {
			return m_items.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup parent) {
			FunctionHolder holder;
			FunctionItem functionItem = m_items.get(i);
			if (convertView == null) {
				holder = new FunctionHolder();
				convertView = inflater.inflate(R.layout.function_item, parent,
						false);
				holder.description = (TextView) convertView
						.findViewById(R.id.tvDescription);
				holder.head = (ImageView) convertView
						.findViewById(R.id.imgHead);
				convertView.setTag(holder);
			} else {
				holder = (FunctionHolder) convertView.getTag();
			}

			holder.description.setText(functionItem.description);
			holder.head.setImageBitmap(functionItem.getIcon());
			return convertView;
		}

		class FunctionHolder {
			TextView description;
			ImageView head;
		}
	}
}
