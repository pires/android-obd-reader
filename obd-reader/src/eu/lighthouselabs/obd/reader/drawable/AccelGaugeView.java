package eu.lighthouselabs.obd.reader.drawable;

import eu.lighthouselabs.obd.reader.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class AccelGaugeView extends GradientGaugeView {

	public final static int TEXT_SIZE = 15;
	public final static int range = 20;
	private double accel = 2;

	public AccelGaugeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		paint = new Paint();
		paint.setTextSize(TEXT_SIZE);
		Typeface bold = Typeface.defaultFromStyle(Typeface.BOLD);
		paint.setTypeface(bold);
		paint.setStrokeWidth(3);
		paint.setStyle(Paint.Style.FILL);
	}
	
	public void setAccel(double accel) {
		this.accel = accel;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Resources res = context.getResources();
		Drawable container = (Drawable) res.getDrawable(R.drawable.accel_gauge);
		int width = getWidth();
		int left = getLeft();
		int top = getTop();
		paint.setColor(Color.GREEN);
		canvas.drawText("Soft",left,top+TEXT_SIZE,paint);
		paint.setColor(Color.RED);
		canvas.drawText("Hard", left+width-TEXT_SIZE*3, top+TEXT_SIZE, paint);
		drawGradient(canvas, container, TEXT_SIZE+5, accel, range);
	}
}
