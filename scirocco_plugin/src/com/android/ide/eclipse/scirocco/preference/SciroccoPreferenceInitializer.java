package com.android.ide.eclipse.scirocco.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.android.ide.eclipse.scirocco.AdtPlugin;

import scirocco.Activator;

public class SciroccoPreferenceInitializer extends AbstractPreferenceInitializer {

	public static String SCIROCCO_TMS_URL = "SCIROCCO_SERVER_URL";
	public static String ANDROID_SDK_LOCATION = "ANDROID_SDK_LOCATION";
	@Override
	public void initializeDefaultPreferences() {
		// TODO Auto-generated method stub
		IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
		store.setDefault(SCIROCCO_TMS_URL, "");
		store.setDefault(ANDROID_SDK_LOCATION, "");
	}

}
