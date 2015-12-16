package com.carlo.arffmaker;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Vote;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.ExecuteLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;
import jp.halfmoon.inaba.aiwolf.mark.AIWolfMark;

public class SeerArffMaker extends AbstractArffMaker{
	@Override
	protected String getRelationName(){
		return "seer";
	}
	public String[] getAttributeNames(){
		ArffValueGetter getter=new ArffValueGetter();
		return new String[] {"day "+getter.getDay(),"role "+getter.getRole(),"result "+getter.getSpecies(),"correct "+getter.getBoolean()};
	}
	@Override
	protected void readGameLog(GameLog log) {
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
						buffer+=","+(targetStatus.getRole().getSpecies()==utterance.getResult())+"\n";
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {

		SeerArffMaker maker = new SeerArffMaker();

		for( int i = 1; i <= 3; i++ ){
			for(int j=1;j<=1000;j++){
				maker.exec("./logs/comp_data/log"+i+"/aiwolf" + j + ".log");
			}

		}
		maker.printBufferToFile("newseer5.arff");
		//maker.printBuffer();
		//System.out.println("complete!");

	}

}
