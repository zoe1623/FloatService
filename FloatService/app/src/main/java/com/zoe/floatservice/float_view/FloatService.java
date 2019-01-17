package com.zoe.floatservice.float_view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class FloatService implements View.OnTouchListener {
    private Context mContext = null;
    private Activity mActivity = null;
    private View mViewFloat = null;
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mParams = null;
    private int x = 100, y=100;

    private static final int LIE_LEFT = 0;
    private static final int LIE_TOP = 1;
    private static final int LIE_RIGHT = 2;
    private static final int LIE_BOTTOM = 3;
    private int lie = LIE_LEFT;
    private static final String SHOW = "show";
    private static final String LIE = "lie";
    private static final String LIE_X = "lie_x";
    private static final String LIE_Y = "lie_y";

    private static final int CLOSING = 100;//0.1秒

    private static FloatService f = null;

    public static FloatService newInstance(Activity activity){
        if(f != null) f.onDestroy();
        f = new FloatService(activity);
        return f;
    }
    public static FloatService getInstance(Activity activity){
        if(f == null) f = new FloatService(activity);
        return f;
    }
    private FloatView mFloat;
    private FloatService(Context ctx){
        mContext = ctx.getApplicationContext();
        onCreate();
    }
    public void onCreate(){
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams(600,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);

        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        mParams.gravity = Gravity.RIGHT|Gravity.TOP;
//        mParams.gravity = Gravity.LEFT|Gravity.TOP;
        mParams.x = x;
        mParams.y = y;
        // 浮标
        mFloat = FloatView.getInstance(mContext);
        mViewFloat = mFloat.getViewFloat();
        mFloat.setOnTouchListener(this);
        mFloat.showRight();
    }

    public void onDestroy(){
        if(!isLogoHide){
            hide();
        }
//        mFloatView.onDestroy();
        mWindowManager=null;
    }
    boolean isLogoHide = false;
    public void hide(){
        isLogoHide = true;
        if(mWindowManager!=null) {
            try {
                mWindowManager.removeView(mViewFloat);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void saveLocation(){
        SPUtil.put(mContext, LIE, lie);
        SPUtil.put(mContext, LIE_X, x);
        SPUtil.put(mContext, LIE_Y, y);
    }
    public void show(){
        refreshView();
        clickable = true;
    }

    private void refreshView() {
        refreshView(x,y);
    }
    /** 根据x,y的值,刷新浮标的位置 */
    private void refreshView(int x, int y) {
        Log.e(TAG, "refreshView: " + x + "   y: " + y);
        mParams.x = x;
        mParams.y = y;
        // 更新指定View的位置
        try {
            mWindowManager.updateViewLayout(mViewFloat, mParams);
        }catch (Exception e){
            try {
                mWindowManager.addView(mViewFloat, mParams);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }


    public void initLocation(int x, int y){

    }

    private float tmpX, tmpY;
    private int imageHalfWidth = 50, imageWidth = 100, screenWidth = 720, screenHeight = 1280, centerWidth = 360;
    private boolean clickable = true;
    private String TAG = "FloatService";
    int i = 10;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mFloat.onTouchLogo();
        if(mFloat.isOpen()){
            return false;
        }
       switch (event.getAction()) {
           case MotionEvent.ACTION_DOWN:
               tmpX = event.getRawX();
               tmpY = event.getRawY();
               v.setFocusable(true);
               clickable = true;
               break;
           case MotionEvent.ACTION_MOVE:
               int x = (int) event.getRawX();
               int y = (int) event.getRawY();
               if(!clickable){
                   if(mFloat.isShowLeft()){
                       refreshView(x - imageHalfWidth, y - imageHalfWidth);
                   }else {
                       refreshView(screenWidth - x - imageHalfWidth, y - imageHalfWidth);
                   }
                   break;
               }
               if(Math.abs(x - tmpX) > 20 || Math.abs(y - tmpY) > 20) {
                   clickable = false;
                   if(mFloat.isShowLeft()){
                       mParams.gravity =  Gravity.START | Gravity.TOP;//基准点
                       refreshView(x - imageHalfWidth, y - imageHalfWidth);
                   }else {
                       mParams.gravity =  Gravity.END | Gravity.TOP;//基准点
                       refreshView(screenWidth - x - imageHalfWidth, y - imageHalfWidth);
                   }
               }else {
                   clickable = true;
               }
               break;
           case MotionEvent.ACTION_UP:
               int xOff = (int) event.getRawX();
               int yOff = (int) event.getRawY();
               if(clickable && Math.abs(yOff - this.y) < imageWidth){
                   if(mFloat.isShowLeft()){
                       mFloat.onClick(v);
                   }else {
                       mFloat.onClick(v);
                   }
                   return true;
               }
               clickable = true;
               mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                       | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                       | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
               location(xOff, yOff);
               v.setFocusable(false);
               mFloat.onReleaseTouchLogo();
               break;
           case MotionEvent.ACTION_CANCEL:
               v.setPressed(false);
               v.setFocusable(false);
               mFloat.onReleaseTouchLogo();
               break;
           default:
               break;
       }
       return true;
    }
    /** 确定手抬起时,  xy的位置 */
    private void location(int x, int y){
        if (x < centerWidth) { // 贴左
            showLeft();
            x = 0;
            lie = LIE_RIGHT;
        } else { //
            showRight();
            x = 0;
            lie = LIE_LEFT;
        }
        y -= imageHalfWidth;
        this.x = x;
        this.y = y;
        limitBorder();
        saveLocation();
        refreshView();
    }
    /** 限定xy的边界 */
    private void limitBorder(){
        if(x < 0) x = 0;
        if(y < 0) y = 0;
        if(x > screenWidth - imageWidth) x = screenWidth - imageWidth;
        if(y > screenHeight - imageWidth) y = screenHeight - imageWidth;
    }
    private boolean lastIsShowRight = false;
    private void showRight() {
        mParams.gravity = Gravity.END | Gravity.TOP;//基准点
        if(!lastIsShowRight) {
            mFloat.showRight();
            lastIsShowRight = true;
        }
    }
    private void showLeft() {
        mParams.gravity = Gravity.START | Gravity.TOP;//基准点
        if(lastIsShowRight) {
            mFloat.showLeft();
            lastIsShowRight = false;
        }
    }

}
