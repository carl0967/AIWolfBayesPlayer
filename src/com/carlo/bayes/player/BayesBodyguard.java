package com.carlo.bayes.player;

import org.aiwolf.client.base.player.AbstractBodyguard;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.carlo.bayes.trust.TrustLevel;
import com.carlo.bayes.trust.TrustListManager;
import com.carlo.lib.AgentInformationManager;

public class BayesBodyguard extends AbstractBodyguard {
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
		trustListManager.printTrustList(getLatestDayGameInfo());
	}

	@Override
	public String talk() {
		return TemplateTalkFactory.over();
	}

	@Override
	public Agent vote() {
		return trustListManager.getAgent(TrustLevel.LOWEST, true);
	}
	@Override
	public Agent guard() {
		Agent agent=trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, Role.SEER, false);
		if(agentInfo.isAlive(agent)==false) agent=trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, null, true);
		return agent;
	}



}
