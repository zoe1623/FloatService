package com.zoe.floatservice.float_view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.zoe.floatservice.R;

/**
 * Created by Administrator on 2016/10/13.
 */
public class FloatView implements View.OnClickListener, Handler.Callback {

    private Context mContext;
    private static final int DELAY_ALPHA = 3000;//隐藏
    private static final int DELAY_CLOSE = 3000;//隐藏
    public static final int MSG_ALPHA = 1;//透明
    private static final int MSG_CLOSE = 2;//关闭
    private Handler mHandler;
    private static FloatView f = null;
    protected static FloatView getInstance(Context ctx){
        if(f == null) {
            f = new FloatView(ctx);
        }
        return f;
    }
    private FloatView(Context ctx){
        mContext = ctx;
        mHandler = new Handler(Looper.getMainLooper(), this);
        initUI();
    }
    private View mLeftView, mRightView, mTextView, mRoot, mRootView;
    private boolean mShowLeft = true, isOpen;
    private void initUI() {
        mRoot = View.inflate(mContext, R.layout.drag_menu, null);
        mLeftView = mRoot.findViewById(R.id.iv_drag_menu_left);
        mRootView = mRoot.findViewById(R.id.ll_drag_menu_root);
        mRightView = mRoot.findViewById(R.id.iv_drag_menu_right);
        mTextView = mRoot.findViewById(R.id.tv_drag_menu_text);

        mLeftView.setOnClickListener(this);
        mRightView.setOnClickListener(this);

        showLeft();
    }
    public void setOnTouchListener(View.OnTouchListener l){
        mLeftView.setOnTouchListener(l);
        mRightView.setOnTouchListener(l);
        mRootView.setOnTouchListener(l);
    }
    public View getViewFloat(){ return mRoot; }
    public boolean isShowLeft(){ return mShowLeft;}
    public boolean isOpen(){ return isOpen;}
    public void onTouchLogo(){
        mHandler.removeMessages(MSG_ALPHA);
        mRootView.setAlpha(1.0f);
    }
    public void onReleaseTouchLogo(){
        mHandler.sendEmptyMessageDelayed(MSG_ALPHA, DELAY_ALPHA);
    }
    public void showLeft(){
        mShowLeft = true;
        mLeftView.setBackgroundResource(R.drawable.menu_close);
        mRightView.setBackgroundResource(R.drawable.menu_confirm);
        mRightView.setVisibility(View.GONE);
        mLeftView.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mRootView.getLayoutParams();
        layoutParams.gravity = Gravity.LEFT;
        mRootView.setLayoutParams(layoutParams);
    }
    public void showRight(){
        mShowLeft = false;
        mRightView.setBackgroundResource(R.drawable.menu_close);
        mLeftView.setBackgroundResource(R.drawable.menu_confirm);
        mRightView.setVisibility(View.VISIBLE);
        mLeftView.setVisibility(View.GONE);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mRootView.getLayoutParams();
        layoutParams.gravity = Gravity.RIGHT;
        mRootView.setLayoutParams(layoutParams);
    }

    private void open(){
        onTouchLogo();
        isOpen = true;
        mTextView.setVisibility(View.VISIBLE);
        if(mShowLeft) {
            mRightView.setVisibility(View.VISIBLE);
        }else {
            mLeftView.setVisibility(View.VISIBLE);
        }
        mHandler.sendEmptyMessageDelayed(MSG_CLOSE, DELAY_CLOSE);
    }
    private void close(){
        isOpen = false;
        mTextView.setVisibility(View.GONE);
        if(mShowLeft){
            mRightView.setVisibility(View.GONE);
        }else {
            mLeftView.setVisibility(View.GONE);
        }
        mHandler.removeMessages(MSG_CLOSE);
        onReleaseTouchLogo();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.iv_drag_menu_left){
            clickLeft();
        }else if(id == R.id.iv_drag_menu_right){
            clickRight();
        }else {
            close();
        }
    }

    private void clickLeft(){
        if (mShowLeft){
            if(isOpen){
                close();
            }else {
                open();
            }
        }else {
            if(mListener != null){
                mListener.onConfirmClick();
            }
            close();
        }
    }

    private void clickRight(){
        if(mShowLeft){
            if(mListener != null){
                mListener.onConfirmClick();
            }
            close();
        }else {
            if (isOpen){
                close();
            }else {
                open();
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case MSG_ALPHA:
                mRootView.animate().alpha(0.5f).setDuration(500).start();
                break;
            case MSG_CLOSE:
                close();
                break;
        }
        return false;
    }

    private OnFloatViewListener mListener;
    public void setOnFloatViewListener(OnFloatViewListener l){
        mListener = l;
    }
    public interface OnFloatViewListener{
        void onConfirmClick();
    }
}
