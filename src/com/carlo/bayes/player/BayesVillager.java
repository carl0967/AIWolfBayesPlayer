package com.carlo.bayes.player;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.client.base.player.AbstractVillager;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.carlo.bayes.trust.DataCorrecter;
import com.carlo.bayes.trust.TrustListManager;
import com.carlo.lib.AgentInformationManager;
import com.carlo.lib.CauseOfDeath;
/**
 *  動作:信用度が低いエージェントに投票するのみ。発言もしない。
 * @author carlo
 *
 */
public class BayesVillager extends AbstractVillager {
	
	private AgentInformationManager agentInfo;
	private TrustListManager trustListManager;
	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		agentInfo=new AgentInformationManager(gameInfo,this.getMe());
		trustListManager=new TrustListManager(gameInfo.getAgentList(), this, agentInfo);
	}
	@Override
	public void update(GameInfo gameInfo){
		super.update(gameInfo);
		agentInfo.update(gameInfo);
		trustListManager.update();
		
	}

	@Override
	public void dayStart() {
		agentInfo.dayStart();
		trustListManager.dayStart();
		trustListManager.printTrustList();
		
	}

	@Override
	public void finish() {
		trustListManager.printTrustList();
	}

	@Override
	public String talk() {
		return TemplateTalkFactory.over();
	}

	@Override
	public Agent vote() {
		return trustListManager.getLowestTrustAliveAgent();
	}
	
	/** 性能テスト用 */
	public int getCorrectNum(){
		DataCorrecter dataCorrecter=new DataCorrecter(getMe());
		return dataCorrecter.start(trustListManager.getSortedList(), getLatestDayGameInfo());
	}


}
