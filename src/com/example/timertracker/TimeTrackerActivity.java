package com.example.timertracker;

import android.os.Bundle;
import android.os.IBinder;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class TimeTrackerActivity extends FragmentActivity implements OnClickListener, ServiceConnection {

	private TimeListAdapter mTimeListAdapter;
	private TimerService mTimerService = null;
	private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
            long time = intent.getLongExtra("time", 0);

            if (ACTION_TIME_UPDATE.equals(action)) {
                TextView counter = (TextView) TimeTrackerActivity.this.findViewById(R.id.counter);
                counter.setText(DateUtils.formatElapsedTime(time/1000));
            } else if (ACTION_TIMER_FINISHED.equals(action)) {
                if (mTimeListAdapter != null && time > 0)
                    mTimeListAdapter.add(time/1000);
            }
			
		}
	};

	private static final String TAG = "TimeTrackerActivity";
	public static final String ACTION_TIME_UPDATE = "ActionTimeUpdate";
	public static final String ACTION_TIMER_FINISHED = "ActionTimerFinished";

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
			
			long[] times = savedInstanceState.getLongArray("times");
			if (times != null) {
				for (int i = 0; i < times.length; i++) {
					mTimeListAdapter.add(times[i]);
				}
			}
		}

		Button startButton = (Button) findViewById(R.id.start_stop);
		startButton.setOnClickListener(this);
		Button resetButton = (Button) findViewById(R.id.reset);
		resetButton.setOnClickListener(this);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_TIME_UPDATE);
		intentFilter.addAction(ACTION_TIMER_FINISHED);
		registerReceiver(mTimeReceiver, intentFilter);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		ListView list = (ListView) findViewById(R.id.time_list);
		int position = list.getFirstVisiblePosition();
		outState.putInt("first_position", position);
		
		if (mTimeListAdapter != null) {
			int count = mTimeListAdapter.getCount();
			long[] times = new long[count];
			for (int i = 0; i < count; i++) {
				times[i] = mTimeListAdapter.getItem(i);
			}
			outState.putLongArray("times", times);
		}
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
		
		// Bind to the TimerService
        bindTimerService();
	}
	
	@Override
    protected void onDestroy() {
        if (mTimeReceiver != null)
            unregisterReceiver(mTimeReceiver);
        
        if (mTimerService != null) {
            unbindService(this);
            mTimerService = null;
        }
        super.onDestroy();
    }

	@Override
	public void onClick(View v) {
		Button ssButton = (Button) findViewById(R.id.start_stop);

		switch (v.getId()) {
		case R.id.start_stop:
			if (mTimerService == null) {
                ssButton.setText(R.string.stop);
                startService(new Intent(this, TimerService.class));
            } else if (!mTimerService.isTimerRunning()) {
                ssButton.setText(R.string.stop);
                mTimerService.startService(new Intent(this, TimerService.class));
            } else {
                ssButton.setText(R.string.start);
                mTimerService.stopTimer();
            }
			break;
		case R.id.reset:
			if (mTimerService != null) {
                mTimerService.resetTimer();
            }
			TextView counter = (TextView) findViewById(R.id.counter);
			counter.setText(DateUtils.formatElapsedTime(0));
			ssButton.setText(R.string.start);
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_all:
			FragmentManager fm = getFragmentManager();
			if (fm.findFragmentByTag("dialog") == null) {
				ConfirmClearDialogFragment frag = ConfirmClearDialogFragment.newInstance(mTimeListAdapter);
				frag.show(fm, "dialog");
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void bindTimerService() {
		// TODO : call context, application? difference?
        bindService(new Intent(this, TimerService.class), this, Context.BIND_AUTO_CREATE);
    }
	
	@Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "onServiceConnected");
        mTimerService = ((TimerService.LocalBinder)service).getService();
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "onServiceDisconnected");
        mTimerService = null;
    }
}