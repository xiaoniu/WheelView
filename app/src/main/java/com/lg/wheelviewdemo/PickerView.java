package com.lg.wheelviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 滚动选择器
 */
public class PickerView extends View {

    public static final String TAG = "PickerView";
    /**
     * text之间间距和minTextSize之比
     */
    public static final float MARGIN_ALPHA = 2.3f;
    /**
     * 自动回滚到中间的速度
     */
    public float speed = 5;

    private List<String> mDataList;
    /**
     * 选中的位置，这个位置是mDataList的中心位置，一直不变
     */
    private int mCurrentSelected = 0;
    private Paint mPaint;

    private float mMaxTextSize = 30;
    private float mMinTextSize = 15;

    private float mMaxTextAlpha = 255;
    private float mMinTextAlpha = 100;

    private int mViewHeight;
    private int mViewWidth;

    /**
     * 滑动的距离
     */
    private float mMoveLen = 0;
    private boolean isInit = false;
    private Timer timer;
    private MyTimerTask mTask;

    /**
     * 滚动动画的核心，以Speed
     */

    Handler updateHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (Math.abs(mMoveLen) < speed) {
                mMoveLen = 0;
                if (mTask != null) {
                    mTask.cancel();
                    mTask = null;
                }
            } else
                // 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
                mMoveLen = mMoveLen - mMoveLen / Math.abs(mMoveLen) * speed;
            invalidate();
        }

    };

    public PickerView(Context context) {
        super(context);
        init();
    }

    public PickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setData(List<String> datas) {
        mDataList = datas;
        invalidate();
    }

    public int getSelected() {
        return mCurrentSelected;
    }

    /**
     * 选择选中的item的index
     *
     * @param selected
     */
    public void setSelected(int selected) {
        mCurrentSelected = selected;
        invalidate();
    }

    /**
     * 选择选中的内容
     *
     * @param mSelectItem
     */
    public void setSelected(String mSelectItem) {
        for (int i = 0; i < mDataList.size(); i++)
            if (mDataList.get(i).equals(mSelectItem)) {
                setSelected(i);
                break;
            }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewHeight = getMeasuredHeight();
        mViewWidth = getMeasuredWidth();
        // 按照View的高度计算字体大小
        mMaxTextSize = mViewHeight / 4.0f;
        mMinTextSize = mMaxTextSize / 2f;
        isInit = true;
        invalidate();
    }

    private void init() {
        timer = new Timer();
        mDataList = new ArrayList<String>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.FILL);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mDataList.isEmpty()) {
            if (mCurrentSelected < 0) {
                mCurrentSelected = 0;
            } else if (mCurrentSelected >= mDataList.size()) {
                mCurrentSelected = mDataList.size() - 1;
            }
        }

        // 根据index绘制view
        if (isInit)
            drawData(canvas);
    }

    /**
     * 文字的绘制是以mMoveLen的大小为标准的，mMoveLen越大，文字字体越小，透明度越高。
     * @param canvas
     */

    private void drawData(Canvas canvas) {
        // 先绘制选中的text再往上往下绘制其余的text
        float scale = parabola(mViewHeight / 4.0f, mMoveLen);
        float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
        mPaint.setTextSize(size);
        mPaint.setColor(this.getResources().getColor(android.R.color.black));
        mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
        // text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
        float x = (float) (mViewWidth / 2.0);
        float y = (float) (mViewHeight / 2.0 + mMoveLen);
        FontMetricsInt fmi = mPaint.getFontMetricsInt();
        float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
        if (mCurrentSelected < mDataList.size()) {
            canvas.drawText(mDataList.get(mCurrentSelected), x, baseline, mPaint);
        } else {
            Log.e(TAG, "mCurrentSelected超出了mDataList.size()");
        }
        if (mCurrentSelected > 0 && mCurrentSelected < mDataList.size()) {
            // 绘制上方data
            for (int i = 1; (mCurrentSelected - i) >= 0; i++) {
                drawOtherText(canvas, i, -1);
            }
        }
        if (mCurrentSelected < mDataList.size()) {
            // 绘制下方data
            for (int i = 1; (mCurrentSelected + i) < mDataList.size(); i++) {
                drawOtherText(canvas, i, 1);
            }
        }
    }

    /**
     * @param canvas
     * @param position 距离mCurrentSelected的差值
     * @param type     1表示向下绘制，-1表示向上绘制
     */
    private void drawOtherText(Canvas canvas, int position, int type) {
        float d = (float) (MARGIN_ALPHA * mMinTextSize * position + type
                * mMoveLen);
        float scale = parabola(mViewHeight / 4.0f, d);
        float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
        mPaint.setTextSize(size);
        mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
        float y = (float) (mViewHeight / 2.0 + type * d);
        FontMetricsInt fmi = mPaint.getFontMetricsInt();
        float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
        canvas.drawText(mDataList.get(mCurrentSelected + type * position),
                (float) (mViewWidth / 2.0), baseline, mPaint);
    }

    /**
     * 抛物线
     *
     * @param zero 零点坐标
     * @param x    偏移量
     * @return scale
     */
    private float parabola(float zero, float x) {
        float f = (float) (1 - Math.pow(x / zero, 2));
        return f < 0 ? 0 : f;
    }

    public void next() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mMoveLen = MARGIN_ALPHA * mMinTextSize;
        speed = 5;
        if (mCurrentSelected < mDataList.size() - 1) {
            mCurrentSelected++;
            mTask = new MyTimerTask(updateHandler);
            timer.schedule(mTask, 0, 10);
        }
    }

    public void moveToHead() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        if (mCurrentSelected != 0) {
            mMoveLen = -mCurrentSelected * MARGIN_ALPHA * mMinTextSize;
            speed = mCurrentSelected * 5;
            mCurrentSelected = 0;
            mTask = new MyTimerTask(updateHandler);
            timer.schedule(mTask, 0, 10);
        }
    }

    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }

    }
}
