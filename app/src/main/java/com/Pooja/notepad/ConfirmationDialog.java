package com.Pooja.notepad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;

import com.mick88.notepad.R;

public class ConfirmationDialog extends DialogFragment
{
	Bundle bundle;
	
	public interface ConfirmationDialogListener
	{
		public void onYesClicked(DialogFragment dialog, Bundle bundle);
		public void onNoClicked(DialogFragment dialog, Bundle bundle);
	}
	
	static ConfirmationDialogListener listener;

	public ConfirmationDialog()
	{
		this.bundle=new Bundle();
	}
	@Override
	public void setArguments(Bundle bundle)
	{
		this.bundle.putAll(bundle);
		super.setArguments(bundle);
	}
	

	public static ConfirmationDialog newInstance(Activity activity, String strQues, int ID)
	{
		try
		{
			listener = (ConfirmationDialogListener)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString()+" must implement ConfirmationDialogListener");
		}
		ConfirmationDialog confirmationDialog = new ConfirmationDialog();
		Bundle bundle = new Bundle();
		bundle.putString("text", strQues);
		bundle.putInt("dialogId", ID);
		confirmationDialog.setArguments(bundle);
		return confirmationDialog;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog));
		
		builder.setMessage(bundle.getString("text"));
		builder.setTitle(R.string.app_name);
		
		builder.setPositiveButton(getString(android.R.string.yes), new OnClickListener()
		{			
			public void onClick(DialogInterface dialog, int which)
			{
				listener.onYesClicked(ConfirmationDialog.this, bundle);
			}
		});
		builder.setNegativeButton(getString(android.R.string.no), new OnClickListener()
		{
			
			public void onClick(DialogInterface dialog, int which)
			{
				listener.onNoClicked(ConfirmationDialog.this, bundle);
				
			}
		});
		return builder.create();
	}
}
