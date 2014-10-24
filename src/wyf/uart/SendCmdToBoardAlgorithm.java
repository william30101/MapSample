package wyf.uart;

import java.util.ArrayList;

import wyf.ytl.Game;
import wyf.ytl.GameView;
import wyf.ytl.MapList;
import xmpp.XMPPSetting;
import android.util.Log;

public class SendCmdToBoardAlgorithm {

	private String TAG = "william";
	
	private static XMPPSetting XMPPSet;
	
	int nextX = 0 , nextY = 0;
	int originalX = 0, originalY = 0;
	MapList mapList;
	
	ArrayList<int[][]> pathQ = new ArrayList<int[][]>();
	//GameView gameView;
	
	public void SendCommand(String inString) {
		// TODO Auto-generated method stub
		Log.i(TAG," Send command = " + inString);
		
		
				if (inString.equals("left") || inString.equals("right")) {
					for (int i = 0; i < 50; i++) {

						synchronized (XMPPSet) {
						try {
								XMPPSet.XMPPSendText("james1", "direction " + inString);
								Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
						/*try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
						}
					}
					for (int i = 0; i < 50; i++) {
						synchronized (XMPPSet) {
						try {
								XMPPSet.XMPPSendText("james1", "direction " + "forward");
								Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
						/*try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
						}
					}
				} else {
					for (int i = 0; i < 100; i++) {
						synchronized (XMPPSet) {
							try {
									XMPPSet.XMPPSendText("james1", "direction " + inString);
									Thread.sleep(50);
							} catch (Exception e) {
								e.printStackTrace();
							}
					
						}
					}
				}
	}
	
	
	public String FindDirection(int inTheta)
	{
		String direction = "forward";
		
		if (inTheta == 0)
		{
			direction = "forward";
		}
		if (inTheta == 45)
		{
			direction = "forRig";
		}
		if (inTheta == -45)
		{
			direction = "forLeft";
		}
		else if (inTheta == 90)
		{
			direction = "right";
		}
		else if (inTheta == 90)
		{
			direction = "left";
		}
		else if (inTheta == 135)
		{
			direction = "bacRig";
		}
		else if (inTheta == 135)
		{
			direction = "bacLeft";
		}
		else if (inTheta == 180)
		{
			direction = "backward";
		}
		else if (inTheta == 225)
		{
			direction = "bacLeft";
		}
		else if (inTheta == 225)
		{
			direction = "bacRig";
		}
		else if (inTheta == 270)
		{
			direction = "left";
		}
		else if (inTheta == -270)
		{
			direction = "right";
		}
		else if (inTheta == 315)
		{
			direction = "forLeft";
		}
		else if (inTheta == -315)
		{
			direction = "forRig";
		}
		
		return direction;
	}
	
	public int FindCompass(int dx , int dy)
	{
		int compass = 0;
		if (dx == 0 && dy == 1) {
			compass = 0;
			//SendCommand("forward");
		} else if (dx == 1 && dy == -1) {
			compass = 45;
			//SendCommand("forRig");
		} else if (dx == -1 && dy == 0) {
			compass = 90;
			//SendCommand("right");
		} else if (dx == -1 && dy == -1) {
			compass = 135;
			//SendCommand("bacRig");
		} else if (dx == 0 && dy == -1) {
			compass = 180;
			//SendCommand("backward");
		} else if (dx == -1 && dy == 1) {
			compass = 225;
			//SendCommand("bacLeft");
		} else if (dx == 1 && dy == 0) {
			compass = 270;
			//SendCommand("left");
		} else if (dx == 1 && dy == 1) {
			compass = 315;
			//SendCommand("forLeft");
		}
		
		return compass;
	}
	
	public void RobotStart(final GameView gameView , final Game game , XMPPSetting inXMPPSet)
	{
		Log.i("william", "Robot thread running");

		XMPPSet = inXMPPSet;
		
		new Thread() {
			public void run() {

				if (gameView.algorithmDone == true) { // When user press Start , path cal done
					int old_dx , old_dy;
					gameView.drawCircleFlag = true;
					gameView.setDrawLastCircle(false);
					gameView.flag = false; // Stop onDraw
					//PathQueue[size - 1] == Start  . . .  PathQueue[0] == Target
					
					pathQ = gameView.getPathQueue();
					
					for (int i = pathQ.size() - 1; i >= 0; i--) {
						gameView.drawCount = i;
						gameView.postInvalidate();
						try {
							Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}

						int[][] axis = pathQ.get(i);
						int[][] old_axis = new int[2][2];

						//if ((old_axis = gameView.getPathQueue().get(
						//		gameView.getPathQueue().size() - 2)) != null)
						//	gameView.over2Grid = true;
						originalX = axis[1][0];
						originalY = axis[1][1];
						nextX = axis[0][0];
						nextY = axis[0][1];

						if (  i <  (pathQ.size() - 1) )
							old_axis = pathQ.get( i  + 1);
						else
						{
							// If move only 2 times , we don't have pre move data.
							old_axis[0][0] = nextX;
							old_axis[0][1] = nextY;
							old_axis[1][0] = originalX;
							old_axis[1][1] = originalY;
						}
						// Move on horizontal direction
						if (i <  (pathQ.size() - 1))
						{
							old_dx = originalX - old_axis[1][0];
							old_dy = originalY - old_axis[1][1];
						}
						else
						{
							// If first move , default forward direction.
							old_dx = 0;
							old_dy = 1;
						}

						Log.i(TAG, " ( oldX , oldY ) = (" + old_axis[1][0] + " , " + old_axis[1][1]+
								")  (oX , oY) = (" + originalX + " , " + originalY + 
								")  (nX , nY) = (" + nextX+ " , " + nextY + ")");
						
						int dx = nextX - originalX;
						int dy = nextY - originalY;
						
						int OriginalCompass = FindCompass(old_dx , old_dy);
						int nextCompass = FindCompass(dx , dy);
						
						int theta = nextCompass - OriginalCompass;
						
						Log.i(TAG, " OriginalCompass = " + OriginalCompass+
								" nextCompass = " + nextCompass + " \n theta = " + theta);
						
						String dir = FindDirection(theta);
						SendCommand(dir);
						
					}
					
					//game.target[0] = 1;
					//game.target[1] = 1;
					
					game.source[0] = originalX;
					game.source[1] = originalY;
					
					gameView.drawCircleFlag = false;
					gameView.setDrawLastCircle(true);
					gameView.RunThreadTouch(true); // Start update thread
					
					// Set new start position

					gameView.flagR = false;

				}
			}
		}.start();
	}

}
