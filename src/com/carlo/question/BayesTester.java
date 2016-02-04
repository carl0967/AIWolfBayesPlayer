package com.carlo.question;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameInfoToSend;

import com.carlo.bayes.trust.TrustLevel;
import com.carlo.bayes.trust.TrustListManager;
import com.carlo.lib.AgentInformationManager;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;

/**
 * ゲームログから信用度計算を行う
 * @author carlo
 *
 */
public class BayesTester {
	protected TrustListMngForLog trustListManager;
	AgentInfoManagerForLog agentInfo;
	Agent truthSeer;
	Agent truthMedium;
	
	HashMap<Integer,Agent> truthSeerMap=new  HashMap<Integer,Agent>();
	public BayesTester(String filePath) {
		GameLog log = LogReader.getLogData(filePath);
		if( log == null ){
			System.out.println("error!");
			return;
		}
		ArrayList<Agent> agentList=new ArrayList<Agent>();
		for(int i=1;i<16;i++){
			Agent agent = Agent.getAgent(i);
			agentList.add(agent);
			//System.out.println(agent);
			//agentMap.put(i, agent);
		}
		//村人を探して、myAgentとする
		int i=1;
		Agent myAgent=null;
		while(true){
			StatusLog statusLog= log.getDayLog(0).getStatus().get(i++);
			if(statusLog.getRole()==Role.VILLAGER){
				myAgent=Agent.getAgent(statusLog.getAgentNo());
				break;
			}
		}
		agentInfo=new AgentInfoManagerForLog(agentList);
		trustListManager=new TrustListMngForLog(agentList, myAgent, agentInfo);
		
		
		readGameLog(log);
		truthSeer=trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, Role.SEER, false);
		truthMedium=trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, Role.MEDIUM, false);
		
		//trustListManager.printTrustListForCreatingData();
		//System.out.println("真占い師予想:"+trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, Role.SEER, false));
		
	}	
	public AgentInformationManager getAgentInfo(){
		return this.agentInfo;
	}
	public Agent getGuessTruthSeer(int day){
		while(true){
			if(truthSeerMap.containsKey(day)) return truthSeerMap.get(day);
			else day--;
		}
	}
	//ゲームログからゲームを再現
	public	void readGameLog(GameLog log){
		truthSeerMap=new  HashMap<Integer,Agent>();
		for (Entry<Integer, DayLog> entry : log.getDays().entrySet()) {
			DayLog dayLog=entry.getValue();
			dayStart(dayLog);
			update(dayLog);
			
			truthSeerMap.put(dayLog.getDay(),trustListManager.getRoleCOAgent(TrustLevel.HIGHEST, Role.SEER, false));
			
		}
	}
	private void update(DayLog dayLog){
		agentInfo.update(dayLog.getTalk());
		trustListManager.update();
	}
	private void dayStart(DayLog dayLog){
		agentInfo.dayStart(dayLog);
		trustListManager.dayStart(dayLog);
		trustListManager.printTrustList();
	}
	
	
	public static void main(String[] args) throws FileNotFoundException {
		
		
		
		for(int day=1;day<9;day++){
			int total=0;
			int correctNum=0;
			for(int i=1;i<=1000;i++){
				String fileName="aiwolf"+i;

				//String filePath="./logs/log1209/log/"+fileName+".log";
				String filePath="./logs/comp_data/log"+2+"/aiwolf"+i +".log";
				BayesTester tester=new BayesTester(filePath);
				QuesAnswerCreater answer=new QuesAnswerCreater();
				answer.readGameLog(filePath);
				Agent answerSeer= answer.getTruthSeer();

				//Agent predictSeer=tester.truthSeer;
				Agent predictSeer=tester.getGuessTruthSeer(day);
				//占い師3COのみカウント
				//if(tester.getAgentInfo().countCoAgent(Role.SEER)==3){
				if(answerSeer==predictSeer) correctNum++;
				//else System.out.println("miss:"+fileName);
				total++;
				//}
			}
			System.out.println("day:"+day+" "+correctNum+"/"+total+" :"+(double)correctNum/total);
		}
		
		/*
		//ファイル出力
		PrintStream out = new PrintStream("result52.arff");
		//置き換える
		System.setOut(out);
		for( int i = 1; i <= 3; i++ ){
			for(int j=1;j<=1000;j++){
		//for(int i=1;i<=200;i++){
			String fileName="aiwolf"+j;

			String filePath="./logs/comp_data/log"+i+"/aiwolf" + j + ".log";
			//String filePath="./logs/log1209/log/"+fileName+".log";
			BayesTester tester=new BayesTester(filePath);
			QuesAnswerCreater answer=new QuesAnswerCreater();
			answer.readGameLog(filePath);

			
			}
		}
		*/
		
		//1回実行
		/*
		String fileName="aiwolf4";
		

		String filePath="./logs/log1209/log/"+fileName+".log";
		BayesTester tester=new BayesTester(filePath);
		QuesAnswerCreater answer=new QuesAnswerCreater();
		answer.readGameLog(filePath);
		Agent answerSeer= answer.getTruthSeer();
		Agent predictSeer=tester.truthSeer;
		Agent answerMedium=answer.getTruthMedium();
		Agent predictMedium=tester.truthMedium;
		
		System.out.println("medium ans:"+answerMedium+" pred:"+predictMedium+""+(answerMedium==predictMedium));
		System.out.println("seer ans:"+answerSeer+" pred:"+predictSeer+""+(answerSeer==predictSeer));
		*/
	}

}
