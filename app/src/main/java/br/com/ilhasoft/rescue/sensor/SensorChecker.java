package br.com.ilhasoft.rescue.sensor;

import br.com.ilhasoft.rescue.model.Sensor;

public class SensorChecker {
	
	public SensorSituacao check(Sensor sensor) throws Exception {
		String situacao = sensor.getSituacao();
		
		if(situacao != null) {
			if(situacao.equalsIgnoreCase("aberta") || situacao.equalsIgnoreCase("1")) {
				return SensorSituacao.ABERTA;
			} else {
				return SensorSituacao.FECHADA;
			}
		}
		return SensorSituacao.FECHADA;
	}

}
