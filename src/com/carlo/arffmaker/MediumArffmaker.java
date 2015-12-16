package com.carlo.arffmaker;

import java.util.Map.Entry;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.ExecuteLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;

/**
 * 霊能者の評価用
 * 結局使えそうにないから使わないけれど
 * @author carlo
 *
 */

public class MediumArffmaker extends AbstractArffMaker{
	@Override
	protected String getRelationName(){
		return "medium";
	}
	public String[] getAttributeNames(){
		ArffValueGetter getter=new ArffValueGetter();
		return new String[] {"day "+getter.getDay(),"role "+getter.getRole(),"result "+getter.getSpecies(),"correct "+getter.getBoolean()};
	}
	@Override
	protected void readGameLog(GameLog log) {
		// TODO 自動生成されたメソッド・スタブ
		for (Entry<Integer, DayLog> entry : log.getDays().entrySet()) {
			int day=entry.getKey();
			DayLog dayLog=entry.getValue();

			ExecuteLog executeLog=dayLog.getExecute();

			//buffer+=day+",";
			for(TalkLog talkLog:dayLog.getTalk()){
				if(talkLog.getTalkType()==TalkType.TALK){
					Utterance utterance=new Utterance(talkLog.getContent());
					if(utterance.getTopic()==Topic.INQUESTED){
						StatusLog agentStatus = log.getDayLog(0).getStatus().get( talkLog.getAgentNo() );
						StatusLog targetStatus= log.getDayLog(0).getStatus().get( utterance.getTarget().getAgentIdx());

						buffer+=BayesConverter15.convertDay(talkLog.getDay())+","+agentStatus.getRole()+","+utterance.getResult();
						buffer+=","+(targetStatus.getRole().getSpecies()==utterance.getResult())+"\n";
					}
				}
			}
		}
	}
	public static void main(String[] args) {

		AbstractArffMaker maker = new MediumArffmaker();

		for( int i = 1; i <= 2; i++ ){
			for(int j=1;j<=1000;j++){
				maker.exec("./logs/comp_data/log"+i+"/aiwolf" + j + ".log");
			}

		}
		maker.printBufferToFile("medium3.arff");
		//maker.printBuffer();
		//System.out.println("complete!");

	}
}
