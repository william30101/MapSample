package wyf.ytl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wyf.uart.UartMsg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import xmpp.XMPPSetting;
public class GameView extends View{
	
	private String TAG = "william";
	private static final int VIEW_WIDTH = 640;
	private static final int VIEW_HEIGHT = 640;
	private XMPPSetting XMPPSet = new XMPPSetting();;
 
	Game game;
	Spinner mySpinner;// Spinner���ޥ�
	TextView CDTextView;
	int span = 13;
	int theta = 0;
	boolean drawCircleFlag=false , drawLastCircle= false;

	Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.source);
	Bitmap target = BitmapFactory.decodeResource(getResources(), R.drawable.target)	;
	Paint paint = new Paint();
	
	
	// William Added
	int touchX=0,touchY=0;
	int x,y;
    boolean chk = false;
    int tempwidth=0;
    int tempheight=0;
    String inStr = "test";
    String inStr2 = "test2";
    int fixMapData = 5;
    int gridX = 0 , gridY = 0;
    
    
    double rX = 0 , rY = 0;
    
    public boolean flag = false , flagR = false , over2Grid = false , doubleCmd = false;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    public static ShowThread st;
	
    
    /*	[0] : Original position X
    	[1] : Original position Y
    	[2] : Next position X
    	[3] : Next position Y
    */
	ArrayList<int[][] > pathQueue = new ArrayList<int[][] >();
	
	Canvas gcanvas;
	
	private Handler myHandler = new Handler(){
        public void handleMessage(Message msg) {
        	if(msg.what == 1){
        		CDTextView.setText("Step" + (Integer)msg.obj);
        	}
        }
	};	
	
	public GameView(Context context, AttributeSet attrs) {//�غc����
		super(context, attrs);
		st = new ShowThread();
		//singleThreadExecutor.execute(st);
	}
	protected void onDraw(Canvas canvas) {	//�мg��ø�s��k
		try{
			gcanvas = canvas;
			onMyDraw(canvas);				//�I�s�ۤv��ø�s��k
			
		}
		catch(Exception e){}
	}
	
	public void RunThreadTouch(boolean inFlag)
    {
		st = new ShowThread();
    	flag = inFlag;
    	singleThreadExecutor.execute(st);
    }
	
	public void SetRobotAxis(double x , double y)
	{
		rX = x;
		rY = y;
	}
	
	public void onDrawText(Canvas canvas)
	{
		//float[] position=lbx.getPosition(source[1], source[0]);
		//canvas.drawColor(Color.BLACK);
		//paint.setStyle(Style.FILL);
		//paint.setTextSize(40);
		paint.setARGB(255, 255, 0, 0);
		paint.setStyle(Style.STROKE);
		paint.setTextSize(15);
        canvas.drawText("Tx = " + touchX + " Ty = " + touchY ,380, 100, paint);
        canvas.drawText("chk : " + chk, 380, 120, paint);
        canvas.drawText("tempX,Y : " + tempwidth + "," + tempheight, 380, 140, paint);
        canvas.drawText("GridX,Y : " + gridX + "," + gridY, 380, 160, paint);
        canvas.drawText("RX : " + String.format("%.3f", rX) , 380, 180, paint);
        canvas.drawText("RY : " + String.format("%.3f", rY), 380, 200, paint);
        
	}
	
	public void DrawOrigin(final Canvas canvas){
		new Thread(){
			public void run(){			
				try {
					synchronized (pathQueue) {
						for (int i=pathQueue.size() - 1;i > 0 ;i--)
						{
							int [][] tempA = pathQueue.get(i);
							postInvalidate();
							paint.setColor(Color.WHITE);
							canvas.drawCircle(	
								tempA[1][0]*(span+1)+span/2+fixMapData,tempA[1][1]*(span+1)+span/2+fixMapData,span/2, 
								paint
								);
						
						
							paint.setColor(Color.RED);
							canvas.drawCircle(	
								tempA[0][0]*(span+1)+span/2+fixMapData,tempA[0][1]*(span+1)+span/2+fixMapData,span/2, 
								paint
								);
							

							//BitmapManager.
							try {
								Thread.sleep(200);
								postInvalidate();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						drawCircleFlag = false;
						drawLastCircle = true;
					
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			
			//postInvalidate();
			//paint.setColor(Color.WHITE);
			//canvas.drawCircle(tempA[0][0]*(span+1)+span/2+fixMapData, tempA[0][1]*(span+1)+span/2+fixMapData, span/2, paint); 
		}
	}.start();				
				
	}
	
//	public void DrawOrigin(Canvas canvas)
//	{
//		for (int i=pathQueue.size() - 1;i > 0 ;i--)
//		{
//			int [][] tempA = pathQueue.get(i);
//			postInvalidate();
//			paint.setColor(Color.WHITE);
//			canvas.drawCircle(	
//				tempA[1][0]*(span+1)+span/2+fixMapData,tempA[1][1]*(span+1)+span/2+fixMapData,span/2, 
//				paint
//				);
//		
//		
//			paint.setColor(Color.RED);
//			canvas.drawCircle(	
//				tempA[0][0]*(span+1)+span/2+fixMapData,tempA[0][1]*(span+1)+span/2+fixMapData,span/2, 
//				paint
//				);
//			
//			//BitmapManager.
//			
//		}
//		
//		//postInvalidate();
//		//paint.setColor(Color.WHITE);
//		//canvas.drawCircle(tempA[0][0]*(span+1)+span/2+fixMapData, tempA[0][1]*(span+1)+span/2+fixMapData, span/2, paint); 
//		
//		 
//	}
	
	
	protected void onMyDraw(Canvas canvas){
		super.onDraw(canvas);
		
		gcanvas = canvas;
		pathQueue.clear();
		canvas.drawColor(Color.GRAY);
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);
		canvas.drawRect(5, 55, 325, 376, paint);
		int[][] map = game.map;
		int row = map.length;
		int col = map[0].length;
		for(int i=0; i<row; i++){
			for(int j=0; j<col; j++){
				if(map[i][j] == 0){							
					paint.setColor(Color.WHITE);			
					paint.setStyle(Style.FILL);				
					canvas.drawRect(fixMapData+j*(span+1), fixMapData+i*(span+1), 
							fixMapData+j*(span+1)+span,fixMapData+i*(span+1)+span, paint);
				}
				else if(map[i][j] == 1){//�¦�
					paint.setColor(Color.BLACK);
					paint.setStyle(Style.FILL);
					canvas.drawRect(fixMapData+j*(span+1), fixMapData+i*(span+1),
							fixMapData+j*(span+1)+span, fixMapData+i*(span+1)+span, paint);					
				}
			}
		}
		ArrayList<int[][]> searchProcess=game.searchProcess;
		for(int k=0;k<searchProcess.size();k++){//ø�s�M��L�{
			int[][] edge=searchProcess.get(k);  
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(1);
			canvas.drawLine(
				edge[0][0]*(span+1)+span/2+fixMapData,edge[0][1]*(span+1)+span/2+fixMapData,
				edge[1][0]*(span+1)+span/2+fixMapData,edge[1][1]*(span+1)+span/2+fixMapData,
				paint
			);
		}

		if(
			mySpinner.getSelectedItemId()==0||
			mySpinner.getSelectedItemId()==1||
			mySpinner.getSelectedItemId()==2
		){
			if(game.pathFlag){
				HashMap<String,int[][]> hm=game.hm;
				int[] temp=game.target;
				int count=0;		
				while(true){
					int[][] tempA=hm.get(temp[0]+":"+temp[1]);
					paint.setColor(Color.BLACK);
					paint.setStyle(Style.STROKE);
					paint.setStrokeWidth(2);
					canvas.drawLine(	
						tempA[0][0]*(span+1)+span/2+fixMapData,tempA[0][1]*(span+1)+span/2+fixMapData,
						tempA[1][0]*(span+1)+span/2+fixMapData,tempA[1][1]*(span+1)+span/2+fixMapData, 
						paint
					);
					//William added
					
					int[][] saveData = {{tempA[0][0],tempA[0][1]},
										{tempA[1][0],tempA[1][1]}};
					pathQueue.add(saveData);
					
					count++;
					if(tempA[1][0]==game.source[0]&&tempA[1][1]==game.source[1]){///���_��X�o�I
						break;
					}
					temp=tempA[1];			
				}


				Message msg1 = myHandler.obtainMessage(1, count);//����TextView��r
				myHandler.sendMessage(msg1);//�o�eHandler�T��
			}			
		}
		else if(
			mySpinner.getSelectedItemId()==3||
			mySpinner.getSelectedItemId()==4
		){//"Dijkstra"ø�s
		    if(game.pathFlag){
		    	HashMap<String,ArrayList<int[][]>> hmPath=game.hmPath;
		    	ArrayList<int[][]> alPath=hmPath.get(game.target[0]+":"+game.target[1]);
				for(int[][] tempA:alPath){
					paint.setColor(Color.BLACK);
					paint.setStyle(Style.STROKE);
					paint.setStrokeWidth(2);				    
					canvas.drawLine(	
						tempA[0][0]*(span+1)+span/2+fixMapData,tempA[0][1]*(span+1)+span/2+fixMapData,
						tempA[1][0]*(span+1)+span/2+fixMapData,tempA[1][1]*(span+1)+span/2+fixMapData, 
						paint
					);			
					postInvalidate();
				}

				Message msg1 = myHandler.obtainMessage(1, alPath.size());//����TextView��r
				myHandler.sendMessage(msg1);
		    }
		    
		    
		}
		//ø�s�X�o�I
		canvas.drawBitmap(source, fixMapData+game.source[0]*(span+1), fixMapData+game.source[1]*(span+1), paint);
		//ø�s�ؼ��I
		canvas.drawBitmap(target, fixMapData+game.target[0]*(span+1), fixMapData+game.target[1]*(span+1), paint);
		
		
		//William Added
		onDrawText(canvas);
		
		if (drawCircleFlag == true)
		{
			DrawOrigin(canvas);
		}
		else if (drawLastCircle)
		{
			
			int [][] tempA = pathQueue.get(0);
			//DrawOrigin(canvas);
			
			paint.setColor(Color.WHITE);
			canvas.drawCircle(	
				tempA[1][0]*(span+1)+span/2+fixMapData,tempA[1][1]*(span+1)+span/2+fixMapData,span/2, 
				paint
				);
		
		
			paint.setColor(Color.RED);
			canvas.drawCircle(	
				tempA[0][0]*(span+1)+span/2+fixMapData,tempA[0][1]*(span+1)+span/2+fixMapData,span/2, 
				paint
				);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		//Log.i("william","test");
		
        if (event.getAction() == MotionEvent.ACTION_DOWN  ) {
            touchX = (int)event.getX();
            touchY = (int)event.getY();
            tempwidth = touchX - x;
            tempheight = touchY -y;
            
			int i = 0, j = 0;
			
            int[] pos = getPos(event);//®Ú¾Ú®y¼Ð´«ºâ¦¨©Ò¦bªº¦æ©M¦C
			i = pos[0];
			j = pos[1];
            
            MapList.target[0][0] = i;
            MapList.target[0][1] = j;
           // XMPPSet.XMPPSendText("james1", "direction left");
            //Map.target
            chk = true;
  
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            chk = false;
        }
        return true;

	}
	
	
	public int[] getPos(MotionEvent e){//±N®y¼Ð´«ºâ¦¨°}¦Cªººû¼Æ
		int[] pos = new int[2];
		double x = e.getX();//±o¨ìÂIÀ»¦ì¸mªºx®y¼Ð
		double y = e.getY();//±o¨ìÂIÀ»¦ì¸mªºy®y¼Ð
		if(x>4 && y>4 && x<326 && y<321){//ÂIÀ»ªº¬O´Ñ½L®É
//			pos[0] = Math.round((float)((y-21)/36));//¨ú±o©Ò¦bªº¦æ
//			pos[1] = Math.round((float)((x-21)/35));//¨ú±o©Ò¦bªº¦C
			pos[0] = Math.round((float)((x-8)/14));//¨ú±o©Ò¦bªº¦C
			pos[1] = Math.round((float)((y-8)/14));//¨ú±o©Ò¦bªº¦æ
		}
		else{//ÂIÀ»ªº¦ì¸m¤£¬O´Ñ½L®É
			pos[0] = -1;//±N¦ì¸m³]¬°¤£¥i¥Î
			pos[1] = -1;
		}
		return pos;//±N®y¼Ð°}¦Cªð¦^
	}
	
	public class ShowThread implements Runnable {

		Canvas canvas;
		// flag = false;
		int span = 20;

		public ShowThread() {
			flag = true;

		}

		public void run() {

			while (flag) {
				
				try {
					synchronized (inStr) {
						
						postInvalidate();
						//invalidate();
						//canvas = holder.lockCanvas();
						// onDraw(canvas);
						// onDrawText(canvas);
						//repaint(canvas);

						// holder.unlockCanvasAndPost(canvas);
						Thread.sleep(span);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	public void SendCommand(String inStr) throws InterruptedException
	{

		if (inStr.equals("left")) {
			for (int i = 0; i < 50; i++) {
				try {
					synchronized (XMPPSet) {
						XMPPSet.XMPPSendText("james1", "direction " + inStr);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Thread.sleep(30);
			}
			
			for (int i = 0; i < 50; i++) {
				try {
					synchronized (XMPPSet) {
						XMPPSet.XMPPSendText("james1", "direction " + "forward");

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Thread.sleep(30);
			}
			
		} 
		else if (inStr.equals("right"))
		{
			for (int i = 0; i < 50; i++) {
				try {
					synchronized (XMPPSet) {
						XMPPSet.XMPPSendText("james1", "direction " + inStr);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Thread.sleep(30);
			}
			
			for (int i = 0; i < 50; i++) {
				try {
					synchronized (XMPPSet) {
						XMPPSet.XMPPSendText("james1", "direction " + "forward");

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Thread.sleep(30);
			}
			
		} 
		else if (inStr.equals("forLeft"))
		{
			for (int i = 0; i < 50; i++) {
				try {
					synchronized (XMPPSet) {
						XMPPSet.XMPPSendText("james1", "direction " + inStr);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Thread.sleep(30);
			}
			
			for (int i = 0; i < 50; i++) {
				try {
					synchronized (XMPPSet) {
						XMPPSet.XMPPSendText("james1", "direction " + "forward");

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Thread.sleep(30);
			}
			
		} 
		else {
			for (int i = 0; i < 108; i++) {
				try {
					synchronized (XMPPSet) {
						XMPPSet.XMPPSendText("james1", "direction " + inStr);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Thread.sleep(30);

			}
		}
		
	}
	
	public void RobotStart() throws InterruptedException
	{
		Log.i("william", "Robot thread running");
		
		if (game.pathFlag == true)
		{
		
			drawCircleFlag = true;
			drawLastCircle = false;
			flag = false; // Stop onDraw
			over2Grid = false;
			for (int i=pathQueue.size() - 1;i > 0 ;i--)
			{
				int[][] axis = pathQueue.get(i);
				int[][] old_axis = new int[2][2];
				if ( (old_axis = pathQueue.get(pathQueue.size() - 2)) != null )
					over2Grid = true;
				int oX =axis[1][0] , oY = axis[1][1];
				int nX =axis[0][0] , nY = axis[0][1];
				
				Log.i(TAG,"oX = " + oX + " oY = " + oY + " nX = " + nX + " nY = " + nY);
				// Move on horizontal direction
				int old_dx = oX - old_axis[1][0];
				int old_dy = oY - old_axis[1][1];
				int dx = nX - oX;
				int dy = nY - oY;
				if (dx == 0 && dy == 1)
				{
					
					//for (int count=0; count < (nY - oY) ; count++)
					if (doubleCmd != true)
						SendCommand("forward");
					else
					{
						SendCommand("right");
						doubleCmd = false;
					}
				}
				else if (dx == 0 && dy == -1)
				{
					
					SendCommand("backward");
				}
				else if (dx == 1 && dy == 0)
				{
					SendCommand("left");
					//SendCommand("right");
				}
				else if (dx == -1 && dy == 0)
				{
					SendCommand("right");
				}
				else if (dx == 1 && dy == 1)
				{
					//if(pe =1)
					//{
						if (old_dx == 1 && old_dy  == 1)
						{

								SendCommand("forward");
								doubleCmd = true;

								
						}
						else
							SendCommand("forLeft");
					//}
				}
				else if (dx == 1 && dy == -1)
				{
					if (old_dx == 1 && old_dy  == -1)
						SendCommand("forward");
					else
						SendCommand("forRig");
				}
				else if (dx == -1 && dy == -1)
				{
					if (old_dx == -1 && old_dy  == -1)
						SendCommand("forward");
					else
						SendCommand("bacRig");
				}
				else if (dx == -1 && dy == 1)
				{
					if (old_dx == -1 && old_dy  == 1)
						SendCommand("forward");
					else
						SendCommand("bacLeft");
				}

				//DrawOrigin(gcanvas);
				//invalidate();

				//drawCircleFlag = true;
				MapList.source[0] = nX;
				MapList.source[1] = nY;
				postInvalidate();
			}
			
			flag = true; // Start to update
			//RunThreadTouch(true);
			//canvas = holder.lockCanvas();
			// onDraw(canvas);
			// onDrawText(canvas);
			//repaint(canvas);
	
			// holder.unlockCanvasAndPost(canvas);
			flagR = false;
			//drawCircleFlag = false;
		}
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){//�мg����k�A��^���O��View���j�p
        setMeasuredDimension(VIEW_WIDTH,VIEW_HEIGHT);
    }
}