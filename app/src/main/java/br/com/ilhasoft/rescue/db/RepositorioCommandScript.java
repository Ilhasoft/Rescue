package br.com.ilhasoft.rescue.db;

import android.content.Context;

public class RepositorioCommandScript extends RepositorioCommand {
	
	private static final int versao = 2;
	
	private static final String[] SCRIPT_CREATE_DATABASE = {
							"CREATE TABLE sensor (sensor_mac text primary key," +
							"sensor_id_rest integer," +
							"situacao text not null," +
							"nome text," +
							"icone text,"+
							"data text not null);",
							"CREATE TABLE heart (heart_id INTEGER PRIMARY KEY AUTOINCREMENT," +
							"bpm integer not null," +
							"data text not null);"
							};
	
	private static final String [] SCRIPT_DATABASE_DELETE = {"DROP TABLE IF EXISTS sensor;"
															,"DROP TABLE IF EXISTS heart;"};
	
	private SQLiteHelper dbHelper;
	
	private static RepositorioCommandScript instance;
	
	public static RepositorioCommandScript getInstance(Context context) {
		if(instance == null){
			synchronized (RepositorioCommandScript.class){
				if (instance == null) {
					instance = new RepositorioCommandScript(context);
                }
			}
		}
		return instance;
	}
	
	private RepositorioCommandScript(Context context) {
		dbHelper = new SQLiteHelper(context
				                  , RepositorioCommand.NOME_BANCO
				                  , versao
				                  , RepositorioCommandScript.SCRIPT_CREATE_DATABASE
				                  , RepositorioCommandScript.SCRIPT_DATABASE_DELETE);
		db = dbHelper.getWritableDatabase();
	}
	
	@Override
	public void finalize() {
		if (db != null) {
			db.close();
		}
	}
}