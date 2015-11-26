package com.carlo.bayes.player;

import java.util.List;

import org.aiwolf.client.base.player.AbstractSeer;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.carlo.bayes.trust.DataCorrecter;
import com.carlo.bayes.trust.TrustListManager;
import com.carlo.lib.AgentInformationManager;

public class BayesSeer extends AbstractSeer {

private int readTalkNum=0;
	
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
		readTalkList();
		agentInfo.update(gameInfo);
		trustListManager.update();
		
	}

	@Override
	public void dayStart() {
		agentInfo.dayStart();
		readTalkNum=0;
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
	@Override
	public Agent divine() {
		return trustListManager.getLowestTrustAliveAgent();
	}
	/** 性能テスト用 */
	public int getCorrectNum(){
		DataCorrecter dataCorrecter=new DataCorrecter(getMe());
		return dataCorrecter.start(trustListManager.getSortedList(), getLatestDayGameInfo());
	}
	private void readTalkList(){
		List<Talk> talkList=this.getLatestDayGameInfo().getTalkList();
		for(int i=readTalkNum;i<talkList.size();i++){
			Talk talk=talkList.get(i);
			Utterance utterance=new Utterance(talk.getContent());
			switch (utterance.getTopic()){
			case COMINGOUT:
				switch (utterance.getRole()){
				case MEDIUM:
					break;
				default:
					break;
				}
				break;
			case DIVINED:
				break;
			case INQUESTED:
				break;
			default:
				break;
			}
			readTalkNum++;
		}
	}

}
