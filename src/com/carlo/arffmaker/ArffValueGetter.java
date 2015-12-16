package com.carlo.arffmaker;
/**
 * arffファイルの Value文字列を返すクラス
 * @author carlo
 *
 */

public class ArffValueGetter {
	public  String getBoolean(){
		return "{true,false}";
	}
	public  String getRole(){
		return "{VILLAGER,BODYGUARD,WEREWOLF,POSSESSED,SEER,MEDIUM}";
	}
	public String getRoleWithNull(){
		return "{VILLAGER,BODYGUARD,WEREWOLF,POSSESSED,SEER,MEDIUM,null}";
	}
	public  String getSpecies(){
		return "{HUMAN,WEREWOLF}";
	}
	public  String getDay(){
		return "{0,1,2,3,4,5,6,7,later}";
	}
	public String getTeam(){
		return "{VILLAGER,WEREWOLF}";
	}

}
