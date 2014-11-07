package br.com.ilhasoft.rescue;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import br.com.ilhasoft.rescue.R;

public class UrineFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		
		try {
			view = inflater.inflate(R.layout.fragment_urine, null);
			
			
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		
		return view;
	}
	
}
