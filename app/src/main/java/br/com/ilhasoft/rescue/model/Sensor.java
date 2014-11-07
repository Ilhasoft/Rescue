package br.com.ilhasoft.rescue.model;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Sensor implements Parcelable {

	private Date data;
	
	private String nome;
	
	private String sensorMac;
	
	private String situacao;
	
	private Boolean checked = false;
	
	private Boolean waitingTest = false;
	
	private Boolean resultTestOk = false;
	
	public Date getData() {
		return data;
	}
	
	public void setData(Date data) {
		this.data = data;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getSensorMac() {
		return sensorMac;
	}
	
	public void setSensorMac(String sensorMac) {
		this.sensorMac = sensorMac;
	}

	public String getSituacao() {
		return situacao;
	}
	
	public void setSituacao(String situacao) {
		this.situacao = situacao;
	}
	
	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}
	
	public Boolean getWaitingTest() {
		return waitingTest;
	}

	public void setWaitingTest(Boolean waitingTest) {
		this.waitingTest = waitingTest;
	}
	
	public Boolean getResultTestOk() {
		return resultTestOk;
	}

	public void setResultTestOk(Boolean resultTestOk) {
		this.resultTestOk = resultTestOk;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((sensorMac == null) ? 0 : sensorMac.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sensor other = (Sensor) obj;
		if (sensorMac == null) {
			if (other.sensorMac != null)
				return false;
		} else if (!sensorMac.equals(other.sensorMac))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Sensor [data=" + data + ", nome=" + nome + ", sensorMac="
				+ sensorMac + ", situacao="
				+ situacao + ", checked=" + checked + ", waitingTest="
				+ waitingTest + ", resultTestOk=" + resultTestOk + "]";
	}

	public Sensor() {
		// TODO Auto-generated constructor stub
	}
	
	public Sensor(String sensorMac) {
		this.sensorMac = sensorMac;
	}
	
	public Sensor(Parcel in) {
		try {
			String [] data = new String[8];
			in.readStringArray(data);
			
			this.data = (data[0] != null && data[0].length() > 0 ? new Date(Long.parseLong(data[0])) : null);
			this.nome = (data[1] != null && data[1].length() > 0 ? data[1] : "");
			this.sensorMac = (data[2] != null && data[2].length() > 0 ? data[2] : "");
			this.situacao = (data[3] != null && data[3].length() > 0 ? data[3] : "");
			this.checked = (data[4] != null && data[4].length() > 0 ? Boolean.parseBoolean(data[4]) : false);
			this.waitingTest = (data[5] != null && data[5].length() > 0 ? Boolean.parseBoolean(data[5]) : false);
			this.resultTestOk = (data[6] != null && data[6].length() > 0 ? Boolean.parseBoolean(data[6]) : false);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String [] {
				(this.data == null ? null : this.data.getTime() + ""),
				(this.nome == null ? null : this.nome),
				(this.sensorMac == null ? null : this.sensorMac),
				(this.situacao == null ? null : this.situacao),
				(this.checked == null ? null : this.checked + ""),
				(this.waitingTest == null ? null : this.waitingTest + ""),
				(this.resultTestOk == null ? null : this.resultTestOk + "")
		});
	}
	
	public static final Parcelable.Creator<Sensor> CREATOR = new Parcelable.Creator<Sensor>() {
		public Sensor createFromParcel(Parcel in) {
			return new Sensor(in);
		}

		public Sensor[] newArray(int size) {
			return new Sensor[size];
		}
	};
	
}