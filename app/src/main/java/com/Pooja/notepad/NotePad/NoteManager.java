package com.Pooja.notepad.NotePad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



import android.content.Context;

/**
 * Contains and manages list of NotePad
 * @author Michal
 *
 */
public class NoteManager
{
	Context context=null;
	ArrayList<com.Pooja.notepad.NotePad.NoteActions> noteActions = new ArrayList<com.Pooja.notepad.NotePad.NoteActions>();
	
	public NoteManager(Context context) 
	{
		this.context = context;
	}
	
	
	public boolean isEmpty()
	{
		return noteActions.isEmpty();
	}
	
	public int getNumNotes()
	{
		return noteActions.size();
	}
	
	public boolean getNoteByText(String t)
	{
		for (com.Pooja.notepad.NotePad.NoteActions n : noteActions) if (n.findChanges(t) == false)
		{
			return true;
		}
		return false;
	}
	
	public boolean getNoteByText(CharSequence text)
	{
		return getNoteByText(text.toString());
	}
	
	public List<com.Pooja.notepad.NotePad.NoteActions> getAllNotes()
	{
		return noteActions;
	}
	
	public List<String> getNoteActions()
	{
		ArrayList<String> strings = new ArrayList<String>(noteActions.size());
		for (com.Pooja.notepad.NotePad.NoteActions noteActions : this.noteActions) strings.add(noteActions.toString());
		return strings;
	}
	
	public com.Pooja.notepad.NotePad.NoteActions getNoteById(int id)
	{
		return noteActions.get(id);
	}
	
	public com.Pooja.notepad.NotePad.NoteActions getNoteById(long id)
	{
		return getNoteById((int)id);
	}
	
	/**
	 * Deletes noteActions file and removes it from list
	 * @param noteActions
	 */
	public void deleteNotes(com.Pooja.notepad.NotePad.NoteActions noteActions)
	{
		noteActions.delete(context);
		this.noteActions.remove(noteActions);
	}
	
	public void deleteNote(int index)
	{
		noteActions.get(index).delete(context);
		noteActions.remove(index);
	}
	
	public void addNote(com.Pooja.notepad.NotePad.NoteActions noteActions)
	{
		if (noteActions == null || this.noteActions.contains(noteActions)) return;
		noteActions.noteManager = this;
		this.noteActions.add(noteActions);
		try
		{
			noteActions.saveToFile(context);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public String createFileName(int ID)
	{
		String name = String.format(Locale.getDefault(), "Note_%d.txt", ID);
		if (context.getFileStreamPath(name).exists() == true) return createFileName(ID+1);
		else return name;
	}
	
	public String createFileName()
	{
		return createFileName(0);
	}

	
	public void loadNotes()
	{
		String[] files = context.fileList();
		noteActions.clear();
		for (String fname:files)
		{
			try
			{
				noteActions.add(com.Pooja.notepad.NotePad.NoteActions.readFile(this, context, fname));
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
}
