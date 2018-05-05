package com.feng.cameradome2;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity
{
	private CameraPreview mPreview;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final ImageView mediaPreview=findViewById(R.id.media_preview);
		mediaPreview.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(MainActivity.this, ShowPhotoVideo.class);
				intent.setDataAndType(mPreview.getOutputMediaFileUri(), mPreview.getOutputMediaFileType());
				startActivityForResult(intent, 0);
				
			}
		});
		
		
		initCamera();
		
		Button buttonSetting=findViewById(R.id.button_settings);
		buttonSetting.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//第一种写法
				/*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getFragmentManager().beginTransaction().replace(R.id.camera_preview,new SettingsFragment()).commit();*/
				//第二种写法
				getFragmentManager().beginTransaction().replace(R.id.camera_preview,
						new SettingsFragment()).addToBackStack(null).commit();
				
			}
		});
		
		
		final Button buttonCapturePhoto=findViewById(R.id.button_capture_photo);
		buttonCapturePhoto.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mPreview.takePicture(mediaPreview);
				
			}
		});
		
		final Button buttonCaptureVideo=findViewById(R.id.button_capture_video);
		buttonCaptureVideo.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mPreview.isRecording()) {
					mPreview.stopRecording(mediaPreview);
					buttonCaptureVideo.setText("录像");
				} else {
					if (mPreview.startRecording()) {
						buttonCaptureVideo.setText("停止");
					}
				}
			}
		});
	}
	
	public void initCamera()
	{
		mPreview = new CameraPreview(this);
		FrameLayout preview = findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		
		SettingsFragment.passCamera(mPreview.getCameraInstance());
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
		SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
	}
	
	public void onPause()
	{
		super.onPause();
		mPreview=null;
	}
	
	public void onResume()
	{
		super.onResume();
		if (mPreview==null)
		{
			initCamera();
		}
	}
}
