package com.carlo.question;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Utterance;
import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import com.carlo.lib.CauseOfDeath;
import com.carlo.lib.DeathInfo;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;

public class QuesAnswerCreater {
	ArrayList<Integer> seerCOAgent;
	ArrayList<Integer> mediumCOAgent;
	GameLog log;
	public	void readGameLog(String filePath) {
		log = LogReader.getLogData(filePath);
		if( log == null ){
			System.out.println("error!");
			return;
		}
		seerCOAgent=new ArrayList<Integer>(); 
		mediumCOAgent=new ArrayList<Integer>();



		//日ごとに
		for (Entry<Integer, DayLog> entry : log.getDays().entrySet()) {
			DayLog dayLog=entry.getValue();
			int day=dayLog.getDay();
	
			//TalkLogを見てCOリストを格納
			for(TalkLog talkLog:dayLog.getTalk()){
				if(talkLog.getTalkType()==TalkType.TALK){
					Utterance utterance=new Utterance(talkLog.getContent());
					switch(utterance.getTopic()){
					case COMINGOUT:
						if(utterance.getRole()==Role.SEER) seerCOAgent.add(talkLog.getAgentNo());
						if(utterance.getRole()==Role.MEDIUM) mediumCOAgent.add(talkLog.getAgentNo());
						break;
					case DIVINED:
						int seerNum=talkLog.getAgentNo();
						if(!seerCOAgent.contains(seerNum)){
							seerCOAgent.add(talkLog.getAgentNo());
						}
						
						break;
					case INQUESTED:
						int mediumNum=talkLog.getAgentNo();
						if(!mediumCOAgent.contains(mediumNum)){
							mediumCOAgent.add(talkLog.getAgentNo());
						}
						break;
					default:
						break;
					}
				}
			}
			

		}
	
	}
	public void printAnswer(){
		System.out.println("答え");
		for(int i:seerCOAgent){
			System.out.println(Agent.getAgent(i)+":"+log.getDayLog(0).getStatus().get(i).getRole());
		}
	}
	public Agent getTruthSeer(){
		for(int i:seerCOAgent){
			if(log.getDayLog(0).getStatus().get(i).getRole()==Role.SEER) return Agent.getAgent(i);
		}
		return null;
	}
	public Agent getTruthMedium(){
		for(int i:mediumCOAgent){
			if(log.getDayLog(0).getStatus().get(i).getRole()==Role.MEDIUM) return Agent.getAgent(i);
		}
		return null;
	}
}
