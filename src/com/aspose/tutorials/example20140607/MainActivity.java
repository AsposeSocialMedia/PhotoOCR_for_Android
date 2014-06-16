package com.aspose.tutorials.example20140607;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	public class OcrTask extends AsyncTask<File, Void, String> {
		String requestUrl;
		String appSID = "875775bc-8a52-49ba-8c65-7f4bdfa6802e";
		String appKey = "e96db89a723710c76878a67b5e8c21bf";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			requestUrl = "https://api.aspose.com/v1.1/ocr/recognize?appSID="
					+ appSID;
			try {
				Mac mac = Mac.getInstance("HmacSHA1");
				mac.init(new SecretKeySpec(appKey.getBytes(), "HmacSHA1"));
				mac.update(requestUrl.getBytes());
				String signature = Base64.encodeToString(mac.doFinal(),
						Base64.NO_PADDING);
				requestUrl += "&signature=" + signature;
				Log.w(MainActivity.class.getName(), requestUrl);
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}

		@Override
		protected String doInBackground(File... params) {

			File file = params[0];
			HttpURLConnection connection = null;
			try {
				FileInputStream fstream = new FileInputStream(file);
				int fsize = fstream.available();

				connection = (HttpURLConnection) new URL(requestUrl)
						.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Accept", "application/json");
				connection.setRequestProperty("Content-Length",
						String.valueOf(fsize));

				OutputStream upload = connection.getOutputStream();
				byte[] buffer = new byte[10240];
				int len;
				while ((len = fstream.read(buffer)) != -1) {
					upload.write(buffer, 0, len);
				}
				upload.close();
				fstream.close();

				InputStream i = connection.getInputStream();
				String text = new Scanner(i).useDelimiter("\\A").next();
				i.close();

				file.delete();

				return text;
			} catch (FileNotFoundException fnfx) {
				InputStream e = connection.getErrorStream();
				String text = new Scanner(e)
						.useDelimiter("\\A").next();
				Log.e("aspose", text);

				return text;
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			Log.e(OcrTask.class.getName(), result);

			String text = "";
			try {
				JSONObject json = new JSONObject(result);
				if (json.has("Status") && json.getString("Status").equals("OK")) {
					text = json.getString("Text");
				} else if (json.has("Message")) {
					text = "Error: " + json.getString("Message");
				}
			} catch (JSONException x) {
				throw new RuntimeException(x);
			}

			displayTextResults(text);
		}
	}

	protected static final int REQUEST_IMAGE_CAPTURE = 1;
	File tmpfile = null;

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		if (request == REQUEST_IMAGE_CAPTURE && result == RESULT_OK) {
			

//			if (data.hasExtra(MediaStore.EXTRA_OUTPUT)) {				
//				Log.w("aspose", "==============MediaStore.EXTRA_OUTPUT===========");
//			}
			
		    if (tmpfile == null) {
		        Log.e("onActivityResult", "Photo was not saved. Doing nothing");
		    }

			new OcrTask().execute(tmpfile);

			displayTextResults("Uploading photo and recognizing text. This may take a few seconds.");
		}
	}

	protected void displayTextResults(String text) {
		TextView t = (TextView) findViewById(R.id.text_results);
		t.setText(text);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void captureImage(View view) {
		Intent intCaptureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if (intCaptureImage.resolveActivity(getPackageManager()) != null) {
			try {
				// tmpfile = File.createTempFile("photo", null, getCacheDir());
				tmpfile = File.createTempFile("Photo", ".jpg", Environment .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
			} catch (IOException x) {
				Log.e("aspose", "no way to save the file");
				// We are lost :)
				finish();
			}
			intCaptureImage.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(tmpfile));
			startActivityForResult(intCaptureImage, REQUEST_IMAGE_CAPTURE);

		}

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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
