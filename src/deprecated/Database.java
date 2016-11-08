package deprecated;

import java.util.ArrayList;

import com.sumbioun.android.pitstop.database.Gasstation;

public class Database {
	ArrayList<Gasstation> temp = new ArrayList<Gasstation>();
	
	void add(int flag, float price, float distance, int time, String lastUpdate){
		//temp.add(new Gasstation(flag, price, distance, time, lastUpdate));
	}
	
	Gasstation getGasstation(int index){
		return temp.get(index);
	}

}
