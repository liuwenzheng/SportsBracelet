package com.blestep.sportsbracelet.entity;

import java.io.Serializable;

public class Step implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Step() {
	}

	public Step(String count) {
		this.count = count;
	}

	public String date;
	public String count;
	public String duration;
	public String distance;
	public String calories;
}
