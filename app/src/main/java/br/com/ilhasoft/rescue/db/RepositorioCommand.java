package br.com.ilhasoft.rescue.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import br.com.ilhasoft.rescue.model.Batimento;
import br.com.ilhasoft.rescue.model.Sensor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RepositorioCommand {

	private static final String TAG = "TOPUPDB";

	//Atributos do banco de dados
	public static final String NOME_BANCO = "topupcentral";
	public static final String TABLE_SENSOR = "sensor";
	public static final String TABLE_HEART = "heart";

	private SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static final String[] columns_sensor = {
		"sensor_mac",
		"nome",
		"situacao",
		"icone",
		"data"
	};
	
	public static final String[] columns_heart = {
		"bpm",
		"data"
	};

	//Instncia do banco de dados
	protected SQLiteDatabase db;

	protected RepositorioCommand(Context ctx) {
		// Abre o banco de dados j existente
		db = ctx.openOrCreateDatabase(NOME_BANCO, Context.MODE_PRIVATE, null);
	}

	protected RepositorioCommand() {}
	
	public Sensor buscaSensor(String selection, String [] selectionArgs) throws Exception {
		Cursor cursor = getCursor(TABLE_SENSOR, columns_sensor, null, selection, selectionArgs);

		Sensor sensor = new Sensor();

		if (cursor != null && cursor.moveToFirst()) {
			// Recupera os ndices das colunas
			int idxSensorMac = cursor.getColumnIndex("sensor_mac");
			int idxSituacao = cursor.getColumnIndex("situacao");
			int idxNome = cursor.getColumnIndex("nome");
			int idxData = cursor.getColumnIndex("data");

			// Loop at o final
			do {
				//Recupera os atributos do sensor
				sensor.setSensorMac(cursor.getString(idxSensorMac));
				sensor.setSituacao(cursor.getString(idxSituacao));
				sensor.setNome(cursor.getString(idxNome));
				
				try {
					sensor.setData(simpleFormat.parse(cursor.getString(idxData)));
				} catch (Exception exception) {
					//Sensor Exception
				}
			} while (cursor.moveToNext());
		}

		return sensor;
	}
	
	public List<Sensor> buscaSensores(String... sensoresMac) throws Exception {
		StringBuffer selection = new StringBuffer("");
		
		for(int i = 0; i < sensoresMac.length; i++) {
			String sensorMac = sensoresMac[i];
			selection.append(sensorMac);
			
			if(i < (sensoresMac.length-1)) {
				selection.append(",");
			}
		}
		
		Cursor cursor = getCursor(TABLE_SENSOR, columns_sensor, null, "sensor_mac IN (?)", new String[]{ selection.toString() });

		List<Sensor> sensores = new ArrayList<Sensor>();

		if (cursor != null && cursor.moveToFirst()) {
			// Recupera os ndices das colunas
			int idxSensorMac = cursor.getColumnIndex("sensor_mac");
			int idxSituacao = cursor.getColumnIndex("situacao");
			int idxNome = cursor.getColumnIndex("nome");
			int idxData = cursor.getColumnIndex("data");

			// Loop at o final
			do {
				Sensor sensor = new Sensor();

				//Recupera os atributos do sensor
				sensor.setSensorMac(cursor.getString(idxSensorMac));
				sensor.setSituacao(cursor.getString(idxSituacao));
				sensor.setNome(cursor.getString(idxNome));
				
				try {
					sensor.setData(simpleFormat.parse(cursor.getString(idxData)));
				} catch (Exception exception) {
					//Sensor Exception
				}
				
				sensores.add(sensor);
			} while (cursor.moveToNext());
		}

		return sensores;
	}

	public List<Sensor> buscaSensores() throws Exception {
		Cursor cursor = getCursor(TABLE_SENSOR, columns_sensor, null, "", null);

		List<Sensor> sensores = new ArrayList<Sensor>();

		if (cursor != null && cursor.moveToFirst()) {
			// Recupera os ndices das colunas
			int idxSensorMac = cursor.getColumnIndex("sensor_mac");
			int idxSituacao = cursor.getColumnIndex("situacao");
			int idxNome = cursor.getColumnIndex("nome");
			int idxData = cursor.getColumnIndex("data");

			// Loop at o final
			do {
				Sensor sensor = new Sensor();

				//Recupera os atributos do sensor
				sensor.setSensorMac(cursor.getString(idxSensorMac));
				sensor.setSituacao(cursor.getString(idxSituacao));
				sensor.setNome(cursor.getString(idxNome));
				
				try {
					sensor.setData(simpleFormat.parse(cursor.getString(idxData)));
				} catch (Exception exception) {
					//Sensor Exception
				}
				
				sensores.add(sensor);
			} while (cursor.moveToNext());
		}

		return sensores;
	}
	
	public List<Batimento> buscaBatimentos() throws Exception {
		Cursor cursor = getCursor(TABLE_HEART, columns_heart, null, "", null);

		List<Batimento> batimentos = new ArrayList<Batimento>();

		if (cursor != null && cursor.moveToFirst()) {
			// Recupera os ndices das colunas
			int idxBpm = cursor.getColumnIndex("bpm");
			int idxData = cursor.getColumnIndex("data");

			// Loop at o final
			do {
				Batimento batimento = new Batimento();

				//Recupera os atributos do sensor
				batimento.setBpm(cursor.getInt(idxBpm));
				
				try {
					batimento.setDate(simpleFormat.parse(cursor.getString(idxData)));
				} catch (Exception exception) {
					//Sensor Exception
				}
				
				System.out.println("Consulta DaTE: " + cursor.getString(idxData));
				
				batimentos.add(batimento);
			} while (cursor.moveToNext());
		}

		return batimentos;
	}
	
	//Insere informaes de minhas aes
	public long putBatimento(Batimento batimento) throws Exception {
		ContentValues values = new ContentValues();
		values.put("bpm", batimento.getBpm());
		values.put("data", simpleFormat.format(batimento.getDate()));

		System.out.println("Batimento DaTE: " + batimento.getDate());
		
		long id = 0;
		inserir(values, TABLE_HEART, SQLiteDatabase.CONFLICT_REPLACE);
		
		return id;
	}
	

	//Insere informaes de minhas aes
	public long putSensor(Sensor sensor) throws Exception {
		ContentValues values = new ContentValues();
		values.put("sensor_mac", sensor.getSensorMac());
		values.put("situacao", sensor.getSituacao());
		values.put("data", simpleFormat.format(sensor.getData()));
		values.put("nome", sensor.getNome());

		long id = 0;
		if(sensor.getSensorMac() != null) {
			int rows = db.updateWithOnConflict(TABLE_SENSOR, values, "sensor_mac = ?", new String[]{ sensor.getSensorMac() }, SQLiteDatabase.CONFLICT_REPLACE);
			if(rows == 0) {
				id = inserir(values, TABLE_SENSOR, SQLiteDatabase.CONFLICT_REPLACE);
			}
		} else {
			id = inserir(values, TABLE_SENSOR, SQLiteDatabase.CONFLICT_REPLACE);
		}
		
		return id;
	}

	/**
	 * Retorna um cursor para auxiliar na consulta
	 * @param tabela tabela do cursor que ser retornado
	 * @param columns array com as colunas da tabela passada
	 * @return Cursor para auxiliar na consulta;
	 */
	private Cursor getCursor(String tabela, String [] columns, String orderBy, String selection, String [] selectionArgs) throws Exception {
		try {
			return db.query(tabela , columns, selection, selectionArgs, null, null, orderBy, null);
		} catch (SQLException e) {
			Log.e(TAG, "Erro na busca: " + e.toString());
			return null;
		}
	}

	/**
	 * Insere no banco de dados um registro
	 * @param valores registro a ser inserido
	 * @param tabela nome da tabela que ser inserido o registro
	 * @param confictAlgorithm escolher como tratar se j existir o registro no banco de dados
	 * @return id do registro que foi inserido
	 * @throws Exception
	 */
	private long inserir(ContentValues valores, String tabela, int confictAlgorithm) throws Exception {
		long id = db.insertWithOnConflict(tabela, "", valores, confictAlgorithm); 
		return id;
	}

	/**
	 * Deleta alguma informao do banco de dados
	 * @param where clusula where do comando DELETE que ser executado
	 * @param whereArgs argumentos da clucula where
	 * @param tabela tabela da qual os dados sero excludos
	 * @return resultado da execuo do comando DELETE retornado pelo banco de dados
	 */
	public int deletar(String where, String[] whereArgs, String tabela) throws Exception {
		int count = db.delete(tabela, where, whereArgs);
		Log.i(TAG, "Deletou [" + count + "] registros");
		return count;
	}
}