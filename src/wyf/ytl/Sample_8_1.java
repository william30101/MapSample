package wyf.ytl;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
public class Sample_8_1 extends Activity {
	private static final String[] mySpinner_str = {//�j���U�ԲM�檺���e
		"深度","廣度","廣度*","Dijkstra","Dijkstra A*"
	}; 
	Spinner mySpinner;		//�j���U�Ԧ��M����
	Spinner targetSpinner;	//�ؼФU�Ԧ��M����
	Button goButton;		//�}�l���s
	GameView gameView;		//�ۤv��{���a��View
	TextView BSTextView;	//�ϥΨB�ƪ���r
	TextView CDTextView;	//���|��ת���r
	Game game;
	private ArrayAdapter<String> adapter;//�j���U�ԲM�檺�ҫ�
	private ArrayAdapter<String> adapter2;//�ؼФU�ԲM�檺�ҫ�
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mySpinner = (Spinner)findViewById(R.id.mySpinner);
        targetSpinner = (Spinner)findViewById(R.id.target);
        gameView = (GameView) findViewById(R.id.gameView);
        BSTextView = (TextView)findViewById(R.id.bushu);
        CDTextView = (TextView)findViewById(R.id.changdu);
        goButton = (Button) findViewById(R.id.go);
        game = new Game();//��l�ƺt��k���O
        //�s�طj���U�ԲM�檺�ҫ�
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mySpinner_str);
        String[] target_str = new String[MapList.target.length];//�ھڥؼ��I���ӼƷs�ؤ@�Ӱ}�C
        for(int i=0; i<MapList.target.length; i++){
        	target_str[i] = "Target"+i;
        }

        adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, target_str);
        mySpinner.setAdapter(adapter);//�]�w�ҫ�
        targetSpinner.setAdapter(adapter2);//�]�w�ҫ�
        goButton.setOnClickListener(//���s��ť��
        	new Button.OnClickListener(){
				public void onClick(View v) {
					game.runAlgorithm();//�I�s�B���k
					goButton.setEnabled(false);
				}
	        }
        );
        targetSpinner.setOnItemSelectedListener(//�ؼп�ܪ��U�ԲM���ť
        	new Spinner.OnItemSelectedListener(){
				public void onItemSelected(AdapterView<?> a, View v,int arg2, long arg3){
					game.target = MapList.target[arg2];
					game.clearState();//�Ngame�����A�M��
					gameView.postInvalidate();//�мgø�sgameView
				}
				public void onNothingSelected(AdapterView<?> arg0){
				}
        	}
        );
        mySpinner.setOnItemSelectedListener(//�t��k��ܪ��U�ԲM���ť
            	new Spinner.OnItemSelectedListener(){
    				public void onItemSelected(AdapterView<?> ada, View v,int arg2, long arg3){
    					game.clearState();//�Ngame�����A�M��
    					game.algorithmId =  (int) ada.getSelectedItemId();//�o���ܪ��t��kID
    					gameView.postInvalidate();//�мgø�sgameView
    				}
    				public void onNothingSelected(AdapterView<?> arg0) {
    				}
            	}
         );
        
        gameView.RunThreadTouch(true);
        
        this.initIoc();//�I�s�̿�`�J��k
    }
    public void initIoc()
    {//�̿�`�J
    	gameView.game = this.game;
    	gameView.mySpinner = this.mySpinner;
    	gameView.CDTextView = this.CDTextView;
    	game.gameView = this.gameView;
    	game.goButton = this.goButton;
    	game.BSTextView = this.BSTextView;
    }
}
