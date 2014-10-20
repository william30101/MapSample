package wyf.ytl;
import java.util.*;//引入相關套件
public class AStarComparator implements Comparator<int[][]>{	//實作了Comparator介面
	Game game;
	public AStarComparator(Game game){//建構式
		this.game=game;
	}
	public int compare(int[][] o1,int[][] o2){//比較方法
		int[] t1=o1[1];
		int[] t2=o2[1];
		int[] target=game.target;//得到目標點
		//直線物理距離
		int a=(t1[0]-target[0])*(t1[0]-target[0])+(t1[1]-target[1])*(t1[1]-target[1]);
		int b=(t2[0]-target[0])*(t2[0]-target[0])+(t2[1]-target[1])*(t2[1]-target[1]);
		//蒙地卡羅距離
		//int a=game.visited[o2[0][1]][o2[0][0]]+Math.abs(t1[0]-target[0])+Math.abs(t1[1]-target[1]);
		//int b=game.visited[o2[0][1]][o2[0][0]]+Math.abs(t2[0]-target[0])+Math.abs(t2[1]-target[1]);	
		return a-b;//返回差值
	}
	public boolean equals(Object obj){
		return false;
	}
}