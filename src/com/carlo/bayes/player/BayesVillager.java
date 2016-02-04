package com.carlo.bayes.player;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.client.base.player.AbstractVillager;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.carlo.bayes.trust.TrustLevel;
import com.carlo.bayes.trust.TrustListManager;
import com.carlo.lib.AgentInformationManager;
import com.carlo.lib.CauseOfDeath;
/**
 *  動作:信用度が低いエージェントに投票するのみ。発言もしない。
 * @author carlo
 *
 */
public class BayesVillager extends AbstractVillager {
	
	protected AgentInformationManager agentInfo;
	protected TrustListManager trustListManager;
	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		agentInfo=new AgentInformationManager(gameInfo,this.getMe());
		trustListManager=new TrustListManager(gameInfo.getAgentList(), this, agentInfo);
	}
	@Override
	public void update(GameInfo gameInfo){
		//long start = System.currentTimeMillis();

		super.update(gameInfo);
		agentInfo.update(gameInfo);
		trustListManager.update();
		

		

		//long stop = System.currentTimeMillis();
		//if(stop - start>50) System.out.println("update 実行にかかった時間は " + (stop - start) + " ミリ秒です。");

	}

	@Override
	public void dayStart() {
		//long start = System.currentTimeMillis();
		
		agentInfo.dayStart();
		trustListManager.dayStart();
		trustListManager.printTrustList();
		
		
		//long stop = System.currentTimeMillis();
		//if(stop - start>50) System.out.println("daystart 実行にかかった時間は " + (stop - start) + " ミリ秒です。");
	}

	@Override
	public void finish() {
		//if(agentInfo.isAlive(getMe())){
			//trustListManager.printTrustListForCreatingData(getLatestDayGameInfo());
		//}
		//trustListManager.printTrustList(getLatestDayGameInfo());
	}

	@Override
	public String talk() {
		return TemplateTalkFactory.over();
	}

	@Override
	public Agent vote() {
		//if(getDay()<3) return trustListManager.getRoleCOAgent(TrustLevel.LOWEST, null, true);
		return trustListManager.getAgent(TrustLevel.LOWEST, true);
		
	}


}
