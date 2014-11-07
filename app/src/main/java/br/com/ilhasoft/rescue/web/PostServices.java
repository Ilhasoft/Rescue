package br.com.ilhasoft.rescue.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.ilhasoft.rescue.R;
import br.com.ilhasoft.rescue.model.Batimento;
import br.com.ilhasoft.rescue.model.SensorData;

import android.content.Context;
import android.util.Log;

public class PostServices {
	
	enum Sensors {
		temperature,
		sound,
		relativeHumidity,
		luminousIntensity
	};
	
	SimpleDateFormat sqlServerFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	SimpleDateFormat sqlServerTimeFormat = new SimpleDateFormat("HH:mm:ss");

	private static final String TAG = "";
	private final Context context;

	public PostServices(Context context) {
		this.context = context;
	}
	
	public void enviaEmail(List<Batimento> batimentos, String email) {
		String url = "https://api.sendgrid.com/api/mail.send.json?api_user=danielblx&api_key=ilha@1q2w3e&to[]=%1$s&toname[]=Daniel&subject=RescueReport&html=%2$s&from=contato@ilhasoft.com.br";
        try {
        	Batimento batimento = batimentos.get(batimentos.size()-1);
        	String batimentoCardiaco = "Seu%20batimento%20cardiaco%20no%20dia%2026-04-2014%20foi%20" + batimento.getBpm() + " BPM.";
        	
        	url = String.format(url, email, batimentoCardiaco);
        	
        	WebService rest = new WebService(url, 15000);
        	rest.webGet();
        } catch (JSONException exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
        } catch (IOException exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
        }
    }

    public void enviaDados(Batimento batimento) {
        String url = context.getString(R.string.url_envia_dados);
        WebService rest = new WebService(url, 15000);

        try {
        	JSONObject jsonBatimento = new JSONObject();
        	jsonBatimento.put("bpm", batimento.getBpm());
        	
        	JSONObject jsonBatimentoRoot = new JSONObject();
        	jsonBatimentoRoot.put("batimento", jsonBatimento);
        	
        	Log.i("TESTE-JOHN", "" + jsonBatimentoRoot);
        	
        	String response = rest.webInvoke("", jsonBatimentoRoot.toString(), "application/json");
        } catch (JSONException exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
        } catch (IOException exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
        }
    }
    
    public SensorData getSensorData() {
    	SensorData sensorData = new SensorData();
    	String url = context.getString(R.string.url_sensor);
        WebService rest = new WebService(url, 15000);

        try {
        	String response = rest.webGet();
        	
        	JSONObject jsonData = new JSONObject(response).getJSONObject("data");
        	JSONArray jsonSensors = jsonData.getJSONArray("sensorData");
        	
        	for(int i = 0; i < jsonSensors.length(); i++) {
        		JSONObject jsonSensor = jsonSensors.getJSONObject(i);
        		
        		JSONObject jsonMs = jsonSensor.getJSONObject("ms");
        		Integer sensorValue = jsonMs.getInt("v");
        		String sensorType = jsonMs.getString("p");
        		
        		if(sensorType.equalsIgnoreCase(Sensors.temperature.toString())) {
        			sensorData.setTemperature(sensorValue);
        		} else if(sensorType.equalsIgnoreCase(Sensors.sound.toString())) {
        			sensorData.setSound(sensorValue);
        		} else if(sensorType.equalsIgnoreCase(Sensors.relativeHumidity.toString())){
        			sensorData.setRelativeHumidity(sensorValue);
        		} else if(sensorType.equalsIgnoreCase(Sensors.luminousIntensity.toString())){
        			sensorData.setLuminousIntensity(sensorValue);
        		}
        	}
        	
        	Log.i("TESTE-JOHN", "Response: " + response);
        } catch (JSONException exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
            return null;
        } catch (IOException exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
            return null;
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.w(TAG, "ERRO NO REST: " + exception.getMessage());
            return null;
        }
        
        return sensorData;
    }
}
