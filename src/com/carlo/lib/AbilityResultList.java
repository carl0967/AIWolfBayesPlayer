package com.carlo.lib;

import java.util.ArrayList;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Species;

/**
 * AbilityResultのリスト
 * @author carlo
 *
 */

public class AbilityResultList {
	private ArrayList<AbilityResult> resultList=new ArrayList<AbilityResult>();
	/**
	 * 
	 * @param topic INQUESTED,DIVINED,GUARDED
	 * @param day 能力行使日 <br> -1:前回の結果からインクリメントした値を入れる(前回がなければ占いは0,霊媒は1) それ以外ならそのまま入れる
	 * @param talkedDay 発言した日
	 * @param target 能力行使先のエージェント
	 * @param species 能力行使結果。GUARDEDでは使わない。
	 */
	public void addAbilityResult(Topic topic,int day,int talkedDay,Agent agent,Agent target,Species species){
		if(day==-1 ){
			if(resultList.size()>0) day=resultList.get(resultList.size()-1).getDay()+1;
			else {
				if(topic==Topic.DIVINED) day=0;
				else if(topic==Topic.INQUESTED || topic==Topic.GUARDED) day=1;
			}
		}
		resultList.add(new AbilityResult(topic,day,talkedDay,agent,target,species));
		
	}
	public void printList(){
		for(AbilityResult result:resultList){
			System.out.println(result);
		}
	}
	/** 線形探索するよ */
	public AbilityResult getAbilityResut(Agent target){
		for(AbilityResult result:resultList){
			if(result.getTarget()==target) return result;
		}
		return null;
	}

}
