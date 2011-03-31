/*
 * GT-Mconf: Multiconference system for interoperable web and mobile
 * http://www.inf.ufrgs.br/prav/gtmconf
 * PRAV Labs - UFRGS
 * 
 * This file is part of Mconf-Mobile.
 *
 * Mconf-Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mconf-Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mconf-Mobile.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mconf.bbb.android;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.mconf.bbb.api.Meeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
//page where the user chooses the room, the name, and connects to a conference
public class LoginPage extends Activity {

	private static final Logger log = LoggerFactory.getLogger(LoginPage.class);

	public static final String SERVER_CHOSED ="org.mconf.bbb.android.Client.SERVER_CHOSED";
	public static final String CHOOSE_SERVER="Choose a Server";

	SharedPreferences preferencesFile;
	Map<String,String> storedPreferences;

	private ArrayAdapter<String> spinnerAdapter;
	private boolean moderator;
	//private static final String labelCreateMeeting = "== Create a new meeting ==";
	private String username="Android";
	private String serverURL="";
	private String meeting="custom Meeting";
	//private Context context = this;
	BroadcastReceiver serverChosed = new BroadcastReceiver(){ 
		public void onReceive(Context context, Intent intent)
		{ 
			Bundle extras = intent.getExtras();
			serverURL=extras.getString("serverURL");
			Button serverView = (Button) findViewById(R.id.server);
			serverView.setText(serverURL);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		//get Username and server if already saved on the preferences file
		setPreferencesFile();
		setUserPreferences();

		final EditText editName = (EditText) findViewById(R.id.login_edittext_name);
		editName.setText(username);
		Button serverView = (Button) findViewById(R.id.server);
		if(serverURL.length()>3)
			serverView.setText(serverURL);
		else
			serverView.setText(CHOOSE_SERVER);


		final Spinner spinner = (Spinner) findViewById(R.id.login_spinner);
		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);

		spinner.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					updateMeetingsList();
					return true;
				} 
				return false;
			}
		});

		//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		//
		//			@Override
		//			public void onItemSelected(AdapterView<?> parent, View view,
		//					int position, long id) {
		//				
		//				// the create new meeting label
		//				if (spinnerAdapter.getItem(position).equals(labelCreateMeeting)) {
		//					final AlertDialog.Builder alert = new AlertDialog.Builder(LoginPage.this);
		//					final EditText input = new EditText(LoginPage.this);
		//					alert.setTitle("New meeting");
		//					alert.setMessage("Enter the meeting name:");
		//					alert.setView(input);
		//					alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
		//						
		//						@Override
		//						public void onClick(DialogInterface dialog, int which) {
		//							runOnUiThread(new Runnable() {
		//								
		//								@Override
		//								public void run() {
		//									spinnerAdapter.add(input.getText().toString());
		//									spinnerAdapter.notifyDataSetChanged();
		//									spinner.setSelection(spinnerAdapter.getCount()-1);
		//								}
		//							});
		//						}
		//					});
		//					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		//						
		//						@Override
		//						public void onClick(DialogInterface dialog, int which) {
		//							
		//						}
		//					});
		//					alert.show();
		//				}
		//			}
		//
		//			@Override
		//			public void onNothingSelected(AdapterView<?> parent) {
		//			}
		//		});

		final Button join = (Button) findViewById(R.id.login_button_join);       
		join.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick(View viewParam)
			{
				EditText usernameEditText = (EditText) findViewById(R.id.login_edittext_name);
				final String username = usernameEditText.getText().toString();

				if (username.length() < 1) {
					Toast.makeText(getApplicationContext(), R.string.login_empty_name, Toast.LENGTH_SHORT).show();  
					return;
				}

				if (spinner.getSelectedItemPosition() == Spinner.INVALID_POSITION) {
					Toast.makeText(getApplicationContext(), R.string.login_select_meeting, Toast.LENGTH_SHORT).show();
					return;
				}
//				if (spinner.getSelectedItem() == labelCreateMeeting)
//				{
//					System.out.println("dialog");
//					final AlertDialog.Builder alert = new AlertDialog.Builder(context);
//					final EditText input = new EditText(context);
//					alert.setView(input);
//					alert.setTitle("Type the meeting name");
//					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int whichButton) {
//							meeting = input.getText().toString().trim();
//							System.out.println(meeting);
//							connect();
//
//						}
//					});
//
//					alert.setNegativeButton("Cancel",
//							new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int whichButton) {
//							dialog.cancel();
//						}
//					});
//					alert.show();
//
//				}
				else
				{
					System.out.println("no dialog");
					meeting = (String) spinner.getSelectedItem();



					connect();
				}
			}
		}
		);
		//button to change the server
		final Button server = (Button) findViewById(R.id.server);       
		server.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick(View viewParam)
			{
				Intent intent = new Intent(getApplicationContext(), ServerChoosing.class);
				log.debug("BACK_TO_SERVERS");
				startActivity(intent);
			}

		}
		);


		updateRoleOption();
		RadioGroup role = (RadioGroup) findViewById(R.id.login_role);
		role.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				updateRoleOption();
			}
		});

		IntentFilter filter = new IntentFilter(SERVER_CHOSED); 
		registerReceiver(serverChosed, filter); 
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(serverChosed);
		super.onDestroy();
	}

	private void updateRoleOption() {
		RadioButton moderator = (RadioButton) findViewById(R.id.login_role_moderator);
		if (moderator.isChecked())
			this.moderator = true;
		else
			this.moderator = false;
	}

	private void updateMeetingsList() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(R.string.wait);
		progressDialog.setMessage(getResources().getString(R.string.login_updating));

		final Thread updateThread = new Thread(new Runnable() {
			@Override
			public void run() {			        
				if (!Client.bbb.getJoinService().load(serverURL)) {
					progressDialog.dismiss();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), R.string.login_cant_contact_server, Toast.LENGTH_SHORT).show();
						}
					});
					log.error("Can't contact the server. Try it later");
					return;
				}

				if (Thread.interrupted())
					return;

				final List<Meeting> meetings = Client.bbb.getJoinService().getMeetings();

				progressDialog.dismiss();

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						spinnerAdapter.clear();
						for (Meeting m : meetings) {
							spinnerAdapter.add(m.getMeetingID());
						}
						spinnerAdapter.sort(new Comparator<String>() {

							@Override
							public int compare(String s1, String s2) {
								return s1.compareTo(s2);
							}
						});
						//spinnerAdapter.add(labelCreateMeeting);
						spinnerAdapter.notifyDataSetChanged();
						Spinner spinner = (Spinner) findViewById(R.id.login_spinner);
						spinner.performClick();
					}
				});
			}
		});

		progressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				updateThread.interrupt();
				progressDialog.dismiss();
			}
		});

		progressDialog.show();
		updateThread.start();		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Client.MENU_ABOUT, Menu.NONE, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Client.MENU_ABOUT:
			new AboutDialog(this).show();
			return true;
		default:			
			return super.onOptionsItemSelected(item);
		}
	}    

	public SharedPreferences getPreferencesFile() {
		return preferencesFile;
	}

	public void setUserPreferences(){
		if(!storedPreferences.isEmpty())
		{
			this.username = this.storedPreferences.get("username");
			this.serverURL = this.storedPreferences.get("serverURL");
		}
	}

	@SuppressWarnings("unchecked")
	public void setPreferencesFile() {
		if(this.getSharedPreferences("storedPreferences", MODE_PRIVATE)!=null)
			this.preferencesFile = this.getSharedPreferences("storedPreferences", MODE_PRIVATE);
		else
		{
			SharedPreferences.Editor serverEditor = preferencesFile.edit();
			serverEditor.commit(); 
			this.preferencesFile = this.getSharedPreferences("storedPreferences", MODE_PRIVATE);
		}
		this.storedPreferences = (Map<String, String>) preferencesFile.getAll();
	}

	public void updatePreferences(String username, String serverURL)
	{
		SharedPreferences.Editor preferenceEditor = preferencesFile.edit();
		if(!preferencesFile.getString("username", "").equals(username))
		{
			preferenceEditor.remove("username");
			preferenceEditor.putString("username", username);

		}
		if(!preferencesFile.getString("serverURL", "").equals(serverURL))
		{
			preferenceEditor.remove("serverURL");
			preferenceEditor.putString("serverURL", serverURL);
		}
		preferenceEditor.commit();
	}

	public void connect ()
	{
		Client.bbb.getJoinService().join(meeting, username, moderator);
		if (Client.bbb.getJoinService().getJoinedMeeting() == null) {
			Toast.makeText(getApplicationContext(), R.string.login_cant_join, Toast.LENGTH_SHORT).show();
			return;
		}

		updatePreferences(username, serverURL);

		Intent myIntent = new Intent(getApplicationContext(), Client.class);
		myIntent.putExtra("username", username);
		startActivity(myIntent);

		finish();
	}


}
