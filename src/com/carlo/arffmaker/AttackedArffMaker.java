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
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Vote;

import com.carlo.lib.CauseOfDeath;
import com.carlo.lib.DeathInfo;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.ExecuteLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;

public class AttackedArffMaker extends AbstractArffMaker{
	@Override
	protected String getRelationName(){
		return "attacked";
	}
	public String[] getAttributeNames(){
		//team:あるエージェントが所属しているチーム
		//attacked:そのエージェントがそのゲーム中に襲撃されたかどうか
		//corole:そのエージェントがCOしていた役職
		//day:襲撃された日
		
		ArffValueGetter getter=new ArffValueGetter();
		return new String[] {"team "+getter.getTeam(),"corole "+getter.getRoleWithNull(),"attacked"+getter.getBoolean()};
	}
	
	@Override
	protected void readGameLog(GameLog log) {
		//COのリスト
		Map<Integer,Role> coRoleMap=new HashMap<Integer,Role>();
		//初期化
		for(Entry<Integer, StatusLog> entry:log.getDayLog(0).getStatus().entrySet()){
			coRoleMap.put(entry.getValue().getAgentNo(),null);
		}
		
		//attackeのリスト
		Map<Integer,DeathInfo> deathMap=new HashMap<Integer,DeathInfo>();
		//初期化
		for(Entry<Integer, StatusLog> entry:log.getDayLog(0).getStatus().entrySet()){
			deathMap.put(entry.getValue().getAgentNo(),null);
		}
		
		
		for (Entry<Integer, DayLog> entry : log.getDays().entrySet()) {
			DayLog dayLog=entry.getValue();
			int day=dayLog.getDay();
			//襲撃による死亡情報
			if(dayLog.getAttack()!=null) {
				deathMap.put(dayLog.getAttack().getTargetAgentNo(),new DeathInfo(day,CauseOfDeath.ATTACKED));
				//System.out.println(dayLog.getDay()+"day:"+dayLog.getAttack().getTargetAgentNo());
			}
			//TalkLogを見てCOリストを格納
			for(TalkLog talkLog:dayLog.getTalk()){
				if(talkLog.getTalkType()==TalkType.TALK){
					Utterance utterance=new Utterance(talkLog.getContent());
					switch(utterance.getTopic()){
					case COMINGOUT:
						coRoleMap.put(talkLog.getAgentNo(), utterance.getRole());
						break;
					case DIVINED:
						if(coRoleMap.get(talkLog.getAgentNo())==null){
							coRoleMap.put(talkLog.getAgentNo(), Role.SEER);
						}
						break;
					case INQUESTED:
						if(coRoleMap.get(talkLog.getAgentNo())==null){
							coRoleMap.put(talkLog.getAgentNo(), Role.MEDIUM);
						}
						break;
					case GUARDED:
						if(coRoleMap.get(talkLog.getAgentNo())==null){
							coRoleMap.put(talkLog.getAgentNo(), Role.BODYGUARD);
						}
						break;
					default:
						break;
					}
				}
			}
		}

		DayLog lastDayLog= log.getDayLog(log.getDays().size()-1);
		
		for(Entry<Integer,StatusLog> entry : lastDayLog.getStatus().entrySet()){
			StatusLog status=entry.getValue();
			//System.out.println(status.getAgentNo()+" "+ status.getRole()+status.getStatus());
			buffer+=status.getRole().getTeam()+","+coRoleMap.get(status.getAgentNo());
			boolean isAttacked=false;
			if(deathMap.get(status.getAgentNo())!=null && deathMap.get(status.getAgentNo()).getCause()==CauseOfDeath.ATTACKED){
				isAttacked=true;
			}
			buffer+=","+String.valueOf(isAttacked)+"\n";
			//System.out.println(status.getRole().getTeam()+","+coRoleMap.get(status.getAgentNo()));
		}
		
		
		
		

	}
	
	public static void main(String[] args) {

		AbstractArffMaker maker = new AttackedArffMaker();

		for( int i = 1; i <= 100; i++ ){
			maker.exec("./logs/log1209/log/aiwolf" + i + ".log");
			
		}
		//maker.printBuffer();
		maker.printBufferToFile("newattacked2.arff");;
	}

}
