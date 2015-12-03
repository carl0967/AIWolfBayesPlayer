package com.carlo.bayes.trust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import com.carlo.bayes.lib.WekaBayesManager;
import com.carlo.lib.AgentInformationManager;
import com.carlo.lib.CauseOfDeath;

enum  Correct{
	YES,
	NO,
	UNKNOWN
}
/**
 * 各Agentに対する信用度のリスト
 * @author carlo
 *
 */

public class TrustList {
	/**
	 * Anget,そのAgentに対する信用度<br>
	 * 信用度 0~100 高いほど村人陣営だと思っている
	 */
	private HashMap<Agent,Double> trustMap=new HashMap<Agent,Double>();

	private WekaBayesManager seerBayes;
	private WekaBayesManager voterBayes;
	private WekaBayesManager attackedBayes;
	/**  コンソール出力 */
	private boolean isShowConsoleLog=false;

	private AgentInformationManager agentInfo;
	public TrustList(List<Agent> agents,Agent myAgent,AgentInformationManager agentInfo){
		this.agentInfo=agentInfo;
		for(Agent agent:agents){
			if(agent==myAgent) continue;
			//初期値は50
			trustMap.put(agent, 50.0);
		}

		//
		seerBayes=new WekaBayesManager("xml/seer.xml");
		voterBayes=new WekaBayesManager("xml/newvote2.xml");
		attackedBayes=new WekaBayesManager("xml/attacked2.xml");
	}


	/**
	 *  
	 * @param coRole COした役職。nullならCOがない人を対象に。
	 * @param isAliveOnly true:生存者のみ。false:生存・非生存問わない
	 * @return 信用度が低い順にソートされたAgentのList
	 * 必要
	 */
	public List<Agent> getSortedRoleCOAgentList(Role coRole,boolean isAliveOnly){
		List<Agent> sortedAgentList=new ArrayList<Agent>();

		for(Agent agent:agentInfo.getCoAgentList(coRole,isAliveOnly)){
			if(sortedAgentList.size()==0) sortedAgentList.add(agent);
			else{
				int i=0;
				for(Agent sortedAgent :sortedAgentList){
					if(trustMap.get(agent)<trustMap.get(sortedAgent)){
						sortedAgentList.add(i, agent);
						break;
					}
					i++;
				}
				if(!sortedAgentList.contains(agent)) sortedAgentList.add(agent);
			}
		}
		return sortedAgentList;
	}
	/**
	 * 
	 * @param isAliveOnly
	 * @return
	 */
	public List<Agent> getSortedAgentList(boolean isAliveOnly){
		List<Agent> sortedAgentList=new ArrayList<Agent>();

		for(Agent agent:agentInfo.getAgentList(isAliveOnly)){
			if(sortedAgentList.size()==0) sortedAgentList.add(agent);
			else{
				int i=0;
				for(Agent sortedAgent :sortedAgentList){
					if(trustMap.get(agent)<trustMap.get(sortedAgent)){
						sortedAgentList.add(i, agent);
						break;
					}
					i++;
				}
				if(!sortedAgentList.contains(agent)) sortedAgentList.add(agent);
			}
		}
		return sortedAgentList;
	}



	public double getTrustPoint(Agent agent){
		return trustMap.get(agent);
	}
	/**
	 * 死亡情報から信用度計算
	 * @param deadAgent 死んだエージェント
	 * @param cause 死因
	 * @param day 死んだ日にち
	 */
	public void deadAgent(Agent deadAgent,CauseOfDeath cause,int day){
		if(!trustMap.containsKey(deadAgent)) return;
		//襲撃されたらattackedネットワークから信用度を計算
		if(cause==CauseOfDeath.ATTACKED) {
			if(isShowConsoleLog)  System.out.println("calc trust based on dead:"+deadAgent+day+cause);
			attackedBayes.clearAllEvidenceWithCalc();
			//エビデンスがセットされたいない場合の確率を境界とする
			double threshold=attackedBayes.getMarginalProbability("team", "villager");
			
			attackedBayes.setEvidence("attacked","yes");
			Role deadCoRole=agentInfo.getCoRole(deadAgent);
			String convertRole=BayesConverter.convert(deadCoRole);
			attackedBayes.setEvidence("corole",convertRole);
			attackedBayes.calcMargin();
			
			double margin=attackedBayes.getMarginalProbability("team", "villager");
			changeTrust(deadAgent,margin,threshold,false);
		}
	}
	public void printTrustList(){
		if(isShowConsoleLog){
			System.out.println("\nAgent[番号]\t信用度\t生存\tCO");
			for(Entry<Agent, Double> entry : trustMap.entrySet()) {
				System.out.print(entry.getKey()+"\t");
				System.out.printf("%.3f",entry.getValue());
				System.out.print("\t"+agentInfo.isAlive(entry.getKey()));
				System.out.println("\t"+agentInfo.getCoRole(entry.getKey()));
			}
			System.out.println();
		}
	}
	public void printTrustList(GameInfo finishedGameInfo){
		if(isShowConsoleLog){
			System.out.println("\nAgent[番号]\t信用度\t生存\tCO\t役職");
			for(Entry<Agent, Double> entry : trustMap.entrySet()) {
				System.out.print(entry.getKey()+"\t");
				System.out.printf("%.3f",entry.getValue());
				System.out.print("\t"+agentInfo.isAlive(entry.getKey()));
				System.out.print("\t"+agentInfo.getCoRole(entry.getKey()));
				System.out.println("\t"+finishedGameInfo.getRoleMap().get(entry.getKey()));
			}
			System.out.println();
		}
	}
	/**
	 *  占い発言から信用度計算
	 * @param agent 占い師
	 * @param day 占い発言した日にち
	 * @param species 占い結果
	 * @param correct 占い結果が合ってたかどうか
	 */
	public void changeSeerTrust(Agent agent,int day,Species species,Correct correct){
		changeSeerTrust(agent,day,species,correct,false);
	}
	/**
	 * @param reverse 逆の計算をするかどうか。通常はfalse
	 */
	public void changeSeerTrust(Agent agent,int day,Species species,Correct correct,boolean reverse){
		if(isShowConsoleLog)  System.out.print("calc trust based on seer:");
		if(isShowConsoleLog) System.out.println(agent+" day:"+day+" species:"+species+" correct:"+correct+" reverse:"+reverse);
		
		if(!trustMap.containsKey(agent)) return;
		seerBayes.clearAllEvidence();
		seerBayes.setEvidence("day", BayesConverter.convert(day));
		seerBayes.calcMargin();
		//閾値。エビデンスを与えた場合にこれをどれだけ上回るか下回るかで信用度を上下させる
		double threshold=seerBayes.getMarginalProbability("seer_role", "seer");
		
		seerBayes.setEvidence("species",BayesConverter.convert(species));
		if(correct!=Correct.UNKNOWN) seerBayes.setEvidence("correct", BayesConverter.convert(correct));
		seerBayes.calcMargin();
		
		double margin=seerBayes.getMarginalProbability("seer_role", "seer");
		//信用度の変化
		changeTrust(agent,margin,threshold,reverse);
	}
	/** 
	 *  投票結果から信用度計算
	 * @param agent 投票者
	 * @param targetSpecies 投票先の種族
	 */
	public void changeVoterTrust(Agent agent,Species targetSpecies){
		if(!trustMap.containsKey(agent)) return;
		if(isShowConsoleLog) System.out.println("calc trust based on vote "+agent+"target:"+targetSpecies);
		voterBayes.clearAllEvidenceWithCalc();
		double threshold=voterBayes.getMarginalProbability("user_s","human");
		voterBayes.setEvidence("target_s",BayesConverter.convert(targetSpecies));
		voterBayes.calcMargin();
		double margin=voterBayes.getMarginalProbability("user_s","human");
		changeTrust(agent,margin,threshold,false);
	}
	public void setShowConsoleLog(boolean isShowConsoleLog){
		this.isShowConsoleLog=isShowConsoleLog;
	}

	/**
	 * threshold,marginが村人陣営の確率なら、reverseはfalse<br>
	 *  Agentの信用度 +=(margin-threshold)*100 <br>
	 *  if(reverse)  (threshold-margin)   <br>
	 *  
	 */
	private void changeTrust(Agent agent,double margin,double threshold,boolean reverse){
		double point;
		point=(margin-threshold)*100;
		double pre=trustMap.get(agent);
		double result;
		if(reverse) result=addTrust(agent,-point);
		else result=addTrust(agent,point);
		if(isShowConsoleLog){
			System.out.print(""+agent);
			if(reverse) System.out.printf("%.2f-%.2f(%.2f-%.2f)=%.2f\n",pre,point,margin,threshold,result);
			else System.out.printf("%.2f+%.2f(%.2f-%.2f)=%.2f\n",pre,point,margin,threshold,result);
		}
	}
	/**
	 *  0<=trustPoint<=100 になるように計算
	 * @param agent
	 * @param point
	 * @return
	 */
	private double addTrust(Agent agent,double point){
		double beforePoint=trustMap.get(agent);
		double afterPoint=beforePoint+point;
		if(afterPoint>100) afterPoint=100;
		else if(afterPoint<0) afterPoint=0;
		trustMap.put(agent, afterPoint);
		return afterPoint;
	}


}
