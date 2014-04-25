package cz.zcu.kiv.runstat.ui;

import cz.zcu.kiv.runstat.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class HistoryActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		WebView webview = new WebView(this);
		 setContentView(webview);

		 getWindow().requestFeature(Window.FEATURE_PROGRESS);

		 webview.getSettings().setJavaScriptEnabled(true);

		 final Activity activity = this;
		 webview.setWebChromeClient(new WebChromeClient() {
		   public void onProgressChanged(WebView view, int progress) {
		     // Activities and WebViews measure progress with different scales.
		     // The progress meter will automatically disappear when we reach 100%
		     activity.setProgress(progress * 1000);
		   }
		 });
		 webview.setWebViewClient(new WebViewClient() {
		   public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		     Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
		   }
		 });

		 
		 webview.loadUrl("http://www.akordynakytaru.com/zswi/index.php?q=stats");

	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.action_settings:
	    	Intent intent = new Intent(this, SettingsActivity.class); 
			startActivity(intent);
	        return true;
	    case R.id.action_help:
	    	Intent intent2 = new Intent(this, HelpActivity.class); 
			startActivity(intent2);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
