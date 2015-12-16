package com.carlo.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;

/**
 *  Agentについて、死亡や役職のCO、能力結果発言、投票リストの情報を管理するクラス
 *  TODO:誰に投票する予定かを保持
 * @author carlo
 *
 */

public class AgentInformationManager {
	private GameInfo gameInfo;
	/** 自分を除く各エージェントがCOした役職 */
	private HashMap<Agent,Role> coRoleMap=new HashMap<Agent,Role>();
	/** エージェントの死亡日,死因マップ */
	private HashMap<Integer,HashMap<CauseOfDeath,Agent>> deadAgentMap=new HashMap<Integer,HashMap<CauseOfDeath,Agent>>();
	/** 各エージェントが発言した能力結果のリストのマップ */
	private HashMap<Agent,AbilityResultList> abilityResultListMap=new HashMap<Agent,AbilityResultList>();
	/** 各投票日ごとの投票リスト index:1が1日目の投票リスト。*/
	private ArrayList<List<Vote>> voteLists=new ArrayList<List<Vote>>();
	private int readTalkNum;
	
	private Agent myAgent;
	
	public AgentInformationManager(GameInfo gameInfo,Agent myAgent){
		this.gameInfo=gameInfo;
		this.myAgent=myAgent;
		for(Agent agent:gameInfo.getAgentList()){
			abilityResultListMap.put(agent, new AbilityResultList());
			if(agent==myAgent) continue;
			coRoleMap.put(agent,null);
		}
		
	}
	public void update(GameInfo gameInfo){
		this.gameInfo=gameInfo;
		readTalkList();
	}
	public void dayStart(){
		readTalkNum=0;
		//死亡者リストの追加
		int yesterday=gameInfo.getDay()-1;
		if(yesterday>=0){ 
			deadAgentMap.put(yesterday,new HashMap<CauseOfDeath,Agent>());	
			deadAgentMap.get(yesterday).put(CauseOfDeath.EXECUTED,gameInfo.getExecutedAgent());
			deadAgentMap.get(yesterday).put(CauseOfDeath.ATTACKED,gameInfo.getAttackedAgent());
		}
		if(gameInfo.getDay()!=0) voteLists.add(gameInfo.getVoteList());
		
	}
	/** 死亡日、死因から死んだエージェントを取得する。なければnull */
	public Agent getDeadAgent(int deadDay,CauseOfDeath cause){
		return deadAgentMap.get(deadDay).get(cause);
	}
	public int getDayAgentDied(Agent agent){
		if(isAlive(agent)) return -1;
		for(Entry<Integer, HashMap<CauseOfDeath, Agent>> entry : deadAgentMap.entrySet()) {
			for(Entry<CauseOfDeath, Agent> subEntry : entry.getValue().entrySet()){
				if(subEntry.getValue()==agent) return entry.getKey();
			}
		}
		return -1;
	}
	/**
	 * @param agnet
	 * @return agentが最後にCOした役職。なければnull
	 */
	public Role getCoRole(Agent agnet){
		return coRoleMap.get(agnet);
	}
	/**
	 * 
	 * @param coRole COした役職。nullならCOがない人を対象に。 
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return  条件に合うエージェントのリストを返す
	 */
	public List<Agent> getCoAgentList(Role coRole,boolean isAliveOnly){
		ArrayList<Agent> agents=new ArrayList<Agent>();
		for(Entry<Agent, Role> entry : coRoleMap.entrySet()) {
			if(isAliveOnly){
				if(isAlive(entry.getKey()) && entry.getValue()==coRole) agents.add(entry.getKey());
			}
			else{
				if(entry.getValue()==coRole) agents.add(entry.getKey());
			}
		}
		return agents;
	}
	/**
	 * 
	 * @param isAliveOnly 生存しているエージェントのみを対象とするか
	 * @return 条件に合うエージェントのリストを返す
	 */
	public List<Agent> getAgentList(boolean isAliveOnly){
		ArrayList<Agent> agents=new ArrayList<Agent>();
		for(Entry<Agent, Role> entry : coRoleMap.entrySet()) {
			if(isAliveOnly){
				if(isAlive(entry.getKey())) agents.add(entry.getKey());
			}
			else{
				 agents.add(entry.getKey());
			}
		}
		return agents;
	}
	/** 役職roleをCOしたエージェントの数を返す */
	public int countCoAgent(Role role){
		int count=0;
		for(Entry<Agent, Role> entry : coRoleMap.entrySet()) {
			if(entry.getValue()==role) count++;
		}
		return count;
	}
	public Agent getMyAgent(){
		return myAgent;
	}
	
	public void printDeadAgentMap(){
		for(int i=0;i<gameInfo.getDay();i++){
			HashMap<CauseOfDeath,Agent> map=deadAgentMap.get(i);
			System.out.println(i+"day attacked agent:"+map.get(CauseOfDeath.ATTACKED));
			System.out.println(i+"day executed agent:"+map.get(CauseOfDeath.EXECUTED));
		}
	}
	public void printCoRoleMap(){
		for(Entry<Agent, Role> entry : coRoleMap.entrySet()) {
			System.out.println(entry.getKey()+" "+entry.getValue());
		}
	}
	public void printAbilityResultList(){
		for(Entry<Agent, AbilityResultList> entry : abilityResultListMap.entrySet()) {
			System.out.print(entry.getKey());
			entry.getValue().printList();
			System.out.println();
		}
	}
	/** agentが生存しているかどうか */
	public boolean isAlive(Agent agent){
		if(gameInfo.getStatusMap().get(agent)==Status.ALIVE) return true;
		else return false;
	}
	/** targetに投票したエージェントを全て探して返す。複数回投票していたら、その回数分リストに入れる。 */
	public List<Agent> searchVoter(Agent target){
		ArrayList<Agent> voter=new ArrayList<Agent>();
		for(List<Vote> voteList:voteLists){
			for(Vote vote:voteList){
				if(vote.getTarget()==target){
					voter.add(vote.getAgent());
				}
			}
		}
		return voter;
	}
	/** targetに投票したことのあるエージェントを全て探して返す。複数回投票していたら、その回数分リストに入れる。
	 * @return Map<投票したことのあるエージェント,その日> */
	public Map<Agent,Integer> searchVoterMap(Agent target){
		//ArrayList<Agent> voter=new ArrayList<Agent>();
		HashMap<Agent,Integer> map=new HashMap<>();
		for(List<Vote> voteList:voteLists){
			for(Vote vote:voteList){
				if(vote.getTarget()==target){
					map.put(vote.getAgent(), vote.getDay());
				}
			}
		}
		return map;
	}
	/** day日にtargetに投票したエージェントの配列を返す */
	public List<Agent> searchVoter(int day,Agent target){
		ArrayList<Agent> voters=new ArrayList<Agent>();
		for(Vote vote:voteLists.get(day)){
			if(vote.getTarget()==target){
				voters.add(vote.getAgent());
			}
		}
		return voters;
	}
	/** agentがday日に投票したターゲットを返す */
	public Agent getVoteTarget(int day,Agent agent){
		for(Vote vote:voteLists.get(day)){
			if(vote.getAgent()==agent){
				return vote.getTarget();
			}
		}
		return null;
	}
	public List<List<Vote>> getVoteLists(){
		return voteLists;
	}
	
	public HashMap<Agent,AbilityResultList> getAbilityResultListMap(){
		return abilityResultListMap;
	}
	/**
	 * 発言を読んで、情報を格納
	 * COをしていなくても、能力結果発言を元にCO役職を入れる。
	 * 複数COは対応していない
	 */
	private void readTalkList(){
		List<Talk> talkList=gameInfo.getTalkList();
		for(int i=readTalkNum;i<talkList.size();i++){
			Talk talk=talkList.get(i);
			Utterance utterance=new Utterance(talk.getContent());
			//System.out.println(talk.getIdx()+" "+utterance.getTopic());
			Agent speaker=talk.getAgent();
			switch (utterance.getTopic()){
			case COMINGOUT:
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
