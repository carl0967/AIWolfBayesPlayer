package com.carlo.bayes.trust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

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
	public Agent getLowestTrustAgent(){
		Agent tmpAgent=null;
		double tmpTrustP=100;
		for(Entry<Agent, Double> entry : trustMap.entrySet()) {
			if(entry.getValue()<tmpTrustP){
				tmpAgent=entry.getKey();
				tmpTrustP=entry.getValue();
			}
		}
		return tmpAgent;
	}
	/**
	 * 　信頼度が低い順にエージェントを入れて返す
	 * @return
	 */
	public List<Agent> getSortedList(){
		List<Agent> sortedAgent=new ArrayList<Agent>();
		List<Double> sortedScore=new ArrayList<Double>();
		for(Entry<Agent, Double> entry : trustMap.entrySet()) {
			if(sortedAgent.size()==0){
				sortedAgent.add(entry.getKey());
				sortedScore.add(entry.getValue());
			}
			else{
				boolean add=false;
				for(int i=0;i<sortedAgent.size();i++){
					if(entry.getValue()<sortedScore.get(i)){
						sortedAgent.add(i, entry.getKey());
						sortedScore.add(i, entry.getValue());
						add=true;
						break;
					}
				}
				if(add==false){
					sortedAgent.add(entry.getKey());
					sortedScore.add(entry.getValue());
				}
			}
		}
		return sortedAgent;

	}
	/** もっとも信用してない生きているエージェントを返す。 */
	public Agent getLowestTrustAliveAgent(){
		Agent tmpAgent=null;
		double tmpTrustP=100;
		for(Entry<Agent, Double> entry : trustMap.entrySet()) {
			Agent agent=entry.getKey();
			if(agentInfo.isAlive(agent)){
				if(entry.getValue()<tmpTrustP){
					tmpAgent=entry.getKey();
					tmpTrustP=entry.getValue();
				}
			}
		}
		return tmpAgent;
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
			System.out.println("Agent[番号]\t信用度\t生存\tCO");
			for(Entry<Agent, Double> entry : trustMap.entrySet()) {
				System.out.print(entry.getKey()+"\t");
				System.out.printf("%.3f",entry.getValue());
				System.out.print("\t"+agentInfo.isAlive(entry.getKey()));
				System.out.println("\t"+agentInfo.getCoRole(entry.getKey()));
			}
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
		if(isShowConsoleLog) System.out.println(agent+" day:"+day+" species:"+species+" correct:"+correct+" reverse:"+reverse);
		if(isShowConsoleLog)  System.out.print("seer  ");
		
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
		if(isShowConsoleLog) System.out.print("vote");
		if(!trustMap.containsKey(agent)) return;
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
			System.out.print(agent);
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
