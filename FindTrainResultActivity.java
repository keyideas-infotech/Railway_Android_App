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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.keyideas.adapter.FindTrainResultAdapter;
import com.keyideas.dto.FindTrainParameters;
import com.keyideas.dto.FindTrainResultTrainListObject;
import com.keyideas.other.CheckConnection;


@SuppressLint("NewApi")
public class FindTrainResultActivity extends SherlockActivity {

	Context context;
	FindTrainParameters findTrainParameters;
	ListView listView;
	ArrayList<FindTrainResultTrainListObject> trainResultTrainListObjects;	
	TextView no_records_text;
	String days[]=new String[]{"Mo","Tue","Wed","Th","Fr","Sa","Su"};
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_train_result);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		context=this;
		no_records_text=(TextView) findViewById(R.id.no_records_text);
		listView=(ListView) findViewById(R.id.listView);
		findTrainParameters=(FindTrainParameters)getIntent().getSerializableExtra("findTrainParameters");
		
		
		
		listView.setOnItemClickListener(new OnItemClickListener() {
		
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if(!findTrainParameters.getStation_class().trim().equals("ZZ")){
				Intent intent=new Intent(context,AvailabilityActivity.class);
				intent.putExtra("findTrainParameters", findTrainParameters);
				intent.putExtra("findTrainResultTrainListObject", trainResultTrainListObjects.get(arg2));
				
				startActivity(intent);
				overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
				}else{
					Toast.makeText(context, "Please click on class (Red Bubble)", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		
		
		
		
		getData();
		
		
		
		
	}
	
	
	
	
	private void getData(){
		no_records_text.setVisibility(View.GONE);
		if(new CheckConnection(context).isConnectedToInternet()){
			if(findTrainParameters!=null){
				
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
					new GetTrainsList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
				}else{
					new GetTrainsList().execute();
				}
				
			}
			
		}else{
			Toast.makeText(context, getResources().getString(R.string.internet_message), Toast.LENGTH_LONG).show();
		}
		
	}
	
	

	private class GetTrainsList extends AsyncTask<String, String, Document>{
		
		
		
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
			String url="http://pnrbuddy.com/index.php/hauth/seatavailtrains?class="+findTrainParameters.getStation_class()
					+"&date="+findTrainParameters.getDate()+"&from="+findTrainParameters.getFrom()+"&quota="
					+findTrainParameters.getStation_quota()+"&to="+findTrainParameters.getTo();
			
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
					
				}
				
				
				
			}else{
				no_records_text.setVisibility(View.VISIBLE);
			}
		}
		
		
	}
	
	
	private void fillData(Document document) throws Exception{
		
		

		Elements elements=document.getElementsByTag("tr");
	
		if(elements.size()>1){
			
			
			trainResultTrainListObjects=new ArrayList<FindTrainResultTrainListObject>();
			
			for(int i=1;i<elements.size();i++){
				
				FindTrainResultTrainListObject findTrainResultTrainListObject=new FindTrainResultTrainListObject();
				Elements elements2=elements.get(i).getElementsByTag("td");
				
				findTrainResultTrainListObject.setTrainNumber(elements2.get(0).text().trim());
				findTrainResultTrainListObject.setTrainName(elements2.get(1).text().trim());
				findTrainResultTrainListObject.setFrom(elements2.get(2).text().trim());
				findTrainResultTrainListObject.setDepartureTime(elements2.get(3).text().trim());
				findTrainResultTrainListObject.setTo(elements2.get(4).text().trim());
				findTrainResultTrainListObject.setArrivalTime(elements2.get(5).text().trim());
				findTrainResultTrainListObject.setTraveTime(elements2.get(6).text().trim());
			
				StringBuilder runsOn=new StringBuilder();
				
				for(int j=7;j<14;j++){
					if(elements2.get(j).text().trim().equals("Y")){
						runsOn.append(days[j-7]+", ");
						
					}
				}
				runsOn.setLength(runsOn.length()-2);
				findTrainResultTrainListObject.setRunsOn(runsOn.toString());
				String availability=null;
				
				ArrayList<String> trainAvailabilityClasses=new ArrayList<String>();
				
				for(int k=14;k<elements2.size();k++){
					try {
						String s=null;
						availability = (s=elements2.get(k).select("a[href]").
								attr("onclick").split("\\(")[1].split(",")[0]).trim().
								substring(1, s.length()-1).trim();
						
						if(findTrainParameters.getStation_class().trim().equals("ZZ")){
							trainAvailabilityClasses.add((s=elements2.get(k).select("a[href]").
									attr("onclick").split("\\(")[1].split(",")[4]).trim().
									substring(1, s.length()-2).trim());
						}else{
							break;
						}
						
					
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}
						
				}
				
				
			
			
				findTrainResultTrainListObject.setCheckAvailability(availability);
				findTrainResultTrainListObject.setTrainAvailabilityClasses(trainAvailabilityClasses);
				
				trainResultTrainListObjects.add(findTrainResultTrainListObject);
			}
			
			
		}
		
		if(trainResultTrainListObjects!=null&&trainResultTrainListObjects.size()>0){
			listView.setAdapter(new FindTrainResultAdapter(context, 
					trainResultTrainListObjects,new FindTrainResultAdapter.TrainClassCircelClickListener() {
				
				@Override
				public void onCircleClick(int position, String trainClass) {
					// TODO Auto-generated method stub
					Intent intent=new Intent(context,AvailabilityActivity.class);
					findTrainParameters.setChoosedSeatClass(trainClass);
					intent.putExtra("findTrainParameters", findTrainParameters);
					intent.putExtra("findTrainResultTrainListObject", trainResultTrainListObjects.get(position));
					
					startActivity(intent);
					overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
				}
			}));
			
			
		}else{
			no_records_text.setVisibility(View.VISIBLE);
		}
		
		
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.find_train_result, menu);
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
				Intent intent = new Intent(context, MainList.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
				overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
				
				return true;
				
		default:return super.onOptionsItemSelected(item);
		
		
		}
		
		
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
