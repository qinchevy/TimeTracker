package com.example.timertracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class TimeTrackerActivity extends Activity implements OnClickListener {

	private TimeListAdapter mTimeListAdapter;

	private long mStart;
	private long mTime;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			long current = System.currentTimeMillis();
			mTime += current - mStart;
			mStart = current;
			TextView counter = (TextView) TimeTrackerActivity.this
					.findViewById(R.id.counter);
			counter.setText(DateUtils.formatElapsedTime(mTime));
			mHandler.sendEmptyMessageDelayed(0, 250);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView counter = (TextView) findViewById(R.id.counter);
		counter.setText(DateUtils.formatElapsedTime(0));

		ListView list = (ListView) findViewById(R.id.time_list);

		if (mTimeListAdapter == null) {
			mTimeListAdapter = new TimeListAdapter(this, 0);
			list.setAdapter(mTimeListAdapter);
		}

		if (savedInstanceState != null) {
			int position = savedInstanceState.getInt("first_position", 0);
			list.setTop(position);
		}

		Button startButton = (Button) findViewById(R.id.start_stop);
		startButton.setOnClickListener(this);
		Button resetButton = (Button) findViewById(R.id.reset);
		resetButton.setOnClickListener(this);
	}

	private void startTimer() {
		// TODO
		// mHandler.removeMessages(0);
		mHandler.sendEmptyMessage(0);
	}

	private void stopTimer() {
		mHandler.removeMessages(0);
	}

	private void resetTimer() {
		stopTimer();
		if (mTimeListAdapter != null) {
			mTimeListAdapter.add(mTime / 1000);
		}
		mTime = 0;
	}

	private boolean isTimerRunning() {
		return mHandler.hasMessages(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ListView list = (ListView) findViewById(R.id.time_list);
		int position = list.getFirstVisiblePosition();
		outState.putInt("first_position", position);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		Button startButton = (Button) findViewById(R.id.start_stop);

		switch (v.getId()) {
		case R.id.start_stop:
			if (isTimerRunning()) {
				stopTimer();
				startButton.setText(R.string.stop);
			} else {
				startTimer();
				startButton.setText(R.string.start);
			}
			break;
		case R.id.reset:
			resetTimer();
			TextView counter = (TextView) findViewById(R.id.counter);
			counter.setText(DateUtils.formatElapsedTime(0));
			startButton.setText(R.string.start);
			break;
		default:
			break;
		}

	}

}
