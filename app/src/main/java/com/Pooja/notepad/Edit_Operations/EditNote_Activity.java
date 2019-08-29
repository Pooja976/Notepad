package com.Pooja.notepad.Edit_Operations;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
//import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.Pooja.notepad.ConfirmationDialog;
//import com.mick88.notepad.NotepadApplication;
import com.Pooja.notepad.NotepadApplication;

import com.Pooja.notepad.NotePad.NoteActions;
import com.Pooja.notepad.NotePad.NoteManager;
import com.mick88.notepad.R;

import java.io.IOException;
import java.lang.reflect.Field;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class EditNote_Activity extends AppCompatActivity implements ConfirmationDialog.ConfirmationDialogListener
{
	NotepadApplication application;
	NoteActions currentNoteActions;
	NoteManager noteManager;
	ImageView imageView = null;
	EditText textEdit;
	final int NOTE_IMAGE= 2;
	
	/*Dialog IDs*/
	private final int DIALOG_DELETE		= 1;
	private final int DIALOG_RESTORE	= 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_edit);
		application = (NotepadApplication) this.getApplication();
		noteManager = application.getNoteManager();
		
		int id = getIntent().getExtras().getInt("noteId");
		currentNoteActions = noteManager.getNoteById(id);

		textEdit = (EditText) findViewById(R.id.editContent);
		imageView= (ImageView)findViewById(R.id.viewImage);
		if (currentNoteActions == null)
		{
			finish();
			return;
		}
		String s = currentNoteActions.getText();
		Bitmap bitmap = currentNoteActions.getBitmap();

		textEdit.setText(s);
		imageView.setImageBitmap(bitmap);
		moveTextCaret();
		
		try
		{
			setupActionBar();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void setupActionBar()
	{
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public void onPause()
	{
		super.onPause();		
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Deprecated
	void showOverflowButton()
	{
		try
		{
			ViewConfiguration viewConfiguration = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null)
			{
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(viewConfiguration, false);
			}			
		}
		catch (Exception e)
		{
				
		}
	}
	
	public void moveTextCaret()//this will put the cursor position in the end
	{
		textEdit.setSelection(textEdit.getText().toString().length());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.note_edit_menu, menu);
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		saveOrDelete();
		super.onBackPressed();
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
					onBackPressed();
				break;
			case R.id.saveItem:
				saveCurrentNote();
				break;
			case R.id.deleteItem:
				ConfirmationDialog dialog = ConfirmationDialog.newInstance(this, getString(R.string.dialogDeleteNote), DIALOG_DELETE);
				dialog.show(getSupportFragmentManager(), "delete");
				break;
			case R.id.undoChanges:
				ConfirmationDialog d = ConfirmationDialog.newInstance(this, getString(R.string.dialogRevertChanges), DIALOG_RESTORE);
				d.show(getSupportFragmentManager(), "restore");
				break;
			case R.id.shareItem:
				saveCurrentNote();
				currentNoteActions.share(this);
				break;
			case R.id.addImage:
				Intent intent = new Intent();
				// Show only images, no videos or anything else
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				// Always show the chooser (if there are multiple options available)
				startActivityForResult(intent, NOTE_IMAGE);
				break;
			default:
				return false;
		}
		return true;	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == NOTE_IMAGE  && data != null && data.getData() != null) {

			Uri uri = data.getData();

			try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
				 	imageView = (ImageView) findViewById(R.id.viewImage);
					imageView.setImageBitmap(bitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void saveOrDelete()
	{
		if (TextUtils.isEmpty(textEdit.getText()))
		{
			deleteCurrentNote();
		}
		else{ saveCurrentNote();}
	}
	
	void deleteCurrentNote()
	{
		noteManager.deleteNotes(currentNoteActions);
		currentNoteActions = null;
		Toast.makeText(getApplicationContext(), getString(R.string.NoteDeletedMsg), Toast.LENGTH_SHORT).show();
	}
	
	void refreshNote()
	{
		/*Reverts any changes*/
		textEdit.setText(currentNoteActions.getText());
		moveTextCaret();
	}

	private void saveCurrentNote()
	{
		try
		{
			Bitmap bitmap1= null ;
			String s = textEdit.getText().toString();

			if(imageView!=null) {
				 bitmap1 = ((BitmapDrawable)imageView.getDrawable()).getBitmap();}

			currentNoteActions.setText(s);
			currentNoteActions.setBitmap(bitmap1);
			currentNoteActions.saveToFile(getApplicationContext());
			Toast.makeText(getApplicationContext(), getString(R.string.NoteSavedMsg), Toast.LENGTH_SHORT).show();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	public void onYesClicked(DialogFragment dialog, Bundle bundle)
	{
		switch (bundle.getInt("dialogId"))
		{
			case DIALOG_DELETE:
				deleteCurrentNote();
				this.setResult(RESULT_OK);
				finish();
				break;
				
			case DIALOG_RESTORE:
				refreshNote();
				break;
		}
	}

	public void onNoClicked(DialogFragment dialog, Bundle bundle)
	{
	}
}
