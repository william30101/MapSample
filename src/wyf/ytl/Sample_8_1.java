package wyf.ytl;

import wyf.encoder.Encoder;
import wyf.uart.SetBtnFun;
import xmpp.XMPPSetting;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
public class Sample_8_1 extends Activity {
	private static final String[] mySpinner_str = {
		"深度","廣度","廣度*","Dijkstra","Dijkstra A*"
	}; 
	Spinner mySpinner;		
	Spinner targetSpinner;	
	Button goButton;
	Button runButton;
	GameView gameView;		
	TextView BSTextView;	
	TextView CDTextView;	
	Game game;
	Encoder encoder;
	
	Button jamesSignButton,williamSignButton;
	
	private NetworkStatus loggin;
	
	private String mName;	//For XMPP thread user name
	private String mPass;	//For XMPP thread user password
	
	private XMPPSetting XMPPSet;
	public Thread XMPPThreadv = new XMPPThread(); 
	
	private String TAG = "william";
	
	private SetBtnFun setBtn = new SetBtnFun();
	
	EditText nameText;
	
	
	
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapter2;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mySpinner = (Spinner)findViewById(R.id.mySpinner);
        targetSpinner = (Spinner)findViewById(R.id.target);
        gameView = (GameView) findViewById(R.id.gameView);
        BSTextView = (TextView)findViewById(R.id.bushu);
        CDTextView = (TextView)findViewById(R.id.changdu);
        goButton = (Button) findViewById(R.id.go);
        runButton = (Button) findViewById(R.id.runBtn);
        jamesSignButton =  (Button) findViewById(R.id.jamesSignBtn);
        williamSignButton =  (Button) findViewById(R.id.williamSignBtn);

        //For demo test
        signin("william1");
        runButton.setEnabled(false);
        game = new Game();//��l�ƺt��k���O
        //�s�طj���U�ԲM�檺�ҫ�
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mySpinner_str);
        String[] target_str = new String[MapList.target.length];
        for(int i=0; i<MapList.target.length; i++){
        	target_str[i] = "Target"+i;
        }

        adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, target_str);
        mySpinner.setAdapter(adapter);
        targetSpinner.setAdapter(adapter2);
        goButton.setOnClickListener(
        	new Button.OnClickListener(){
				public void onClick(View v) {
					game.runAlgorithm();
					
					goButton.setEnabled(false);
				}
	        }
        );
        targetSpinner.setOnItemSelectedListener(
        	new Spinner.OnItemSelectedListener(){
				public void onItemSelected(AdapterView<?> a, View v,int arg2, long arg3){
					game.target = MapList.target[arg2];
					game.clearState();
					gameView.postInvalidate();
				}
				public void onNothingSelected(AdapterView<?> arg0){
				}
        	}
        );
        mySpinner.setOnItemSelectedListener(
            	new Spinner.OnItemSelectedListener(){
    				public void onItemSelected(AdapterView<?> ada, View v,int arg2, long arg3){
    					game.clearState();
    					game.algorithmId =  (int) ada.getSelectedItemId();
    					gameView.postInvalidate();
    				}
    				public void onNothingSelected(AdapterView<?> arg0) {
    				}
            	}
         );
        

        //Start Encoder r/w thread here
        encoder = new Encoder(gameView);
        
        williamSignButton.setOnClickListener(onClickListener);
        jamesSignButton.setOnClickListener(onClickListener);
        //if (nameText.getText() != null)
        //	mName = nameText.getText().toString();
       // mPass = "0000";
        //loggin = NetworkStatus.getInstance();
		//XMPPSet = new XMPPSetting();
		//XMPPThreadv.start();
		 
		setBtn.SetBtn(this);
		
        this.initIoc();//�I�s�̿�`�J��k
    }
    
    public void signin(String name)
    {
			mName = name;
			mPass = "0000";

			loggin = NetworkStatus.getInstance();
			XMPPSet = new XMPPSetting();
			XMPPThreadv.start();
    }
    
	 private Button.OnClickListener onClickListener = new OnClickListener() {

			int btnMsg;

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btnMsg = v.getId();

				switch (btnMsg) {
				
				case R.id.jamesSignBtn:
					Log.i(TAG,"sign in");
					signin("james1");
					
					
					//XMPPSet.XMPPSendText("james1", "stretch top");
					break;
				case R.id.williamSignBtn:
					Log.i(TAG,"sign in");
					signin("william1");
					
					
					
					//XMPPSet.XMPPSendText("james1", "stretch top");
					break;
				default:
					Log.i(TAG,"onClickListener not support");
					break;
				}
			}
		};
    
    class XMPPThread extends Thread {
		 
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            try {
            	Log.i(TAG,"Name = "+mName + " Pass = "+mPass);
            	loggin.SetLogStatus(XMPPSet.XMPPStart(mName,mPass));
            	
            	
            	if (loggin.GetLogStatus())
            	{
            		Log.i(TAG,mName + " Loggin successful");
            		//Sendmsg("ok");
            	}
            	else
            	{
            		Log.i(TAG,mName + " Loggin Fail");
            		//Sendmsg("Loggin Fail");
            	}

            } catch (Exception e) {
                 e.printStackTrace();
            }
        }

    }
    
    public void initIoc()
    {//�̿�`�J
    	gameView.game = this.game;
    	gameView.mySpinner = this.mySpinner;
    	gameView.CDTextView = this.CDTextView;
    	game.gameView = this.gameView;
    	game.goButton = this.goButton;
    	game.BSTextView = this.BSTextView;
    	game.runButton = this.runButton;
    }
}
