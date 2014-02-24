package com.example.timertracker;

import java.lang.ref.WeakReference;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

public class TimerService extends Service {

	private static final String TAG = "TimerService";
	public static final int TIMER_NOTIFICATION = 0;
	private NotificationManager mNM = null;
	private Notification mNotification;
	private long mStart = 0;
	private long mTime = 0;
	private TimeHandler mHandler = new TimeHandler(this);
	
	public class LocalBinder extends Binder {
		TimerService getService() {
			return TimerService.this;
		}
	}
	private final IBinder mBinder = new LocalBinder();
	
	private static class TimeHandler extends Handler {
        WeakReference<TimerService> mServiceRef;
        
        public TimeHandler(TimerService service) {
            mServiceRef = new WeakReference<TimerService>(service);
        }
        
        @Override
        public void handleMessage(Message msg) {
            TimerService service = mServiceRef.get();
            if (service != null) {
                long current = System.currentTimeMillis();
                service.mTime += current - service.mStart;
                service.mStart = current;
                
                service.updateTime(service.mTime);
                
                sendEmptyMessageDelayed(0, 250);
            }
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind: " + intent);
		return mBinder;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Show notification when we start the timer
		showNotification();

		mStart = System.currentTimeMillis();
		// Only a singe message type, 0
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessage(0);
		
		// Keep restarting until we stop the service
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		// Cancel the ongoing notification
		mNM.cancel(TIMER_NOTIFICATION);
		mHandler.removeMessages(0);
	}
	
	public void stopTimer() {
		mHandler.removeMessages(0);
		stopSelf();
		mNM.cancel(TIMER_NOTIFICATION);
	}
	
	public boolean isStopped() {
		return !mHandler.hasMessages(0);
	}
	
	public boolean isTimerRunning() {
        return mHandler.hasMessages(0);
    }
	
	public void resetTimer() {
		stopTimer();
		timerStopped(mTime);
		mTime = 0;
	}
	
	private void updateTime(long time) {
		Intent i = new Intent(TimeTrackerActivity.ACTION_TIME_UPDATE);
		i.putExtra("time", time);
		sendBroadcast(i);
		
		updateNotification(time);
	}
	
	private void timerStopped(long time) {
		Intent i = new Intent(TimeTrackerActivity.ACTION_TIMER_FINISHED);
		i.putExtra("time", time);
		sendBroadcast(i);
	}
	
	/**
     * Shows the timer notification
     */
    private void showNotification() {
    	NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
    	mNotification = builder.setSmallIcon(R.drawable.icon).setWhen(System.currentTimeMillis()).build();
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        
        // Use start foreground as user would notice if timer was stopped
        startForeground(TIMER_NOTIFICATION, mNotification);
    }
    
    /**
     * Update an existing notification.
     * 
     * @param time Time in milliseconds.
     */
	private void updateNotification(long time) {
		String title = getResources().getString(R.string.running_timer_notification_title);
		String message = DateUtils.formatElapsedTime(time/1000);
		Context context = getApplicationContext();
		Intent intent = new Intent(context, TimeTrackerActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		mNotification = builder.setSmallIcon(R.drawable.icon).setContentTitle(title).setContentText(message).setContentIntent(pendingIntent).build();
		mNM.notify(TIMER_NOTIFICATION, mNotification);
	}
}
