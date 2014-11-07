package br.com.ilhasoft.rescue.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AndroidUtils {

	public boolean isNetworkAvailable(Context context) {
		if(context != null){
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity == null) {
				return false;
			} else {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public int getNetworkTypeEnabled(Context context) {
		int networkEnabled = -1;
		if(context != null){
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity == null) {
				return -1;
			} else {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							networkEnabled = info[i].getType();
							return networkEnabled;
						}
					}
				}
			}
		}
		return networkEnabled;
	}

	public String getNetworkEnabled(Context context) {
		String networkEnabled = "";
		if(context != null){
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity == null) {
				System.out.println("Connectivity " + null);
				return networkEnabled;
			} else {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							networkEnabled = info[i].getTypeName();
							return networkEnabled;
						}
					}
				}
			}
		}
		return networkEnabled;
	}

	public boolean isLocationAvailable(Context context) {
		if(context != null){
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			if(locationManager == null){
				return false;
			} else {
				List<String> allProviders = locationManager.getAllProviders();
				
				if(allProviders != null) {
					for(String provider : allProviders) {
						Location lastLocation = locationManager.getLastKnownLocation(provider);
						if(locationManager.isProviderEnabled(provider) && lastLocation != null) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public byte[] getImageByUrl(String url) {
		try {
			URL imageUrl = new URL(url);
			URLConnection ucon = imageUrl.openConnection();

			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			ByteArrayBuffer baf = new ByteArrayBuffer(500);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			return baf.toByteArray();
		} catch (Exception e) {
			Log.d("ImageManager", "Error: " + e.toString());
		}
		return null;
	}

	public Uri getUriByUrl(File file, String url) throws Exception {
		File fileDir = file.getParentFile();

		if(!fileDir.exists()) {
			fileDir.mkdirs();
		}

		if(!file.exists()) {
			file.createNewFile();

			//Faz o download da imagem
			byte [] dataLogo = getImageByUrl(url);
			if(dataLogo == null){
				return null;
			}

			//Escreve os bytes no arquivo com a imagem
			FileOutputStream fileOutputImage = new FileOutputStream(file);
			fileOutputImage.write(dataLogo);
			fileOutputImage.close();
		}	

		//Faz a coleta do Uri da imagem salva
		Uri logoOperadoraUri = Uri.fromFile(file);
		return logoOperadoraUri;
	}

	public int getBatteryLevel(Context context){
		if(context != null){
			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			Intent batteryStatus = context.registerReceiver(null, ifilter);
			int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			float batteryPct = level / (float)scale;
			batteryPct = batteryPct * 100;
			int batteryLevel = (int)batteryPct;
			return batteryLevel;
		}
		return 0;
	}

	public String formatName(String name){
		String [] nameSplited = name.split(" ");
		String firstName = "";
		String lastName = "";
		String formatedName = "";

		if(nameSplited != null && nameSplited.length > 1){
			firstName = nameSplited[0];
			lastName = nameSplited[nameSplited.length-1];

			formatedName = firstName + " " + lastName;	
			return formatedName;
		} else {
			return name;
		}
	}

	public float convertDpToPixel(float dp,Context context){
		if(context != null) {
			Resources resources = context.getResources();
			DisplayMetrics metrics = resources.getDisplayMetrics();
			float px = dp * (metrics.densityDpi/160f);
			return px;
		}
		return 0;
	}

	public float convertPixelToDp(float px,Context context){
		if(context != null) {
			Resources resources = context.getResources();
			DisplayMetrics metrics = resources.getDisplayMetrics();
			float dp = px * (metrics.densityDpi/160f);
			return dp;
		}
		return 0;
	}

	public String getNetworkType(TelephonyManager telephonyManager){
		String networkType = "";

		switch(telephonyManager.getNetworkType()){
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			networkType = "UNKNOWN";
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			networkType = "GPRS";
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			networkType = "EDGE";
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			networkType = "UMTS";
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			networkType = "HSDPA";
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			networkType = "HSPA";
			break;
		case TelephonyManager.NETWORK_TYPE_CDMA:
			networkType = "CDMA";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			networkType = "EVDO_0";
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			networkType = "EVDO_A";
			break;
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			networkType = "1xRTT";
			break;
		case TelephonyManager.NETWORK_TYPE_IDEN:
			networkType = "IDEN";
			break;
		default:
			networkType = String.valueOf(telephonyManager.getNetworkType());
		}
		return networkType;
	}

	/**
	 * Verifica qual a rede agrupada que o cara está
	 * @param telephonyManager
	 * @return
	 */
	public String getNetworkGrouped(TelephonyManager telephonyManager){
		String networkGrouped = "";

		switch(telephonyManager.getNetworkType()){
		case TelephonyManager.NETWORK_TYPE_EDGE: case TelephonyManager.NETWORK_TYPE_1xRTT: case TelephonyManager.NETWORK_TYPE_IDEN:
			networkGrouped = "2G";
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA: case TelephonyManager.NETWORK_TYPE_HSPA: case TelephonyManager.NETWORK_TYPE_HSPAP: case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EVDO_0: case TelephonyManager.NETWORK_TYPE_EVDO_A: case TelephonyManager.NETWORK_TYPE_EVDO_B: case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_UMTS: case TelephonyManager.NETWORK_TYPE_HSUPA:
			networkGrouped = "3G";
			break;
		case TelephonyManager.NETWORK_TYPE_EHRPD: case TelephonyManager.NETWORK_TYPE_LTE:
			networkGrouped = "4G";
			break;
		default:
			networkGrouped = "3G";
		}
		return networkGrouped;
	}

	/**
	 * /**
	 * Cria uma conta modelo para o SyncAdapter
	 *
	 * @param context contexto no qual será pegado as informações
	 * @param accountName nome da conta modelo
	 * @param accountType tipo da conta modelo
	 * @return Account a ser enviada para o SyncAdapter
	 */
	public Account createSyncAccount(Context context, String accountName, String accountType) {
		// Cria um tipo de conta e uma conta padrão
		Account newAccount = new Account(accountName, accountType);

		AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		/*
		 * Add the account and account type, no password or user data
		 * If successful, return the Account object, otherwise report an error.
		 */
		if (accountManager.addAccountExplicitly(newAccount, null, null)) {
			/*
			 * If you don't set android:syncable="true" in
			 * in your <provider> element in the manifest,
			 * then call context.setIsSyncable(account, AUTHORITY, 1)
			 * here.
			 */
		} else {
			/*
			 * The account exists or some other error occurred. Log this, report it,
			 * or handle it internally.
			 */
		}
		return newAccount;
	}

	public Date blankDate(Date minDate) {
		if(minDate != null) {
			Calendar minCalendar = Calendar.getInstance();
			minCalendar.setTime(minDate);
			minCalendar.set(Calendar.HOUR_OF_DAY, 0);
			minCalendar.set(Calendar.MINUTE, 0);
			minCalendar.set(Calendar.SECOND, 0);
			minCalendar.set(Calendar.MILLISECOND, 0);
			Date minDateMod = minCalendar.getTime();
			return minDateMod;
		}
		return minDate;
	}

	public Date fullDate(Date maxDate) {
		if(maxDate != null) {
			Calendar maxCalendar = Calendar.getInstance();
			maxCalendar.setTime(maxDate);
			maxCalendar.set(Calendar.HOUR_OF_DAY, 23);
			maxCalendar.set(Calendar.MINUTE, 59);
			maxCalendar.set(Calendar.SECOND, 59);
			maxCalendar.set(Calendar.MILLISECOND, 0);
			Date maxDateMod = maxCalendar.getTime();
			return maxDateMod;
		}
		return maxDate;
	}

	public String searchPlace(Context context, Double latitude, Double longitude) throws Exception {
		String localidade = "";

		if(latitude != null && longitude != null && latitude != 0 && longitude != 0) {
			Geocoder geoCoder = new Geocoder(context);
			List<Address> addressList = geoCoder.getFromLocation(latitude, longitude, 1);
			Address currentAddress = addressList.get(0);

			String addressLine = currentAddress.getAddressLine(1);

			String estado = addressLine.substring(addressLine.length() - 2, addressLine.length());
			String bairro = currentAddress.getSubLocality();
			if(estado != null && estado.length() > 0
					&& bairro != null && bairro.length() > 0) {
				localidade = bairro + "-" + estado;
			}
		}
		return localidade;
	}
	
	public boolean isVersionBelow(){
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			return true;
		} else {
			return false;
		}
	}

	public void overrideFonts(final Context context, final View v) throws Exception {
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View child = vg.getChildAt(i);
				overrideFonts(context, child);
			}
		} else if (v instanceof TextView) {
			((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLight.ttf"));
		}
	}
}
