package com.sumbioun.android.fragments;

import com.sumbioun.android.pitstop.R;
import com.sumbioun.android.pitstop.customwidgets.CustTextView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class PitstopHelp extends Fragment {

	private CustTextView mLogo;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.pitstop_help, container, false);

		mLogo = (CustTextView) view.findViewById(R.id.pitstop_help_logo);	
		mLogo.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				//TODO: add user help functionality
			}

		});
		
        return view;
    }
	
}
