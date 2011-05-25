package com.android.ide.eclipse.scirocco.preference;

//import org.eclipse.draw2d.GridData;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.android.ide.eclipse.scirocco.AdtPlugin;

public class SciroccoPreferencePage extends PreferencePage implements IWorkbenchPreferencePage  {

	private Text text;
	
	@Override
	public Point computeSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean okToLeave() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean performCancel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
		store.setValue(SciroccoPreferenceInitializer.ANDROID_SDK_LOCATION, text.getText());
		return false;
	}

	@Override
	public void performDefaults() {
		IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
		text.setText(store.getDefaultString(SciroccoPreferenceInitializer.ANDROID_SDK_LOCATION));
	}
	
	@Override
	public void setContainer(IPreferencePageContainer preferencePageContainer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSize(Point size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Control getControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performHelp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImageDescriptor(ImageDescriptor image) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbench workbench) {
		
	}

	@Override
	protected Control createContents(Composite parent) {
		IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("adb Location");
		
		text = new Text(composite,SWT.SINGLE | SWT.NONE);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setText(store.getString(SciroccoPreferenceInitializer.ANDROID_SDK_LOCATION));
		
		return composite;
	}

}
