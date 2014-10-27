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
	GameView GV;
	Spinner mySpinner;// Spinner���ޥ�
	TextView CDTextView;
	int span = 16;
	int theta = 0;
	public boolean drawCircleFlag=false;
	private boolean drawLastCircle= false;

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
    int row = 0;
	int col = 0;
	Game gamejava = new Game();
	
    public static int drawCount = 0; // For drawcircle position
    
    
    double rX = 0 , rY = 0;
    int[][] map;
    int[] old_pos;
    MapList maplist = new MapList();
    
    public boolean flag = false , flagR = false , doubleCmd = false , algorithmDone = false;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    public static ShowThread st;
	
    
    /*	[0] : Original position X
    	[1] : Original position Y
    	[2] : Next position X
    	[3] : Next position Y
    */
	private ArrayList<int[][] > pathQueue = new ArrayList<int[][] >();
	
	Canvas gcanvas;
	
	private Handler myHandler = new Handler(){
        public void handleMessage(Message msg) {
        	if(msg.what == 1){
        		CDTextView.setText("Step" + (Integer)msg.obj);
        		//game.pathFlag = false;
        		algorithmDone = true;
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
	
//	public void RunThreadTouch(boolean inFlag)
//    {
//		st = new ShowThread();
//    	flag = inFlag;
//    	//singleThreadExecutor.execute(st);
//    }
	
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

		int[][] tempA = getPathQueue().get(drawCount);
		
		paint.setStyle(Style.FILL);
		paint.setColor(Color.RED);
		canvas.drawCircle(tempA[0][0] * (span + 1) + span / 2 + fixMapData,
				tempA[0][1] * (span + 1) + span / 2 + fixMapData, span / 2,
				paint);

		Log.i(TAG, "Draw Circle X , Y ( " + tempA[0][0] + " " + tempA[0][1]
				+ " )");
		// BitmapManager.

		// drawCircleFlag = false;
		// drawLastCircle = true;

		// postInvalidate();
		// paint.setColor(Color.WHITE);
		// canvas.drawCircle(tempA[0][0]*(span+1)+span/2+fixMapData,
		// tempA[0][1]*(span+1)+span/2+fixMapData, span/2, paint);
	}
	
	protected void onMyDraw(Canvas canvas){
		super.onDraw(canvas);
		
		canvas.drawColor(Color.GRAY);
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);
		canvas.drawRect(5, 55, 325, 376, paint);
		map = game.map;
		row = map.length;
		col = map[0].length;
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
					if (algorithmDone == false)
					{
						
						int[][] saveData = {{tempA[0][0],tempA[0][1]},
										{tempA[1][0],tempA[1][1]}};
						getPathQueue().add(saveData);// Add correct path here.
					}
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
			//DrawOrigin(canvas);
			DrawOrigin(canvas);
		}
		else if (isDrawLastCircle())
		{
			
			int [][] tempA = getPathQueue().get(0);
			paint.setStyle(Style.FILL);				
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
        	
			touchX = (int) event.getX();
			touchY = (int) event.getY();
			tempwidth = touchX - x;
			tempheight = touchY - y;

			int i = 0, j = 0;

			int[] pos = getPosW(event);// ®Ú¾Ú®y¼Ð´«ºâ¦¨©Ò¦bªº¦æ©M¦C
			//int[] pos = getPos(event);
			i = pos[0];
			j = pos[1];

			synchronized (MapList.target) {
				try {
					MapList.target[0][0] = i;
					MapList.target[0][1] = j;
					// XMPPSet.XMPPSendText("james1", "direction left");
					// Map.target
					chk = true;

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
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
	
	
	public int[] getPosW(MotionEvent e){
		int[] pos = new int[2];
		double x = e.getX();
		double y = e.getY();

		
		///////////////////////////////////////////////////////////////
		// (col*(span+1)+fixMapData) = X total length                //
		// (row*(span+1)+fixMapData) = Y total length                //
		///////////////////////////////////////////////////////////////
		
		int xGridSize = (col*(span+1)+fixMapData) / col;
		int yGridSize = (row*(span+1)+fixMapData) / row;

		if (x > fixMapData && y > fixMapData
				&& x < (col * (span + 1) + fixMapData)
				&& y < (row * (span + 1) + fixMapData)) {

			int xPos = (int) x / xGridSize;
			int yPos = (int) y / yGridSize;
			if (map[yPos][xPos] == 0) {
				pos[0] = xPos;
				pos[1] = yPos;
			} else {
				pos[0] = MapList.target[0][0];
				pos[1] = MapList.target[0][1];
			}
		}
		else{
			pos[0] = MapList.target[0][0];
			pos[1] = MapList.target[0][1];
		}
		return pos;
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


	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){//�мg����k�A��^���O��View���j�p
        setMeasuredDimension(VIEW_WIDTH,VIEW_HEIGHT);
    }
	public ArrayList<int[][] > getPathQueue() {
		return pathQueue;
	}
	public void setPathQueue(ArrayList<int[][] > pathQueue) {
		this.pathQueue = pathQueue;
	}
	public boolean isDrawLastCircle() {
		return drawLastCircle;
	}
	public void setDrawLastCircle(boolean drawLastCircle) {
		this.drawLastCircle = drawLastCircle;
	}
	
	public void PathQueueClear()
	{
		this.pathQueue.clear();
	}
	


}