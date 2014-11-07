package br.com.ilhasoft.rescue.util;

public abstract class Transaction {

	public void onPostExecute(){}

	public void onPreExecute() {}

	public abstract void run();
}