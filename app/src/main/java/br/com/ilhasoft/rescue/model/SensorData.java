package br.com.ilhasoft.rescue.model;

public class SensorData {

	private Integer temperature = 0;
	
	private Integer sound = 0;
	
	private Integer relativeHumidity = 0;
	
	private Integer luminousIntensity = 0;

	public Integer getTemperature() {
		return temperature;
	}

	public void setTemperature(Integer temperature) {
		this.temperature = temperature;
	}

	public Integer getSound() {
		return sound;
	}

	public void setSound(Integer sound) {
		this.sound = sound;
	}

	public Integer getRelativeHumidity() {
		return relativeHumidity;
	}

	public void setRelativeHumidity(Integer relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}

	public Integer getLuminousIntensity() {
		return luminousIntensity;
	}

	public void setLuminousIntensity(Integer luminousIntensity) {
		this.luminousIntensity = luminousIntensity;
	}

	@Override
	public String toString() {
		return "SensorData [temperature=" + temperature + ", sound=" + sound
				+ ", relativeHumidity=" + relativeHumidity
				+ ", luminousIntensity=" + luminousIntensity + "]";
	}
	
}
