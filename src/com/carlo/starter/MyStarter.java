package com.carlo.starter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.util.CalendarTools;
import org.aiwolf.kajiClient.LearningPlayer.*;
import org.aiwolf.server.*;
import org.aiwolf.server.net.DirectConnectServer;
import org.aiwolf.server.net.GameServer;
import org.aiwolf.server.util.FileGameLogger;
import org.aiwolf.server.util.GameLogger;

import com.carlo.bayes.player.BayesPlayer;
import com.yy.player.*;

public class MyStarter {

	/**
	 * 参加エージェントの数
	 * 大会は15
	 * 10人で全役職あり
	 */
	static protected int PLAYER_NUM = 10;

	/**
	 * 1回の実行で行うゲーム数
	 */
	static protected int GAME_NUM = 100;

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		//村人側勝利数
		int villagerWinNum = 0;
		//人狼側勝利数
		int werewolfWinNum = 0;

		for(int i = 0;i<GAME_NUM;i++){
			List<Player> playerList = new ArrayList<Player>();

			for(int j=0;j<PLAYER_NUM;j++){
				//playerList.add(new KajiRoleAssignPlayer()); //ここで作成したエージェントを指定
				//if(j%2==0) playerList.add(new YanagimatiRoleAssignPlayer());
				//else playerList.add(new KajiRoleAssignPlayer());
				//playerList.add(new YYRoleAssignPlayer());
				playerList.add(new BayesPlayer());
				//playerList.add(new TestPlayer());
			}
			

			GameServer gameServer = new DirectConnectServer(playerList);
			GameSetting gameSetting = GameSetting.getDefaultGame(PLAYER_NUM);

			AIWolfGame game = new AIWolfGame(gameSetting, gameServer);
			game.setShowConsoleLog(false);
			
			//ログ
			/*
		    String timeString = CalendarTools.toDateTime(System.currentTimeMillis()).replaceAll("[\\s-/:]", "");
			File logFile = new File(String.format("%s/aiwolfGame%s_%d.log", "./log/", timeString,villagerWinNum+werewolfWinNum));
			try {
				game.setGameLogger(new FileGameLogger(logFile));
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}*/



			game.setRand(new Random(gameSetting.getRandomSeed()));
			game.start();
			
			if(game.getWinner() == Team.VILLAGER){
				villagerWinNum++;
			}else{
				werewolfWinNum++;
			}
			/*
			System.out.println("print all role");
			for(Agent agent:game.getGameData().getAgentList()){
				Map<Agent, Role> map=game.getGameData().getGameInfo().getRoleMap();
				System.out.println(agent+""+map.get(agent));
			}
			*/


		}
		 System.out.println("村人側勝利:" + villagerWinNum + " 人狼側勝利：" + werewolfWinNum);
	}
}
