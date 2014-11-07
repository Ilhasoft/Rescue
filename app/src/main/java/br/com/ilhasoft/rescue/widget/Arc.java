package br.com.ilhasoft.rescue.widget;

import br.com.ilhasoft.rescue.util.AndroidUtils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class Arc extends View {

	private Paint paint;
	private RectF rectF;
	
	private String strokeColor;
	private float strokeWidth;
	
	private Float sweepAngle;
	private Float size;
	
	private AndroidUtils androidUtils;
	float init;
	private final Context context;
	
	public Arc(Context context, AttributeSet attr) {
		super(context, attr);
		this.context = context;
		
		androidUtils = new AndroidUtils();
		
		//size = 240f;
		size = 225f;//androidUtils.convertDpToPixel(150, context);
		
		strokeWidth = androidUtils.convertDpToPixel(10, context);
		init = androidUtils.convertDpToPixel(10, context);
		float sizeConvert = androidUtils.convertDpToPixel(size, context);
		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(strokeWidth);
		paint.setColor(Color.parseColor("#f26c4f"));
		
		sweepAngle = -0f;
		
		rectF = new RectF(init, init, sizeConvert, sizeConvert);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawArc(rectF, -90, sweepAngle, false, paint);
	}
	
	public Float getSize() {
		return size;
	}

	public void setSize(Float size) {
		float sizeConvert = androidUtils.convertDpToPixel(size, context);
		rectF = new RectF(init, init, sizeConvert, sizeConvert);
		this.size = size;
	}

	public String getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(String strokeColor) {
		paint.setColor(Color.parseColor(strokeColor));
		this.strokeColor = strokeColor;
	}
	
	public float getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(float strokeWidth) {
		paint.setStrokeWidth(strokeWidth);
		this.strokeWidth = strokeWidth;
	}

	public Float getSweepAngle() {
		return sweepAngle;
	}

	public void setSweepAngle(Float sweepAngle) {
		this.sweepAngle = sweepAngle;
	}
	
}
