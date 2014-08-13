package com.keyideas.indianrailways;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.keyideas.dto.FindTrainParameters;
import com.keyideas.other.CheckConnection;

@SuppressLint("NewApi")
public class FindTrain extends SherlockActivity {

	
	
	
	TextView btnSEARCH;
	
	AutoCompleteTextView edFROM_NAME,edTO_NAME;
	Spinner station_class_array,station_quota_array;
	DatePicker datePicker;
	String[]  junction_name;
	String [] class_name;
	String [] quota_name;
	String [] class_codes;
	
	int temp1 = 0, temp2 = 0;
	String cDATE,stemp, selectedDATE,dtemp,tempD, tempM;
	Date CurrentDate;
	LinearLayout sliderMenu;
	
	
	FillData fillData;
	ArrayAdapter<String> adapter;
	ArrayAdapter<String> stationClassAdapter;
	ArrayAdapter<String> stationQuotaAdapter;
	
	
	 AlertDialog.Builder alertDialogBuilder;
	 AlertDialog alertDialog;
	 View dialogView;
	 TextView dialogList ;
		
		TextView dialotList2;
		Button settingIcon;
		
		LocalBroadcastManager manager;
		Context context;
		int days=61;
		LayoutInflater inflater;
	
		
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
			finish();
			overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_train);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	
		context=FindTrain.this;
		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		junction_name = getResources().getStringArray(R.array.station_array);
		class_name=getResources().getStringArray(R.array.station_class);
		quota_name=getResources().getStringArray(R.array.station_quota);
		class_codes=getResources().getStringArray(R.array.station_class_codes);
		
		
		
		adapter= new ArrayAdapter<String>(
				context,
				 android.R.layout.simple_dropdown_item_1line, junction_name);
		stationClassAdapter= new ArrayAdapter<String>(
				context,
				 android.R.layout.simple_dropdown_item_1line, class_name);
		
		stationQuotaAdapter= new ArrayAdapter<String>(
				context,
				 android.R.layout.simple_dropdown_item_1line, quota_name);
		
		
		
		
		datePicker = (DatePicker)findViewById(R.id.datePicker);
		btnSEARCH = (TextView)findViewById(R.id.btnSEARCH_TRAIN);
		

		alertDialogBuilder = new AlertDialog.Builder(context);
		
		dialogView=inflater.inflate(R.layout.rowdialog, null);
		dialogList = (TextView)dialogView.findViewById(R.id.dialoglist);
		dialotList2 = (TextView)dialogView.findViewById(R.id.dialoglist2);
		alertDialogBuilder.setView(dialogView);
		
		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
							
			}
			}); 
		alertDialog=alertDialogBuilder.create();

		alertDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
			    if (inputMethodManager != null) {
			        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			    }
			}
		});
		
		edFROM_NAME = (AutoCompleteTextView)findViewById(R.id.edFROM_NAME);

		 edFROM_NAME.setThreshold(1);

		edFROM_NAME.setAdapter(adapter);

		 edTO_NAME = (AutoCompleteTextView)findViewById(R.id.edTO_NAME);

		
	
		 edTO_NAME.setAdapter(adapter);
		 edTO_NAME.setThreshold(1);
		 
		 
		 station_class_array=(Spinner) findViewById(R.id.station_class_array);
		 station_quota_array=(Spinner) findViewById(R.id.station_quota_array);
		 
		 station_class_array.setAdapter(stationClassAdapter);
		 station_quota_array.setAdapter(stationQuotaAdapter);
		 
		
		 
		 
		 
		 edTO_NAME.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				   InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(edTO_NAME.getWindowToken(), 0);
			}
		});
		 
		 btnSEARCH.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					
					
					InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
	                imm.hideSoftInputFromWindow(edTO_NAME.getWindowToken(), 0);
	                
	                
	                
					String sFROM = edFROM_NAME.getText().toString();
					String dTO = edTO_NAME.getText().toString();
					
					String station_class=class_codes[station_class_array.getSelectedItemPosition()];
					String station_quota=station_quota_array.getSelectedItem().toString().split("-")[1].trim();
					
					if (sFROM.equalsIgnoreCase("") || dTO.equalsIgnoreCase("")) {

						if (sFROM.equalsIgnoreCase("")) {
							dialotList2.setText("Source station required");
							dialotList2.setTypeface(null, Typeface.BOLD);
							alertDialog.show();
						} else if (dTO.equalsIgnoreCase("")) {
							dialotList2.setText("Destination station required");
							dialotList2.setTypeface(null, Typeface.BOLD);
							alertDialog.show();
						}else if (station_class.equalsIgnoreCase("")) {
							dialotList2.setText("Station class required");
							dialotList2.setTypeface(null, Typeface.BOLD);
							alertDialog.show();
						} else if (station_quota.equalsIgnoreCase("")) {
							dialotList2.setText("Station quota required");
							dialotList2.setTypeface(null, Typeface.BOLD);
							alertDialog.show();
						} 
						else {
							Toast.makeText(context,
									"Both Fields Required.", Toast.LENGTH_SHORT)
									.show();
						}
					} else {

						if (!sFROM.matches("[a-zA-Z /+-]+")) {
							dialotList2.setText("Accept alphabets only");
							dialotList2.setTypeface(null, Typeface.BOLD);
							alertDialog.show();
						} else if (!dTO.matches("[a-zA-Z /+-]+")) {
							dialotList2.setText("Accept alphabets only");
							dialotList2.setTypeface(null, Typeface.BOLD);
							alertDialog.show();
						} else if(sFROM.toLowerCase().equals(dTO.toLowerCase())){
							dialotList2.setText("Source and Destination stations cannot be same :)");
							dialotList2.setTypeface(null, Typeface.BOLD);
							alertDialog.show();
							
						}else{
						
							if(checkDateForNextDays()){
								
								
								if(new CheckConnection(context).isConnectedToInternet()){
									
									
									String selectedDATE = String
											.valueOf(datePicker.getDayOfMonth())
											+ "/"
											+ String.valueOf(datePicker.getMonth()+ 1)
											+ "/"
											+ String.valueOf(datePicker.getYear());
									
									Date CurrentDate = new Date();
									String cDATE = String.valueOf(CurrentDate.getDate()) + "/"
											+ String.valueOf(CurrentDate.getMonth() + 1) + "/"
											+ String.valueOf(CurrentDate.getYear() + 1900);
									
									Boolean b2 = isDateAfter(cDATE, selectedDATE);
									if (b2) {
										FindTrainParameters findTrainParameters=new FindTrainParameters();
										findTrainParameters.setDate(selectedDATE);
										findTrainParameters.setFrom(sFROM);
										findTrainParameters.setTo(dTO);
										findTrainParameters.setStation_class(station_class);
										findTrainParameters.setStation_quota(station_quota);
										Intent intent = new Intent(context, FindTrainResultActivity.class);
										intent.putExtra("findTrainParameters", findTrainParameters);
										startActivity(intent);
										overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
										
									}else {
										Toast.makeText(context, "Invalid Date",
												Toast.LENGTH_LONG).show();
									}
									
								
								
								
								}else{
									Toast.makeText(context, "No internet connection found. Please check your GPRS / Wi-fi settings first. ", Toast.LENGTH_SHORT).show();
								}  
								
								
								}
							
							
						}
					}
				}
			});
					
		 
		 new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					((ScrollView) findViewById(R.id.scrollView1)).smoothScrollTo(0, 0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1000	);
		 
		 
	
	}

	
	private boolean checkDateForNextDays(){
		try {
		 SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		
		
		String selected_date = String
				.valueOf(datePicker.getDayOfMonth())
				+ "-"
				+ String.valueOf(datePicker.getMonth()+ 1)
				+ "-"
				+ String.valueOf(datePicker.getYear());
		
		
		
		String comparison_date=	dateadding(days,sdf.format(new Date()));	
		
		 Date date1 = sdf.parse(selected_date);
    	 Date date2 = sdf.parse(comparison_date);
			
    	 if(date1.before(date2)){
    		 return true;
    	 }else{
    		 Toast.makeText(context, "Advance booking period for train tickets is reduced to 60 days. Please select another date. ", Toast.LENGTH_SHORT).show();
    		 return false;
    	 }
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
		
	}
	
	private String dateadding(int DaystoAdd,String Date){
        int Days = DaystoAdd; 
String finaldate="";
        try
        {
        Date date = null;
        String str = Date;          
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");    
        date = formatter.parse(str);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE,Days );      
        finaldate= formatter.format(calendar.getTime());
        }
        catch (Exception e)
        {
            e.printStackTrace();   
        }
        return finaldate;
    }
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		try {
			if(fillData!=null) fillData.cancel(true);
			
				
			
			//getActivity().moveTaskToBack(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private class FillData extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			fillData();
			//getActivity().moveTaskToBack(true);
		}
		
	}
	
	
	private void fillData(){
		
		

		CurrentDate = new Date();
		cDATE = String.valueOf(CurrentDate.getDate()) + "-"
				+ String.valueOf(CurrentDate.getMonth() + 1) + "-"
				+ String.valueOf(CurrentDate.getYear() + 1900);

		selectedDATE = String
				.valueOf(datePicker.getDayOfMonth())
				+ "-"
				+ String.valueOf(datePicker.getMonth()+ 1)
				+ "-"
				+ String.valueOf(datePicker.getYear());
		
		Boolean b2 = isDateAfter(cDATE, selectedDATE);
		if (b2) {
			tempD = String.valueOf(datePicker.getDayOfMonth());
			tempM = String.valueOf(datePicker.getMonth()+1);
			
			Intent intent = new Intent(context, FindTrainResultActivity.class);
			
			intent.putExtra("from", stemp);
			intent.putExtra("to", dtemp);
			intent.putExtra("month", tempM);
			intent.putExtra("day", tempD);
			
			
			
			startActivity(intent);
			overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
			
		
		} else {
			Toast.makeText(context, "Invalid Date",
					Toast.LENGTH_LONG).show();
		}
	}

	private boolean isDateAfter(String startDate, String endDate){
		try {
			String myFormatString = "dd/MM/yyyy";
			SimpleDateFormat df = new SimpleDateFormat(myFormatString);
			Date endingDate = df.parse(endDate);
			Date startingDate = df.parse(startDate);
			
			if (endingDate.equals(startingDate)) {
				return true;
			} else if (endingDate.after(startingDate))
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.find_train, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		 case android.R.id.home:
	     	finish();
				overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
	         return true;
			
			case R.id.action_settings:
				finish();
				overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
				
				return true;
				
		default:return super.onOptionsItemSelected(item);
		
		
		}
		
		
	}

}
