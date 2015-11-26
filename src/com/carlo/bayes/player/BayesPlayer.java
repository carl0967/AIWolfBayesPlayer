package com.carlo.bayes.player;

import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;

import com.yy.player.YYBodyguard;
import com.yy.player.YYMedium;
import com.yy.player.YYPossessed;
import com.yy.player.YYSeer;
import com.yy.player.YYVillager;
import com.yy.player.YYWerewolf;
/**
 *  ベイジアンネットワークを利用して作成したエージェント
 * @author carlo
 *
 */
public class BayesPlayer extends AbstractRoleAssignPlayer {
	public BayesPlayer(){
		setVillagerPlayer(new BayesVillager());
		/*
		setSeerPlayer(new YYSeer());
		setBodyguardPlayer(new YYBodyguard());
		setMediumPlayer(new YYMedium());
		setWerewolfPlayer(new YYWerewolf());
		setPossessedPlayer(new YYPossessed());
		*/
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
