package com.zoe.floatservice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Zoe on 2016/9/30.
 * email: 982194776@qq.com
 */
public class FloatService implements View.OnTouchListener, View.OnClickListener {
    private Context mContext = null;
    private Activity mActivity = null;
    private FrameLayout mViewFloat = null;
    private WindowManager mWindowManager = null;
    /**
     * 这两个是关键, 只是type不同
     * mParamsLogo, type为: TYPE_PRIORITY_PHONE
     * mParamsContent, type为: TYPE_PHONE
     * 不同的层级是为了:当内容向左边展开,收缩时logo不跳动
     * 获得者两个params使用Context,使用activity时会导致低版本dialog弹出有问题
     * */
    private WindowManager.LayoutParams mParamsLogo = null;
    private WindowManager.LayoutParams mParamsContent = null;
    private int screenWidth, screenHeight, imageHeight, imageWidth, imageHalfHeight, imageHalfWidth;
    private float density;
    private int TOP = 100, BOTTOM;
    private int centerWidth = 0;
    private boolean isShowing = false;
    private boolean isRotate = false;
    private boolean isHalf = false;
    private int x, y;
    private View mContent;

    private static final int LIE_LEFT = 0;
    private static final int LIE_TOP = 1;
    private static final int LIE_RIGHT = 2;
    private static final int LIE_BOTTOM = 3;
    private int lie = LIE_LEFT;
    private static final String SHOW = "show";
    private static final String LIE = "lie";
    private static final String LIE_X = "lie_x";
    private static final String LIE_Y = "lie_y";

    private static final int HIDE_MSG = 0;//隐藏
    private static final int ALPHA_MSG = 1;//透明
    private static final int CLOSE_MSG = 2;//关闭
    private static final int DELAY_TIME = 2000;//3秒
    private static final int ROTATE_TIME = 200;//0.3秒
    private static final int CLOSING = 100;//0.8秒
    private Handler mHandler = new Handler() {
        @SuppressLint("NewApi")
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case HIDE_MSG:
                    showHalf();
                break;
                case ALPHA_MSG:
                    mViewFloat.setAlpha(0.5f);
                    mHandler.sendEmptyMessageDelayed(HIDE_MSG, DELAY_TIME);
                break;
                case CLOSE_MSG:
                    close();
                break;
                default:
                    break;
            }
        }
    };

    private static FloatService f = null;

    public static FloatService getInstance(Activity activity){
        if(f == null) f = new FloatService(activity);
        return f;
    }
    private FloatService(Activity activity){
        mContext = activity.getBaseContext();
        mActivity = activity;
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        density = displayMetrics.density;
    }
    public void onCreate(){
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        mParamsLogo = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);

        mParamsLogo.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParamsLogo.x = x;
        mParamsLogo.y = y;

        mParamsContent = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        mParamsContent.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParamsContent.x = x;
        mParamsContent.y = y;

        // 浮标
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-2,-2);
        mViewFloat = new FrameLayout(mContext);
        mViewImage = new ImageView(mContext);
        mViewImage.setBackgroundResource(R.mipmap.jar_float_logo);
        mViewImage.setLayoutParams(params);
        mViewFloat.addView(mViewImage);
        mViewFloat.setFocusable(true);
        mViewFloat.setClickable(true);
        mViewFloat.setEnabled(true);
        initView();
    }

    public void onDestroy(){
        Log.e("==============","onDestroy");
        if(!isLogoHide){
            hide();
        }
        mWindowManager=null;
    }
    boolean isLogoHide = false;
    public void hide(){
        Log.e("==============","hide");
        isLogoHide = true;
        if(mWindowManager!=null) {
            try {
                if(isShowing) mWindowManager.removeView(mContent);
                mWindowManager.removeView(mViewFloat);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(mHandler!=null){
            removeMessages();
        }
        isShowing = false;

        SPUtils.put(mContext, SHOW, showRight);
        SPUtils.put(mContext, LIE, lie);
        SPUtils.put(mContext, LIE_X, x);
        SPUtils.put(mContext, LIE_Y, y);
    }
    public void show(){
        Log.e("==============","show");
        isLogoHide = false;
        lie = SPUtils.get(mContext, LIE, lie);
        showRight = SPUtils.get(mContext, SHOW, showRight);
        lastShowRight = !showRight;
        x = SPUtils.get(mContext, LIE_X, 0);
        y = SPUtils.get(mContext, LIE_Y, screenHeight/2);
        if(mWindowManager!=null) {
            mParamsLogo.x = x;
            mParamsLogo.y = y;
        }else {
            onCreate();
        }
        if(showRight) {
            showRight();
        }else {
            showLeft();
        }
        try {
            mWindowManager.addView(mViewFloat, mParamsLogo);
        }catch (Exception e){
            e.printStackTrace();
        }
        isShowing = false;
        clickable = true;
        removeMessages();
        mHandler.sendEmptyMessageDelayed(ALPHA_MSG, DELAY_TIME);
    }
    private View first, second,third,fourth;
    private ImageView mIvFirst,mIvSecond,mIvThird,mIvFourth;
    private TextView mTvFirst,mTvSecond,mTvThird,mTvFourth;
    private boolean showRight = true;
    private boolean lastShowRight = false;
    private void initView(){
        mContent = View.inflate(mContext, R.layout.content, null);
        first = mContent.findViewById(R.id.ll_left_first);
        second = mContent.findViewById(R.id.ll_left_second);
        third = mContent.findViewById(R.id.ll_left_third);
        fourth = mContent.findViewById(R.id.ll_left_fourth);
        mIvFirst = (ImageView) mContent.findViewById(R.id.iv_first);
        mIvSecond = (ImageView) mContent.findViewById(R.id.iv_second);
        mIvThird = (ImageView) mContent.findViewById(R.id.iv_third);
        mIvFourth = (ImageView) mContent.findViewById(R.id.iv_fourth);
        mTvFirst = (TextView) mContent.findViewById(R.id.tv_first);
        mTvSecond = (TextView) mContent.findViewById(R.id.tv_second);
        mTvThird = (TextView) mContent.findViewById(R.id.tv_third);
        mTvFourth = (TextView) mContent.findViewById(R.id.tv_fourth);

        mTvFirst.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mTvSecond.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mTvThird.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mTvFourth.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        mIvFirst.setOnClickListener(this);
        mTvFirst.setOnClickListener(this);
        first.setOnClickListener(this);

        second.setOnClickListener(this);
        mTvSecond.setOnClickListener(this);
        mIvSecond.setOnClickListener(this);

        third.setOnClickListener(this);
        mTvThird.setOnClickListener(this);
        mIvThird.setOnClickListener(this);

        fourth.setOnClickListener(this);
        mIvFourth.setOnClickListener(this);
        mTvFourth.setOnClickListener(this);
        mContent.setOnClickListener(this);
        if(showRight) {
            showRight();
        }else {
            showLeft();
        }
        mViewFloat.measure(0, 0);
        mContent.measure(0, 0);
        mViewFloat.setOnTouchListener(this);
        mContent.setOnTouchListener(this);
        imageWidth = mViewFloat.getMeasuredWidth();
        imageHeight = mViewFloat.getMeasuredHeight();
        imageHalfHeight = imageHeight/2;
        imageHalfWidth = imageWidth/2;
        BOTTOM = screenHeight - TOP - imageHeight;
        centerWidth = screenWidth/2;
    }
    /** 浮标靠在手机左边(或者上边与下边,但是在中线左边), 展开的内容放在右边 */
    private void showRight(){
        mParamsLogo.gravity = Gravity.START|Gravity.TOP;//基准点
        mParamsContent.gravity = Gravity.START|Gravity.TOP;
        showRight = true;
        Log.e("============","showRight");
        if(!lastShowRight) {//上次不是显示在右边
            LinearLayout.LayoutParams wmParams = new LinearLayout.LayoutParams(-2, -1);
            LinearLayout.LayoutParams wwParams = new LinearLayout.LayoutParams(-2, -2);
            LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp2px(18), dp2px(18));

            LinearLayout.LayoutParams wwFParams1 = new LinearLayout.LayoutParams(-2, -2);
            wwFParams1.setMargins(dp2px(51), dp2px(4), dp2px(17), dp2px(3));
            first.setLayoutParams(wwFParams1);
            LinearLayout.LayoutParams wwFParams4 = new LinearLayout.LayoutParams(-2, -2);
            wwFParams4.setMargins(0, 0, dp2px(4), 0);
            fourth.setLayoutParams(wwFParams4);

            first.setPadding(dp2px(10), dp2px(4), dp2px(8), dp2px(4));
            second.setPadding(dp2px(8), dp2px(4), dp2px(16), dp2px(4));
            third.setPadding(0, dp2px(7), 0, dp2px(7));
            fourth.setPadding(dp2px(6), dp2px(5), dp2px(7), dp2px(5));

            mIvFirst.setBackgroundResource(R.mipmap.jar_float_service);
            mIvFirst.setLayoutParams(wwParams);
            mTvFirst.setText(R.string.service);
            mTvFirst.setPadding(0, dp2px(4), 0, 0);
            mTvFirst.setVisibility(View.VISIBLE);

            mIvSecond.setBackgroundResource(R.mipmap.jar_float_safe);
            mTvSecond.setText(R.string.safe);
            mTvSecond.setVisibility(View.VISIBLE);
            mTvSecond.setPadding(0, dp2px(4), 0, 0);
            mIvSecond.setLayoutParams(wwParams);

            mTvThird.setVisibility(View.GONE);
            mIvThird.setBackgroundResource(R.mipmap.jar_float_line);
            mIvThird.setLayoutParams(wmParams);

            mIvFourth.setBackgroundResource(R.mipmap.jar_float_close);
            mIvFourth.setLayoutParams(closeParams);
            mTvFourth.setVisibility(View.GONE);

        }
        lastShowRight = true;
    }
    /** 浮标靠在手机右边(或者上边与下边,但是在中线右边), 展开的内容放在左边 */
    private void showLeft(){
        mParamsLogo.gravity = Gravity.END|Gravity.TOP;//基准点
        mParamsContent.gravity = Gravity.END|Gravity.TOP;
        showRight = false;
        Log.e("============","showLeft");
        if(lastShowRight) {//不要显示在右边,且上次是显示在右边
            LinearLayout.LayoutParams wmParams = new LinearLayout.LayoutParams(-2, -1);
            LinearLayout.LayoutParams wwParams = new LinearLayout.LayoutParams(-2, -2);
            LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp2px(18), dp2px(18));

            LinearLayout.LayoutParams wwFParams1 = new LinearLayout.LayoutParams(-2, -2);
            wwFParams1.setMargins(0, 0,0,0);
            first.setLayoutParams(wwFParams1);

            LinearLayout.LayoutParams wwFParams4 = new LinearLayout.LayoutParams(-2, -2);
            wwFParams4.setMargins(dp2px(17), dp2px(4), dp2px(51), dp2px(3));
            fourth.setLayoutParams(wwFParams4);

            first.setPadding(dp2px(7), dp2px(5), dp2px(6), dp2px(5));
            second.setPadding(0, dp2px(7), 0, dp2px(7));
            third.setPadding(dp2px(16), dp2px(4), dp2px(8), dp2px(4));
            fourth.setPadding(dp2px(8), dp2px(4), dp2px(10), dp2px(4));


            mIvFirst.setBackgroundResource(R.mipmap.jar_float_close);
            mIvFirst.setLayoutParams(closeParams);
            mTvFirst.setVisibility(View.GONE);

            mTvSecond.setVisibility(View.GONE);
            mIvSecond.setBackgroundResource(R.mipmap.jar_float_line);
            mIvSecond.setLayoutParams(wmParams);

            mIvThird.setBackgroundResource(R.mipmap.jar_float_safe);
            mTvThird.setText(R.string.safe);
            mTvThird.setVisibility(View.VISIBLE);
            mTvThird.setPadding(0, dp2px(4), 0, 0);
            mIvThird.setLayoutParams(wwParams);

            mIvFourth.setBackgroundResource(R.mipmap.jar_float_service);
            mTvFourth.setText(R.string.service);
            mTvFourth.setVisibility(View.VISIBLE);
            mTvFourth.setPadding(0, dp2px(4), 0, 0);
            mIvFourth.setLayoutParams(wwParams);

        }
        lastShowRight = false;
    }

    private long spreadTime;
    private long closeTime;
    /** 点击浮标logo,内容展开 */
    private void spread(){
        if(System.currentTimeMillis() - closeTime > CLOSING) {
            Log.e("=========","spread");
            mParamsContent.x = x;
            mParamsContent.y = y;

            refreshView(x, y);

            mWindowManager.addView(mContent, mParamsContent);
            isShowing = true;
            mHandler.sendEmptyMessageDelayed(CLOSE_MSG, DELAY_TIME);
        }
        spreadTime = System.currentTimeMillis();
    }
    /** 展开的内容关闭 */
    private void close(){
        if(System.currentTimeMillis() - spreadTime > CLOSING) {
            Log.e("=========","close");
            mHandler.removeMessages(CLOSE_MSG);
            mWindowManager.removeView(mContent);
            refreshView();
            isShowing = false;
            mHandler.sendEmptyMessageDelayed(ALPHA_MSG, DELAY_TIME);
        }
        closeTime = System.currentTimeMillis();
    }
    private void removeMessages(){
        mHandler.removeMessages(CLOSE_MSG);
        mHandler.removeMessages(HIDE_MSG);
        mHandler.removeMessages(ALPHA_MSG);
    }
    /** 浮标logo收缩一半到屏幕外 */
    private void showHalf(){
        isHalf = true;
        switch (lie){
            case LIE_TOP:
                refreshView(x, -imageHeight/2);
                startRotateAnimation(0, 90);
                break;
            case LIE_RIGHT:
                refreshView(-imageWidth/2, y);
                break;
            case LIE_BOTTOM:
                refreshView(x, screenHeight - imageHeight/2);
                startRotateAnimation(0, 90);
                break;
            case LIE_LEFT:
            default:
                refreshView(-imageWidth/2, y);
                break;
        }
    }
    private ImageView mViewImage;
    /** 浮标收缩时旋转 */
    private void startRotateAnimation(int start, int end) {
        Log.e("=========",start + " rotate " +end);
        RotateAnimation animation = new RotateAnimation(start, end, Animation.RELATIVE_TO_SELF,
                0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(ROTATE_TIME);
        animation.setFillAfter(true);
        mViewImage.startAnimation(animation);
        isRotate = !isRotate;
    }
    /** 刷新浮标的位置 */
    private void refreshView() {
        refreshView(x,y);
    }
    /** 根据x,y的值,刷新浮标的位置 */
    private void refreshView(int x, int y) {
        // y轴减去状态栏的高度，因为状态栏不是用户可以绘制的区域，不然拖动的时候会有跳动
        mParamsLogo.x = x;
        mParamsLogo.y = y;
        // 更新指定View的位置
        mWindowManager.updateViewLayout(mViewFloat, mParamsLogo);
    }

    private float tmpX, tmpY;
    private long half2SpreadTime;
    private boolean clickable = true;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(mAlertDialog!=null && mAlertDialog.isShowing()) mAlertDialog.dismiss();
        if(System.currentTimeMillis() - half2SpreadTime < CLOSING) return true;
        removeMessages();
        if(isShowing){//当浮标是展开状态时, 关闭
            close();
            tmpX = event.getRawX();
            tmpY = event.getRawY();
            return false;
        }
        mViewFloat.setAlpha(1.0f);
        if(isHalf){//当浮标是收缩状态时, 展开
            refreshView();
            spread();
            half2SpreadTime = System.currentTimeMillis();
            isHalf = false;
            if(isRotate) {
                startRotateAnimation(90,0);
            }
            return true;
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
                   refreshView(x - imageHalfWidth, y - imageHalfHeight);
                   break;
               }
               if(Math.abs(x - tmpX) > 20 || Math.abs(y - tmpY) > 20) {
                   clickable = false;
                   if(!showRight){
                       Log.e("======","Gravity.START | Gravity.TOP;//基准点");
                       mParamsLogo.gravity =  Gravity.START | Gravity.TOP;//基准点
                       showRight = true;
                       refreshView();
                   }
                   Log.e("=====","imageHalfWidth: " + imageHalfWidth);
                   refreshView(x - imageHalfWidth, y - imageHalfHeight);
               }else {
                   clickable = true;
               }
               break;
           case MotionEvent.ACTION_UP:
               int xOff = (int) event.getRawX();
               int yOff = (int) event.getRawY();
               if(clickable && Math.abs(yOff - this.y) < imageHeight){
                   if(showRight){
                       if(Math.abs(xOff - this.x) < imageWidth ) {
                           onClick(v);
                       }
                   }else {
                       Log.e("=======",xOff +" - " + this.x +" = " + (xOff - this.x) );
                       Log.e("=======","screenWidth: " + (screenWidth - xOff - this.x ) );
                       if(Math.abs(screenWidth - xOff - this.x ) < imageWidth ) {
                           onClick(v);
                       }
                   }
                   return true;
               }
               clickable = true;
               mParamsLogo.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                       | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                       | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
               location(xOff, yOff);
               v.setFocusable(false);
               break;
           case MotionEvent.ACTION_CANCEL:
               v.setPressed(false);
               v.setFocusable(false);
               break;
           default:
               break;
       }
       return true;
    }
    /** 确定手抬起时,  xy的位置 */
    private void location(int x, int y){
        if (y < TOP) { // 贴上
            showRight();
            if(x <= centerWidth){
                showRight();
                x -=imageHalfWidth;
            }else{
                showLeft();
                x = screenWidth - x - imageHalfWidth;
            }
            y = 0;
            lie = LIE_TOP;
        } else if (y > BOTTOM) { // 贴下
            showRight();
            if(x <= centerWidth){
                showRight();
                x -=imageHalfWidth;
            }else{
                showLeft();
                x = screenWidth - x - imageHalfWidth;
            }
            y = screenHeight - imageHeight;
            lie = LIE_BOTTOM;
        } else if (x >= centerWidth) { // 贴右
            showLeft();
            x = 0;
            y -= imageHalfHeight;
            lie = LIE_RIGHT;
        } else { // 贴左
            showRight();
            x = 0;
            lie = LIE_LEFT;
            y -= imageHalfHeight;
        }
        this.x = x;
        this.y = y;
        limitBorder();
        refreshView();
        mHandler.sendEmptyMessageDelayed(ALPHA_MSG, DELAY_TIME);
    }
    /** 限定xy的边界 */
    private void limitBorder(){
        if(x < 0) x = 0;
        if(y < 0) y = 0;
        if(x > screenWidth - imageWidth) x = screenWidth - imageWidth;
        if(y > screenHeight - imageHeight) y = screenHeight - imageHeight;
    }

    @Override
    public void onClick(View v) {
        if(System.currentTimeMillis() - half2SpreadTime < CLOSING) return ;
        if(v == mViewFloat){
            if(isShowing){
                close();
            }else {
                spread();
            }
        }
        switch (v.getId()){
            case R.id.ll_left_first:
            case R.id.iv_first:
            case R.id.tv_first:
                if(showRight){
                    clickService();
                }else{
                    clickClose();
                }
                break;
            case R.id.ll_left_second:
            case R.id.iv_second:
            case R.id.tv_second:
                if(showRight){
                    clickSafe();
                }
                break;
            case R.id.ll_left_third:
            case R.id.iv_third:
            case R.id.tv_third:
                if(!showRight){
                    clickSafe();
                }
                break;
            case R.id.ll_left_fourth:
            case R.id.iv_fourth:
            case R.id.tv_fourth:
                if(showRight){
                    clickClose();
                }else{
                    clickService();
                }
                break;
        }
        if(v == mContent) {
            close();
        }
    }

    /** 点击了客服*/
    private void clickService(){
        Toast.makeText(mContext, "点击了客服", Toast.LENGTH_SHORT).show();
    }
    /** 点击了安全*/
    private void clickSafe(){
        Toast.makeText(mContext, "点击了安全", Toast.LENGTH_SHORT).show();
    }
    /** 点击了关闭*/
    private AlertDialog mAlertDialog;
    private void clickClose(){
        Toast.makeText(mContext, "点击了关闭", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                .setTitle("温馨提示")
                .setMessage("关闭悬浮图标，下次登录将重新开启")
                .setPositiveButton("关闭浮标", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onDestroy();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private int dp2px(int dp){
        return (int)(dp * density);
    }
}
