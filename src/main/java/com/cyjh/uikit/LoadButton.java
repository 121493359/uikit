package com.cyjh.uikit;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by HuangJie on 2018/10/9.
 */

public class LoadButton extends View implements ObjectAnimator.AnimatorListener{

    enum State {
        INITIAL,// 初始状态
        FOLDING,// 正在伸缩
        LOADING, // 正在加载
        ERROR,// 加载失败
        SUCCESSED,// 加载成功
        PAUSED // 加载暂停
    }

    public interface LoadListenner {

        void onClick(boolean isSuccessed);

        void needLoading();
    }

    private int mRadiu;
    private int mDefaultRadius;
    private int mRectWidth;
    private int mHeight;
    private int mDefaultTextSize;
    private int mTextSize;
    private int mTextWidth;
    private int mLRPadding;
    private int mTBPaddind;
    private int mDefaultWidth;

    private String mText;

    private int mBackgroundColor;
    private int mProgressColor;
    private int mProgressSecondColor;
    private int mProgressWidth;
    private int mStrokeColor;

    private Drawable mSuccessDrawable;
    private Drawable mErrorDrawable;
    private Drawable mPauseDrawable;

    private Paint mTextPaint;
    private Paint mPaint;

    private Path mPath;
    private State mState;
    private boolean isUnfold = true;

    private LoadListenner mLoadListenner;
    private ObjectAnimator shrinkAnim;
    private ValueAnimator loadAnimator;

    private boolean progressReverse;
    private int mProgressStartAngel;
    private float circleSweep;

    private RectF progressRect;

    OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mState == State.FOLDING){
                return;
            }

            if (mState == State.INITIAL){
                if (isUnfold){
                    shringk();
                }
            }else if (mState == State.ERROR){
                if (mLoadListenner != null){
                    mLoadListenner.onClick(false);
                }
            }else if (mState == State.SUCCESSED){
                if (mLoadListenner != null){
                    mLoadListenner.onClick(true);
                }
            }else if (mState == State.PAUSED){
                if (mLoadListenner != null){
                    mLoadListenner.needLoading();
                    load();
                }
            }else if (mState == State.LOADING){
                mState = State.PAUSED;
                cancelAnimation();
                invaidateSelft();
            }
        }
    };

    private void cancelAnimation() {
        if ( shrinkAnim != null && shrinkAnim.isRunning() ) {
            shrinkAnim.removeAllListeners();
            shrinkAnim.cancel();
            shrinkAnim = null;
        }
        if ( loadAnimator != null && loadAnimator.isRunning() ) {
            loadAnimator.removeAllListeners();
            loadAnimator.cancel();
            loadAnimator = null;
        }
    }

    public void setCircleSweep(float circleSweep) {
        this.circleSweep = circleSweep;
        invaidateSelft();
    }

    private void invaidateSelft() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    private void load() {
        if (loadAnimator == null) {
            loadAnimator = ObjectAnimator.ofFloat(this,"circleSweep",0,360);
        }

        loadAnimator.setDuration(1000);
        loadAnimator.setRepeatMode(ValueAnimator.RESTART);
        loadAnimator.setRepeatCount(ValueAnimator.INFINITE);

        loadAnimator.removeAllListeners();

        loadAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
//                Log.d(TAG,"onAnimationRepeat:"+progressReverse);
                progressReverse = !progressReverse;
            }
        });
        loadAnimator.start();
        mState = State.LOADING;

    }

    private void shringk() {
        if (shrinkAnim == null) {
            shrinkAnim = ObjectAnimator.ofInt(this,"rectWidth", mRectWidth, 0);
        }
        shrinkAnim.addListener(this);

        shrinkAnim.setDuration(500);
        shrinkAnim.start();
        mState = State.FOLDING;
    }

    public LoadButton(Context context) {
        super(context);
    }

    public LoadButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        mTextPaint = new Paint();
        mState = State.INITIAL;
        setOnClickListener(mListener);
    }

    public LoadButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDefaultRadius = 10;
        mDefaultTextSize = 24;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadButton);
        mStrokeColor = typedArray.getColor(R.styleable.LoadButton_stroke_color, Color.RED);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.LoadButton_android_textSize, mDefaultTextSize);
        mText = typedArray.getString(R.styleable.LoadButton_android_text);
        mRadiu = typedArray.getDimensionPixelSize(R.styleable.LoadButton_radiu, mDefaultRadius);
        mLRPadding = typedArray.getDimensionPixelSize(R.styleable.LoadButton_contentPaddingLR, 10);
        mTBPaddind = typedArray.getDimensionPixelSize(R.styleable.LoadButton_contentPaddingTB, 10);

        mBackgroundColor = typedArray.getColor(R.styleable.LoadButton_backColor,Color.WHITE);
        mProgressColor = typedArray.getColor(R.styleable.LoadButton_progressColor,Color.WHITE);
        mProgressSecondColor = typedArray.getColor(R.styleable.LoadButton_progressSecondColor,Color.parseColor("#c3c3c3"));
        mProgressWidth = typedArray.getDimensionPixelOffset(R.styleable.LoadButton_progressWidth,2);

        mSuccessDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadSuccessDrawable);
        mErrorDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadErrorDrawable);
        mPauseDrawable = typedArray.getDrawable(R.styleable.LoadButton_loadPauseDrawable);
        typedArray.recycle();

        if (mSuccessDrawable == null) {
            mSuccessDrawable = context.getResources().getDrawable(R.drawable.yes);
        }
        if (mErrorDrawable == null) {
            mErrorDrawable = context.getResources().getDrawable(R.drawable.no);
        }
        if (mPauseDrawable == null) {
            mPauseDrawable = context.getResources().getDrawable(R.drawable.pause);
        }

        mDefaultWidth = 200;
        mRectWidth = mDefaultWidth - mDefaultRadius * 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int resultW = widthSize;
        int resultH = heightSize;

        int contentW = 0;
        int contentH = 0;

        if (widthMode == MeasureSpec.AT_MOST){
            mTextWidth = (int)mTextPaint.measureText(mText);
            contentW = mTextWidth + mLRPadding * 2 + mRadiu *2;
            resultW = contentW > widthSize? widthSize:contentW;
        }

        if (heightMode == MeasureSpec.AT_MOST){
            contentH = mTBPaddind*2 + mTextSize;
            resultH = contentH > heightSize? heightSize:contentH;
        }

        resultW = resultW < 2 * mRadiu ? 2 * mRadiu : resultW;
        resultH = resultH < 2 * mRadiu ? 2 * mRadiu : resultH;

        // 修整圆形的半径
        mRadiu = resultH / 2;
        // 记录中间矩形的宽度值
        mRectWidth = resultW - 2 * mRadiu;
        setMeasuredDimension(resultW,resultH);

    }

    public void setRectWidth (int width) {
        mRectWidth = width;
        invaidateSelft();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int circleR = mRadiu / 2;
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        drawPath(canvas, cx, cy);

        int textDescent = (int) mTextPaint.getFontMetrics().descent;
        int textAscent = (int) mTextPaint.getFontMetrics().ascent;
        int delta = Math.abs(textAscent) - textDescent;

        if ( mState == State.INITIAL) {
            canvas.drawText(mText,cx,cy + delta / 2,mTextPaint);
        }

        if ( mState == State.LOADING) {

            if ( progressRect == null ) {
                progressRect = new RectF();
            }
            progressRect.set(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mStrokeColor);
            mPaint.setColor(mProgressSecondColor);
            //先绘制背景圆
            canvas.drawCircle(cx,cy,circleR,mPaint);
            mPaint.setColor(mProgressColor);
//            Log.d(TAG,"onDraw() pro:"+progressReverse+" swpeep:"+circleSweep);
            if ( circleSweep != 360 ) {
                mProgressStartAngel = progressReverse ? 270 : (int) (270 + circleSweep);
                //绘制弧线

                canvas.drawArc(progressRect
                        ,mProgressStartAngel,progressReverse ? circleSweep : (int) (360 - circleSweep),
                        false,mPaint);
            }

            mPaint.setColor(mBackgroundColor);
        }

        if ( mState == State.ERROR) {
            mErrorDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mErrorDrawable.draw(canvas);
        } else if (mState == State.SUCCESSED) {
            mSuccessDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mSuccessDrawable.draw(canvas);
        } else if (mState == State.PAUSED) {
            mPauseDrawable.setBounds(cx - circleR,cy - circleR,cx + circleR,cy + circleR);
            mPauseDrawable.draw(canvas);
        }

    }

    private void drawPath(Canvas canvas, int cx, int cy) {
        mPaint = new Paint();

        if (mPath == null){
            mPath = new Path();
        }

        mPath.reset();
        int left = cx - mRectWidth/2 - mRadiu;
        int top = 0;
        int right = cx  + mRectWidth/2 + mRadiu;
        int bottom = getHeight();

        RectF leftRect = new RectF(left,top,left + mRadiu*2, bottom);
        RectF rightRect = new RectF(right- mRadiu *2,top, right,bottom);


        mPath.moveTo(cx - mRectWidth/2, bottom);
        mPath.arcTo(leftRect,90f,180f);
        mPath.lineTo(cx + mRectWidth /2, top);
        mPath.arcTo(rightRect, 270.0f,180f);
        mPath.close();

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(mPath, mPaint);
    }

    public void loadSuccessed() {
        mState = State.SUCCESSED;
        cancelAnimation();
        invaidateSelft();
    }

    public void loadFailed() {
        mState = State.ERROR;
        cancelAnimation();
        invaidateSelft();
    }

    public void reset(){
        mState = State.INITIAL;
        mRectWidth = getWidth() - mRadiu * 2;
        isUnfold = true;
        cancelAnimation();
        invaidateSelft();
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        isUnfold = false;
        load();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
