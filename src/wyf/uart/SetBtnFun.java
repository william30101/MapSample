package wyf.uart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wyf.encoder.Encoder;
import wyf.ytl.Game;
import wyf.ytl.GameView;
import wyf.ytl.NetworkStatus;
import wyf.ytl.R;
import xmpp.XMPPSetting;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

public class SetBtnFun {

	private String TAG = "william";
	private boolean isNeedAdd = false ;
	private boolean startR = false;
	private XMPPSetting XMPPSet;
	private NetworkStatus loggin;
	private UartMsg uartCmd;
	//private UartReceive uartRec;
	private int alllen=0;
	File sdcard,file;
	private int[] wnum;
	GameView gameView;
	Game game;
	SendCmdToBoardAlgorithm SendAlgo;
	
	Button runBtn;
	
	ArrayList<double[]> axisData = new ArrayList<double[]>();
	
	private ExecutorService service = Executors.newFixedThreadPool(10);
	
	
	public void SetBtn(Activity v)
	{
		uartCmd = new UartMsg();
		loggin = NetworkStatus.getInstance();

		XMPPSet = new XMPPSetting();
		
		gameView = (GameView) v.findViewById(R.id.gameView);
		
		game = new Game();
		
		SendAlgo = new SendCmdToBoardAlgorithm();
		
		//uartRec = new UartReceive();
		//uartRec.RunRecThread();
		
		ImageButton backward= (ImageButton)v.findViewById(R.id.backward);
		ImageButton forward= (ImageButton)v.findViewById(R.id.forward);
		ImageButton left = (ImageButton)v.findViewById(R.id.left);
		ImageButton right= (ImageButton)v.findViewById(R.id.right);
		ImageButton stop = (ImageButton)v.findViewById(R.id.stop);
		ImageButton forRig= (ImageButton)v.findViewById(R.id.forRig);
		ImageButton forLeft = (ImageButton)v.findViewById(R.id.forLeft);
		ImageButton bacRig = (ImageButton)v.findViewById(R.id.bacRig);
		ImageButton bacLeft= (ImageButton)v.findViewById(R.id.bacLeft);
		
		Button saveBtn =  (Button)v.findViewById(R.id.saveBtn);
		Button sixForwardBtn =  (Button)v.findViewById(R.id.sixForwareBtn);
		runBtn =  (Button)v.findViewById(R.id.runBtn);
		
		backward.setOnTouchListener(ClickListener);
		forward.setOnTouchListener(ClickListener);
		left.setOnTouchListener(ClickListener);
		right.setOnTouchListener(ClickListener);
		stop.setOnTouchListener(ClickListener);
		forRig.setOnTouchListener(ClickListener);
		forLeft.setOnTouchListener(ClickListener);
		bacRig.setOnTouchListener(ClickListener);
		bacLeft.setOnTouchListener(ClickListener);
		
		saveBtn.setOnClickListener(onClickListener);
		sixForwardBtn.setOnClickListener(onClickListener);
		runBtn.setOnClickListener(onClickListener);


	}

	 private Button.OnClickListener onClickListener = new OnClickListener() {

			int btnMsg;

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btnMsg = v.getId();

				switch (btnMsg) {
				case R.id.runBtn:
						SendAlgo.RobotStart(gameView,game,XMPPSet);

					break;
				case R.id.sixForwareBtn:
					Log.i(TAG,"Forward six");
					
					
				for (int i = 0; i < 6; i++) {
					try {
						SendToBoard("direction forward");
						Thread.sleep(50);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
					
					break;
				
				case R.id.saveBtn:
					Log.i(TAG,"save btn");
					
					boolean sdCardExist = Environment.getExternalStorageState()   
		                    .equals(android.os.Environment.MEDIA_MOUNTED);
			    			
					
					if (sdCardExist)
					{
							Encoder enc = new Encoder();
			    			alllen = 0;
			    			
			    			sdcard = Environment.getExternalStorageDirectory();

			    			String dirc = sdcard.getParent();
			    			dirc = dirc + "/legacy";
			    			
			    			file = new File(dirc,"axisData.txt");
			    			Log.i(TAG," External storage path =" + dirc);
			    			
			    			 if (!file.exists())
			    			 {
								try {
									file.createNewFile();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
			    			 }
			    			 else
			    			 {
			    				 file.delete();
			    				 
			    				 try {
									file.createNewFile();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			    			 }
			    			
			    			String  x , y;

			    			BufferedWriter writer;
							try {
								writer = new BufferedWriter(new FileWriter(file, true /*append*/));
								
								
								axisData = enc.GetAxisQueue();
								for (int i=0;i<axisData.size();i++)
								{
									double[] da = axisData.get(i);
									x = new Double(da[0]).toString();
									y = new Double(da[1]).toString();
									writer.write("index = " + i +" x = "+x +"  y = "+y + "\r\n");
								}
				    			writer.close();
				    			enc.CleanAxisQueue();
				    			
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			    			   
					}

					//XMPPSet.XMPPSendText("james1", "stretch top");
					break;
				default:
					Log.i(TAG,"onClickListener not support");
					break;
				}
			}
		};
	
	private Button.OnTouchListener ClickListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//return gestureDetector.onTouchEvent(event);

			int eventAction = event.getAction();
			switch(eventAction){

				case MotionEvent.ACTION_DOWN:
					isNeedAdd = true;
					service.execute(new MyThread(v));
                   	//Runnable r = new MyThread(v);
                   	//new Thread(r).start();
					
					break;
				case MotionEvent.ACTION_UP:

					isNeedAdd = false;
				try {
					SendToBoard("stop stop");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					//XMPPSet.XMPPSendText("james1","stop stop");
					break;
			
				case MotionEvent.ACTION_MOVE:
				//	System.out.println("action move");
					break;
			default:

					break;
		}
			
			return false;
		}


  };
    
 
	
	private void SendToBoard(String inStr) throws IOException
	{
		//Log.i(TAG," loggin status = " + loggin.GetLogStatus());
		
		if (loggin.GetLogStatus())
			XMPPSet.XMPPSendText("james1", inStr);
		else
		{
			String[] inM = inStr.split("\\s+");
			byte[] cmdByte = uartCmd.GetAllByte(inM);
			String decoded = new String(cmdByte, "ISO-8859-1");
			UartMsg.SendMsgUart( 1, cmdByte);
		}
	}
	
	
	public class MyThread implements Runnable {

		   private View view;
		   String SendMsg;

			public MyThread(View v) {
				// store parameter for later user
				this.view = v;
			}

			public void run() {
				while (isNeedAdd) {
					// uiHandler.sendEmptyMessage(0);
					try {
						// Using SCTP transmit message

						// SendMsg = this.view.getTag().toString();
						SendMsg = view.getResources().getResourceName(view.getId());
						String sub = SendMsg.substring(SendMsg.indexOf("/") + 1);
						Log.i(TAG, "Send message" + sub);
						if (sub.equals("stop"))
							SendToBoard("stop stop");
							//XMPPSet.XMPPSendText("james1", "stop stop"); // Stop button be pressed.
						else
							SendToBoard("direction " + sub);
							//XMPPSet.XMPPSendText("james1", "direction " + sub);
						// XMPPSet.XMPPSendText("james1",sub+" test");
						// sctc.SctpSendData(sub);
						// comm.setMsg(this.view.getId(), 1);
						// start(service);
						Thread.sleep(100l);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
}
