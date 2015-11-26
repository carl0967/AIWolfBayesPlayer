package com.carlo.lib;

import java.util.List;
import java.util.Random;

import org.aiwolf.common.data.Agent;

public class RandomSelect {
	public static Agent randomSelect(List<Agent> agentList){
		int num=new Random().nextInt(agentList.size());
		return agentList.get(num);
	}

}
