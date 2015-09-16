package org.ccnx.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class StartScreen extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		Button button;
		
		//button = (Button) findViewById(R.id.btnStartBlocking);
		//button.setOnClickListener(this);

		button = (Button) findViewById(R.id.btnDownloadandPlay);
		button.setOnClickListener(this);

    }

   
    
	public void onClick(View v) {
		// do something when the button is clicked

		Log.d("StartScreen", "OnClickListener " + String.valueOf(v.getId()));

		switch (v.getId()) {
			case R.id.btnDownloadandPlay:
				onDownloadandPlay ( );
			break;
			
			default:
			break;
		}
	}

	private void onDownloadandPlay ( ) {
		String fileURL;
		for(int i=0;i<100;i++)
		{
		Intent downloadandPlay = new Intent(this, CCNVideoPlayer.class);
		EditText editText;

		// extract IP from textfield
		editText = (EditText) findViewById(R.id.inputIP);
		downloadandPlay.putExtra("ccn_ip",editText.getText().toString());

		// extract Port from textfield
		editText = (EditText) findViewById(R.id.inputPort);
		downloadandPlay.putExtra("ccn_port",editText.getText().toString());

		// extract Security URL from text
		editText = (EditText) findViewById(R.id.securityURL);
		downloadandPlay.putExtra("security_url",editText.getText().toString());
		
		// extract Video URL from text
		editText = (EditText) findViewById(R.id.videoURL);
		downloadandPlay.putExtra("video_url",editText.getText().toString());
		fileURL = editText.getText().toString();

		// extract Video URL from text
		editText = (EditText) findViewById(R.id.poly_size);
		downloadandPlay.putExtra("poly_size",editText.getText().toString());

		// extract Video URL from text
		editText = (EditText) findViewById(R.id.point_x);
		downloadandPlay.putExtra("point_x",editText.getText().toString());

		// extract Video URL from text
		editText = (EditText) findViewById(R.id.point_y);
		downloadandPlay.putExtra("point_y",editText.getText().toString());

		startActivity(downloadandPlay);

		try
		{
		//SSH ccn2hi 'ccnrm <fileName>'
		Process proc = Runtime.getRuntime().exec("SSH ccn2hi 'ccnrm "+fileURL+"'");
		proc.waitFor();
		}
		catch(Exception e)
		{

		}
	}

	}

	

}