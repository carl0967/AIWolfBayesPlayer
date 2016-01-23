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

import com.carlo.arffmaker.BayesConverter15;
import com.carlo.bayes.lib.WekaBayesManager;
import com.carlo.lib.AgentInformationManager;
import com.carlo.lib.CauseOfDeath;


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
	private WekaBayesManager mediumBayes;
	/**  コンソール出力 */
	private boolean isShowConsoleLog=false;

	private AgentInformationManager agentInfo;
	public TrustList(List<Agent> agents,Agent myAgent,AgentInformationManager agentInfo){
		this.agentInfo=agentInfo;
		for(Agent agent:agents){
			if(agent==myAgent) continue;
			//信用度の初期値は50
			trustMap.put(agent, 50.0);
		}
		

		//ベイズネットワーククラス生成
		seerBayes=new WekaBayesManager("xml/newseer4.xml");
		voterBayes=new WekaBayesManager("xml/newvote3_1.xml");
		attackedBayes=new WekaBayesManager("xml/newattacked1.xml");
		mediumBayes=new WekaBayesManager("xml/medium3_correct.xml");
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
			attackedBayes.clearAllEvidence();
			String convertRole=BayesConverter15.convertRole(agentInfo.getCoRole(deadAgent));
			attackedBayes.setEvidence("corole",convertRole);
			
			attackedBayes.calcMargin();
			//エビデンスがセットされたいない場合の確率を境界とする
			double threshold=attackedBayes.getMarginalProbability("team", "VILLAGER");
			
			attackedBayes.setEvidence("attacked","true");
			//Role deadCoRole=agentInfo.getCoRole(deadAgent);
			//nullのことを考えてconvert
			attackedBayes.calcMargin();
			
			double margin=attackedBayes.getMarginalProbability("team", "VILLAGER");
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
	public void printTrustListForCreatingData(GameInfo finishedGameInfo){
		//System.out.println("\n信用度,CO,役職,占いCO数,霊能CO数");
		for(Entry<Agent, Double> entry : trustMap.entrySet()) {
			//System.out.print(entry.getKey()+",");
			//System.out.print(agentInfo.getDayAgentDied(agentInfo.getMyAgent()));
			double trustP=entry.getValue();
			String label="";
			if(trustP<25)  label="low";
			else if(trustP<45) label="little_low";
			else if(trustP<56) label="middle";
			else if(trustP<76) label="little_high";
			else label="high";
			System.out.print(""+label);
			//System.out.printf(",%.3f",entry.getValue());
			System.out.print(","+agentInfo.getCoRole(entry.getKey()));
			System.out.print(","+finishedGameInfo.getRoleMap().get(entry.getKey()));
			System.out.print(","+agentInfo.countCoAgent(Role.SEER));
			System.out.println(","+agentInfo.countCoAgent(Role.MEDIUM));
			//System.out.println(","+agentInfo.getDayAgentDied(entry.getKey()));
		}
		//System.out.println();

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
		seerBayes.setEvidence("day", BayesConverter15.convertDay(day));
		seerBayes.calcMargin();
		//閾値。エビデンスを与えた場合にこれをどれだけ上回るか下回るかで信用度を上下させる
		double threshold=seerBayes.getMarginalProbability("role", "SEER");
		
		seerBayes.setEvidence("result",species.toString());
		if(correct!=Correct.UNKNOWN) seerBayes.setEvidence("correct", BayesConverter15.convert(correct));
		seerBayes.calcMargin();
		
		double margin=seerBayes.getMarginalProbability("role", "SEER");
		//信用度の変化
		changeTrust(agent,margin,threshold,reverse);
	}
	/**
	 *  霊能結果発言から信用度計算
	 * @param agent
	 * @param targetSpecies
	 * @param day
	 */
	public void changeMediumTrust(Agent agent,Species targetSpecies,int day){
		if(!trustMap.containsKey(agent)) return;
		if(isShowConsoleLog) System.out.println("calc trust based on medium agent:"+agent+"result:"+targetSpecies);
		mediumBayes.clearAllEvidence();
		mediumBayes.setEvidence("day",BayesConverter15.convertDay(day));
		voterBayes.calcMargin();
		double threshold=mediumBayes.getMarginalProbability("role", "MEDIUM");
		
		mediumBayes.setEvidence("result",targetSpecies.toString());
		mediumBayes.calcMargin();
		double margin=mediumBayes.getMarginalProbability("role", "MEDIUM");
		
		changeTrust(agent,margin,threshold,false);
	}
	/** 
	 *  投票結果から信用度計算
	 * @param agent 投票者
	 * @param targetSpecies 投票先の種族
	 * @param day 投票日
	 * @param assist  booleanをstringにしたもの
	 * TODO:booleanを引数にして、こっちでStringにするべきかも
	 */
	public void changeVoterTrust(Agent agent,Species targetSpecies,int day,String assist){
		if(!trustMap.containsKey(agent)) return;
		if(isShowConsoleLog) System.out.println("calc trust based on vote "+agent+"target:"+targetSpecies);
		voterBayes.clearAllEvidence();
		voterBayes.setEvidence("day",BayesConverter15.convertDay(day));
		voterBayes.calcMargin();
		
		double threshold=voterBayes.getMarginalProbability("voter","human");
		if(targetSpecies!=null) voterBayes.setEvidence("target",BayesConverter.convert(targetSpecies));
		if(assist!=null) voterBayes.setEvidence("assist", assist);
		voterBayes.calcMargin();
		double margin=voterBayes.getMarginalProbability("voter","human");
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
