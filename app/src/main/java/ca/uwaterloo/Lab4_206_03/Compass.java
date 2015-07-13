package ca.uwaterloo.Lab4_206_03;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Compass extends View {

    private float direction = 0;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean firstDraw;
    public String compassText = "";

    public Compass(Context context) {
        super(context);
// TODO Auto-generated constructor stub
        init();
    }

    public Compass(Context context, AttributeSet attrs) {
        super(context, attrs);
// TODO Auto-generated constructor stub
        init();
    }

    public Compass(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
// TODO Auto-generated constructor stub
        init();
    }

    private void init(){

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(30);

        firstDraw = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
// TODO Auto-generated method stub
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
// TODO Auto-generated method stub

        int cxCompass = getMeasuredWidth()/2;
        int cyCompass = getMeasuredHeight()/2;
        float radiusCompass;

        if(cxCompass > cyCompass){
            radiusCompass = (float) (cyCompass * 0.9);
        }
        else{
            radiusCompass = (float) (cxCompass * 0.9);
        }
        canvas.drawCircle(cxCompass, cyCompass, radiusCompass, paint);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);

        if(!firstDraw){

            canvas.drawLine(cxCompass, cyCompass,
                    (float)(cxCompass + radiusCompass * Math.sin((double)(direction) * 3.14/180)),
                    (float)(cyCompass - radiusCompass * Math.cos((double)(direction) * 3.14/180)),
                    paint);
            canvas.drawText(compassText, cxCompass, cyCompass-40, paint);
            canvas.drawText(String.valueOf(direction), cxCompass, cyCompass, paint);
        }

    }

    public void updateDirection(float dir)
    {
        firstDraw = false;
        direction = dir;
        invalidate();
    }

}