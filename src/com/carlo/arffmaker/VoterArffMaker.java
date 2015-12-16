package com.carlo.arffmaker;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Vote;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.ExecuteLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;
import jp.halfmoon.inaba.aiwolf.mark.AIWolfMark;

public class VoterArffMaker extends AbstractArffMaker{
	@Override
	protected String getRelationName(){
		return "vote";
	}
	@Override
	public String[] getAttributeNames(){
		ArffValueGetter getter=new ArffValueGetter();
		return new String[] {"day "+getter.getDay(),"voter "+getter.getRole(),"target "+getter.getRole(),"assist "+getter.getBoolean()};
	}
	protected void readGameLog(GameLog log) {
		for (Entry<Integer, DayLog> entry : log.getDays().entrySet()) {
			int day=entry.getKey();
			DayLog dayLog=entry.getValue();
			
			ExecuteLog executeLog=dayLog.getExecute();
			
			//buffer+=day+",";
			for(Entry<Integer, Vote> voteEntry : dayLog.getVoteList().entrySet()) {
				Vote vote= voteEntry.getValue();
				StatusLog agentStatus = log.getDayLog(0).getStatus().get( vote.getAgent().getAgentIdx() );
				StatusLog targetStatus = log.getDayLog(0).getStatus().get( vote.getTarget().getAgentIdx() );
				
				boolean isVoteExecutedMan = executeLog.getExecuteAgentNo()==targetStatus.getAgentNo();
				
				//System.out.println(day+","+agentStatus.getAgentNo()+","+ agentStatus.getRole()  +","+targetStatus.getAgentNo()+","+targetStatus.getRole() +","+isVoteExecutedMan);
				String str= BayesConverter15.convertDay(day)+","+ BayesConverter15.convertRoleToSpecies(agentStatus.getRole())  +","+ BayesConverter15.convertRoleToSpecies(targetStatus.getRole()) +","+isVoteExecutedMan;
				buffer+=str+"\n";
			}
		}
		
	}
	public static void main(String[] args) {

		VoterArffMaker maker = new VoterArffMaker();

		for( int i = 1; i <= 50; i++ ){
			maker.exec("./logs/log1209/log/aiwolf" + i + ".log");
			
		}
		maker.printBufferToFile("newvote3.arff");;
	}

}
