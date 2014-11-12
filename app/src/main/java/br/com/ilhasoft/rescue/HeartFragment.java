package br.com.ilhasoft.rescue;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import br.com.ilhasoft.rescue.R;
import br.com.ilhasoft.rescue.anim.ArcAnim;
import br.com.ilhasoft.rescue.util.AndroidUtils;
import br.com.ilhasoft.rescue.widget.Arc;

public class HeartFragment extends Fragment implements SensorEventListener {
	
	private static final float MIN_AXIS_Y = 0.0f;
	private static final float MAX_AXIS_Y = 2.0f;
	
	private static final int PLAY_SOUND = 0;
	
	private Button btStart;
	private Button btStop;
	private TextView tvX;
	private TextView tvY;
	private TextView tvZ;
	private Arc arcProgress;
	
	private MediaPlayer mediaPlayer;
	private ScheduledExecutorService scheduleTaskExecutor;
	
	private AndroidUtils androidUtils;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		try {
			view = inflater.inflate(R.layout.fragment_heart, null);
			
			androidUtils = new AndroidUtils();
			
			btStart = (Button) view.findViewById(R.id.btStart);
			btStop = (Button) view.findViewById(R.id.btStop);
			tvX = (TextView) view.findViewById(R.id.tvX);
			tvY = (TextView) view.findViewById(R.id.tvY);
			tvZ = (TextView) view.findViewById(R.id.tvZ);
			arcProgress = (Arc) view.findViewById(R.id.arcProgress);
			arcProgress.setStrokeColor("#77FF0000");
			arcProgress.setStrokeWidth(androidUtils.convertDpToPixel(15, getActivity()));
			
			btStart.setOnClickListener(onStartClickListener);
			btStop.setOnClickListener(onStopClickListener);
			
			mediaPlayer = MediaPlayer.create(getActivity(), R.raw.beep);
			
			mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		} catch(Exception exception){
			exception.printStackTrace();
		}
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	
	private OnClickListener onStartClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				scheduleTaskExecutor = new ScheduledThreadPoolExecutor(2);
				scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
					public void run() {
						System.out.println("Bip chamado!");
						soundHandler.sendEmptyMessage(PLAY_SOUND);
					}
				}, 0, 600, TimeUnit.MILLISECONDS);
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	};
	
	private OnClickListener onStopClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				if(!scheduleTaskExecutor.isShutdown() || !scheduleTaskExecutor.isTerminated()){
					scheduleTaskExecutor.shutdownNow();
				}
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	};
	
	@Override
	public void onDestroyView() {
		try {
			scheduleTaskExecutor.shutdownNow();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		super.onDestroyView();
	}
	
	private Handler soundHandler = new Handler(){
		@Override
		public void handleMessage(final Message msg) {
    		switch(msg.what){
    		case PLAY_SOUND:
    			mediaPlayer.start();
    			break;
    		}
    	}
	};

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		//tvX.setText("" + x);
		//tvY.setText("" + y + " < -2 ?" + (y<-2));
		//tvZ.setText("" + z);
		
		float pctProgress = 0;
		if(y < -MAX_AXIS_Y) {
			pctProgress = -100;
		} else if(y <= MIN_AXIS_Y) {
			pctProgress = (100 / MAX_AXIS_Y) * y;
		}
		
		//Faz a animaçãoo da bolinha
		ArcAnim arcAnimation = new ArcAnim(arcProgress, -1 * (float) (3.6 * pctProgress));
		arcAnimation.setDuration(50);
//		arcAnimation.setInterpolator(new AccelerateInterpolator());
		arcAnimation.setInterpolator(new LinearInterpolator());
		arcProgress.startAnimation(arcAnimation);
		
		/*
		final float alpha = 0.8f; 
		 
		float [] gravity = new float[3];
		float [] linear_acceleration = new float[3];

		// Isolate the force of gravity with the low-pass filter.
		gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
		gravity[1] = alpha * gravity[1] + (1 - alpha) * y;
		gravity[2] = alpha * gravity[2] + (1 - alpha) * z;

		// Remove the gravity contribution with the high-pass filter.
		linear_acceleration[0] = event.values[0] - gravity[0];
		linear_acceleration[1] = event.values[1] - gravity[1];
		linear_acceleration[2] = event.values[2] - gravity[2];*/
	}
	
}
