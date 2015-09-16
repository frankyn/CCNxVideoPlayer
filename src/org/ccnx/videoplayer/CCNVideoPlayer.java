package org.ccnx.videoplayer;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.concurrent.CountDownLatch;

import org.ccnx.android.ccnlib.CCNxConfiguration;
import org.ccnx.android.ccnlib.CCNxServiceCallback;
import org.ccnx.android.ccnlib.CCNxServiceControl;
import org.ccnx.android.ccnlib.CCNxServiceStatus.SERVICE_STATUS;
import org.ccnx.android.ccnlib.CcndWrapper.CCND_OPTIONS;
import org.ccnx.android.ccnlib.RepoWrapper.REPO_OPTIONS;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.config.UserConfiguration;
import org.ccnx.ccn.profiles.ccnd.CCNDaemonException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;

import org.ccnx.android.ccnlib.CCNxConfiguration;
import org.ccnx.android.ccnlib.CCNxServiceCallback;
import org.ccnx.android.ccnlib.CCNxServiceControl;
import org.ccnx.android.ccnlib.CCNxServiceStatus.SERVICE_STATUS;
import org.ccnx.android.ccnlib.CcndWrapper.CCND_OPTIONS;
import org.ccnx.android.ccnlib.RepoWrapper.REPO_OPTIONS;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.profiles.ccnd.CCNDaemonException;
import org.ccnx.ccn.profiles.ccnd.SimpleFaceControl;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.ccnx.ccn.utils.*;

import android.content.Intent;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNInputStream;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import android.webkit.URLUtil;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.VideoView;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.spec.KeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CCNVideoPlayer extends StartupBase {
	protected String TAG="CCNVideoPlayer";
	public String _securitypath = null;
	public boolean _securityKeyReady = false;
	public boolean _videoReady = false;
	public String _securityKey = null;
	public String _securityurl = null;
	public String _videourl = null;
	public int poly = 0;
	public int point_x = 0;
	public int point_y = 0;
	protected VideoView mVideoView = null;
	protected String _videopath = null;
	protected String _currentpath = null;
	private static long startTime;
    private static long endTime;
    private static long huffStart;
    
    // Link to the poly_decryption library compiled through NDK
    static {
        System.loadLibrary("poly_decryption");
    }

    // Define a native method call to the poly_decryption library
    public native String  extractPolyKey ( String filename , int poly, int x ,int y );

    // Helper methods for timers
    public static void startTimer ( ) {
        startTime = System.currentTimeMillis();
    }
    
    public static long endTimer ( ) {
        endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

	// ===========================================================================
	// Process control Methods

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent incomingIntent = getIntent();
		_securityurl = incomingIntent.getStringExtra("security_url");
		_videourl = incomingIntent.getStringExtra("video_url");
		poly = Integer.parseInt(incomingIntent.getStringExtra("poly_size"));
		point_x = Integer.parseInt(incomingIntent.getStringExtra("point_x"));
		point_y = Integer.parseInt(incomingIntent.getStringExtra("point_y"));
		
		TextView title = (TextView) findViewById(R.id.tvTitle);
		
		title.setText("Log View");
		TextView urlview = (TextView) findViewById(R.id.inputURL);
		urlview.setText(_videourl);
		mVideoView = (VideoView) findViewById(R.id.surface_view);


		postToUI("Beginning download of files.");
		_worker = new CCNWorker(_securityurl,_videourl);
		_thd = new Thread(_worker);
		_thd.start();
	}

	/** Called when video has completely downloaded from server. */
	public void onVideoComplete(String path) {
		try
		{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("/sdcard/TimesLogged.txt", true)));
		    		out.println((System.currentTimeMillis() - huffStart));
		    		out.close();
		}
		catch(Exception e)
		{

		}
		finish();
		postToUI("Playing: " + path );
		_videopath = path;
		_videoReady = true;
		if ( _securityKeyReady ) {
			onPlayVideo ( );
		}
	}

	/** Called when the security key has been extracted. */
	public void onSecurityComplete(String key, double elapsedTime) {
		postToUI("Security Polynomial downloaded. Took " + elapsedTime + " seconds.");
		_securityKey = key;
		_securityKeyReady = true;
		postToUI("Key: " + key);
		if ( _videoReady ) {
			onPlayVideo ( );
		}
	}

	/** Called when completion occurs and then plays video if both security and video files are available. */
	public void onPlayVideo ( ) {
		if ( _videopath != null && _videopath != _currentpath && _securityKeyReady && _videoReady ) {
			// Decode video 
			// kept empty for now...
			//postToUI("Sudo decryption");
			//decodeVideo ( );

			// Play Video
			_currentpath = _videopath;
			mVideoView.setVideoPath(_currentpath);
			mVideoView.requestFocus();
			mVideoView.start();	
		}
		
	}

	/** Called to decrypt cached file encrypted by DES */
	public void decodeVideo ( ) {
		String sudoKey = _securityKey;
		byte[] key = sudoKey.getBytes();
		Cipher cipher;
		// Get a cipher object in decrypt mode
		try {
		    DESKeySpec dks = new DESKeySpec(key);
		    SecretKeyFactory skf = SecretKeyFactory.getInstance("DES/CBC");
		    SecretKey desKey = skf.generateSecret(dks);
		    cipher = Cipher.getInstance("DES");
		    cipher.init(Cipher.DECRYPT_MODE, desKey);
		
			File temp = File.createTempFile("tempfile", Long.toString(System.nanoTime()));
			FileOutputStream fos = new FileOutputStream(temp);
			// This is the line that throws the exception
		    CipherInputStream cipherStream = new CipherInputStream(new FileInputStream(_videopath), cipher); 
			byte[] buffer = new byte[4000];
			int len = 0;
			while ( (len = cipherStream.read(buffer)) != -1 ) {
				fos.write(buffer,0,len);
				fos.flush();
			}
			fos.close();

		    // set decoded path
		    _videopath = temp.getAbsolutePath();
		} catch (IOException e) {
			postToUI("Error: " + e);
		} catch (InvalidKeyException e) {
			postToUI("Error: " + e);
		} catch (NoSuchAlgorithmException e) {
			postToUI("Error: " + e);
		} catch (InvalidKeySpecException e) {
			postToUI("Error: " + e);
		} catch (NoSuchPaddingException e) {
			postToUI("Error: " + e);
		}
	}

	/** Called when the activity starts. */
	@Override
	public void onStart() {
		super.onStart();	
		Log.i(TAG,"onStart");
		
	}

	/** Called when the activity resumes. */
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy()");

		_worker.stop();

		super.onDestroy();
	}

	// ===========================================================================
	// UI Methods

	@Override
	void doExit() {
		
	}
	
	@Override
	void doShutdown() {
		_worker.shutdown();
	}

	// ====================================================================
	// Internal implementation

	protected CCNWorker _worker = null;
	protected Thread _thd = null;
	
	// ===============================================
	protected class CCNWorker implements Runnable, CCNxServiceCallback {
		protected final static String TAG="CCNWorker";
		protected final CCNVideoPlayer _hw;
		private String security_url = null;
		private String video_url = null;

		//protected SecuredPolynomial _decryptor;
		protected Thread _thd;
		/**
		 * Create a worker thread to handle all the CCNx calls.
		 */
		public CCNWorker(String securityUrl, String videoUrl) {
			_context = CCNVideoPlayer.this.getBaseContext();
			_hw = CCNVideoPlayer.this;
			security_url = securityUrl;
			video_url = videoUrl;

			postToUI("Setting CCNxConfiguration");
			
			// Use a shared key directory
			CCNxConfiguration.config(_context, false);

			File ff = getDir("storage", Context.MODE_WORLD_READABLE);
			postToUI("Setting setUserConfigurationDirectory: " + ff.getAbsolutePath());
			
			Log.i(TAG,"getDir = " + ff.getAbsolutePath());
			UserConfiguration.setUserConfigurationDirectory( ff.getAbsolutePath() );
			
			// Do these CCNx operations after we created ChatWorker
			ScreenOutput("User name = " + UserConfiguration.userName());
			ScreenOutput("ccnDir    = " + UserConfiguration.userConfigurationDirectory());
			ScreenOutput("Waiting for CCN Services to become ready");
		}

		/**
		 * Exit the worker thread, but keep services running
		 */
		public synchronized void stop() {
			// this is called form onDestroy too, so only do something
			// if the user didn't select a menu option to exit or shutdown.
			if( _latch.getCount() > 0 ) {
				_latch.countDown();
				_ccnxService.disconnect();
			}
		}

		/**
		 * Exit the worker thread and shutdown services
		 */
		public synchronized void shutdown() {
			_latch.countDown();
			try {
				_ccnxService.stopAll();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Runnable method
		 */
		@Override
		public void run() {
			// Startup CCNx in a blocking call
			postToUI("Starting CCNx");
			initializeCCNx();
			

			// wait for shutdown
			//postToUI("Worker thread now blocking until exit");

			while( _latch.getCount() > 0 ) {
					try {
						_latch.await();
					} catch (InterruptedException e) {
				}
			}
			
			Log.i(TAG, "run() exits");		
		}

		// ==============================================================================
		// Internal implementation
		protected final CountDownLatch _latch = new CountDownLatch(1);
		protected final Context _context;
		protected CCNxServiceControl _ccnxService;

		/*********************************************/
		// These are all run in the CCN thread
		/**
		 * These are all run in the CCN thread
		 */
		private void initializeCCNx() {
			_ccnxService = new CCNxServiceControl(_context);
			_ccnxService.registerCallback(this);
			_ccnxService.setCcndOption(CCND_OPTIONS.CCND_DEBUG, "1");
			_ccnxService.setRepoOption(REPO_OPTIONS.REPO_DEBUG, LOG_LEVEL);
			postToUI("calling startAllInBackground");
			_ccnxService.startAllInBackground();
		}


		/**
		 * Reimplementation of ccnGetFile functionality from CCN core source.
		 * This code can be cited the ccngetfile source files in ccnx root directory
		 */
		public String ccnGetFile ( String path ) {
			huffStart = System.currentTimeMillis();
			try {
				int readsize = 10024; // make an argument for testing...
				// If we get one file name, put as the specific name given.
				// If we get more than one, put underneath the first as parent.
				// Ideally want to use newVersion to get latest version. Start
				// with random version.
				ContentName argName = ContentName.fromURI(path);
				File temp = File.createTempFile("tempfile", Long.toString(System.nanoTime()));
				//temp.deleteOnExit();
				final String tempPath = temp.getAbsolutePath();
				FileOutputStream output = new FileOutputStream(temp);

				long starttime = System.currentTimeMillis();
				CCNInputStream input;
				if (CommonParameters.unversioned)
					input = new CCNInputStream(argName, _handle);
				else
					input = new CCNFileInputStream(argName, _handle);
				if (CommonParameters.timeout != null) {
					input.setTimeout(CommonParameters.timeout);
				}
				byte [] buffer = new byte[readsize];

				int readcount = 0;
				long readtotal = 0;
				
				while ((readcount = input.read(buffer)) != -1){
					readtotal += readcount;
					output.write(buffer, 0, readcount);
					output.flush();
				}
				if (CommonParameters.verbose) {
					postToUI("ccngetfile took: "+(System.currentTimeMillis() - starttime)+"ms");
					
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("/data/data/org.ccnx.videoplayer/TimesLogged.txt", true)));
		    		out.println((System.currentTimeMillis() - starttime));
		    		out.close();
		    		finish();
				}
				postToUI("Retrieved content " + path + " got " + readtotal + " bytes.");
				
				
				return tempPath;
			} catch (MalformedContentNameStringException e) {
				postToUI("Malformed name: " + path + " " + e.getMessage());
			} catch (IOException e) {
				postToUI("Cannot write file or read content. " + e.getMessage());
			}
			return "";
		}

		/**
		 * Downloads a given security file from a path on a CCN server
		 */
		public boolean getSecurity ( String path ) {
			/*final String tempPath = ccnGetFile(path);

			if ( !tempPath.isEmpty() ) {
				//_hw.securitypath = tempPath;
				// Begin timer for experiments
				_hw.startTimer();

				// Extract key using the poly_encryption library
				// Can only function inside non main-thread.
				new Thread() {
					
					public void run ( ) {
						final String key = _hw.extractPolyKey(tempPath,_hw.poly,_hw.point_x,_hw.point_y);
						final double elapsedTime = (double)((double)_hw.endTimer()/1000);
				
						// Once extraction is completed start a thread to notify the main UI thread.
						runOnUiThread ( new Runnable ( ) {
						  public void run ( ) {
						        _hw.onSecurityComplete(key,elapsedTime);
						  }
						});
					}
				}.start();
				return true;
			} 
			return false;*/
			return true;
		}

		/**
		 * Downloads a given security file from a path on a CCN server
		 */
		public boolean getVideo ( String path ) {
			final String tempPath = ccnGetFile(path);
			if ( !tempPath.isEmpty() ) {
				runOnUiThread(new Runnable() {
					  public void run() {
					        _hw.onVideoComplete(tempPath);
					  }
				});
				return true;
			} 
			
			return false;
		}
		/**
		 * Called from CCNxServiceControl
		 */
		@Override
		public void newCCNxStatus(SERVICE_STATUS st) {
			postToUI("CCNxStatus: " + st.toString());
			
			switch(st) {

				case START_ALL_DONE:
					try {
						postToUI("Opening CCN key manager/handle");
						openCcn();
						
						setupFace();
						postToUI("Finished CCNx Initialization");
						new Runnable ( ) {
								public void run ( ) {
									getSecurity ( security_url );
									getVideo ( video_url );
									
								}
						}.run();

					} catch (CCNDaemonException e) {
						e.printStackTrace();
						postToUI("SimpleFaceControl error: " + e.getMessage());
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				break;
			case START_ALL_ERROR:
				postToUI("CCNxStatus ERROR");
				break;
			}
		}
	}
}
