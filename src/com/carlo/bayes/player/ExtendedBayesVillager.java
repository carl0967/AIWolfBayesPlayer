package com.carlo.bayes.player;

import org.aiwolf.common.data.Agent;

import com.carlo.bayes.trust.TrustLevel;

public class ExtendedBayesVillager extends BayesVillager {
	
	@Override
	public Agent vote() {
		return trustListManager.getAgent(TrustLevel.LOWEST, true);
	}

}
