package com.patrick.easypanel.views;
//本类中所有的的角度，是从直角坐标系y轴逆时针开始算
//android的角度是从它的x轴顺时针开始算

//import com.patrick.util.Toolkit;

import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.view.*;
import com.patrick.easypanel.FlipService;
import com.patrick.easypanel.R;
import com.patrick.easypanel.logic.Toolkit;

import android.content.Context;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import android.view.WindowManager.LayoutParams;

import static android.view.SurfaceHolder.*;


public class FlipWindow extends SurfaceView implements Callback
{
	private int numPart;
	public int getNumPart() {
		return numPart;
	}

	public void setNumPart(int numPart) {
		this.numPart = numPart;
	}
	private float outterCirR;
	private float midiumCirR;
	private float innerCirR;
	//private float outterPer;
	private float midiumPer;
	private float innerPer;     
	private boolean touching;
	private WindowManager m_wm;
	Context context;
	OnSelectedListener m_onSelected;
	
	SurfaceHolder holder;
	public void setOnSelectedListener(OnSelectedListener m_onSelected) {
		this.m_onSelected = m_onSelected;
	}


    private boolean isSetting; //是不是设置界面
	public FlipWindow(final Context context, WindowManager wm) {
		super(context);
        shared = PreferenceManager.getDefaultSharedPreferences(context);
		initialParams(context, wm);
		holder = this.getHolder();
        assert holder != null;
        holder.addCallback(this);
		holder.setFormat(PixelFormat.TRANSPARENT);
		holder.setType(SURFACE_TYPE_GPU);
		initialTouchView(context);

    }
    public FlipWindow(Context context, boolean isSetting)
    {
        super(context);
        this.isSetting = isSetting;
        shared = PreferenceManager.getDefaultSharedPreferences(context);
        initialParams(context, null);
        holder = this.getHolder();
        assert holder != null;
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);
        if (isSetting)
        {
            innerPer = 1.0f;
            midiumPer = 1.0f;
            isDefault = false;
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getPointerCount() > 1)
                        return false;
                    x = event.getRawX();
                    y = event.getRawY() - 25;

                    refresh();
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        if (m_onSelected != null)
                            m_onSelected.onSelect(curPart);
                        curPart = -1;
                        x = centerPoint.x -1;
                        y = centerPoint.y -1;
                        refresh();
                    }

                    return true;
                }
            });
        }
        curPart = -1;
        x = centerPoint.x -1;
        y = centerPoint.y -1;
    }
	
	private View touchView;
	public View getTouchView() {
		return touchView;
	}
    public void updateTouchView()
    {
        initialParams(context,m_wm);
        m_wm.updateViewLayout(getTouchView(),getTouchLayoutParams());
    }
	public LayoutParams getTouchLayoutParams()
	{
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				|      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				 ;
		params.format = PixelFormat.TRANSPARENT;
		params.width = (int) defaultCirR;
		params.height = params.width * 2;
        params.gravity = Gravity.RIGHT | Gravity.TOP;

        int statusBarHeight = shared.getInt("statusBarHeight",40);
        params.y = (int) (centerPoint.y -  defaultCirR);//屏幕中点
		return params;
	}
    Thread delayDrawThread;///需要触摸一段时间才能弹出
    boolean isInTouchView;
    boolean validTouch;
	private void initialTouchView(final Context context) {
		View.OnTouchListener touchListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getPointerCount() > 1)
					return false;
				x = event.getRawX();
	        	y = event.getRawY() - 25;
                final int touchDelayMilles = shared.getInt(getResources().getString(R.string.pref_sensitivity),500);
                final boolean shouldVibrate = shared.getBoolean(getResources().getString(R.string.pref_vibrate), true);
		        switch(event.getAction())
		        {
		        case MotionEvent.ACTION_DOWN:
//		        	Log.d("down", "touch");
		        	initialParams(context, m_wm);//待修改
		        	if (exiting)
		        		return false;
                    validTouch = true;
		        	touching = true;
		        	isDefault = false;
		        	touchView.invalidate();
		        	midiumPer = 0;
		        	innerPer = 0;
                    delayDrawThread = new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(touchDelayMilles);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (touching ){//&& isInTouchView) {//长按还是长按加长滑
                                if (shouldVibrate)
                                    touchView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                                //Toolkit.vibrateForSeconds(context, 25);
                                new AnimInThread().start();//进入时的动画
                            }
                            else
                            {//直接跳过移动的判断
                                validTouch = false;
                            }
                        }
                    };
                    delayDrawThread.start();

		        	break;
		        case MotionEvent.ACTION_MOVE:
                    isInTouchView = getDist(x,y,centerPoint.x,centerPoint.y) <= defaultCirR;
		        	refresh();
		        	break;
		        case MotionEvent.ACTION_UP:
		        	if (exiting)
		        		break;
                    if (delayDrawThread!=null && !delayDrawThread.isInterrupted())
                        delayDrawThread.interrupt();//终止判断线程
		        	touching = false;
		        	exiting = true;  	
		        	Log.d("curPart", String.valueOf(curPart));
                    if (m_onSelected!= null && validTouch)
                        m_onSelected.onSelect(curPart);
                    curPart = -1;
		        	new AnimOutThread().start();//退出时的动画
		        	break;
		        }
				return true;
			}
		};
		//this.setOnTouchListener(touchListener);
		touchView = new View(context){
			@Override
			protected void onDraw(Canvas canvas) {
				canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
				if (isDefault)
				{
					Paint p = new Paint();
					p.setAntiAlias(true);
					p.setColor(Color.GRAY & 0x88FFFFFF);
					canvas.drawCircle(defaultCirR + 10, defaultCirR, defaultCirR, p);
				}
				super.onDraw(canvas);
			}
			
		};
		
		touchView.setOnTouchListener(touchListener);
	}
	
	private Paint lostPaint;
	private float defaultCirR;
	private Paint iconPaint;
	private float iconSize;

    private SharedPreferences shared;
	private void initialParams(final Context context, WindowManager wm) {
		
		this.context = context;
		m_wm = wm;
		centerPoint = Toolkit.getCenterPoint(context);
        //菜单大小,用比例计算
        float sizePer = shared.getFloat(getResources().getString(R.string.pref_size_per_vert),1.0f);
        float posPer = shared.getFloat(getResources().getString(R.string.pref_positonY_per_vert),0.5f);
        if (isSetting)
        {
            posPer = 0.5f;
            sizePer = 1.0f;
        }
		if (centerPoint.x < centerPoint.y * 2)
		{//竖屏
			midiumCirR = centerPoint.x / 3 * 1.2f;
            centerPoint.y = (int) (centerPoint.y * 2 * posPer);//半圈的位置
		}
		else
		{
            sizePer = shared.getFloat(getResources().getString(R.string.pref_size_per_horiz),1.0f);
            posPer = shared.getFloat(getResources().getString(R.string.pref_positonY_per_horiz),0.5f);
			float width = centerPoint.y * 2 * 10 / 16;
			midiumCirR = width / 3 * 1.2f;
            centerPoint.y = (int) (centerPoint.y * 2 * (1-posPer));//半圈的位置,横屏刚好反过来
		}
        midiumCirR *= sizePer;//设置扇形大小
        innerCirR = midiumCirR / 4;//内外圈的大小
        outterCirR = innerCirR * 6f;

		defaultCirR = innerCirR *3 /4 / sizePer;//默认状态下的半圈提示
		
		iconSize = midiumCirR / 3;
		
		innerPaint = new Paint();
		innerPaint.setAntiAlias(true); //内圈的Paint
		innerPaint.setStrokeWidth(2);
		innerPaint.setColor(Color.GRAY);
		
		partPaint = new Paint();
		partPaint.setAntiAlias(true);//普通选择块的Paint
		//partPaint.setColor(Color.BLACK & 0x55FFFFFF);
		partPaint.setColor(getResources().getColor(R.color.lightgrey));
		partPaint.setAlpha(230);
		
		focusPaint = new Paint();
		focusPaint.setAntiAlias(true);//选中块的Paint
		focusPaint.setColor(0xFF436CFC);
		
		lostPaint = new Paint();
		lostPaint.setAntiAlias(true);
		lostPaint.setColor(partPaint.getColor());
		lostPaint.setAlpha(255);
		//lostPaint.setColor(Color.LTGRAY);//选中二级菜单时候，一级菜单的Paint
		
		iconPaint = new Paint();
		iconPaint.setAntiAlias(true);
		iconPaint.setAlpha(0);
		
		//this.setBackgroundColor(Color.GRAY);
		setNumPart(5);
		midiumOval = new RectF(1,2,3,4);
		outterOval = new RectF(1,2,3,4);
	}
	boolean isDefault = true;
	
	private boolean exiting;
	private Point centerPoint;

	private RectF midiumOval;
	private RectF outterOval;
	private float x,y;
	private Paint innerPaint;
	private Paint partPaint;
	private Paint focusPaint;
	
	private int curPart;
	public static LayoutParams getDefaultParams()
	{//全屏，不可触摸，可透明
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		params.flags = 0
				|	   WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				|      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| 	   WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				 ;
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.MATCH_PARENT;
		params.format = PixelFormat.TRANSPARENT;
		params.gravity = Gravity.CENTER;
		params.alpha = 1f;
		return params;
	}
	private float getDist(float fx,float fy,float tx, float ty)
	{
		return (float) Math.sqrt((fx-tx)*(fx-tx) + (fy-ty)*(fy-ty));
	}
	void drawFlip(Canvas canvas)
	{
		if (canvas == null) return;
        //Canvas抗锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
		if (isDefault)
		{
			drawDefault(canvas);
		}
		else
		{
			float curX = x;
			float curY = y;
			float dis = getDist(curX,curY,centerPoint.x,centerPoint.y);
			boolean isInFirst = dis >= innerCirR && dis <= midiumCirR;
			boolean isInSecond = dis > midiumCirR;
			if (dis < innerCirR)
				curPart = -1;
			double curRad = Math.atan((curX - centerPoint.x) / (curY - centerPoint.y));
			curRad = curRad >0? curRad : Math.PI + curRad;//tan角度转换
			//canvas.drawColor(Color.GRAY, Mode.CLEAR);
			float outterCurR = outterCirR*midiumPer;
			float midiumCurR = midiumCirR*midiumPer;
			float innerCurR = innerCirR*innerPer;
			
			midiumOval.left = centerPoint.x - midiumCurR;
			midiumOval.top = centerPoint.y - midiumCurR;
			midiumOval.right = centerPoint.x + midiumCurR;
			midiumOval.bottom = centerPoint.y + midiumCurR;
			outterOval.left = centerPoint.x - outterCurR;
			outterOval.top = centerPoint.y - outterCurR;
			outterOval.right = centerPoint.x + outterCurR;
			outterOval.bottom = centerPoint.y + outterCurR;
			//绘图数据初始化完毕
			
			if ((isInFirst || isInSecond)
                    ||isSetting)//&& curPart != -1)
				canvas.drawCircle(centerPoint.x,centerPoint.y, outterCurR, partPaint);
			//画外部黑色透明圆
			if (((!isInSecond && !isInFirst)||exiting)
                    && !isSetting)//|| curPart == -1)
			    canvas.drawCircle(centerPoint.x,centerPoint.y, midiumCurR, partPaint);
			else
			{
				innerPaint.setStyle(Style.STROKE);
				canvas.drawCircle(centerPoint.x,centerPoint.y, midiumCurR, innerPaint);
				innerPaint.setStyle(Style.FILL);
			}
			double rad = 0;
			
			
			
			for (int i = 0; i< numPart ; ++i)
			{
				//画选中块
				if(curRad > rad && curRad < rad + Math.PI / numPart && !exiting)
				{
					if (isInFirst||isInSecond || isSetting)
					{//二级菜单
						for (int j = 0; j <2* numPart ;++j)
						{
							float fromX = (float) (centerPoint.x - midiumCurR  * Math.sin( j*0.5*Math.PI / numPart));
							float fromY = (float) (centerPoint.y - midiumCurR  * Math.cos( j*0.5*Math.PI / numPart));
							float toX   = (float) (centerPoint.x - outterCurR  * Math.sin( j*0.5*Math.PI / numPart));
							float toY   = (float) (centerPoint.y - outterCurR  * Math.cos( j*0.5*Math.PI / numPart));
							
							canvas.drawLine(fromX, fromY, toX, toY, innerPaint);
						}
					}
					if (isInSecond)
					{
						boolean isUp = curRad - rad < (0.5*Math.PI / numPart);
						double radBegin = isUp ? rad + 0.5* Math.PI / numPart : rad + Math.PI / numPart;
						curPart = numPart - 1 + 2*i + (isUp?1:2);
						canvas.drawArc(outterOval
								, 270 - (float) Math.toDegrees(radBegin), 90 / numPart
								, true, focusPaint);//第二级菜单
						canvas.drawArc(midiumOval
								, 270 - (float) Math.toDegrees(rad + Math.PI / numPart), 180 / numPart
								, true, lostPaint);
					}
					else if (isInFirst)
					{
						canvas.drawArc(midiumOval
								, 270 - (float) Math.toDegrees(rad + Math.PI / numPart), 180 / numPart
								, true, focusPaint);
						curPart = i;
					}
				}
				//画块的分割线
				float fromX = (float) (centerPoint.x - innerCurR  * Math.sin(rad));
				float fromY = (float) (centerPoint.y - innerCurR  * Math.cos(rad));
				float toX =   (float) (centerPoint.x - midiumCurR * Math.sin(rad));
				float toY =   (float) (centerPoint.y - midiumCurR * Math.cos(rad));
				rad += Math.PI / numPart;
				if (i != 0) 
					canvas.drawLine(fromX, fromY, toX, toY, innerPaint);
				
			}
			
			//画内部灰色圆
			canvas.drawCircle(centerPoint.x,centerPoint.y, innerCurR, innerPaint);
		
			//画Icons
			rad = Math.PI/ numPart / 2;
			for (int i = 0; i<3* numPart; ++i)
			{
				if (i < FlipService.currentItems.size())
				{
                    if (i >= numPart && (!isInFirst && !isInSecond) && !isSetting)
                        break;//第二级菜单不用画
                    Bitmap b = FlipService.currentBitmaps.get(i);
                    Matrix m = new Matrix();
                    //缩放比例
                    float per = iconSize / b.getHeight();
                    //旋转
                    //	m.postRotate((float) (( Math.toDegrees(Math.PI - rad)) * innerPer),b.getHeight()/2,b.getWidth()/2);
                    m.postRotate((float) (360 * innerPer),b.getHeight()/2,b.getWidth()/2);
                    m.postScale(per, per,b.getHeight()/2,b.getWidth()/2);//缩放
                    float off = b.getHeight() * (1 - per) /2 + iconSize/2 ; //x与y的offset
                    float len = (i < numPart ? midiumCirR* 3/4 : outterCirR * 5/6)  * midiumPer;
                    float iX =   (float) (centerPoint.x - len * Math.sin(rad));//中点位置
                    float iY =   (float) (centerPoint.y - len * Math.cos(rad));//中点位置
                    m.postTranslate(iX - off, iY - off);//位移

                    iconPaint.setAlpha((int) (255 * innerPer));//透明度变化
                    canvas.drawBitmap(b, m, iconPaint);
				}
				//第一级菜单还是第二级菜单
				rad += i < numPart ?( Math.PI/ numPart) : (Math.PI /numPart /2);
				
				if (i == numPart -1)
					rad = Math.PI /numPart /4; //画第二级菜单，清零
			}
		}
	}


	private void drawDefault(Canvas canvas) {
		this.post(new Runnable(){
			public void run() {
				m_wm.updateViewLayout(touchView, getTouchLayoutParams());
				}
		});
		touchView.postInvalidate();
	}
	class AnimInThread extends Thread
	{
		public void run()
		{
			while (midiumPer < 1.1 && touching)
			{
				midiumPer += midiumPer > 0.8 ? (midiumPer > 1 ? 0.01 : 0.03) : 0.05;
				if (innerPer < 1)
				{
					innerPer += 0.05;
				}
				refresh();
				//postInvalidate();
				try {
					sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			while (midiumPer > 1 && touching)
			{
				midiumPer -= 0.01;
				refresh();
				//postInvalidate();
				try {
					sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	};
	class AnimOutThread extends Thread{
		public void run()
		{
			float speed = 0.05f;
			while (midiumPer > 0 && !touching)
			{
				//speed = midiumPer > 0.7 ? 0.01f : (midiumPer > 0.45 ? 0.03f : 0.05f);
				midiumPer -= speed;
				innerPer -= speed;
				refresh();//postInvalidate();
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			isDefault = true;
			exiting = false;
			try {//延后刷新，保证覆盖掉之前的刷新
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refresh();
		}
	}
	public interface OnSelectedListener
	{
		public void onSelect(int which);
	}
	public void refresh()
	{//通知绘图线程DrawThread刷新一下绘图。
		synchronized(syncDraw)
		{
			drawed = false;
			syncDraw.notifyAll();
		}
	}
	boolean drawed = false;
	Object syncDraw = new Object();
	class DrawThread extends Thread
	{
		public void run()
		{
			while(true)
			{
				if (drawed)
				{
					synchronized(syncDraw)
					{
						try {
							syncDraw.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				Canvas canvas = holder.lockCanvas();
				if (canvas == null)
					continue ;
                if (isSetting){
                    Bitmap wallpaper = ((BitmapDrawable) context.getWallpaper()).getBitmap();
                    canvas.drawBitmap(wallpaper,0,0,null);//防止单色刺眼
                    //canvas.drawColor(getResources().getColor(R.color.paleturquoise));
                }
                else {
                    canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
                }

				drawFlip(canvas);
				holder.unlockCanvasAndPost(canvas);
				drawed = true;
			}
		}
	}
	

	DrawThread drawthread;
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		drawthread = new DrawThread();
		drawthread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		drawthread.interrupt();
		drawthread = null;
        System.gc();
	}

}