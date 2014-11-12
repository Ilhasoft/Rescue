package br.com.ilhasoft.rescue;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import br.com.ilhasoft.rescue.R;
import br.com.ilhasoft.rescue.anim.ArcAnim;
import br.com.ilhasoft.rescue.util.AndroidUtils;
import br.com.ilhasoft.rescue.widget.Arc;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BeginFragment extends Fragment implements SensorEventListener {
	
	private static final int AMBULANCIA_TIME = 58 * 1000;
	private static final int TIME = 600;
	
	private static final float MIN_AXIS_Y = 0.0f;
	private static final float MAX_AXIS_Y = 2.0f;
	private static final int COUNT_DEFAULT = -3;
	
	private static final int PLAY_SOUND = 0;
	
	private MediaPlayer mediaPlayer;
	private ScheduledExecutorService scheduleTaskExecutor;
	private AndroidUtils androidUtils = new AndroidUtils();
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	
	private CountDownTimer countDownTimer;
	
	private LinearLayout llCenterTitle;
	private LinearLayout llCenterAnimation;
	private ImageView ivBackgroundInnerBorder;
	private ImageView ivMao;
	private TextView tvQtdFinal;
	private Button btBackgroundCenter;
	private Button btHeart;
	private Button btCall;
	private TextView tvTime;
	private ImageView ivAmbulancia;
	private LinearLayout llAmbulancia;
	private Arc arcDeep;
	private GradientDrawable bgShape;
	private ImageView ivBoca;
	
	private FrameLayout flHeart;
	private FrameLayout flCall;
	
	private AnimatorSet scaleUp;
	private AnimatorSet scaleMaoUp;
	private AnimatorSet translationAnim;
	private ObjectAnimator ambulanciaTranslationX;
	
	private boolean started = false;
	private boolean tapStarted = false;
	private int count = COUNT_DEFAULT;
	
	private ObjectAnimator heartTranslationY;
	private ObjectAnimator ambulanciaTranslationY;
	private ObjectAnimator callTranslationY;
	private ObjectAnimator alphaDown;
	private ObjectAnimator scaleMaoUpX;
	private ObjectAnimator scaleMapUpY;
	private ObjectAnimator rotationBorder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		try {
			view = inflater.inflate(R.layout.fragment_begin, null);
			
			ivBoca = (ImageView) view.findViewById(R.id.ivBoca);
			tvQtdFinal = (TextView) view.findViewById(R.id.tvQtdFinal);
			ivMao = (ImageView) view.findViewById(R.id.ivMao);
			llCenterTitle = (LinearLayout) view.findViewById(R.id.llCenterTitle);
			llCenterAnimation = (LinearLayout) view.findViewById(R.id.llCenterAnimation);
			ivBackgroundInnerBorder = (ImageView) view.findViewById(R.id.ivBackgroundInnerBorder);
			tvTime = (TextView) view.findViewById(R.id.tvTime);
			ivAmbulancia = (ImageView) view.findViewById(R.id.ivAmbulancia);
			llAmbulancia = (LinearLayout) view.findViewById(R.id.llAmbulancia);
			btCall = (Button) view.findViewById(R.id.btCall);
			btHeart = (Button) view.findViewById(R.id.btHeart);
			
			flHeart = (FrameLayout) view.findViewById(R.id.flHeart);
			flCall = (FrameLayout) view.findViewById(R.id.flCall);
			
			btBackgroundCenter = (Button) view.findViewById(R.id.btBackgroundCenter);
			btBackgroundCenter.setOnClickListener(onStartClicked);
			bgShape = (GradientDrawable) btBackgroundCenter.getBackground();
			
			arcDeep = (Arc) view.findViewById(R.id.arcDeep);
			
			mediaPlayer = MediaPlayer.create(getActivity(), R.raw.beep);
			
			mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			countDownTimer = new CountDownTimer(AMBULANCIA_TIME, 1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					try {
						int segundosRestantes = (int) (millisUntilFinished / 1000);
						String tempoMinutos = "";
						String tempoSegundos = "";
						
						long minutos = TimeUnit.SECONDS.toMinutes(segundosRestantes) - (TimeUnit.SECONDS.toHours(segundosRestantes)* 60);
						long segundos = TimeUnit.SECONDS.toSeconds(segundosRestantes) - (TimeUnit.SECONDS.toMinutes(segundosRestantes) *60);
						
						if(minutos < 10) {
							tempoMinutos = "0" + minutos;
						} else {
							tempoMinutos = "" + minutos;
						}
						
						if(segundos < 10) {
							tempoSegundos = "0" + segundos;
						} else {
							tempoSegundos = "" + segundos;
						}
						
						tvTime.setText(getString(R.string.ambulancia_time, tempoMinutos, tempoSegundos));
					} catch(Exception exception){
						cancel();
					}
				}
				
				@Override
				public void onFinish() {
					try {
						
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			};
			
			btHeart.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent itNextActivity = new Intent(getActivity(), FingerActivity.class);
						startActivity(itNextActivity);
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}
			});
			
			btCall.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						countDownTimer.cancel();
						
						ambulanciaTranslationY.cancel();
						ambulanciaTranslationX.cancel();
						translationAnim.cancel();
						
						countDownTimer.start();
						ambulanciaTranslationX.start();
						translationAnim.start();
						
						started = true;
					} catch(Exception exception){
						exception.printStackTrace();
					}
				}
			});
			
			/*
			Transaction transactionTask = new Transaction() {
				@Override
				public void run() {
					PostServices postServices = new PostServices(getActivity());
					SensorData sensorData = postServices.getSensorData();
					System.out.println("Sensor Data: " + sensorData);
				}
			};
			
			TransactionTaskNoDialog task = new TransactionTaskNoDialog(transactionTask);
			task.execute();
			*/
			
			rotationBorder = ObjectAnimator.ofFloat(ivBackgroundInnerBorder, "rotation", 0f, 360f);
			rotationBorder.setRepeatCount(ObjectAnimator.INFINITE);
			rotationBorder.setRepeatMode(ObjectAnimator.INFINITE);
			rotationBorder.setInterpolator(new LinearInterpolator());
			rotationBorder.setDuration(TIME * 20);
			
			alphaDown = ObjectAnimator.ofFloat(ivBackgroundInnerBorder, "alpha", 1f, 0f);
			alphaDown.setRepeatCount(ObjectAnimator.INFINITE);
			alphaDown.setRepeatMode(ObjectAnimator.INFINITE);
			alphaDown.setDuration(TIME);
			
			ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(ivBackgroundInnerBorder, "scaleX", 1.1f);
			ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(ivBackgroundInnerBorder, "scaleY", 1.1f);
			scaleUpX.setRepeatCount(ObjectAnimator.INFINITE);
			scaleUpX.setRepeatMode(ObjectAnimator.INFINITE);
			scaleUpX.setDuration(TIME);
			scaleUpY.setRepeatCount(ObjectAnimator.INFINITE);
			scaleUpY.setRepeatMode(ObjectAnimator.INFINITE);
			scaleUpY.setDuration(TIME);
			
			scaleMaoUpX = ObjectAnimator.ofFloat(ivMao, "scaleX", 0.9f);
			scaleMapUpY = ObjectAnimator.ofFloat(ivMao, "scaleY", 0.9f);
			scaleMaoUpX.setRepeatCount(ObjectAnimator.INFINITE);
			scaleMaoUpX.setRepeatMode(ObjectAnimator.INFINITE);
			scaleMaoUpX.setDuration(TIME);
			
			scaleMapUpY.setRepeatCount(ObjectAnimator.INFINITE);
			scaleMapUpY.setRepeatMode(ObjectAnimator.INFINITE);
			scaleMapUpY.setDuration(TIME);
			
			heartTranslationY = ObjectAnimator.ofFloat(flHeart, "translationY", 1, androidUtils.convertDpToPixel(200, getActivity()));
			callTranslationY = ObjectAnimator.ofFloat(flCall, "translationY", 1, androidUtils.convertDpToPixel(200, getActivity()));
			callTranslationY.setStartDelay(120);
			
			ambulanciaTranslationY = ObjectAnimator.ofFloat(llAmbulancia, "translationY", androidUtils.convertDpToPixel(250, getActivity()), 0);
			ambulanciaTranslationX = ObjectAnimator.ofFloat(ivAmbulancia, "translationX", androidUtils.convertDpToPixel(40, getActivity()), androidUtils.convertDpToPixel(215, getActivity()));
			ambulanciaTranslationX.setDuration(AMBULANCIA_TIME);
			
			scaleUp = new AnimatorSet();
			scaleUp.play(scaleUpX).with(scaleUpY).with(alphaDown);
			
			scaleMaoUp = new AnimatorSet();
			scaleMaoUp.play(scaleMaoUpX).with(scaleMapUpY);
			
			translationAnim = new AnimatorSet();
			translationAnim.play(heartTranslationY).with(callTranslationY).before(ambulanciaTranslationY);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return view;
	}
	
	@Override
	public void onResume() {
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	private OnClickListener onStartClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				resetValues();
				startReanimation();
				
				countDownTimer.start();
				
				ambulanciaTranslationX.start();
				translationAnim.start();
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	};
	
	@Override
	public void onDestroyView() {
		try {
			if(scheduleTaskExecutor != null) {
				scheduleTaskExecutor.shutdownNow();
			}
			
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		super.onDestroyView();
	}
	
	private Handler soundHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			try {
	    		switch(msg.what) {
		    		case PLAY_SOUND:
		    			count++;
		    			mediaPlayer.start();
		    			
		    			if(!scaleUp.isRunning()) {
		    				scaleUp.start();
		    			}
		    			
		    			if(started) {
		    				if(!scaleMaoUp.isRunning()) {
		    					Log.i("TESTE-JOHN", "Scale Mao Up Started!");
		    					
		    					scaleMaoUp.setStartDelay(TIME);
		    					scaleMaoUp.start();
		    				}
		    			} else {
		    				if(scaleMaoUp.isRunning()) {
		    					scaleMaoUp.cancel();
		    				}
		    			}
		    			
		    			if(count >= 0) {
		    				tvQtdFinal.setText("" + count);
		    			}
		    			
		    			//Significa que finalizou
		    			if(count >= 30) {
		    				count = COUNT_DEFAULT;
		    				
		    				try {
		    					scheduleTaskExecutor.shutdownNow();
		    				} catch(Exception exception) {
		    					exception.printStackTrace();
		    				}
		    				
		    				//TROCAR IMAGEM E TODAS AS CORES
		    				ivBackgroundInnerBorder.setAlpha(1f);
		    				ivBackgroundInnerBorder.setBackgroundResource(R.drawable.elipse_azul);
		    				rotationBorder.start();
		    				
		    				//Faz a animação da bolinha
		    				ArcAnim arcAnimation = new ArcAnim(arcDeep, -1 * (float) (3.6 * -100));
		    				arcAnimation.setDuration(350);
		    				arcAnimation.setInterpolator(new LinearInterpolator());
		    				arcDeep.startAnimation(arcAnimation);
		    				
		    				
		    				ivMao.setVisibility(View.GONE);
		    				ivBoca.setVisibility(View.VISIBLE);
		    				
		    				tvQtdFinal.setText(R.string.tap_to_continue);
		    				tvQtdFinal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		    				
		    				arcDeep.setStrokeColor("#0072bc");
							bgShape.setColor(Color.parseColor("#4093e0"));
							
							btBackgroundCenter.setOnClickListener(onContinueClicked);
		    				
		    				scaleMaoUp.end();
		    				
		    				translationAnim.cancel();
		    				scaleMaoUp.cancel();
		    				scaleMaoUp.cancel();
		    				scaleUp.cancel();
		    			}
		    			
		    			break;
	    		}
	    	} catch(Exception ex){
	    		ex.printStackTrace();
	    	}
		}
			
	};
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		try {
			if(started){
				float y = event.values[1];
				
				float pctProgress = 0;
				if(y < -MAX_AXIS_Y) {
					pctProgress = -100;
				} else if(y <= MIN_AXIS_Y) {
					pctProgress = (100 / MAX_AXIS_Y) * y;
				}
				
				//Faz a animação da bolinha
				ArcAnim arcAnimation = new ArcAnim(arcDeep, -1 * (float) (3.6 * pctProgress));
				arcAnimation.setDuration(50);
				arcAnimation.setInterpolator(new LinearInterpolator());
				arcDeep.startAnimation(arcAnimation);
			}
		} catch(Exception exception){
			exception.printStackTrace();
		}
	}
	
	private OnClickListener onStopClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			stopReanimation();
		}
	};
	
	private OnClickListener onContinueClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			resetValues();
			startReanimation();
		}
	};
	
	public void stopReanimation() {
		try {
			resetValues();
			
			countDownTimer.cancel();
			
			ambulanciaTranslationY.cancel();
			ambulanciaTranslationX.cancel();
			translationAnim.cancel();
			scaleMaoUp.cancel();
			scaleUp.cancel();
			
			ambulanciaTranslationY.reverse();
			ambulanciaTranslationX.reverse();
			heartTranslationY.reverse();
			callTranslationY.reverse();
			
			ivBackgroundInnerBorder.setAlpha(1f);
			rotationBorder.cancel();
			arcDeep.setSweepAngle(0f);
			
			tvTime.setText("0");
			count = COUNT_DEFAULT;
			started = false;
			
			try {
				scheduleTaskExecutor.shutdownNow();
			} catch(Exception exception) {
				exception.printStackTrace();
			}
			
			llCenterTitle.setVisibility(View.VISIBLE);
			llCenterAnimation.setVisibility(View.GONE);
			btBackgroundCenter.setOnClickListener(onStartClicked);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
	
	private void resetValues() {
		try {
			ivBackgroundInnerBorder.setBackgroundResource(R.drawable.elipse);
			ivBackgroundInnerBorder.setAlpha(1f);
			rotationBorder.cancel();
			arcDeep.setSweepAngle(0f);
			
			ivMao.setVisibility(View.VISIBLE);
			ivBoca.setVisibility(View.GONE);
			
			tvQtdFinal.setText("0");
			tvQtdFinal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
			
			arcDeep.setStrokeColor("#f26c4f");
			bgShape.setColor(Color.parseColor("#db5436"));
			
			llCenterTitle.setVisibility(View.GONE);
			llCenterAnimation.setVisibility(View.VISIBLE);
			btBackgroundCenter.setOnClickListener(onStopClicked);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
	
	private void startReanimation() {
		try {
			started = true;
			
			scheduleTaskExecutor = new ScheduledThreadPoolExecutor(2);
			scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
				public void run() {
					soundHandler.sendEmptyMessage(PLAY_SOUND);
				}
			}, 0, TIME, TimeUnit.MILLISECONDS);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
	
}
