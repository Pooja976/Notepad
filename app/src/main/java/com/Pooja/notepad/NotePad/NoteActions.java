/**
 * 
 */
package com.Pooja.notepad.NotePad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;


import com.mick88.notepad.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ author Pooja
 * 
 */
public class NoteActions implements Serializable
{
	private String text;
	private Bitmap setImage;
	String fileName = "";
	NoteManager noteManager = null;
	List<String> hyperlinks = new ArrayList<String>();

	public static final int BUFFER_SIZE = 512;

	public NoteActions(NoteManager noteManager) {
		this.noteManager = noteManager;
		this.text = "";
	}

	public NoteActions(NoteManager noteManager, String content,Bitmap bitmap) {
		this(noteManager);
		if (content == null)
			setText("");
		else
			setText(content);
		if(bitmap==null)
			setBitmap(null);
		else
			setBitmap(bitmap);
	}

	public NoteActions(NoteManager noteManager, CharSequence content,Bitmap bitmap) {
		this(noteManager, content.toString(),bitmap);
	}

	public String getText()
	{
		return text;
	}
	public Bitmap getBitmap() { return setImage;}

	public void setText(String t)
	{
		this.text = t;
	}
	public void setBitmap(Bitmap bitmap)
	{
		setImage=bitmap;
	}

	public void appendText(String t)
	{
		setText(text.concat(t));
	}

	public String getStart(int numberCharacters, boolean ignoreNewline,
			boolean addEllipsis)
	{
		String s = ignoreNewline ? text : text.trim();
		int end = Math.min(numberCharacters, s.length());
		if (ignoreNewline == false)
		{
			int nlPos = s.indexOf('\n');
			if (nlPos > 0)
			{
				end = Math.min(end, nlPos);
			}
		}
		return addEllipsis ? s.substring(0, end) + "�" : s.substring(0, end);
	}

	public String toString()
	{
		return getStart(100, false, false);
	}

	public int getID()
	{
		return noteManager.noteActions.indexOf(this);
	}

	void delete(Context context)
	{
		if (TextUtils.isEmpty(fileName) == false)
		{
			context.deleteFile(fileName);
		}
	}

	public boolean findChanges(String newVersion)
	{
		return (text.equals(newVersion) == false);
	}

	public void share(Context context)
	{
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(android.content.Intent.EXTRA_TITLE,
				context.getString(R.string.shareEntityName));
		share.putExtra(android.content.Intent.EXTRA_TEXT, text);
		share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(Intent.createChooser(share,
				context.getString(R.string.sharePromptText)));
	}

	public void saveToFile(Context context) throws IOException
	{
		if (TextUtils.isEmpty(fileName) == true)
		{
			fileName = noteManager.createFileName();
		}
		FileOutputStream file = context.openFileOutput(fileName,
				Context.MODE_PRIVATE);

		byte[] buffer = text.getBytes();
		file.write(buffer);
		file.flush();
		file.close();
	}

	/**
	 * Reads a note from file
	 * 
	 * @return
	 * @throws IOException
	 */
	public static NoteActions readFile(NoteManager noteManager, Context context,
										  String filename) throws IOException {
		FileInputStream inputFileStream = context.openFileInput(filename);
		StringBuilder stringBuilder = new StringBuilder();
		byte[] buffer = new byte[BUFFER_SIZE];
		int len;

		while ((len = inputFileStream.read(buffer)) > 0) {
			String line = new String(buffer, 0, len);
			stringBuilder.append(line);

			buffer = new byte[NoteActions.BUFFER_SIZE];
		}

		NoteActions n = new NoteActions(noteManager, stringBuilder.toString().trim(),null);
		n.fileName = filename;
		inputFileStream.close();

		return n;
	}
}
