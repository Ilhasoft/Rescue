package br.com.ilhasoft.rescue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.ilhasoft.rescue.R;
import br.com.ilhasoft.rescue.db.RepositorioCommandScript;
import br.com.ilhasoft.rescue.finger.ImageProcessing;
import br.com.ilhasoft.rescue.model.Batimento;
import br.com.ilhasoft.rescue.util.AndroidUtils;
import br.com.ilhasoft.rescue.util.Transaction;
import br.com.ilhasoft.rescue.util.TransactionTaskNoDialog;
import br.com.ilhasoft.rescue.web.PostServices;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FingerFragment extends Fragment {
	
	private static List<Integer> beatsCount = new ArrayList<Integer>();
	
    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
//    private static View image = null;
    
    private static TextView text = null;
    private static ImageView ivCircle01;
    private static ImageView ivCircle02;
    private static ImageView ivCircle03;
    private static ImageView ivCircle04;
    private static FrameLayout flRepeat;
    private static FrameLayout flSend;
    private static View frontView;
    
    private LinearLayout llButton;
    private Button btRepeat;
    private Button btSend;
    private ImageView ivFrontCam;

    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];

    public static enum TYPE {
        GREEN, RED
    };

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }

    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;

    private static RepositorioCommandScript repositorioCommand;
    private static PostServices postServices;
    private AndroidUtils androidUtils;

	private static AnimatorSet translationAnim;
	private static AnimatorSet translationAnimReverse;
	private static AnimatorSet scaleAnim;
	
	private static boolean finished = false;
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		try {
			view = inflater.inflate(R.layout.fragment_finger, null);
			
			androidUtils = new AndroidUtils();
			postServices = new PostServices(FingerFragment.this.getActivity());
			repositorioCommand = RepositorioCommandScript.getInstance(FingerFragment.this.getActivity());
			
			beatsCount = new ArrayList<Integer>();
			beatsCount.clear();
			
			llButton = (LinearLayout) view.findViewById(R.id.llButton);
			btRepeat = (Button) view.findViewById(R.id.btRepeat);
			btSend = (Button) view.findViewById(R.id.btSend);
			ivCircle01 = (ImageView) view.findViewById(R.id.ivCircle01);
			ivCircle02 = (ImageView) view.findViewById(R.id.ivCircle02);
			ivCircle03 = (ImageView) view.findViewById(R.id.ivCircle03);
			ivCircle04 = (ImageView) view.findViewById(R.id.ivCircle04);
			ivFrontCam = (ImageView) view.findViewById(R.id.ivFrontCam);
			flRepeat = (FrameLayout) view.findViewById(R.id.flRepeat);
			flSend = (FrameLayout) view.findViewById(R.id.flSend);
			frontView = (View) view.findViewById(R.id.frontView);
			
			frontView.setVisibility(View.GONE);
			
			preview = (SurfaceView) view.findViewById(R.id.preview);
	        previewHolder = preview.getHolder();
	        previewHolder.addCallback(surfaceCallback);
	        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        
	        resetCircles();

//	        image = view.findViewById(R.id.image);
	        text = (TextView) view.findViewById(R.id.text);
	        
	        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(ivFrontCam, "scaleX", 1.1f);
			ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(ivFrontCam, "scaleY", 1.1f);
			scaleUpX.setRepeatCount(ObjectAnimator.REVERSE);
			scaleUpX.setRepeatMode(ObjectAnimator.REVERSE);
			scaleUpX.setDuration(200);
			scaleUpY.setRepeatCount(ObjectAnimator.REVERSE);
			scaleUpY.setRepeatMode(ObjectAnimator.REVERSE);
			scaleUpY.setDuration(200);
			
			scaleAnim = new AnimatorSet();
			scaleAnim.play(scaleUpX).with(scaleUpY);
			
	        ObjectAnimator repeatTranslationY = ObjectAnimator.ofFloat(flRepeat, "translationY", androidUtils.convertDpToPixel(200, getActivity()), 0);
	        ObjectAnimator sendTranslationY = ObjectAnimator.ofFloat(flSend, "translationY", androidUtils.convertDpToPixel(200, getActivity()), 0);
	        sendTranslationY.setStartDelay(120);
	        
	        translationAnim = new AnimatorSet();
			translationAnim.play(repeatTranslationY).with(sendTranslationY);
			
			ObjectAnimator repeatTranslationYReverse = ObjectAnimator.ofFloat(flRepeat, "translationY", 0, flRepeat.getTranslationY());
	        ObjectAnimator sendTranslationYReverse = ObjectAnimator.ofFloat(flSend, "translationY", 0, flSend.getTranslationY());
	        sendTranslationYReverse.setStartDelay(120);
	        
	        translationAnimReverse = new AnimatorSet();
			translationAnimReverse.play(repeatTranslationYReverse).with(sendTranslationYReverse);
			
			btSend.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						RepositorioCommandScript repositorioCommand = RepositorioCommandScript.getInstance(getActivity());
						final List<Batimento> batimentos = repositorioCommand.buscaBatimentos();
						
						View view = inflater.inflate(R.layout.dialog_email, null);
						final EditText etEmail = (EditText) view.findViewById(R.id.etEmail);
						
						AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
						alertBuilder.setView(view);
						alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									if(etEmail == null || etEmail.getText() == null || etEmail.getText().length() == 0 || !etEmail.getText().toString().contains("@")){
										return;
									}
									
									Transaction sendEmailTransaction = new Transaction() {
										@Override
										public void run() {
											try {
												postServices.enviaEmail(batimentos, etEmail.getText().toString());
											} catch(Exception exception) {
												exception.printStackTrace();
											}
										}
									};
									
									TransactionTaskNoDialog sendEmailTask = new TransactionTaskNoDialog(sendEmailTransaction);
									sendEmailTask.execute();
								} catch(Exception exception) {
									exception.printStackTrace();
								}
							}
						});
						
						alertBuilder.create().show();
						
//						System.out.println("Batimentos: " + batimentos);
					} catch(Exception exception){
						exception.printStackTrace();
					}
				}
			});
			
			btRepeat.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						text.setText(R.string.default_text);
						
						beatsCount.clear();
						finished = false;
						frontView.setVisibility(View.GONE);
						
						translationAnimReverse.start();
						
						resetCircles();
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				}
			});
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		
		return view;
	}
	
	@Override
	public void onResume() {
		camera = Camera.open();
        startTime = System.currentTimeMillis();
        
		super.onResume();
	}
	
	@Override
	public void onPause() {
		camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
        
		super.onPause();
	}
	
	private static PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
        	try {
	            if (data == null) throw new NullPointerException();
	            Camera.Size size = cam.getParameters().getPreviewSize();
	            if (size == null) throw new NullPointerException();
	
	            if (!processing.compareAndSet(false, true)) return;
	
	            int width = size.width;
	            int height = size.height;
	
	            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
	            // Log.i(TAG, "imgAvg="+imgAvg);
	            if (imgAvg == 0 || imgAvg == 255) {
	                processing.set(false);
	                return;
	            }
	
	            int averageArrayAvg = 0;
	            int averageArrayCnt = 0;
	            for (int i = 0; i < averageArray.length; i++) {
	                if (averageArray[i] > 0) {
	                    averageArrayAvg += averageArray[i];
	                    averageArrayCnt++;
	                }
	            }
	
	            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
	            TYPE newType = currentType;
	            if (imgAvg < rollingAverage) {
	                newType = TYPE.RED;
	                if (newType != currentType) {
	                    beats++;
	                    // Log.d(TAG, "BEAT!! beats="+beats);
	                }
	            } else if (imgAvg > rollingAverage) {
	                newType = TYPE.GREEN;
	            }
	
	            if (averageIndex == averageArraySize) averageIndex = 0;
	            averageArray[averageIndex] = imgAvg;
	            averageIndex++;
	
	            // Transitioned from one state to another to the same
	            if (newType != currentType) {
	                currentType = newType;
	//                image.postInvalidate();
	            }
	            
	            long endTime = System.currentTimeMillis();
	            double totalTimeInSecs = (endTime - startTime) / 1000d;
	            
	            if (totalTimeInSecs >= 2) {
	                double bps = (beats / totalTimeInSecs);
	                int dpm = (int) (bps * 60d);
	                if (dpm < 30 || dpm > 180) {
	                    startTime = System.currentTimeMillis();
	                    beats = 0;
	                    processing.set(false);
	                    return;
	                }
	                
	                // Log.d(TAG,
	                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);
	
	                if (beatsIndex == beatsArraySize) beatsIndex = 0;
	                beatsArray[beatsIndex] = dpm;
	                beatsIndex++;
	
	                int beatsArrayAvg = 0;
	                int beatsArrayCnt = 0;
	                for (int i = 0; i < beatsArray.length; i++) {
	                    if (beatsArray[i] > 0) {
	                        beatsArrayAvg += beatsArray[i];
	                        beatsArrayCnt++;
	                    }
	                }
	                final int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
	                
	                Transaction transaction = new Transaction() {
						@Override
						public void run() {
							Batimento batimento = new Batimento();
							batimento.setBpm(beatsAvg);
							
							postServices.enviaDados(batimento);
						}
					};
					
					TransactionTaskNoDialog transactionTask = new TransactionTaskNoDialog(transaction);
					transactionTask.execute();
	                
					beatsCount.add(beatsAvg);
					int count = beatsCount.size();
					
					if(count <= 4) {
						text.setText(String.valueOf(beatsAvg));
						scaleAnim.start();
					}
					
					ImageView ivCircle = null;
					switch(count){
					case 1:
						ivCircle = ivCircle01;
						break;
					case 2:
						ivCircle = ivCircle02;
						break;
					case 3:
						ivCircle = ivCircle03;
						break;
					case 4:
						ivCircle = ivCircle04;
						break;
					}
					
					if(ivCircle != null) {
						GradientDrawable bgShape = (GradientDrawable) ivCircle.getBackground();
						bgShape.setColor(Color.parseColor("#db5436"));
					}
					
					if(count == 4) {
						int total = 0;
						for(Integer beats : beatsCount){
							total += beats;
						}
						
						int beatsResult = (int)(total/beatsCount.size());
						text.setText(String.valueOf(beatsResult));
						
						Batimento batimento = new Batimento();
						batimento.setBpm(beatsResult);
						batimento.setDate(new Date());
						
						repositorioCommand.putBatimento(batimento);
						
						finished = true;
						frontView.setVisibility(View.VISIBLE);
						translationAnim.start();
					}
					
	                startTime = System.currentTimeMillis();
	                beats = 0;
	            }
	            processing.set(false);
        	} catch(Exception exception) {
        		exception.printStackTrace();
        	}
        }
    };

    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };
    
    private void resetCircles() {
    	GradientDrawable bgShape01 = (GradientDrawable) ivCircle01.getBackground();
		bgShape01.setColor(Color.parseColor("#99999999"));
		
		GradientDrawable bgShape02 = (GradientDrawable) ivCircle02.getBackground();
		bgShape02.setColor(Color.parseColor("#99999999"));
		
		GradientDrawable bgShape03 = (GradientDrawable) ivCircle03.getBackground();
		bgShape03.setColor(Color.parseColor("#99999999"));
		
		GradientDrawable bgShape04 = (GradientDrawable) ivCircle04.getBackground();
		bgShape04.setColor(Color.parseColor("#99999999"));
    }

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
	
}
