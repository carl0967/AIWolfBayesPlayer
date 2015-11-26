package com.carlo.random.player;

import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;
import com.yy.player.YYBodyguard;
import com.yy.player.YYMedium;
import com.yy.player.YYPossessed;
import com.yy.player.YYSeer;
import com.yy.player.YYWerewolf;

public class RandomPlayer extends AbstractRoleAssignPlayer {
	public RandomPlayer(){
		setVillagerPlayer(new RandomVillager());
		setSeerPlayer(new YYSeer());
		setBodyguardPlayer(new YYBodyguard());
		setMediumPlayer(new YYMedium());
		setWerewolfPlayer(new YYWerewolf());
		setPossessedPlayer(new YYPossessed());
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "Normal";
	}
	public int getCorrectNum(){
		if(getVillagerPlayer().getClass()==RandomVillager.class){
			return ((RandomVillager)getVillagerPlayer()).getCorrectNum();
		}
		else return -1;
	}

}
