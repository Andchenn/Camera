package com.feng.cameradome2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.MediaController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by feng on 17-11-16.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private Uri outputMediaFileUri;
	private String outputMediaFileType;
	
	private static final String TAG = "CameraPreview";
	private SurfaceHolder mHolder;
	private Camera mCamera;
	
	private MediaRecorder mMediaRecorder;
	private float oldDist=1f;
	
	public CameraPreview(Context context)
	{
		super(context);
		mHolder=getHolder();
		mHolder.addCallback(this);
	}
	
	Camera getCameraInstance()
	{
		if (mCamera==null);
		try
		{
			mCamera=Camera.open();
		}catch (Exception e)
		{
			Log.d(TAG,"camera is not available");
		}
		return mCamera;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		mCamera = getCameraInstance();
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		} catch (IOException e)
		{
			Log.d(TAG, "Error setting camera preview: "+e.getMessage());
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mHolder.removeCallback(this);
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}
	
	private File getOutputMediaFile(int type)
	{
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), TAG);
		if (!mediaStorageDir.exists())
		{
			if (!mediaStorageDir.mkdirs())
			{
				Log.d(TAG, "failed to create directory");
				return null;
			}
		}
		String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type==MEDIA_TYPE_IMAGE)
		{
			mediaFile=new File(mediaStorageDir.getPath()+File.separator+ "IMG_" + timeStamp + ".jpg");
			outputMediaFileType = "image/*";
			
		}else if (type==MEDIA_TYPE_VIDEO)
		{
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_" + timeStamp + ".mp4");
			outputMediaFileType = "video/*";
		}else
		{
			return null;
		}
		outputMediaFileUri = Uri.fromFile(mediaFile);
		return mediaFile;
	}
	public Uri getOutputMediaFileUri() {
		return outputMediaFileUri;
	}
	
	public String getOutputMediaFileType() {
		return outputMediaFileType;
	}
	
	public void takePicture(final ImageView view)
	{
		mCamera.takePicture(null,null, new Camera.PictureCallback()
		{
			@Override
			public void onPictureTaken(byte[] data, Camera camera)
			{
				File pictureFile=getOutputMediaFile(MEDIA_TYPE_IMAGE);
				if (pictureFile == null)
				{
					Log.d(TAG, "Error creating media file, check storage permissions");
					return;
				}
				try
				{
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
					view.setImageURI(outputMediaFileUri);
					camera.startPreview();
				} catch (FileNotFoundException e)
				{
					Log.d(TAG, "File not found: " + e.getMessage());
				} catch (IOException e)
				{
					Log.d(TAG, "Error accessing file: " + e.getMessage());
				}
				
			}
		});
	}
	
	public boolean startRecording()
	{
		if (prepareVideoRecorder()) {
			mMediaRecorder.start();
			return true;
		} else {
			releaseMediaRecorder();
		}
		return false;
	}
	
	public void stopRecording(final ImageView view)
	{
		if (mMediaRecorder != null) {
			mMediaRecorder.stop();
			Bitmap thumbnail= ThumbnailUtils.createVideoThumbnail(outputMediaFileUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
			view.setImageBitmap(thumbnail);
		}
		releaseMediaRecorder();
	}
	
	public boolean isRecording()
	{
		return mMediaRecorder != null;
	}
	private void releaseMediaRecorder()
	{
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			mCamera.lock();
		}
	}
	
	private boolean prepareVideoRecorder()
	{
		mCamera = getCameraInstance();
		mMediaRecorder = new MediaRecorder();
		
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		
		mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		
		mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
		
		mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
		
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}
	
	private static Rect calculateTapArea(float x, float y, float coefficient, int width, int height)
	{
		float focusAreaSize = 300;
		int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
		int centerX = (int) (x / width * 2000 - 1000);
		int centerY = (int) (y / height * 2000 - 1000);
		
		int halfAreaSize = areaSize / 2;
		RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
				, clamp(centerY - halfAreaSize, -1000, 1000)
				, clamp(centerX + halfAreaSize, -1000, 1000)
				, clamp(centerY + halfAreaSize, -1000, 1000));
		return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
	}
	
	private static int clamp(int x, int min, int max)
	{
		if (x > max) {
			return max;
		}
		if (x < min) {
			return min;
		}
		return x;
	}
	
	private void handleFocus(MotionEvent event, Camera camera)
	{
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);
		Rect meteringRct=calculateTapArea(event.getX(),event.getY(),1.5f,viewWidth,viewHeight);
		
		
		camera.cancelAutoFocus();
		Camera.Parameters params = camera.getParameters();
		if (params.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<>();
			List<Camera.Area> meteringAreas=new ArrayList<>();
			meteringAreas.add(new Camera.Area(meteringRct,800));
			focusAreas.add(new Camera.Area(focusRect, 800));
			params.setFocusAreas(focusAreas);
			params.setMeteringAreas(meteringAreas);
		} else {
			Log.i(TAG, "focus areas not supported");
		}
		final String currentFocusMode = params.getFocusMode();
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
		camera.setParameters(params);
		
		camera.autoFocus(new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				Camera.Parameters params = camera.getParameters();
				params.setFocusMode(currentFocusMode);
				camera.setParameters(params);
			}
		});
	}
	
	public boolean onTouchEvent(MotionEvent event)//触摸事件
	{
		if (event.getPointerCount() == 1) {
			handleFocus(event, mCamera);
		}else
		{
			switch (event.getAction()&MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist=getFingerSpacing(event);
					break;
				case MotionEvent.ACTION_MOVE:
					float newDist=getFingerSpacing(event);
					if (newDist>oldDist)
					{
						handleZoom(true,mCamera);
					}else if (newDist<oldDist)
					{
						handleZoom(false,mCamera);
					}
					oldDist=newDist;
					break;
			}
		}
		return true;
	}
	
	private static float getFingerSpacing(MotionEvent event)
	{
		float x=event.getX(0)-event.getX(1);
		float y=event.getY(0)-event.getY(1);
		return (float) Math.sqrt(x*x+y*y);
	
	}
	
	private void handleZoom(boolean isZoomIn, Camera camera)
	{
		Camera.Parameters parameters=camera.getParameters();
		if (parameters.isZoomSupported())
		{
			int maxZoom=parameters.getMaxZoom();
			int zoom=parameters.getZoom();
			if (isZoomIn&&zoom<maxZoom)
			{
				zoom++;
			}else if (zoom>0)
			{
				zoom--;
			}
			parameters.setZoom(zoom);
			camera.setParameters(parameters);
		}else {
			Log.i(TAG,"zoom not supported");
		}
	
	}
}
