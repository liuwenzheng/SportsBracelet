package com.blestep.sportsbracelet.entity;

import java.io.Serializable;

public class BleDevice implements Serializable, Comparable<BleDevice> {

	private static final long serialVersionUID = 1L;
	public String address;
	public String name;
	public boolean isChecked;
	public int rssi;

	@Override
	public int compareTo(BleDevice another) {
		if (this.rssi > another.rssi) {
			return -1;
		} else if (this.rssi < another.rssi) {
			return 1;
		}
		return 0;
	}
}
