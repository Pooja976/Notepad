package com.Pooja.notepad;

import android.app.Application;


import com.Pooja.notepad.NotePad.NoteManager;

public class NotepadApplication extends Application
{
	NoteManager manager = null;
	
	public NotepadApplication() {
	}
	
	@Override
	public void onCreate()
	{
		this.manager = new NoteManager(getApplicationContext());
		super.onCreate();
	}

	public NoteManager getNoteManager()
	{
		return manager;
	}
}
