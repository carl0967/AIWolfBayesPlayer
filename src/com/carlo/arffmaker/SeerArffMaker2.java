package com.carlo.arffmaker;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Vote;

import com.carlo.lib.CauseOfDeath;
import com.carlo.lib.DeathInfo;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.ExecuteLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;
import jp.halfmoon.inaba.aiwolf.mark.AIWolfMark;

public class SeerArffMaker2 extends AbstractArffMaker{
	@Override
	protected String getRelationName(){
		return "seer";
	}
	public String[] getAttributeNames(){
		ArffValueGetter getter=new ArffValueGetter();
		return new String[] {"day "+getter.getDay(),"role "+getter.getRole(),"result "+getter.getSpecies(),"correct "+getter.getBoolean(),"isAttacked "+getter.getBoolean()};
	}
	@Override
	protected void readGameLog(GameLog log) {
		
		//襲撃のリスト
		//<AgentIdx,isAttacked>
		Map<Integer,Boolean> attackedMap=new HashMap<Integer,Boolean>();
		//初期化
		for(Entry<Integer, StatusLog> entry:log.getDayLog(0).getStatus().entrySet()){
			attackedMap.put(entry.getValue().getAgentNo(),false);
		}
		for (Entry<Integer, DayLog> entry : log.getDays().entrySet()) {
			DayLog dayLog=entry.getValue();
			int day=dayLog.getDay();
			//襲撃による死亡情報
			if(dayLog.getAttack()!=null) {
				attackedMap.put(dayLog.getAttack().getTargetAgentNo(),true);
				//System.out.println(dayLog.getDay()+"day:"+dayLog.getAttack().getTargetAgentNo());
			}
		}
		
		
		for (Entry<Integer, DayLog> entry : log.getDays().entrySet()) {
			int day=entry.getKey();
			DayLog dayLog=entry.getValue();

			ExecuteLog executeLog=dayLog.getExecute();

			//buffer+=day+",";
			for(TalkLog talkLog:dayLog.getTalk()){
				if(talkLog.getTalkType()==TalkType.TALK){
					Utterance utterance=new Utterance(talkLog.getContent());
					if(utterance.getTopic()==Topic.DIVINED){
						StatusLog agentStatus = log.getDayLog(0).getStatus().get( talkLog.getAgentNo() );
						StatusLog targetStatus= log.getDayLog(0).getStatus().get( utterance.getTarget().getAgentIdx());
						//System.out.println(talkLog.getDay()+" "+agentStatus.getRole()+" "+utterance.getResult()+" "+utterance.getTarget());
						//buffer+=talkLog.getAgentNo();

						buffer+=BayesConverter15.convertDay(talkLog.getDay())+","+agentStatus.getRole()+","+utterance.getResult();
						buffer+=","+(targetStatus.getRole().getSpecies()==utterance.getResult())+","+attackedMap.get(utterance.getTarget().getAgentIdx())+"\n";
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {

		SeerArffMaker2 maker = new SeerArffMaker2();

		for( int i = 1; i <= 3; i++ ){
			for(int j=1;j<=500;j++){
				maker.exec("./logs/comp_data/log"+i+"/aiwolf" + j + ".log");
			}
		}
		maker.printBufferToFile("newseer2_6.arff");
		//maker.printBuffer();
		//System.out.println("complete!");

	}

}
