package wyf.encoder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import wyf.uart.UartMsg;
import wyf.ytl.GameView;

import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

public class Encoder {

	
	
	
	private static String TAG = "Encoder";
	
	private byte[] askEncoderData = {0x53,0x06,0x0d,0x00,0x00,0x45};
	private int getNanoDataSize = 3 , getEncoderDataSize = 1 , beSentMessage = 13;
	
	private int nanoInterval = 100 , encoderWriteWiatInterval = 20 , encoderReadWaitInterval = 80 , combineInterval = 200;
	
	double D=11.83;
	double pi=3.14;
	double piD=(pi*D)/2,dt=0.2;
	double X1,Y1,dX,dY;
	double cosine,sine,VL,VR,V;
	double W;
	static double X0=0,Y0=0,initial=0,d_theta,theta1;
	private double DegToRad = 3.141592653/180;
	
	private boolean nanoOpend = false, encoderOpend = false , debug_msg = false , debugEncoder = false;
	
	byte [] ReByteEnco = new byte[11];
	
	private Handler handler = new Handler();
	Runnable rEncoder = new EncoderThreadPool();
	Runnable rWEncoder = new EncoderWriteThread();
	Runnable rREncoder = new EncoderReadThread();
	Runnable rCombine = new CombineThread();
	
	GameView gV ;

	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	private static EncoderCmd encoderCmd = new EncoderCmd();
	
	private static ArrayList<byte[]> encoderQueue = new ArrayList<byte[]>();
	
	private static ArrayList<double[]> AxisQueue = new ArrayList<double[]>();
	
	private byte[] encoderDebugData = {0x53,0x0d,0x02,0x00,0x30,0x01,0x00,0x30,0x00,0x20,0x45};
	
	private byte count = 0x30;
	
	boolean arduinoDebug = true;
	
	public Encoder (GameView inGV)
	{
		StartEncoderThread(); // Start encoder thread now
		gV = inGV;
		
	}
	
	public Encoder ()
	{
		
	}
	
	public class EncoderThreadPool implements Runnable {

		public void run() {

			//Log.i(TAG, "EncoderThreadPool");
			singleThreadExecutor.execute(rWEncoder);
			singleThreadExecutor.execute(rREncoder);

			handler.postDelayed(rEncoder, encoderWriteWiatInterval
					+ encoderReadWaitInterval);
		}

	}
	
	public void StartEncoderThread()
	{
		if (debugEncoder == true)
		{
			handler.postDelayed(rEncoder, encoderWriteWiatInterval + encoderReadWaitInterval);
			
			handler.postDelayed(rCombine, combineInterval);
		}
		else if (encoderOpend == false)
		{
			String openPort = null;
			if (arduinoDebug)
				openPort = "ttymxc2";
			else
				openPort = "ttymxc4";
			
			if (UartMsg.OpenSetUartPort(openPort) > 0)
			{
				Log.i(TAG,"Open " + openPort + " success");
				encoderOpend = true;
			
				//handler.postDelayed(rEncoder, encoderWriteWiatInterval + encoderReadWaitInterval);
				
				//handler.postDelayed(rCombine, combineInterval);
			}
			
		}
	}
	
	
	
	public class EncoderWriteThread implements Runnable {

		public void run() {

			// Log.i(TAG,"opend fd = " + uartCmd.GetDrivingOpend());

			if (encoderOpend == true) {
				// writeLock();
				if (debug_msg)
					Log.i(TAG, "Send Ask to Driving board");
				
				String ReStrEnco = null;
				try {
					ReStrEnco = new String(askEncoderData, "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				UartMsg.SendMsgUart(1, askEncoderData);

			}
			try {
				Thread.sleep(encoderWriteWiatInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// handler.postDelayed(rWEncoder, encoderWriteInterval);

		}
	}
	
	
	public class EncoderReadThread implements Runnable {

		public void run() {


				/*if (uartCmd.GetDrivingOpend() == false) {
					// Use UART1 for nanopan
					encFd = uartCmd.OpenSetUartPort("ttymxc0");
				}*/
				
				
				//Log.i(TAG,"encoder fd = " + encoderOpend);
				if (debugEncoder == true)
				{
					
					//encoderDebugData
					//increase L wheel data 
					//encoderDebugData[4] = count;
					
					//if (count < 0x80)
					//	count = (byte)(count + 0x10);
					//else
					//	count = 0x30;
					
					encoderCmd.SetDataByte(encoderDebugData);
					encoderQueue.add(encoderCmd.GetDataByte());
					
					
				}
				else if (encoderOpend == true) {
					
					//readLock();
					//ReStrEnco = ReceiveMsgUart(1);
					//Log.i(TAG,"encoder read running");
					//while(true);
					//handler.postDelayed(rREncoder, encoderReadInterval);

					ReByteEnco = UartMsg.ReceiveByteMsgUart(1);

					
				

						//Log.i(TAG,"encoder rec msg = " + ReByteEnco + " leng = " + ReByteEnco.length);
						//for(int i=0;i<ReByteEnco.length;i++)
						//	Log.i("wr","encoder data[ " + i + "] = " + ReByteEnco[i]);
						if (  ReByteEnco.length  ==  11 && ReByteEnco[0] == 0x53 &&  ReByteEnco[1] == 0x0d)
						{
							
							//Log.i(TAG,"Receive message = "+ ReStrEnco);
							//encoderCmd.SetByte(ReStrEnco);
							//byte [] test = encoderCmd.GetDataByte();
							if (debug_msg)
								Log.i(TAG,"Encoder Receive test[0] = "+ReByteEnco[0] + " test1 = "+ ReByteEnco[1] + " test2 = "+ ReByteEnco[2]+ " test3 = "+ ReByteEnco[3]+ 
									" test4 = "+ ReByteEnco[4] + " test5 = "+ ReByteEnco[5]);
							//Log.i("123", "test6 = "+ ReByteEnco[6]);
							// Add receive message from Driving Board
							
							encoderCmd.SetDataByte(ReByteEnco);
							//byte [] test = encoderCmd.GetDataByte();
							
							encoderQueue.add(encoderCmd.GetDataByte());
							
							 
							 //Log.i(TAG,"receive Data byte = " + Arrays.copyOfRange(ReByteEnco, 2, 10));
							 //encoderQueue.add(Arrays.copyOfRange(ReByteEnco, 2, 10));
							
							//Log.i(TAG,"receive msg = " + ReStrEnco);
	
							// view.append(ReStr);
							// scrollView.fullScroll(ScrollView.FOCUS_DOWN);
							// Arrays.fill(ReByteEnco, (byte)0x00);
							// ReStrEnco = null;
							
							try {
								Thread.sleep(encoderReadWaitInterval);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						//readUnLock();
					
				}

				//handler.postDelayed(rREncoder, encoderReadWaitInterval);
			

		}
	}
	
	
	
	public class CombineThread implements Runnable {

		public void run() {

			// Log.i(TAG,"encoderOpend = " + encoderOpend + "  nanoOpend = "
			// + nanoOpend );
			//Log.i(TAG, "nanoQueue.size() = " + nanoQueue.size()
			//		+ " encoderQueue.size() = " + encoderQueue.size());
			
			if ( encoderOpend == true  ||  debugEncoder == true) {
				// byte[] beSendMsg = new byte[beSentMessage];;

				if (debug_msg)
					Log.i(TAG, " encoderQueue.size() = " + encoderQueue.size());

				if (encoderQueue.size() >= getEncoderDataSize ) {

					// Arrays.fill(beSendMsg, (byte)0x00);

					
					ArrayList<byte[]> encoderData = getEncoderRange(encoderQueue,
							encoderQueue.size() - getEncoderDataSize,
							encoderQueue.size());

					double[] axisdata = new double[2];
					
					
					
					// Calculate nanopan data and encoder data here (java
					// layer).
					// Encoder data format
					// [L Polarity] [L2] [L1] [R polarity] [R2] [R1] [COM2] [COM1] [0x45]
					// Save to byte array beSendMsg[11]
					// ....................
					

					ArrayList<int[]> encoderDataQueue = new ArrayList<int[]>();
					byte[] encoByte = encoderData.get(0);
					int[] tempInt = new int[3]; // L Wheel , R Wheel , Compass
					for (int i=0;i<encoderData.size();i++)
					{
						tempInt[0]  = ( (encoByte[1] << 8) & 0xff00 | (encoByte[2] & 0xff));
						if (encoByte[0] == 2)
							tempInt[0] = -tempInt[0];
						
						tempInt[1]  = ( (encoByte[4] << 8) & 0xff00 | (encoByte[5] & 0xff));
						if (encoByte[3] == 2)
							tempInt[1] = -tempInt[1];
						
						tempInt[2]  = ( (encoByte[6] << 8) & 0xff00 | (encoByte[7] & 0xff));
						
						
						Log.i(TAG,"encoder data L=" + tempInt[0] + " R=" + tempInt[1] + " com = " + tempInt[2]);
						
						encoderDataQueue.add(tempInt);
					}
					
					
					
					///////////////////////////////
					
					//StateEquation(ReByteEnco[3],ReByteEnco[5],0);
					////////////////////////////////////////////////
					VL=((((double)tempInt[0]/6)*piD)/dt);
					VR=((((double)tempInt[1]/6)*piD)/dt);
					
					//VL=((((double)10/6)*piD)/dt);
					//VR=((((double)-10/6)*piD)/dt);
					
					V=(VL+VR)/60;
					W=(VR-VL)/22.26;//���ɰw���t�A�f�ɰw�����A�ثe�L�ϥ�

					//�ثe���]��J���׬��ثe�����H��쨤
					//d_theta=W*dt;
					//�L���׿�J�A�ϥ�W���B���
					d_theta=W*dt;
					theta1=initial+d_theta;
					initial=theta1;
					//�����׿�J�A�ثe���״�쥻����
					//d_theta=(double)ReByteEnco[6]-theta1;
					//theta1+=d_theta;
					cosine= Math.cos(theta1*DegToRad);
					sine= Math.sin(theta1*DegToRad);

					dX=(V*dt)*(cosine);
					dY=(V*dt)*(sine);

					X1=X0+dX;
					Y1=Y0+dY;

					X0=X1;
					Y0=Y1;
					////////////////////////////////////////////////
					
					gV.SetRobotAxis(X1,Y1);
					

					axisdata[0] = X1;
					axisdata[1] = Y1;
					AxisQueue.add(axisdata);
					Log.i("123","X1="+X1+",Y1="+Y1);

					
					///////////////////////////////
					
					
					
					//Combine(nanoData, encoderDataQueue);
					// .................

					// End
					//encoderCount = 0;
					//nanoCount = 0;
					//nanoQueue.clear();
					encoderQueue.clear();
					
					// One Output Here
					// SendMsgUart(beSendMsg.toString(),1);
				}

				
			}
			handler.postDelayed(rCombine, combineInterval);
		}
	}
	
	public static ArrayList<byte[]> getEncoderRange(ArrayList<byte[]> list, int start, int last) {

		ArrayList<byte[]> temp = new ArrayList<byte[]>();
		//Log.i(TAG,"encoder start = " + start + " last = " + last);
		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}

		return temp;
	}
	
	
	public static ArrayList<double[]> GetAxisQueue()
	{
		if (AxisQueue.size() > 0 )
			return AxisQueue;
		return null;
	}
	
	public void CleanAxisQueue()
	{
		if (AxisQueue.size() > 0 )
			AxisQueue.clear();
	}
}
