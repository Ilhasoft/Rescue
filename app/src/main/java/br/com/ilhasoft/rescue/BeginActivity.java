package br.com.ilhasoft.rescue;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import br.com.ilhasoft.rescue.R;

public class BeginActivity extends Activity {
	
	private BeginFragment beginFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_begin);

		if (savedInstanceState == null) {
			beginFragment = new BeginFragment();
			getFragmentManager().beginTransaction().add(R.id.container, beginFragment).commit();
		}
	}
	
	@Override
	public void onBackPressed() {
		if(beginFragment != null && beginFragment.isStarted()){
			beginFragment.stopReanimation();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.begin, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
