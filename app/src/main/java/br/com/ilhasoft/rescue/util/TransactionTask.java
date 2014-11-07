package br.com.ilhasoft.rescue.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class TransactionTask extends AsyncTask<Void, Void, Boolean>{
	
	private ProgressDialog progressDialog;
	private final Context context;
	private final Transaction transaction;
	private final int message;

	public TransactionTask(Context context, Transaction transaction, int message) {
		this.context = context;
		this.transaction = transaction;
		this.message = message;
	}

	@Override
	protected void onPreExecute() {
		transaction.onPreExecute();
		super.onPreExecute();
		openProgress();
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		transaction.onPostExecute();
		super.onPostExecute(result);
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		try{
			transaction.run();
		} catch (Throwable e){
			e.printStackTrace();
		} finally {
			try {
				closeProgress();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private void openProgress() {
		try {
			progressDialog = ProgressDialog.show(context, "", context.getString(message));
		} catch (Throwable e){
			e.printStackTrace();
		}
	}
	
	private void closeProgress() {
		try {
			if(progressDialog != null){
				progressDialog.dismiss();
				progressDialog = null;
			}
		} catch (Throwable e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onCancelled() {
		if(progressDialog != null) {
			System.out.println("ProgressDialog: " + progressDialog);
			progressDialog.dismiss();
			progressDialog = null;
		}
		super.onCancelled();
	}
}
