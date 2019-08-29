package com.Pooja.notepad.MainViewList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
//import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Pooja.notepad.NotePad.NoteActions;
import com.Pooja.notepad.NotePad.NoteManager;
import com.Pooja.notepad.NotepadApplication;
import com.Pooja.notepad.ConfirmationDialog;

import com.mick88.notepad.R;


import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainList_Activity extends AppCompatActivity implements
		ConfirmationDialog.ConfirmationDialogListener
{

	NoteManager noteManager = null;
	NotepadApplication application;

	NoteActions selectedNoteActions = null;
	HashMap<NoteActions, View> noteTiles = new HashMap<NoteActions, View>();
	final int DIALOG_DELETE = 1, NOTE_EDIT = 2;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notepad_list);
		application = (NotepadApplication) this.getApplication();
		noteManager = application.getNoteManager();

		noteManager.loadNotes();
		loadNotes();

		/* Handling incoming intent */
		Intent intent = getIntent();
		String type = intent.getType(), action = intent.getAction();

		if (type != null && Intent.ACTION_SEND.equals(action))
		{
			/* Intent received */
			if (type.startsWith("text/"))
			{
				String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (sharedText != null)
				{
					openNote(new NoteActions(noteManager, sharedText,null));
				}
			}
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_item_context, menu);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo contextMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		NoteActions selectedNoteActions = noteManager.getNoteById(contextMenuInfo.id);

		switch (item.getItemId())
		{
		case R.id.deleteText:
			ConfirmationDialog dialog = ConfirmationDialog.newInstance(this,
                    getString(R.string.dialogDeleteSelected), DIALOG_DELETE);
			Bundle b = new Bundle();
			b.putInt("noteId", selectedNoteActions.getID());
			dialog.setArguments(b);
			dialog.show(getSupportFragmentManager(), "contextDelete");
			break;

		case R.id.editText:
			openNote((int) contextMenuInfo.id);
	 		break;
		}

		return super.onContextItemSelected(item);
	}

	void collapseNote(NoteActions noteActions)
	{
		View v = noteTiles.get(noteActions);
		v.findViewById(R.id.tile_options).setVisibility(View.GONE);
		((TextView) v.findViewById(R.id.noteTitle)).setMaxLines(getResources()
				.getInteger(R.integer.max_tile_lines));
		((ImageView)v.findViewById(R.id.btn_tile_menu)).setImageResource(R.drawable.icon_dark_expand);
	}

	void expandNoteTile(NoteActions noteActions)
	{
		View tile = noteTiles.get(noteActions);
		
		tile.findViewById(R.id.tile_options).setVisibility(View.VISIBLE);
		((ImageView)tile.findViewById(R.id.btn_tile_menu)).setImageResource(R.drawable.icon_dark_collapse);
		((TextView) tile.findViewById(R.id.noteTitle)).setMaxLines(9);
	}
	
	/**
	 * Selects noteActions by expanding its tile.
	 * @param noteActions
	 */
	void selectNote(NoteActions noteActions)
	{
		// Unknown noteActions?
		if (noteTiles.containsKey(noteActions) == false) return;
		
		if (selectedNoteActions != null)
		{
			collapseNote(selectedNoteActions);
			selectedNoteActions =null;
		}
		expandNoteTile(noteActions);
		selectedNoteActions = noteActions;
	}

	private void addTile(NoteActions noteActions)
	{
		Animation tileAnimation = 
				new TranslateAnimation(1000,0,0,0);
		tileAnimation.setDuration(300);
		tileAnimation.setFillAfter(true);
		
		findViewById(R.id.emptyNotifier).setVisibility(View.GONE);
		addTile(noteActions, (ViewGroup)findViewById(R.id.tile_container), (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE), tileAnimation);
		selectNote(noteActions);
	}
	
	private void removeTile(NoteActions noteActions)
	{
		if (noteTiles.containsKey(noteActions) == false) return;
		if (selectedNoteActions == noteActions) selectedNoteActions = null;
		
		Animation tileAnimation = 
				new TranslateAnimation(0,1000,0,0);
		tileAnimation.setDuration(300);
		tileAnimation.setFillAfter(true);
		
		final View tile = noteTiles.get(noteActions);
		tile.setAnimation(tileAnimation);
		
		tile.setVisibility(View.INVISIBLE);
		
		final Handler handler = new Handler();
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
			
			@Override
			public void run()
			{
				handler.post(new Runnable()
				{
					
					public void run()
					{
						ViewGroup parent = (ViewGroup) findViewById(R.id.tile_container);
						parent.removeView(tile);
						if (parent.getChildCount() == 0)
						{
							findViewById(R.id.emptyNotifier).setVisibility(View.VISIBLE);
						}
					}
				});
			}
		}, 500);
	}
	
	/**
	 * Adds tile - use this in a loop and remove "List empty" beforehand!
	 * @param noteActions
	 * @param parent
	 * @param inflater
	 */
	private void addTile(NoteActions noteActions, ViewGroup parent, LayoutInflater inflater, Animation inAnimation)
	{
		final ViewGroup child = (ViewGroup) inflater.inflate(
				R.layout.note_list_tile, parent, false);
		noteTiles.put(noteActions, child);
		TextView tv = (TextView) child.findViewById(R.id.noteTitle);
		tv.setText(noteActions.getText());
		final NoteActions n = noteActions;
		
		child.findViewById(R.id.btn_tile_expand).setOnClickListener(
				new OnClickListener()
				{
					public void onClick(View v)
					{
						if (selectedNoteActions == n)
						{
							collapseNote(n);
							selectedNoteActions = null;
							return;
						} else if (selectedNoteActions != null)
						{
							collapseNote(selectedNoteActions);
						}
						selectedNoteActions = n;
						expandNoteTile(n);

					}
				});
		child.findViewById(R.id.tile_clickable).setOnClickListener(
				new OnClickListener()
				{
					public void onClick(View v)
					{
						openNote(n);
					}
				});
		child.findViewById(R.id.tile_clickable).setOnLongClickListener(
				new OnLongClickListener()
				{

					public boolean onLongClick(View v)
					{
						if (selectedNoteActions == n) return true;
						else if (selectedNoteActions != null)
						{
							collapseNote(selectedNoteActions);
						}
						selectedNoteActions = n;
						expandNoteTile(n);
						return true;
					}
				});
		implementMenuItems(child.findViewById(R.id.tile_options), n);
		
		child.setAnimation(inAnimation);
		parent.addView(child);
	}
	
	public void fillNoteItems()
	{
		List<NoteActions> noteActions = noteManager.getAllNotes();
		TextView textViewEmpty = (TextView) findViewById(R.id.emptyNotifier);
		ViewGroup parent = (ViewGroup) findViewById(R.id.tile_container);
		noteTiles.clear();
		parent.removeAllViews();
		selectedNoteActions =null;
		if (noteManager.isEmpty() == true)
		{
			textViewEmpty.setVisibility(View.VISIBLE);
			return;
		}
		textViewEmpty.setVisibility(View.GONE);
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		for (NoteActions note : noteActions)
		{

			addTile(note, parent, inflater, null);
		}
	}
	
	private void implementMenuItems(View container, final NoteActions noteActions)
	{
		final AppCompatActivity activity = this;

		container.findViewById(R.id.btn_tile_delete).setOnClickListener(new OnClickListener()
		{
			
			public void onClick(View v)
			{
				ConfirmationDialog dialog = ConfirmationDialog.newInstance(activity,
                        getString(R.string.dialogDeleteSelected), DIALOG_DELETE);
				Bundle b = new Bundle();
				b.putInt("noteId", noteActions.getID());
				dialog.setArguments(b);
				dialog.show(getSupportFragmentManager(), "deleteText");
			}
		});
		
		container.findViewById(R.id.btn_tile_delete).setOnLongClickListener(new OnLongClickListener()
		{
			
			public boolean onLongClick(View v)
			{
				noteManager.deleteNotes(noteActions);
				removeTile(noteActions);
				//Toast.makeText(getApplicationContext(), getString(R.string.toastNoteDeleted), Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		container.findViewById(R.id.btn_tile_share).setOnClickListener(new OnClickListener()
		{
			
			public void onClick(View v)
			{
				noteActions.share(activity);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_notepad_list, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.addNote:
			openNote(new NoteActions(noteManager));
			break;
		case android.R.id.home:
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent)
	{
		switch (requestCode)
		{
		case NOTE_EDIT:
			loadNotes();
			break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	public void loadNotes()
	{
		fillNoteItems();
	}

	void openNote(int noteId)
	{
		Intent intent = new Intent(this, com.Pooja.notepad.Edit_Operations.EditNote_Activity.class);
		intent.putExtra("noteId", noteId);
		intent.putExtra("noteText", noteManager.getNoteById(noteId).getText());
		startActivityForResult(intent, NOTE_EDIT);
	}

	/**
	 * Opens noteActions in editor. Adds noteActions to the noteActions manager.
	 */
	void openNote(NoteActions noteActions)
	{
		noteManager.addNote(noteActions);
		openNote(noteActions.getID());
	}

	public void onYesClicked(DialogFragment dialog, Bundle bundle)
	{
		switch (bundle.getInt("dialogId"))
		{
		case DIALOG_DELETE:
			if (bundle.containsKey("noteId") == true)
			{
				int noteId = bundle.getInt("noteId");
				removeTile(noteManager.getNoteById(noteId));
				noteManager.deleteNote(noteId);
					
				Toast.makeText(getApplicationContext(), getString(R.string.NoteDeletedMsg), Toast.LENGTH_SHORT).show();
			}
			break;
		}

	}

	public void onNoClicked(DialogFragment dialog, Bundle bundle)
	{

	}
}
