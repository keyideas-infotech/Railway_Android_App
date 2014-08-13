package com.keyideas.indianrailways;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.keyideas.adapter.AvailabilityAdapter;
import com.keyideas.dto.AvailabilityListObject;
import com.keyideas.dto.FindTrainParameters;
import com.keyideas.dto.FindTrainResultTrainListObject;
import com.keyideas.other.CheckConnection;

@SuppressLint("NewApi")
public class AvailabilityActivity extends SherlockActivity {

	Context context;
	
	FindTrainParameters findTrainParameters;
	FindTrainResultTrainListObject findTrainResultTrainListObject;
	
	TextView no_records_text;
	ListView listView;
	ArrayList<AvailabilityListObject>  availabilityListObjects;
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.availability);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		context=this;
		
		findTrainResultTrainListObject=(FindTrainResultTrainListObject)getIntent().getSerializableExtra("findTrainResultTrainListObject");
		findTrainParameters=(FindTrainParameters)getIntent().getSerializableExtra("findTrainParameters");
		no_records_text=(TextView) findViewById(R.id.no_records_text);
		listView=(ListView) findViewById(R.id.listView);
		
		getData();
		
	}
	
	
	private void getData(){
		no_records_text.setVisibility(View.GONE);
		findViewById(R.id.container).setVisibility(View.GONE);
		if(new CheckConnection(context).isConnectedToInternet()){
			if(findTrainParameters!=null){
				
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
					new GetAvailabilityData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
				}else{
					new GetAvailabilityData().execute();
				}
				
			}
			
		}else{
			Toast.makeText(context, getResources().getString(R.string.internet_message), Toast.LENGTH_LONG).show();
		}
		
	}
	
	
	private class GetAvailabilityData extends AsyncTask<String, String, Document>{

		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
			
		}
		
		@Override
		protected Document doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			Document document=null;
			
			
			String[] dayAndMonth=findTrainParameters.getDate().split("/");
			String seatClass=null;
			
			if(findTrainParameters.getStation_class().equals("ZZ")){
				seatClass=findTrainParameters.getChoosedSeatClass();
			}else{
				seatClass=findTrainParameters.getStation_class();
			}
			
			String url="http://pnrbuddy.com/index.php/hauth/seatavail?classopt=ZZ"+"&day="+dayAndMonth[0]+
					"&month="+dayAndMonth[1]+"&quota="
					+findTrainParameters.getStation_quota()+"&seatclass="+seatClass
					+"&traindtl="+findTrainResultTrainListObject.getCheckAvailability();
			
			try {
				document=Jsoup.connect(url).get();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return document;
		}
		
		@Override
		protected void onPostExecute(Document result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			findViewById(R.id.progressBar).setVisibility(View.GONE);
			
			if(result!=null){
				try {
					
					fillData(result);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					no_records_text.setVisibility(View.VISIBLE);
					findViewById(R.id.container).setVisibility(View.GONE);
				}
				
				
				
			}else{
				no_records_text.setVisibility(View.VISIBLE);
				findViewById(R.id.container).setVisibility(View.GONE);
			}
		}
		
		
		
	}
	
	private void fillData(Document document) throws Exception{
		Elements elements=document.getElementsByTag("tr");
		
		
		if(elements.size()>1){
			
			availabilityListObjects=new ArrayList<AvailabilityListObject>();
			
			
			
			Elements elements2=elements.get(1).getElementsByTag("td");
			
			
			((TextView) findViewById(R.id.trainNumber)).setText("Train# "+elements2.get(0).text().trim());
			((TextView) findViewById(R.id.trainName)).setText(elements2.get(1).text().trim());
			((TextView) findViewById(R.id.trainDate)).setText(elements2.get(2).text().trim());
			((TextView) findViewById(R.id.destinationFrom)).setText(elements2.get(3).text().trim());
			((TextView) findViewById(R.id.destinationTo)).setText(elements2.get(4).text().trim());
			((TextView) findViewById(R.id.trainQuota)).setText(elements2.get(5).text().trim());
			
			for(int i=3;i<elements.size();i++){
				AvailabilityListObject availabilityListObject=new AvailabilityListObject();
				elements2=elements.get(i).getElementsByTag("td");
				availabilityListObject.setTrainSerialNumber(elements2.get(0).text().trim());
				availabilityListObject.setTrainDateOfDeparture(elements2.get(1).text().trim());
				availabilityListObject.setTrainStatus(elements2.get(2).text().trim());
				availabilityListObjects.add(availabilityListObject);
			}
			
			if(availabilityListObjects.size()>0){
			findViewById(R.id.container).setVisibility(View.VISIBLE);
			listView.setAdapter(new AvailabilityAdapter(context, availabilityListObjects));
			}else{
				no_records_text.setVisibility(View.VISIBLE);
			}
			
		}else{
			no_records_text.setVisibility(View.VISIBLE);
		}
		
		
		
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
				Intent intent = new Intent(context, MainList.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
				
				return true;
				
		default:return super.onOptionsItemSelected(item);
		
		
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.availability, menu);
		return true;
	}
	
	
	
		

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
 
	
	
	

}
