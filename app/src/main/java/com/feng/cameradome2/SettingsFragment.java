package com.feng.cameradome2;

import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feng on 17-11-16.
 */

public class SettingsFragment extends PreferenceFragment
{
	static Camera mCamera;
	static Camera.Parameters mParameters;
	
	public static final String KEY_PREF_PREV_SIZE = "preview_size";
	public static final String KEY_PREF_PIC_SIZE = "picture_size";
	public static final String KEY_PREF_VIDEO_SIZE = "video_size";
	public static final String KEY_PREF_FLASH_MODE = "flash_mode";
	public static final String KEY_PREF_FOCUS_MODE = "focus_mode";
	public static final String KEY_PREF_WHITE_BALANCE = "white_balance";
	public static final String KEY_PREF_SCENE_MODE = "scene_mode";
	public static final String KEY_PREF_GPS_DATA = "gps_data";
	public static final String KEY_PREF_EXPOS_COMP = "exposure_compensation";
	public static final String KEY_PREF_JPEG_QUALITY = "jpeg_quality";
	private static SharedPreferences aDefault;
	
	public static void setDefault(SharedPreferences aDefault)
	{
		SettingsFragment.aDefault = aDefault;
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		
		loadSupportedPreviewSize();
		loadSupportedPictureSize();
		loadSupportedVideoeSize();
		loadSupportedFlashMode();
		loadSupportedFocusMode();
		loadSupportedWhiteBalance();
		loadSupportedSceneMode();
		loadSupportedExposeCompensation();
	}
	
	public static void passCamera(Camera camera)
	{
		mCamera=camera;
		mParameters=camera.getParameters();
	}
	
	private void loadSupportedPreviewSize()
	{
		cameraSizeListToListPreference(mParameters.getSupportedPreviewSizes(), KEY_PREF_PREV_SIZE);
	}
	
	private void loadSupportedPictureSize() {
		cameraSizeListToListPreference(mParameters.getSupportedPictureSizes(), KEY_PREF_PIC_SIZE);
	}
	
	private void loadSupportedVideoeSize() {
		cameraSizeListToListPreference(mParameters.getSupportedVideoSizes(), KEY_PREF_VIDEO_SIZE);
	}
	
	private void loadSupportedFlashMode() {
		stringListToListPreference(mParameters.getSupportedFlashModes(), KEY_PREF_FLASH_MODE);
	}
	
	private void loadSupportedFocusMode() {
		stringListToListPreference(mParameters.getSupportedFocusModes(), KEY_PREF_FOCUS_MODE);
	}
	
	private void loadSupportedWhiteBalance() {
		stringListToListPreference(mParameters.getSupportedWhiteBalance(), KEY_PREF_WHITE_BALANCE);
	}
	
	private void loadSupportedSceneMode() {
		stringListToListPreference(mParameters.getSupportedSceneModes(), KEY_PREF_SCENE_MODE);
	}
	
	
	private void cameraSizeListToListPreference(List<Camera.Size> list1, String key1)
	{
		List<String> stringList = new ArrayList<>();
		for (Camera.Size size : list1) {
			String stringSize = size.width + "x" + size.height;
			stringList.add(stringSize);
		}
		stringListToListPreference(stringList, key1);
	}
	
	private void stringListToListPreference(List<String> list2, String key2)
	{
		final CharSequence[] charSeq = list2.toArray(new CharSequence[list2.size()]);
		ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key2);
		listPref.setEntries(charSeq);
		listPref.setEntryValues(charSeq);
		
	}
	private void loadSupportedExposeCompensation()
	{
		int minExposComp = mParameters.getMinExposureCompensation();
		int maxExposComp = mParameters.getMaxExposureCompensation();
		List<String> exposComp = new ArrayList<>();
		for (int value = minExposComp; value <= maxExposComp; value++)
		{
			exposComp.add(Integer.toString(value));
		}
		stringListToListPreference(exposComp, KEY_PREF_EXPOS_COMP);
		
	}
	
	public static void init(SharedPreferences defaultSharedPreferences)
	{
	
	}
}
