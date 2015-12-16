package com.carlo.lib;

public class DeathInfo {
	private CauseOfDeath cause;
	private int day; 
	public DeathInfo(int day,CauseOfDeath cause){
		this.day=day;
		this.cause=cause;
	}
	public CauseOfDeath getCause(){
		return cause;
	}
	public int getDay(){
		return day;
	}

}
