package br.com.ilhasoft.rescue.model;

import java.util.Date;

public class Batimento {
	
	private Integer bpm;
	
	private Date date;

	public Integer getBpm() {
		return bpm;
	}

	public void setBpm(Integer bpm) {
		this.bpm = bpm;
	}

	@Override
	public String toString() {
		return "Batimento [bpm=" + bpm + ", date=" + date + "]";
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
