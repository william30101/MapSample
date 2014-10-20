package wyf.ytl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	private static final int VIEW_WIDTH = 1300;
	private static final int VIEW_HEIGHT = 1000;
	Game game;
	Spinner mySpinner;// Spinner���ޥ�
	TextView CDTextView;
	int span = 45;

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
    int fixMapData = 5;
    int gridX = 0 , gridY = 0;
    
    boolean flag = false;
    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    public static ShowThread st;
	
	private Handler myHandler = new Handler(){//�Ψӧ�sUI�����
        public void handleMessage(Message msg) {
        	if(msg.what == 1){//���ܪ�ת�TextView����
        		CDTextView.setText("���|��סG" + (Integer)msg.obj);
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
	
	public void onDrawText(Canvas canvas)
	{
		//float[] position=lbx.getPosition(source[1], source[0]);
		//canvas.drawColor(Color.BLACK);
		//paint.setStyle(Style.FILL);
		//paint.setTextSize(40);
		paint.setARGB(255, 255, 0, 0);
		paint.setStyle(Style.STROKE);
		paint.setTextSize(20);
        canvas.drawText("Tx = " + touchX + " Ty = " + touchY ,1050, 500, paint);
        canvas.drawText("chk : " + chk, 1050, 520, paint);
        canvas.drawText("tempX,Y : " + tempwidth + "," + tempheight, 1050, 540, paint);
        canvas.drawText("GridX,Y : " + gridX + "," + gridY, 1050, 560, paint);
        
	}
	
	protected void onMyDraw(Canvas canvas){
		super.onDraw(canvas);
		

		
		canvas.drawColor(Color.GRAY);
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);
		canvas.drawRect(5, 5, 480, 580, paint);
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
					paint.setStyle(Style.STROKE);//�[��
					paint.setStrokeWidth(2);//�]�w�e���ʫ׬�2px 						    
					canvas.drawLine(	
						tempA[0][0]*(span+1)+span/2+fixMapData,tempA[0][1]*(span+1)+span/2+fixMapData,
						tempA[1][0]*(span+1)+span/2+fixMapData,tempA[1][1]*(span+1)+span/2+fixMapData, 
						paint
					);			
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
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.i("william","test");
		
        if (event.getAction() == MotionEvent.ACTION_DOWN  ) {
            touchX = (int)event.getX();
            touchY = (int)event.getY();
            tempwidth = touchX - x;
            tempheight = touchY -y;
            MapList.target[0][0] = touchX;
            MapList.target[0][1] = touchY;
            //Map.target
            chk = true;
            
            
        }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            chk = false;
        }
        return true;

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
						Log.i("william", "flag = " + flag);
						postInvalidate();
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
}