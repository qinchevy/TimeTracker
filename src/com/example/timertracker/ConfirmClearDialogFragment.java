package com.example.timertracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ConfirmClearDialogFragment extends DialogFragment {

	private TimeListAdapter mAdapter = null;
	
	public ConfirmClearDialogFragment() {
		super();
	}

	
	public TimeListAdapter getAdapter() {
		return mAdapter;
	}


	public void setAdapter(TimeListAdapter adapter) {
		this.mAdapter = adapter;
	}


	public static ConfirmClearDialogFragment newInstance(TimeListAdapter adapter) {
		ConfirmClearDialogFragment f = new ConfirmClearDialogFragment();
		f.setAdapter(adapter);
		return f;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
		.setTitle(R.string.confirm_clear_all_title)
				.setMessage(R.string.confirm_clear_all_message)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mAdapter.clear();
					}
				})
				.setNegativeButton(R.string.cancel, null).create();
	}
	
	
	
}
