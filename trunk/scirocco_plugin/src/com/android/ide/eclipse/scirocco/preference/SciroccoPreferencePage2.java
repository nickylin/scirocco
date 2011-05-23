package com.android.ide.eclipse.scirocco.preference;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.android.ide.eclipse.scirocco.AdtPlugin;
import com.android.ide.eclipse.scirocco.internal.preferences.AdtPrefs;

public class SciroccoPreferencePage2 extends FieldEditorPreferencePage implements
	IWorkbenchPreferencePage{

	public SciroccoPreferencePage2() {
		// TODO Auto-generated constructor stub
		super(GRID);
		setPreferenceStore(AdtPlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected void createFieldEditors() {
		// TODO Auto-generated method stub
		addField(new DirectoryFieldEditor(
				SciroccoPreferenceInitializer.ANDROID_SDK_LOCATION,"Android SDK Location:",getFieldEditorParent()));
		
		addField(new StringFieldEditor(
				SciroccoPreferenceInitializer.SCIROCCO_TMS_URL,"Scirocco_TMS URL:",getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean performOk() {
		super.performOk(); //Ç±ÇÍÇ≈thisÉNÉâÉXÇ…ç≈êVÇÃê›íËÇ™îΩâfÇ≥ÇÍÇÈ
		IPreferenceStore store = this.getPreferenceStore();
		String sdk = store.getString(SciroccoPreferenceInitializer.ANDROID_SDK_LOCATION);
//		AdtPrefs.init(this.getPreferenceStore());
		AdtPrefs.getPrefs().loadValues(null /*event*/);
		return super.performOk();
	}


}
