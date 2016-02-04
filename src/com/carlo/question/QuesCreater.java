package com.carlo.question;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aiwolf.client.lib.Utterance;
import org.aiwolf.client.lib.TemplateTalkFactory.TalkType;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Vote;

import com.carlo.lib.CauseOfDeath;
import com.carlo.lib.DeathInfo;

import jp.halfmoon.inaba.aiwolf.log.DayLog;
import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
import jp.halfmoon.inaba.aiwolf.log.StatusLog;
import jp.halfmoon.inaba.aiwolf.log.TalkLog;

/**
 * アンケ自動生成クラス
 * @author carlo
 *
 */
public class QuesCreater {

	public static  void readGameLog(GameLog log,String fileName,int qIndex) throws FileNotFoundException {
		
		ArrayList<Integer> seerCOAgent=new ArrayList<Integer>(); 
		ArrayList<Integer> mediumCOAgent=new ArrayList<Integer>();
		//seerCOとmediumCOした人物を探し出す(COなしで結果発言も含む)
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
		
		if(seerCOAgent.size()!=3) {
			//System.out.println("占いCO数=3 ではありません");
			return;
		}
		
		//ファイル出力
		PrintStream out = new PrintStream("ques/"+fileName+".txt");
		//置き換える
		System.setOut(out);
		        
		System.out.println("問題番号"+qIndex);
		System.out.println("エージェント同士が対戦した以下の人狼ゲームログを読み");
		for(Integer i:seerCOAgent){
			System.out.print("Agent["+i+"],");
		}
		System.out.println("の中から本物の占い師を当てよ");
		System.out.println("配役は 村人8,占い師1,霊能者1,狩人1,人狼3,狂人1の計15人である");
		System.out.println("人狼は襲撃されることはなく、誰が仲間の人狼かを知っている");
		if(mediumCOAgent.size()==1){
			if(log.getDayLog(0).getStatus().get(mediumCOAgent.get(0)).getRole()!=Role.MEDIUM) return;
			System.out.println("なおAgent["+mediumCOAgent.get(0)+"]は本物の霊能者とする。");
		}
		System.out.println("--------------------------------");
		
		/** 以降ログ出力  */
		for (Entry<Integer, DayLog> entry : log.getDays().entrySet()) {
			DayLog dayLog=entry.getValue();
			int day=dayLog.getDay();
			System.out.println("--------------------------------");
			System.out.println(day+"日目");
			
			//会話
			System.out.println("-会話");
			for(TalkLog talkLog:dayLog.getTalk()){
				if(talkLog.getTalkType()==TalkType.TALK){
					Utterance utterance=new Utterance(talkLog.getContent());
					switch(utterance.getTopic()){
					case COMINGOUT:
						if(seerCOAgent.contains(talkLog.getAgentNo())){
							//System.out.println("COMINGOUT\t"+utterance.getTarget()+"\t"+utterance.getRole());
							System.out.println(utterance.getTarget()+":私は占い師です");
						}
						break;
					case DIVINED:
						//System.out.println(talkLog.getAgentNo()+"\tDIVINED\t"+utterance.getTarget()+"\t"+utterance.getResult());
						System.out.println("Agent["+talkLog.getAgentNo()+"]:"+utterance.getTarget()+"を占った結果、"+convertJp(utterance.getResult())+"でした");
						break;
					case INQUESTED:
						if(mediumCOAgent.size()==1){
							System.out.println("Agent["+talkLog.getAgentNo()+"]:"+utterance.getTarget()+"の死体を調べた結果、"+convertJp(utterance.getResult())+"でした");
						}
						break;
					default:
						break;
					}
				}
			}
			
			//投票
			System.out.println("-投票");
			for (Entry<Integer, Vote> voteEntry : dayLog.getVoteList().entrySet()) {
				Vote vote=voteEntry.getValue();
				if(seerCOAgent.contains(vote.getAgent().getAgentIdx())){
					//System.out.println("VOTE\t"+vote.getAgent()+"\t"+vote.getTarget());
					System.out.println(vote.getAgent()+"は"+vote.getTarget()+"に投票しました");
				}
			}
			//処刑
			System.out.println("-処刑");
			if(dayLog.getExecute()!=null){
				System.out.println("Agent["+dayLog.getExecute().getExecuteAgentNo()+"]が処刑されました");
			}
			System.out.println("-襲撃");
			//襲撃
			if(dayLog.getAttack()!=null){
				if(dayLog.getAttack().isSuccess())	System.out.println("人狼によってAgent["+dayLog.getAttack().getTargetAgentNo()+"]が襲撃されました");
				else System.out.println("死体はありませんでした");
			}
			

		}
	//結果
		System.out.println("ゲーム終了");
		System.out.println("ログファイル名:"+"./logs/log1209/log/"+fileName+".log");

	}
	public static String convertJp(Species species){
		if(species==Species.HUMAN) return "人間";
		else return "人狼";
	}
	
	public static void main(String[] args) {

		
		for(int i=1;i<100;i++){
			String fileName="aiwolf"+i;


			String filePath="./logs/log1209/log/"+fileName+".log";
			GameLog log = LogReader.getLogData(filePath);
			if( log == null ){
				System.out.println("error!");
				return;
			}
			try {
				readGameLog(log,fileName,i);
			} catch (FileNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		

	}
	

}
