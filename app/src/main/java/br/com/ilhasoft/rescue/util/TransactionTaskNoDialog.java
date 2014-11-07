package br.com.ilhasoft.rescue.util;

import android.os.AsyncTask;

public class TransactionTaskNoDialog extends AsyncTask<Void, Void, Boolean> {
	
	private final Transaction transaction;

	public TransactionTaskNoDialog(Transaction paramTransaction) {
		this.transaction = paramTransaction;
	}

	protected Boolean doInBackground(Void... params) {
		try {
			this.transaction.run();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return true;
	}

	protected void onPreExecute() {
		this.transaction.onPreExecute();
		super.onPreExecute();
	}
	
	protected void onPostExecute(Boolean result) {
		this.transaction.onPostExecute();
		super.onPostExecute(result);
	}
}