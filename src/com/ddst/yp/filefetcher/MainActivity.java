package com.ddst.yp.filefetcher;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Locale;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	EditText edittext_url, edittext_location;
	Button button_fetch;
	CheckBox checkbox_backup;
	
	final String setting_url = "target_url", setting_location = "location", setting_name = "locations"; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		edittext_url = (EditText) findViewById(R.id.edittext_url);
		edittext_location = (EditText) findViewById(R.id.edittext_location);
		button_fetch = (Button) findViewById(R.id.button_fetch);
		checkbox_backup = (CheckBox) findViewById(R.id.checkbox_backup);
		
		// load saved locations
		SharedPreferences settings = getSharedPreferences(setting_name, 0);
		String saved_url = settings.getString(setting_url, "");
		String saved_location = settings.getString(setting_location, "");
		
		if (!saved_url.isEmpty()) {
			edittext_url.setText(saved_url);
		}
		
		if (!saved_location.isEmpty()) {
			edittext_location.setText(saved_location);
		}
		
		button_fetch.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_fetch:
			// when button fetch is tapped
			this.button_fetch.setEnabled(false);
			String uri = this.edittext_url.getText().toString();
			String location = this.edittext_location.getText().toString();
			boolean backup = this.checkbox_backup.isChecked();
			
			if (uri!=null && !uri.isEmpty()) {
				SharedPreferences settings = getSharedPreferences(setting_name, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(setting_url, uri);
				editor.commit();
			}
			
			if (location!=null && !location.isEmpty()) {
				SharedPreferences settings = getSharedPreferences(setting_name, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(setting_location, location);
				editor.commit();
			}
			
			if (uri!=null && location!= null 
					&& !uri.isEmpty() && !location.isEmpty() 
					&& !uri.equals(location)) {
				
				try {
					locationFile = new File(location);
					
					if (locationFile.getCanonicalPath().startsWith("/system/etc")) {
						this.executeShell("mount -o remount,rw /system");
						this.executeShell("chmod 0777 /system");
						this.executeShell("chmod 0777 /system/etc");
						
						systemMounted = true;
					}
					
					// backup
					if (backup) {
						Toast.makeText(this, "Backup...", Toast.LENGTH_SHORT).show();
						
						File bak = new File(location+".bak");
						if (locationFile.exists() && !locationFile.isDirectory()) {
							fileExisted = true;
							// backup file permissions
							filePermissions = getFilePermissions(locationFile.getCanonicalPath());
							this.executeShell("chmod 0777 " + locationFile.getCanonicalPath());
							
							// backup file
							if (!bak.exists()) {
								this.executeShell("dd if=" + locationFile.getCanonicalPath() + " of=" + bak.getCanonicalPath());
								if (filePermissions != null) {
									this.setFilePermissions(bak.getCanonicalPath(), filePermissions);
								}
							}
						} else {
							fileExisted = false;
						}
					}

					if (!locationFile.exists() || !locationFile.isDirectory()) {				
						// write temporary file
						Toast.makeText(this, "Fetching...", Toast.LENGTH_SHORT).show();
						FetchFileTask task = new FetchFileTask();
						task.execute(uri);
					}
					
				} catch (Exception e) {
					Log.e("Exception", e.getMessage());

					asyncTaskFinished();
				}
			}
			
			break;

		default:
			break;
		}
		
	}
	
	File locationFile = null;
	boolean fileExisted = false;
	String filePermissions = null;
	List<String> parents, parentPermissions;
	private void fetchFileCallback() {
		if (locationFile == null) {
			return;
		}
		try {
			// make directories
			File parent = locationFile.getParentFile();
			if (parent != null && !parent.exists()) {
				this.executeShell("mkdir -p " + parent.getCanonicalPath());
			}
			
			// copy to location
			if (parent.exists()) {
				File cacheDir = getCacheDir();
				File tempFile = new File(cacheDir, "file.temp");
				
				if (systemMounted) {
					// replace line endings
					writeStringToFile(tempFile.getCanonicalPath(), getStringFromFile(tempFile.getCanonicalPath()));
				}
				
				this.executeShell("dd if=" + tempFile.getCanonicalPath() + " of=" + locationFile.getCanonicalPath());
				if (fileExisted && filePermissions!=null) {
					// set file permissions
					this.setFilePermissions(locationFile.getCanonicalPath(), filePermissions);
					
				}
			}
			
			
		} catch (Exception e) {
			Log.e("Exception", e.getMessage());
		}
	}
	
	public static void writeStringToFile(String filePath, String content) throws Exception {
		File file = new File(filePath);
		FileOutputStream fout = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fout));
		writer.write(content);
		writer.close();
	}

	public static String getStringFromFile (String filePath) throws Exception {
	    File fl = new File(filePath);
	    FileInputStream fin = new FileInputStream(fl);
	    String ret = convertStreamToString(fin);
	    //Make sure you close all streams.
	    fin.close();        
	    return ret;
	}
	
	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append(System.getProperty("line.separator"));
	    }
	    reader.close();
	    return sb.toString();
	}
	
	boolean systemMounted = false;
	private void asyncTaskFinished() {
		this.recoveryParentPermissions();
		Toast.makeText(this, "Done.", Toast.LENGTH_SHORT).show();
		
		this.button_fetch.setEnabled(true);
	}
	
	private void recoveryParentPermissions() {
		if (systemMounted) {
			this.executeShell("chmod 0755 /system");
			this.executeShell("chmod 0755 /system/etc");
			this.executeShell("mount -o remount,ro /system");
			systemMounted = false;
		}
	}
	
	class FetchFileTask extends AsyncTask<String, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			if (params.length == 0) return false;
			
			try {
				URL target = new URL(params[0]);
				URLConnection connection = target.openConnection();
				ReadableByteChannel rbc = Channels.newChannel(target.openStream());
				File cacheDir = getCacheDir();
				File tempFile = new File(cacheDir, "file.temp");
				FileOutputStream fos = new FileOutputStream(tempFile);
				long expectedSize = connection.getContentLength();
				Log.w("FETCH", "Expected size: " + expectedSize );
				long transferedSize = 0L;
				long transferedSizeTemp = fos.getChannel().transferFrom( rbc, transferedSize, 1 << 24 );
				transferedSize += transferedSizeTemp;
				int zeroSizeCount = 0;
				while( (transferedSize < expectedSize && zeroSizeCount <= 9) || transferedSizeTemp > 0 || zeroSizeCount <= 3) {
					transferedSizeTemp = fos.getChannel().transferFrom( rbc, transferedSize, 1 << 24 );
					transferedSize += transferedSizeTemp;
					Log.w("FETCH", transferedSize + " bytes received" );
					if (transferedSizeTemp == 0) {
						zeroSizeCount++;
					} else {
						zeroSizeCount = 0;
					}
				}
				fos.close();
				
				return true;
			} catch (Exception e) {
				Log.e("Exception", "Faied to fetch file: " + params[0]);
			}
			
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			
			if (result) {
				MainActivity.this.fetchFileCallback();
			}

			MainActivity.this.asyncTaskFinished();
			
			super.onPostExecute(result);
		}
		
	}
	

	Process process = null;
	private String executeShell(String cmd) {
		String result = "";
		if (cmd==null || cmd.isEmpty()) {
			return result;
		}
		
		if (process != null) {
			process.destroy();
		}
		
		String shell = "su -c '" + cmd + "'";
		Log.i("SHELL", "Try to run shell as su, shell: " + cmd);
		try {
			process = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
		} catch (Exception e) {
			Log.e("Exception", "Failed to run as su, shell: " + shell);
			Log.i("SHELL", "Now run normally, shell: " + cmd);

			try {
				process = Runtime.getRuntime().exec(cmd);
			} catch (IOException e1) {
				Log.e("Exception", "Failed to run shell, shell: " + cmd);
			}
		}
		
		if (process != null) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				while((line=in.readLine())!=null) {
					Log.i("SHELL", line);
					result += line;
				}
				process.waitFor();
			} catch (Exception e) {
				Log.e("Exception", e.getMessage());
			}
		}
		
		return result;
		
	}
	
	private String getFilePermissions(String path) {
		String result = null;
		
		String shellResult = this.executeShell("ls -l " + path);
		if (shellResult!=null && !shellResult.isEmpty() && shellResult.startsWith("-") && shellResult.length()>10) {
			// "-rwxrwxrwx ..."
			
			int p1=7, p2=7, p3=7;
			if (shellResult.charAt(1)=='-') p1-=4;
			if (shellResult.charAt(2)=='-') p1-=2;
			if (shellResult.charAt(3)=='-') p1-=1;

			if (shellResult.charAt(4)=='-') p2-=4;
			if (shellResult.charAt(5)=='-') p2-=2;
			if (shellResult.charAt(6)=='-') p2-=1;

			if (shellResult.charAt(7)=='-') p3-=4;
			if (shellResult.charAt(8)=='-') p3-=2;
			if (shellResult.charAt(9)=='-') p3-=1;
			
			result = String.format(Locale.US,"0%d%d%d", p1, p2,p3);
		}
		
		return result;
	}
	
	private void setFilePermissions(String path, String permissions) {
		this.executeShell("chmod " + permissions + " " + path);
	}
	
	public void finish() {
        /*
         * This can only invoked by the user or the app finishing the activity
         * by navigating from the activity so the HOME key was not pressed.
         */
		this.clean();
        
		super.finish();
    }

    public void onStop() {
        super.onStop();

        /*
         * Check if the HOME key was pressed. If the HOME key was pressed then
         * the app will be killed. Otherwise the user or the app is navigating
         * away from this activity so assume that the HOME key will be pressed
         * next unless a navigation event by the user or the app occurs.
         */
        this.clean();
    }
    
    private void clean() {
    	if (process != null) {
    		process.destroy();
    	}
    }
}
