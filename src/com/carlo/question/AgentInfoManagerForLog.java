package com.carlo.question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;

import com.carlo.lib.AgentInformationManager;
import com.carlo.lib.CauseOfDeath;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;

/**
 * ログからの読み込みでAgentInformationMnagerを使えるようにするクラス
 * gameInfoを使わないようにメソッドをオーバーライド
 * 代わりにdayLogを使う
 * @author carlo
 *
 */

public class AgentInfoManagerForLog extends AgentInformationManager {
	private DayLog dayLog;

	public AgentInfoManagerForLog(ArrayList<Agent> agentList){
		super(agentList);
	}
	public void dayStart(DayLog dayLog){
		readTalkNum=0;
		this.dayLog=dayLog;
		
		int day=dayLog.getDay();
		HashMap<Integer,Vote> voteMap=dayLog.getVoteList();
		
		Agent executedAgent=null;
		if(dayLog.getExecute()!=null){
			executedAgent=Agent.getAgent(dayLog.getExecute().getExecuteAgentNo());
		}
		
		Agent attackedAgent=null;
		//System.out.println(day+"日"+dayLog.getAttack());
		//if(dayLog.getAttack()!=null) System.out.println(dayLog.getAttack().getTargetAgentNo());
		if(dayLog.getAttack()!=null && dayLog.getAttack().isSuccess()){
			attackedAgent=Agent.getAgent(dayLog.getAttack().getTargetAgentNo());
			
			
		}
		//死亡者リストの追加
		
		
		//int yesterday=day-1;
		if(day>=1){ 
			deadAgentMap.put(day,new HashMap<CauseOfDeath,Agent>());	
			deadAgentMap.get(day).put(CauseOfDeath.EXECUTED,executedAgent);
			deadAgentMap.get(day).put(CauseOfDeath.ATTACKED,attackedAgent);
		}
		if(day!=0){
			ArrayList<Vote> voteList=new ArrayList<Vote>();
			for (Entry<Integer, Vote> voteEntry : voteMap.entrySet()) {
				Vote vote=voteEntry.getValue();
				voteList.add(vote);
			}
			voteLists.add(voteList);
		}
	}
	/** agentが生存しているかどうか */
	public boolean isAlive(Agent agent){
		int agentIndex=agent.getAgentIdx();
		if(dayLog.getStatus().get(agentIndex).getStatus()==Status.ALIVE) return true;
		else return false;
	}
	public void update(ArrayList<TalkLog> talkLogs){
		for(int i=readTalkNum;i<talkLogs.size();i++){
			TalkLog talkLog=talkLogs.get(i);
			//ささやきは飛ばす
			if(talkLog.getTalkType()==TalkType.WHISPER) continue;
			//talk→talkLogにしてる
			Talk talk=new Talk(talkLog.getIdx(),talkLog.getDay(),Agent.getAgent(talkLog.getAgentNo()),talkLog.getContent());
			
			Utterance utterance=new Utterance(talk.getContent());
			//System.out.println(talk.getIdx()+" "+utterance.getTopic());
			Agent speaker=talk.getAgent();
			switch (utterance.getTopic()){
			case COMINGOUT:
				//System.out.println(talk.getAgent()+""+utterance.getTarget()+" "+utterance.getRole());
				coRoleMap.put(speaker,utterance.getRole());
				break;
			case DIVINED:
				if(coRoleMap.get(speaker)==null) coRoleMap.put(speaker, Role.SEER);
				abilityResultListMap.get(speaker).addAbilityResult(utterance.getTopic(), -1,talk.getDay(), speaker,utterance.getTarget(),utterance.getResult());
				break;
			case INQUESTED:
				if(coRoleMap.get(speaker)==null) coRoleMap.put(speaker, Role.MEDIUM);
				abilityResultListMap.get(speaker).addAbilityResult(utterance.getTopic(), -1,talk.getDay(), speaker,utterance.getTarget(),utterance.getResult());
				break;
			case GUARDED:
				if(coRoleMap.get(speaker)==null) coRoleMap.put(speaker, Role.BODYGUARD);
				abilityResultListMap.get(speaker).addAbilityResult(utterance.getTopic(), -1,talk.getDay(),speaker,utterance.getTarget(),utterance.getResult());
				break;
			default:
				break;

			}
			readTalkNum++;
		}
	}

}
