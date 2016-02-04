package com.carlo.question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import com.carlo.bayes.trust.Correct;
import com.carlo.bayes.trust.TrustListManager;
import com.carlo.lib.AIMAssister;
import com.carlo.lib.AbilityResult;
import com.carlo.lib.AgentInformationManager;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;

/**
 * 　ログで動作するように、TrustListManagerのメソッドをオーバーライドしたもの
 * @author carlo
 *
 */
public class TrustListMngForLog extends TrustListManager{
	private DayLog dayLog;
	public TrustListMngForLog(List<Agent> agentList, Agent myAgent, AgentInformationManager agentInfo) {
		super(agentList, myAgent, agentInfo);
		setShowConsoleLog(false);
	}
	public void dayStart(DayLog dayLog){
		this.dayLog=dayLog;
		super.dayStart();
		//trustList.getTrustPoint(Agent.getAgent(1));
	}
	@Override
	protected int getDay(){
		return dayLog.getDay();
	}
	/** ログからRoleMapを生成して、動作させる<br>
	 * isShowConsoleLogを無視して表示 */
	public void printTrustListForCreatingData(){
		HashMap<Agent,Role> roleMap=new HashMap();
		//map生成
		for(int i=1;i<=15;i++){
			roleMap.put(Agent.getAgent(i), dayLog.getStatus().get(i).getRole());
		}
		trustList.printTrustListForCreatingData(roleMap);
	}
	@Override
	protected void readTalkList(){
		dayLog.getTalk();
		//talkLog-List変換
		ArrayList<Talk> talkList=new ArrayList<Talk>();
		for(TalkLog talkLog:dayLog.getTalk()){
			if(talkLog.getTalkType()==TalkType.TALK){
				Talk talk=new Talk(talkLog.getIdx(),talkLog.getDay(),Agent.getAgent(talkLog.getAgentNo()),talkLog.getContent());
				talkList.add(talk);
			}
		}
		//後は一緒
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
				Agent seer=talk.getAgent();
				int day=talk.getDay();
				Species species=utterance.getResult();
				trustList.changeSeerTrust(seer,day, species, Correct.UNKNOWN,utterance.getTarget(),Correct.UNKNOWN);
				break;
			case INQUESTED:
				Agent medium=talk.getAgent();
				//霊能者のネットワーク計算
				trustList.changeMediumTrust(medium, utterance.getResult(), talk.getDay());
				
				//霊能COが一人だけなら、霊能を真と仮定して各占いとラインが繋がっているかで信用度の計算
				if(agentInfo.countCoAgent(Role.MEDIUM)==1){
					Species inquestedResult=utterance.getResult();
					//投票結果から計算
					for(Entry<Agent,Integer> voteEntry : agentInfo.searchVoterMap(utterance.getTarget()).entrySet()) {
						int voteDay=voteEntry.getValue();
						Agent voteAgent=voteEntry.getKey();
						Agent executedAgent=utterance.getTarget();
						boolean assist= (executedAgent==agentInfo.getVoteTarget(voteDay, voteAgent));
						trustList.changeVoterTrust(voteEntry.getKey(), utterance.getResult(),voteDay,String.valueOf(assist));
					}
					
					//霊媒先と一致する占い結果を探す
					for(AbilityResult abilityResult:AIMAssister.searchDivinedAgent(agentInfo, utterance.getTarget())){
						//まず前回の結果を戻す
						
						//if(isShowConsoleLog) System.out.println("back trust ");
						trustList.changeSeerTrust(abilityResult.getAgent(),abilityResult.getTalkedDay(), abilityResult.getSpecies(), Correct.UNKNOWN,true,utterance.getTarget(),Correct.UNKNOWN);
						
						//判明した結果を入れて計算
						Correct correct=Correct.UNKNOWN;
						if(inquestedResult==abilityResult.getSpecies()) correct=Correct.YES;
						else correct=Correct.NO;
						
						//if(isShowConsoleLog) System.out.println("recalc trust ");
						trustList.changeSeerTrust(abilityResult.getAgent(),abilityResult.getTalkedDay(), abilityResult.getSpecies(), correct,utterance.getTarget(),Correct.UNKNOWN);
						
					}
				}
				break;
			default:
				break;

			}
			readTalkNum++;
		}
	}

}
