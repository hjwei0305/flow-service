package com.ecmp.flow.api.client.util;

import com.ecmp.flow.entity.IConditionPojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PojoTest1 implements IConditionPojo {

	private int count;
	private Date date;
	private String id;
	private List<String> lists;
	
	public PojoTest1(){
		this.count = 0;
		this.date = new Date();
		this.id = "testId";
		this.lists = new ArrayList<String>();
		lists.add("t"); 
	}
	
	public int getCount() {
		return count;
	}
	public Date getDate() {
		return date;
	}
	public String getId() {
		return id;
	}
	public List<String> getLists() {
		return lists;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setLists(List<String> lists) {
		this.lists = lists;
	}

}
