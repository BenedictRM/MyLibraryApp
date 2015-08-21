//From this tutorial: http://code.tutsplus.com/tutorials/android-sdk-create-a-barcode-reader--mobile-17162
//rest api tutorial from: http://blog.strikeiron.com/bid/73189/Integrate-a-REST-API-into-Android-Application-in-less-than-15-minutes
package com.example.barcodescanner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;









//By intent zxing barcode scanner libraries:
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends Activity implements OnClickListener {

	private Button scanBtn;
	private TextView formatTxt, contentTxt;
	private String scanContent = null;
	private String scanFormat = null;
	//Constant to feed to onPostExecute() function
	public final static String EXTRA_MESSAGE = "com.example.webapitutorial.MESSAGE";
	//public variables s.t. they are available to the private class below
	public final static String apiURL = "http://isbndb.com/api/v2/xml/";
	public final static String accessKey = "QR0I50RW";
	
	
	//Separate thread class to handle REST API call
	private class CallAPI extends AsyncTask<String, String, String>{
		@Override
		protected String doInBackground(String... params){
			
			//Params
			String urlString = params[0]; //url to call, stored in funciton call
			String resultToDisplay = "";
			InputStream in = null;
			APIResults result = null;
			
			//Retrieve the http REST
			try{
				URL url = new URL(urlString);
				HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
				//Wrapping input stream inside a bufferedinputstream as recommended by InputStream API
				in = new BufferedInputStream(urlConnection.getInputStream());
			} catch (Exception e){
				System.out.println(e.getMessage());
				
				//Break out of error
				return e.getMessage();
			}
			
			//Parse XML
			XmlPullParserFactory pullParserFactory;
			
			try{
				//Instantiating a new pullParser factory, from: http://www.sitepoint.com/learning-to-parse-xml-data-in-your-android-app/
				pullParserFactory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = pullParserFactory.newPullParser();
				
				//Setting parser features
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				parser.setInput(in,null);
				
				//calling private funciton to connect to api
				result = parseXML(parser);
			} catch (XmlPullParserException e){
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			}
			
			//set the result in the most basic way
			if (result != null)
			{
				if(!(result.error.isEmpty()) && (result.error != null))
				{
					resultToDisplay += result.error + "\n";
				}
				
				if(!(result.title.isEmpty()) && (result.title != null))
				{
				     resultToDisplay += result.title + "\n"; 
				}
				
				if(!(result.titleLong.isEmpty()) && (result.titleLong != null))
				{
					 resultToDisplay += result.titleLong + "\n";
				}
				
				if(!(result.authors.isEmpty()) && (result.authors != null))
				{
					resultToDisplay += result.authors + "\n";
				}
				
				if(!(result.publisher.isEmpty()) && (result.publisher != null))
				{
					resultToDisplay += result.publisher + "\n";
				}
				
				//******To do later--update this data to pull from xml doc
				if(!(scanContent.isEmpty()))
				{
					 resultToDisplay += "ISBN: " + scanContent;
				}
			}
				else
				{
					resultToDisplay = "Exception occured";
				}
			
			//Return xml object
			return (resultToDisplay);		
		}
		
		protected void onPostExecute(String result){
			Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
		    intent.putExtra(EXTRA_MESSAGE, result);		    
		    startActivity(intent);
		}
		
		//XML Parser function
		private APIResults parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
			
			int eventType = parser.getEventType();
		    APIResults result = new APIResults();

		    while( eventType!= XmlPullParser.END_DOCUMENT) {
		      String name = null;

		      switch(eventType)
		      {
		        case XmlPullParser.START_TAG:
		          name = parser.getName();
		          
		          if(name.equals("error")) {
		        	result.error = parser.nextText();
		          }
		          else if (name.equals("title")) {
		            result.title = parser.nextText();
		          }
		          else if (name.equals("title_long")) {
		            result.titleLong = parser.nextText();
		          }
		          else if (name.equals("name")) {
		            result.authors = parser.nextText();
		          }
		          else if (name.equals("publisher_text")) {
		            result.publisher = parser.nextText();
		          }
		          break;
		          
		        case XmlPullParser.END_TAG:
		          break;
		       } // end switch

		       eventType = parser.next();
		    } // end while

		    return result; 
		}	
	}
	
	//New private class to store api results, more of a struct
	private class APIResults{
		public String title;
		public String titleLong;
		public String authors;
		public String publisher;
		public String error;
		
		//constructor
		public APIResults()
		{
			title = "";
			titleLong = "";
			authors = "";
			publisher = "";
			error = "";
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Set private variables to xml stored values
		scanBtn = (Button)findViewById(R.id.scan_button);
		formatTxt = (TextView)findViewById(R.id.scan_format);
		contentTxt = (TextView)findViewById(R.id.scan_content);
		
		//Create a listener for scanBtn
		scanBtn.setOnClickListener(this);
	}

	public void onClick(View v){
		//respond to clicks
		if(v.getId()==R.id.scan_button){
			//create an intent integrator object
			IntentIntegrator scanIntegrator = new IntentIntegrator(this);
			//now scan with the integrator
			scanIntegrator.initiateScan();
		}
	}
	
	//retrieve scan result, called when onClick called and returns a result
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		
		//Protect from null return values
		if (scanningResult != null) {
			//we have a result
			scanContent = scanningResult.getContents();
			scanFormat = scanningResult.getFormatName();
			
			//Output information to the screen
			formatTxt.setText("FORMAT: " + scanFormat);
			contentTxt.setText("CONTENT: " + scanContent);
			
			//call the api and search by isbn
			verifyISBN(scanContent);
		}
		
		else{
		    Toast toast = Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT);
		    //Show message
		    toast.show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//Once isbn scanned, call api and search
	public void verifyISBN(String scanContent)
	{
		if(scanContent != null && !scanContent.isEmpty()) {
			String urlString = apiURL + accessKey + "/book/" + scanContent;
	       //create a CallAPI object in a background thread:
	       new CallAPI().execute(urlString); 
	    }
	}
}
