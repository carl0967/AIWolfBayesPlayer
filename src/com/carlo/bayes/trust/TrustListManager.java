package com.carlo.bayes.trust;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.client.base.player.AbstractRole;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;

import com.carlo.lib.AIMAssister;
import com.carlo.lib.AbilityResult;
import com.carlo.lib.AgentInformationManager;
import com.carlo.lib.CauseOfDeath;
/**
 * AbstractRoleのdayStart,updateメソッドの中で
 * このクラスのdayStart,updateを呼ぶこと
 * @author carlo
 *
 */
public class TrustListManager {
	protected TrustList trustList;
	protected AgentInformationManager agentInfo;
	private AbstractRole myRole;
	protected int readTalkNum=0;
	
	/**  コンソール出力。この設定がTrustListにも反映される。on,offはここでいじる。 */
	protected boolean isShowConsoleLog=false;
	public TrustListManager(List<Agent> agentList,AbstractRole myRole,AgentInformationManager agentInfo){
		this.agentInfo=agentInfo;
		this.myRole=myRole;
		trustList=new TrustList(agentList,myRole.getMe(),agentInfo);
		trustList.setShowConsoleLog(isShowConsoleLog);
	}
	/** 使わない(forLog用) */
	public TrustListManager(List<Agent> agentList,Agent myAgent,AgentInformationManager agentInfo){
		this.agentInfo=agentInfo;
		trustList=new TrustList(agentList,myAgent,agentInfo);
		trustList.setShowConsoleLog(false);
	}
	/** AbstractRoleのdayStartの最後に呼ぶ */
	public void dayStart(){
		//昨日の投票によって信頼度を上下
		if(getDay()>1){
			int yesterday=getDay()-1;
			Agent executedAgent=agentInfo.getDeadAgent(yesterday,CauseOfDeath.EXECUTED);
			for(Agent agent:agentInfo.getAgentList(true)){
				boolean assist= (executedAgent==agentInfo.getVoteTarget(yesterday, agent));
				trustList.changeVoterTrust(agent, null, yesterday, String.valueOf(assist));
			}
			
		}
		
		
		readTalkNum=0;
	}
	/** AbstractRoleのupdateの最後に呼ぶ */
	public void update(){
		readTalkList();
	}
	
	/**
	 * 
	 * @param  trustLevel 信用度の高さ。Highest or Lowest
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 条件に合うAgentを返す
	 */
	public Agent getAgent(TrustLevel  trustLevel,boolean isAliveOnly){
		List<Agent> sortedList= trustList.getSortedAgentList(isAliveOnly);
		if(sortedList.size()==0) return null;
		
		if(trustLevel==TrustLevel.LOWEST) return sortedList.get(0);
		else if(trustLevel==TrustLevel.HIGHEST) return sortedList.get(sortedList.size()-1);
		return null;
	}
	/**
	 * 
	 * @param  trustLevel 信用度の高さ。Highest or Lowest
	 * @param coRole COした役職。nullならCOがない人を対象に。
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 条件に合うAgentを返す
	 */
	public Agent getRoleCOAgent(TrustLevel trustLevel,Role coRole,boolean isAliveOnly){
		List<Agent> sortedList= trustList.getSortedRoleCOAgentList(coRole, isAliveOnly);
		if(sortedList.size()==0) return null;
		
		if(trustLevel==TrustLevel.LOWEST) return sortedList.get(0);
		else if(trustLevel==TrustLevel.HIGHEST) return sortedList.get(sortedList.size()-1);
		return null;
	}
	
	/**
	 * @param coRole COした役職。nullならCOがない人を対象に。
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 信用度が低い順にソートされたAgentのList
	 */
	public List<Agent> getSortedRoleCOAgentList(Role coRole,boolean isAliveOnly){
		return trustList.getSortedRoleCOAgentList(coRole,isAliveOnly);
	}
	/**
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 信用度が低い順にソートされたAgentのList
	 */
	public List<Agent> getSortedAgentList(boolean isAliveOnly){
		return trustList.getSortedAgentList(isAliveOnly);
	}
	

	/**
	 *  trustListManagerの isShowConsoleLog がtrueの場合のみログ出力を行う
	 */
	public void printTrustList(){
		if(isShowConsoleLog) trustList.printTrustList();
	}
	public void printTrustList(GameInfo finishedGameInfo){
		if(isShowConsoleLog) trustList.printTrustList(finishedGameInfo);
	}
	public void setShowConsoleLog(boolean isShowConsoleLog){
		this.isShowConsoleLog=isShowConsoleLog;
		trustList.setShowConsoleLog(isShowConsoleLog);
	}
	/** isShowConsoleLogを無視して表示 */
	public void printTrustListForCreatingData(GameInfo finishedGameInfo){
		trustList.printTrustListForCreatingData(finishedGameInfo);
	}
	protected int getDay(){
		return myRole.getDay();
	}
	
	/**
	 * 発言を読んで、必要があれば信用度の計算を行う
	 */
	protected void readTalkList(){
		List<Talk> talkList=myRole.getLatestDayGameInfo().getTalkList();
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
				
				trustList.changeSeerTrust(seer,day, species, Correct.UNKNOWN);
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
						
						if(isShowConsoleLog) System.out.println("back trust ");
						trustList.changeSeerTrust(abilityResult.getAgent(),abilityResult.getTalkedDay(), abilityResult.getSpecies(), Correct.UNKNOWN,true);
						
						//判明した結果を入れて計算
						Correct correct=Correct.UNKNOWN;
						if(inquestedResult==abilityResult.getSpecies()) correct=Correct.YES;
						else correct=Correct.NO;
						
						if(isShowConsoleLog) System.out.println("recalc trust ");
						trustList.changeSeerTrust(abilityResult.getAgent(),abilityResult.getTalkedDay(), abilityResult.getSpecies(), correct);
						
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
