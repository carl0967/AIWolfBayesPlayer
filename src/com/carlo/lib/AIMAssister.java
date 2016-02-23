package com.carlo.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
/**
 * AgentInformationManagerをアシストするクラス
 * @author info
 *
 */
public class AIMAssister {
	/**
	 *  たまに1msくらいかかる
	 * @param aim
	 * @param target
	 * @return
	 */
	static public List<AbilityResult> searchDivinedAgent(AgentInformationManager aim,Agent target){
		ArrayList<AbilityResult> abilityResults=new ArrayList<AbilityResult>();

		//Targetが一致したAbilityResultを集めて返す
		for(Entry<Agent, AbilityResultList> entry : aim.getAbilityResultListMap().entrySet()) {
			if(aim.getCoRole(entry.getKey())!=Role.SEER) continue;
			AbilityResult abilityResult=entry.getValue().getAbilityResut(target);
			if(abilityResult!=null) abilityResults.add(abilityResult);
		}
		return abilityResults;
	}

}
