package br.com.ilhasoft.rescue.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Implementacao de SQLiteOpenHelper
 * 
 * Classe utilitria para abrir, criar, e atualizar o banco de dados
 * 
 * @author ricardo
 */
public class SQLiteHelper extends SQLiteOpenHelper {

	private static final String CATEGORIA = "CROWDMOBI";

	private String[] scriptSQLCreate;
	private String[] scriptSQLDelete;

	/**
	 * Cria uma instncia de SQLiteHelper
	 * 
	 * @param context
	 * @param nomeBanco nome do banco de dados
	 * @param versaoBanco verso do banco de dados (se for diferente  para atualizar)
	 * @param scriptSQLCreate SQL com o create table..
	 * @param scriptSQLDelete SQL com o drop table...
	 */
	public SQLiteHelper(Context context, String nomeBanco, int versaoBanco, String[] scriptSQLCreate, String [] scriptSQLDelete) {
		super(context, nomeBanco, null, versaoBanco);
		this.scriptSQLCreate = scriptSQLCreate;
		this.scriptSQLDelete = scriptSQLDelete;
	}

	//Criar novo banco...
	@Override
	public void onCreate(SQLiteDatabase db){
		try {
			Log.i(CATEGORIA, "Criando banco com sql");
			int qtdeScripts = scriptSQLCreate.length;
	
			// Executa cada sql passado como parmetro
			for (int i = 0; i < qtdeScripts; i++) {
				String sql = scriptSQLCreate[i];
				Log.i(CATEGORIA, sql);
				// Cria o banco de dados executando o script de criao
				db.execSQL(sql);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	//Mudou a verso...
	@Override
	public void onUpgrade(SQLiteDatabase db, int versaoAntiga, int novaVersao) {
		Log.w(CATEGORIA, "Atualizando da verso " + versaoAntiga + " para " + novaVersao + ". Todos os registros sero deletados.");
		Log.i(CATEGORIA, "Delete Script: " + scriptSQLDelete);
		
		for(int i = 0; i < scriptSQLDelete.length; i++) {
			String sql = scriptSQLDelete[i];
			// Deleta as tabelas...
			db.execSQL(sql);
		}
		// Cria novamente...
		onCreate(db);
	}
}