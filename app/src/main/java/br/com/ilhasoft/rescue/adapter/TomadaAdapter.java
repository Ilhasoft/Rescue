package br.com.ilhasoft.rescue.adapter;

import java.util.List;

import br.com.ilhasoft.rescue.R;
import br.com.ilhasoft.rescue.model.Sensor;
import br.com.ilhasoft.rescue.sensor.SensorChecker;
import br.com.ilhasoft.rescue.sensor.SensorSituacao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TomadaAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private List<Sensor> tomadas;
	
	private SensorChecker sensorChecker;

	public TomadaAdapter(Context context, List<Sensor> tomadas) {
		this.tomadas = tomadas;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		sensorChecker = new SensorChecker();
	}
	
	public void setTomadas(List<Sensor> tomadas) {
		this.tomadas = tomadas;
	}

	@Override
	public int getCount() {
		return tomadas.size();
	}

	@Override
	public Sensor getItem(int position) {
		return tomadas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Sensor sensor = getItem(position);
		ViewHolder viewHolder;
		try {
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.tomada_item, null);
				viewHolder = new ViewHolder();
				
				viewHolder.tbTomada = (TextView) convertView.findViewById(R.id.tbTomada);
				viewHolder.tvTomada = (TextView) convertView.findViewById(R.id.tvTomada);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
//			viewHolder.tbTomada.setChecked(sensorChecker.check(sensor) == SensorSituacao.ABERTA ? true : false);
			viewHolder.tbTomada.setText((sensorChecker.check(sensor) == SensorSituacao.ABERTA ? true : false) + "");
			viewHolder.tvTomada.setText(sensor.getNome());
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		
		return convertView;
	}
	
	
	private class ViewHolder {
		TextView tbTomada;
		TextView tvTomada;
	}
	

}
