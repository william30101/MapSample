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
public class GameView extends View{
	
	private String TAG = "william";
	private static final int VIEW_WIDTH = 640;
	private static final int VIEW_HEIGHT = 640;
 
	Game game;
	GameView GV;
	Spinner mySpinner;// Spinner���ޥ�
	TextView CDTextView;
	int span = 16;
	int theta = 0;
	public boolean drawCircleFlag=false;

	Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.source);
	Bitmap target = BitmapFactory.decodeResource(getResources(), R.drawable.target)	;
	Paint paint = new Paint();
	
	
	// William Added
	int touchX=0,touchY=0;
	int x,y;
    int tempwidth=0;
    int tempheight=0;
    String inStr = "test";
    String inStr2 = "test2";
    int fixMapData = 5;
    int gridX = 0 , gridY = 0;
    int row = 0;
	int col = 0;
	Game gamejava = new Game();
	int drawBaseLine = 100 , drawIncrease = 20;
	
    public static int drawCount = 0; // For drawcircle position
    
    
    double rX = 0 , rY = 0;
    int[][] map;
    int[] old_pos;
    MapList maplist = new MapList();
    
    public boolean refreshFlag = false  , doubleCmd = false , algorithmDone = false;
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
	
	public void RunThreadTouch(boolean inFlag)
    {
		st = new ShowThread();
		refreshFlag = inFlag;
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
        canvas.drawText("Tx = " + touchX + " Ty = " + touchY ,380, drawBaseLine + drawIncrease, paint);
        canvas.drawText("tempX,Y : " + tempwidth + "," + tempheight, 380, drawBaseLine + drawIncrease * 2 , paint);
        canvas.drawText("GridX,Y : " + gridX + "," + gridY, 380, drawBaseLine + drawIncrease * 3, paint);
        canvas.drawText("RX : " + String.format("%.3f", rX) , 380, drawBaseLine + drawIncrease * 4, paint);
        canvas.drawText("RY : " + String.format("%.3f", rY), 380, drawBaseLine + drawIncrease * 5, paint);
        
	}
	
	
	// Draw robot position
	public void DrawRobotPosition(final Canvas canvas){

		// We get this from our self algorithm
		int[][] tempA = getPathQueue().get(drawCount);
		
		paint.setStyle(Style.FILL);
		paint.setColor(Color.RED);
		canvas.drawCircle(tempA[0][0] * (span + 1) + span / 2 + fixMapData,
				tempA[0][1] * (span + 1) + span / 2 + fixMapData, span / 2,
				paint);

		Log.i(TAG, "Draw Circle X , Y ( " + tempA[0][0] + " " + tempA[0][1]
				+ " )");
	}
	
	protected void onMyDraw(Canvas canvas){
		super.onDraw(canvas);
		
		canvas.drawColor(Color.GRAY);
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);
		//canvas.drawRect(5, 55, 325, 376, paint);
		map = game.map;
		//Log.i(TAG,"getting onMyDraw");
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

		ArrayList<int[][]> searchProcess=game.getSearchProcess();
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
			if(game.isPathFlag()){
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
		    if(game.isPathFlag()){
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
		
		//Log.i(TAG,"Draw source = "+ game.source[0] + " , " + game.source[1]);
		//Log.i(TAG,"Draw target = "+ game.target[0] + " , " + game.target[1]);
		
		//William Added
		onDrawText(canvas);
		
		if (drawCircleFlag == true)
		{
			DrawRobotPosition(canvas);
		}
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		//Log.i("william","test");
		
        if (event.getAction() == MotionEvent.ACTION_DOWN  ) {
        	
        	int pointerCount = event.getPointerCount();
        	
        	//Log.i(TAG," touch down pointer count = " + pointerCount);
        	
        	// Avoid thread competition , when user touch 2 points at the same time
        	// only one touch point can enter this scope.
        	if (pointerCount > 1 )
        		pointerCount = 1;
        	{
        		 for (int i = 0; i < pointerCount; i++) {
        			
		        	//RunThreadTouch(true);
		        	
		
					touchX = (int) event.getX();
					touchY = (int) event.getY();
					tempwidth = touchX - x;
					tempheight = touchY - y;
		
					int[] pos = getPosW(event);

					
					//Log.i(TAG,"Map pos[0] pos[1] = ( " + pos[0] + " , " + pos[1] + " )");
					
					//Draw Grid position on canvas
					gridX = pos[0];
					gridY = pos[1];
					
					//Setting net Target postion
					MapList.target[0][0] = pos[0];
					MapList.target[0][1] = pos[1];
					
					//Update Target bitmap position
					postInvalidate();
					
					//Log.i(TAG,"Thread ID = " + android.os.Process.myTid());
					
					// Avoid thread competition , when user touch 2 points at the same time
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
        		 }
        	}
        	
			//int[] pos = getPos(event);
			//i = pos[0];
			//j = pos[1];

			/*synchronized (MapList.target) {
				try {
					MapList.target[0][0] = i;
					MapList.target[0][1] = j;
					// XMPPSet.XMPPSendText("james1", "direction left");
					// Map.target
					chk = true;
// Avoid thread competition , when user touch 2 points at the same time
				} catch (Exception e) {
					e.printStackTrace();
				}

			}*/
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
        	//Log.i(TAG," touch up");
        	//RunThreadTouch(false);
        }
        return true;

	}
	
	
	public int[] getPos(MotionEvent e){//±N®y¼Ð´«ºâ¦¨°}¦Cªººû¼Æ
		int[] pos = new int[2];
		double x = e.getX();//±o¨ìÂIÀ»¦ì¸mªºx®y¼Ð
		double y = e.getY();//±o¨ìÂIÀ»¦ì¸mªºy®y¼Ð
		if(x>4 && y>4 && x<326 && y<321){//ÂIÀ»ªº¬O´Ñ½L®ÉrefreshFlag
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
			//Log.i(TAG,"( xPos , yPos ) = ( " + xPos + " , " + yPos + " )");
			
			//Avoid map object be used on onMyDraw function
			synchronized (map) { 
				try {
					if (map[yPos][xPos] == 0) {
						//Log.i(TAG, "draw on map[yPos][xPos]= "
						//		+ map[yPos][xPos] + "( xPos , yPos ) = ( "
						//		+ xPos + " , " + yPos + " )");
						pos[0] = xPos;
						pos[1] = yPos;
					} else {
						//Log.i(TAG, "can't draw on map[yPos][xPos]= "
						//		+ map[yPos][xPos] + "( xPos , yPos ) = ( "
						//		+ xPos + " , " + yPos + " )");
						pos[0] = -1;
						pos[1] = -1;
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		else{
			//pos[0] = MapList.target[0][0];
			//pos[1] = MapList.target[0][1];
			pos[0] = -1;
			pos[1] = -1;
		}
		return pos;
	}
	
	// Use this thread for update canvas information frequently
	// We don't use this now. 
	public class ShowThread implements Runnable {

		int delayTime = 50;

		public ShowThread() {
			refreshFlag = true;

		}

		public void run() {

			while (refreshFlag) {
				
				
					synchronized (inStr) {
						
						try {
						postInvalidate();
						//Log.i(TAG,"Thread ID = " + android.os.Process.myTid());
						
						// Avoid thread competition , when user touch 2 points at the same time
						Thread.sleep(delayTime); 
						} catch (Exception e) {
							e.printStackTrace();
						}
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
	
	public void PathQueueClear()
	{
		this.pathQueue.clear();
	}
	


}