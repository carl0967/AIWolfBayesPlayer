package com.carlo.bayes.trust;

import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

/**
 *  ベイジアンネットワークによる予測がどれだけの精度かを確認するためのデータをとるクラス
 *  信用度が低い上位３人を見て、人狼か狂人か合っている数を見る
 *  AbstractRoleを継承したクラスでgetCorrectNum()メソッドを作ってそこで呼ぶ
 * @author carlo
 *
 */
public class DataCorrecter {
	/** 何位まで見るか */
	private int max=3;
	Agent me;
	public DataCorrecter(Agent me){
		this.me=me;
	}
	public int start(List<Agent> sortedList,GameInfo finishGameInfo){
		//自分が生きている時のみ数える
		if(finishGameInfo.getAliveAgentList().contains(me)==false) return -1;
		/*
		for(Agent agent:getLatestDayGameInfo().getAgentList()){
			Map<Agent, Role> map=getLatestDayGameInfo().getRoleMap();
			System.out.println(agent+""+map.get(agent));
		}
		*/
		int max=3;
		int correct=0;
		//trustList.printTrustList();
		
		//信用度が低い順に3人
		for(int i=0;i<max;i++){
			Agent target=sortedList.get(i);
			Map<Agent, Role> roleMap=finishGameInfo.getRoleMap();
			//人狼なら正解
			//System.out.println(i+"\t"+target+"\t"+roleMap.get(target));
			if(roleMap.get(target)==Role.WEREWOLF ){
				correct++;
			}
			
			//System.out.println(i+" "+sortedList.get(i));
		}
		return correct;
	}

}
