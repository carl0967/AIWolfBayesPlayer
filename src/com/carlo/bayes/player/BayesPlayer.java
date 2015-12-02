package com.carlo.bayes.player;

import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;
import org.aiwolf.client.base.smpl.*;
/**
 *  ベイジアンネットワークを利用して作成したエージェント
 *  村人,狩人のみ簡単に実装
 * @author carlo
 *
 */
public class BayesPlayer extends AbstractRoleAssignPlayer {
	public BayesPlayer(){
		setVillagerPlayer(new BayesVillager());
		setBodyguardPlayer(new BayesBodyguard());
		
		setSeerPlayer(new SampleSeer());
		setMediumPlayer(new SampleMedium());
		setWerewolfPlayer(new SampleWerewolf());
		setPossessedPlayer(new SamplePossessed());
		
	}

	@Override
	public String getName() {
		return "Bayes Player";
	}
	
	//性能テスト用。普通の動作とは関係がない。
	public int getCorrectNum(){
		if(getVillagerPlayer().getClass()==BayesVillager.class){
			return ((BayesVillager)getVillagerPlayer()).getCorrectNum();
		}
		else return -1;
	}

}
