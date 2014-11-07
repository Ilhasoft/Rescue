package br.com.ilhasoft.rescue.anim;

import br.com.ilhasoft.rescue.widget.Arc;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ArcAnim extends Animation {
	
	Arc mArcView;
	float mStartAngle;
	float mSweepAngle;

	public ArcAnim(Arc arcView, float sweepAngle) {
		mStartAngle = arcView.getSweepAngle();
		mSweepAngle = sweepAngle;
		mArcView = arcView;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		float currAngle = mStartAngle + ((mSweepAngle - mStartAngle) * interpolatedTime);
		mArcView.setSweepAngle(currAngle);
		mArcView.requestLayout();
	}
}

