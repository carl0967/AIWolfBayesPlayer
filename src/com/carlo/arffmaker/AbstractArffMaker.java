package com.carlo.arffmaker;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import jp.halfmoon.inaba.aiwolf.log.GameLog;
import jp.halfmoon.inaba.aiwolf.log.LogReader;
/**
 *  arffファイルを作るクラス
 *  具象クラスで何を出力するかを決定して、bufferに入れていく
 * 　最終的にそのbufferをarffに出力する
 * @author carlo
 *
 */
public abstract class AbstractArffMaker {
	/** 出力を保持 */
	protected String buffer="";
	public AbstractArffMaker(){
		buffer+="@RELATION "+getRelationName()+"\n";
		for(String attributeName:getAttributeNames()){
			buffer+="@ATTRIBUTE "+attributeName+"\n";
		}
		buffer+="@DATA\n";
	}
	public void printBuffer(){
		System.out.println(buffer);
	}
	/** バッファの内容をファイルに出力 */
	public void printBufferToFile(String fileName){
		PrintStream out;
		try {
			out = new PrintStream(fileName);
			System.setOut(out);
			System.out.println(buffer);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//置き換える
	}
	/** ファイルパスのログファイルに対して処理を行う */
	public  void exec(String filePath){
		GameLog log = LogReader.getLogData(filePath);
		if( log == null ){
			System.out.println("error!");
			return;
		}
		readGameLog(log);
		
	}
	/** 個別の処理 */
	protected abstract void readGameLog(GameLog log);
	/** arffファイルのrelation名  */
	protected abstract String getRelationName();
	/** arffファイルのattribute名 */
	protected abstract String[] getAttributeNames();
	
	

}
