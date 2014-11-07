package br.com.ilhasoft.rescue.util;

import br.com.ilhasoft.rescue.model.Comando;

public class ComandoCoder {
	public Comando encode(String comandoLine) throws Exception {
		String[] comandoSplitted = comandoLine.substring(comandoLine.indexOf("\""), 1 + comandoLine.lastIndexOf("\"")).split("\"");
		String tag = comandoSplitted[1];
		String key = "";
		String value = "";
		
		if (comandoSplitted.length >= 5) {
			key = comandoSplitted[3];
			value = comandoSplitted[5];
		}
		
		Comando comando = new Comando(tag, key, value);
		return comando;
	}
}