package com.carlo.random.player;

import java.util.ArrayList;
import java.util.List;
import org.aiwolf.client.base.player.AbstractVillager;
import org.aiwolf.common.data.Agent;
import com.carlo.bayes.trust.DataCorrecter;
import com.carlo.lib.RandomSelect;

public class RandomVillager extends AbstractVillager {

	@Override
	public void dayStart() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void finish() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public String talk() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Agent vote() {
		// TODO 自動生成されたメソッド・スタブ
		List<Agent> voteCandidate=getLatestDayGameInfo().getAliveAgentList();
		if(voteCandidate.contains(getMe())) voteCandidate.remove(getMe());
		return RandomSelect.randomSelect(voteCandidate);
	}
	public int getCorrectNum(){
		DataCorrecter dataCorrecter=new DataCorrecter(getMe());
		//ランダムにtrustListを作成
		ArrayList<Agent> randomList=(ArrayList<Agent>) getLatestDayGameInfo().getAgentList();
		randomList.remove(getMe());
		return dataCorrecter.start(randomList, getLatestDayGameInfo());
	}

}
