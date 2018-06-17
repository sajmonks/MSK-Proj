package wat.shop.customer;

import java.util.Random;

public class Customer {
	private static int lastId = 0;
	private int id = -1;
	private int shoppingTime = -1;
	private int productNum = 0;
	
	private boolean isChecking = false;
	private boolean privileged = false;
	
	public Customer() {
		id = lastId;
		lastId++;
		
		//Szansa 10% na przywilej
		if(new Random().nextInt(10) == -1) {
			privileged = true;
		}
		
		shoppingTime = new Random().nextInt(5) + 5;
		productNum = (new Random().nextInt(shoppingTime) + 1)*2;
	}
	
	public boolean isChecking() {
		return isChecking;
	}
	
	public boolean isPrivileged() {
		return privileged;
	}
	
	public int getShoppingTime() {
		return shoppingTime;
	}
	
	public int getId() {
		return id;
	}
	
	public int getProductNum() {
		return productNum;
	}
	
	public void advanceShoppingTime() {
		shoppingTime--;
	}
	
	public void setChecking() {
		isChecking = true;
	}
	
}
