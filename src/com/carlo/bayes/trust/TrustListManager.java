package com.carlo.bayes.trust;

import java.util.ArrayList;
import java.util.List;

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
	private TrustList trustList;
	private AgentInformationManager agentInfo;
	private AbstractRole myRole;
	private int readTalkNum=0;
	
	/**  コンソール出力。この設定がTrustListにも反映される。on,offはここでいじる。 */
	private boolean isShowConsoleLog=true;
	public TrustListManager(List<Agent> agentList,AbstractRole myRole,AgentInformationManager agentInfo){
		this.agentInfo=agentInfo;
		this.myRole=myRole;
		trustList=new TrustList(agentList,myRole.getMe(),agentInfo);
		trustList.setShowConsoleLog(isShowConsoleLog);
	}
	/** AbstractRoleのdayStartの最後に呼ぶ */
	public void dayStart(){
		readTalkNum=0;
	}
	/** AbstractRoleのupdateの最後に呼ぶ */
	public void update(){
		readTalkList();
	}
	
	/** 信用度が低い順にソートされたリストを返す */
	public List<Agent> getSortedList(){
		return trustList.getSortedList();
	}
	/** 信用度が最低で生存しているエージェントを返す */
	public Agent getLowestTrustAliveAgent(){
		return trustList.getLowestTrustAgent();
	}
	public void printTrustList(){
		if(isShowConsoleLog) trustList.printTrustList();
	}
	public void printTrustList(GameInfo finishedGameInfo){
		if(isShowConsoleLog) trustList.printTrustList(finishedGameInfo);
	}
	
	/**
	 * 発言を読んで、必要があれば信用度の計算を行う
	 */
	private void readTalkList(){
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
				//霊能COが一人だけなら、霊能を真と仮定して各占いとラインが繋がっているかで信用度の計算
				if(agentInfo.countCoAgent(Role.MEDIUM)==1){
					Species inquestedResult=utterance.getResult();
					for(Agent voter:searchVoter(utterance.getTarget())){
						trustList.changeVoterTrust(voter, utterance.getResult());
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
	
	
	
	/** ある人に投票した人を探す 。複数回投票したら重複してリストに入る */
	private ArrayList<Agent> searchVoter(Agent votedAgent){
		ArrayList<Agent> voter=new ArrayList<Agent>();
		for(Vote vote:myRole.getLatestDayGameInfo().getVoteList()){
			if(vote.getTarget()==votedAgent){
				voter.add(vote.getAgent());
			}
		}
		return voter;

	}
}
