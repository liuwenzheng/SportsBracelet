package com.blestep.sportsbracelet.event;

import com.db.chart.model.BarSet;

public class HistoryChangeUnitClick {

	public int selectHistoryUnit;
	public BarSet dataCount;
	public BarSet dataCalorie;
	public BarSet dataDistance;
	public String[] valuesCount;
	public String[] valuesCalorie;
	public String[] valuesDistance;
	public int barCountMax;
	public int barCalorieMax;
	public int barDistanceMax;

	public HistoryChangeUnitClick(int selectHistoryUnit) {
		this.selectHistoryUnit = selectHistoryUnit;
	}
}
