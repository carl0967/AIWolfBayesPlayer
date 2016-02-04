package com.carlo.starter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;
import org.aiwolf.client.base.smpl.SampleRoleAssignPlayer;
import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.util.CalendarTools;
import org.aiwolf.common.util.Pair;
import org.aiwolf.kajiClient.LearningPlayer.*;
import org.aiwolf.server.*;
import org.aiwolf.server.net.DirectConnectServer;
import org.aiwolf.server.net.GameServer;
import org.aiwolf.server.util.FileGameLogger;
import org.aiwolf.server.util.GameLogger;

import com.carlo.bayes.player.BayesPlayer;
import com.yy.player.*;

import jp.halfmoon.inaba.aiwolf.strategyplayer.StrategyPlayer;

/**
 * RoleRequestで何回も回す
 * Bayesエージェントの性能テスト用
 * @author carlo
 *
 */

public class DataPrintStarter {

	/**
	 * 参加エージェントの数
	 * 大会は15
	 * 10人で全役職あり
	 * 
	 * 結果ネットワーク生成に使ってる
	 */
	static protected int PLAYER_NUM = 15;

	/**
	 * 1回の実行で行うゲーム数
	 */
	 //static protected int GAME_NUM = 2000;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		
		
		int gameNum=1;
		String fileName="result72.arff";
		if(args.length>0){
			//第一引数：試行回数
			 gameNum=Integer.parseInt(args[0]);
			 fileName=args[1];
		}
		
		//村人側勝利数
		int villagerWinNum = 0;
		//人狼側勝利数
		int werewolfWinNum = 0;
		
		//人狼陣営予測が当たった数
		int correct=0;
		//人狼陣営予測数(分母)
		int max=0;
		
		//対戦相手クラス
		ArrayList<Class> classes=new ArrayList<Class>();
		classes.add(Class.forName("org.aiwolf.client.base.smpl.SampleRoleAssignPlayer"));
		classes.add(Class.forName("com.yy.player.YYRoleAssignPlayer"));
		classes.add(Class.forName("jp.halfmoon.inaba.aiwolf.strategyplayer.StrategyPlayer"));
		classes.add(Class.forName("org.aiwolf.kajiClient.LearningPlayer.KajiRoleAssignPlayer"));
		classes.add(Class.forName("com.gmail.jinro.noppo.players.RoleAssignPlayer"));
		classes.add(Class.forName("org.aiwolf.Satsuki.LearningPlayer.AIWolfMain"));
		classes.add(Class.forName("jp.ac.shibaura_it.ma15082.WasabiRoleAssignPlayer"));
		classes.add(Class.forName("takata.player.TakataRoleAssignPlayer"));
		classes.add(Class.forName("ipa.myAgent.IPARoleAssignPlayer"));
		classes.add(Class.forName("org.aiwolf.iace10442.ChipRoleAssignPlayer"));
		classes.add(Class.forName("kainoueAgent.MyRoleAssignPlayer"));
		classes.add(Class.forName("com.github.haretaro.pingwo.role.PingwoRoleAssignPlayer"));
		classes.add(Class.forName("com.gmail.the.seventh.layers.RoleAssignPlayer"));
		classes.add(Class.forName("jp.ac.cu.hiroshima.info.cm.nakamura.player.NoriRoleAssignPlayer"));
		
		
		//ファイル出力
		//PrintStream out = new PrintStream(fileName);
        //置き換える
       // System.setOut(out);
        
        //arffのヘッダー部分
        System.out.println("@RELATION result12\n"+
        		"@ATTRIBUTE trust {low,little_low,middle,little_high,high}\n"+
        		"@ATTRIBUTE co {null,VILLAGER,BODYGUARD,WEREWOLF,POSSESSED,SEER,MEDIUM}\n"+
        		"@ATTRIBUTE role {VILLAGER,BODYGUARD,WEREWOLF,POSSESSED,SEER,MEDIUM}\n"+
        		"@ATTRIBUTE seerCO {0,1,2,3,4,5}\n"+
        		"@ATTRIBUTE mediumCO {0,1,2,3,4}\n"+
        		"@DATA");

		for(int i = 0;i<gameNum;i++){
			Map<Player, Role> playerMap = new HashMap<Player, Role>();
			
			for(int j=0;j<PLAYER_NUM-1;j++){
				
				 //ランダムで追加
				int rnd=new Random().nextInt(classes.size());
				playerMap.put((Player) classes.get(rnd).newInstance(), null);
				
				//playerMap.put(new SampleRoleAssignPlayer(), null);
				//playerMap.put(new YYRoleAssignPlayer(), null);
				//playerMap.put(new StrategyPlayer(), null);
			}
		
			//playerMap.put(new SampleRoleAssignPlayer(), null);
			//playerMap.put(new YYRoleAssignPlayer(), null);	
			
				
			BayesPlayer myPlayer=new BayesPlayer();
			//RandomPlayer myPlayer=new RandomPlayer();
			//myPlayerは村人指定
			playerMap.put(myPlayer, Role.VILLAGER);

			GameServer gameServer = new DirectConnectServer(playerMap);
			GameSetting gameSetting = GameSetting.getDefaultGame(PLAYER_NUM);
			AIWolfGame game = new AIWolfGame(gameSetting, gameServer);
			game.setShowConsoleLog(false);

			game.setRand(new Random(gameSetting.getRandomSeed()));
			game.start();
			
			if(game.getWinner() == Team.VILLAGER){
				villagerWinNum++;
			}else{
				werewolfWinNum++;
			}

		}
		//System.out.println("正答数"+correct+"/"+max);
		//System.out.println("村人側勝利:" + villagerWinNum + " 人狼側勝利：" + werewolfWinNum);
	}
}
