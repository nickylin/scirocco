/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//		com.android.ide.eclipse.scirocco.internal.launch.junit.AndroidJUnitLaunchShortcut
package com.android.ide.eclipse.scirocco.internal.launch.junit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;

import com.android.ide.eclipse.scirocco.AdtPlugin;
import com.android.ide.eclipse.scirocco.internal.preferences.AdtPrefs;

/**
 * Launch shortcut to launch debug/run Android JUnit configuration directly.
 */
public class AndroidJUnitLaunchShortcut extends JUnitLaunchShortcut {

    @Override
    protected String getLaunchConfigurationTypeId() {
        return "com.android.ide.eclipse.scirocco.junit.launchConfigurationType"; //$NON-NLS-1$
    }

    
//    /**
//     * クラスパスを追加する。
//     */
//    private static void addClassPath(URL url) throws 
//        NoSuchMethodException,
//        IllegalAccessException, 
//        InvocationTargetException {
//      
//      // URLClassLoaderのprotectedメソッドaddURLを取得する
//      Method method = URLClassLoader.class.getDeclaredMethod(
//          "addURL",
//          new Class[] { URL.class });
//      
//      // アクセス可能に変更する
//      method.setAccessible(true);
//
//      // システムクラスローダーをURLClassLoaderと仮定し、addURLをコールする
//      method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { url });
//    }
    

    
    /**
     * Creates a default Android JUnit launch configuration. Sets the instrumentation runner to the
     * first instrumentation found in the AndroidManifest.
     */
    @Override
    protected ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element)
            throws CoreException {
    	
    	String sdkLocation = AdtPrefs.getPrefs().getOsSdkFolder();
    	if(sdkLocation ==null || "".equals(sdkLocation)){
    		MessageDialog.openInformation(
                AdtPlugin.getDisplay().getActiveShell(),
                "Scirocco Launch Error",  // title
                "Please Set 'Android SDK Location' by Preferences > Scirocco");
    		return null;
    	}
    	
        ILaunchConfigurationWorkingCopy config = super.createLaunchConfiguration(element);
        // just get first valid instrumentation runner
        String instrumentation = new InstrumentationRunnerValidator(element.getJavaProject()).
                getValidInstrumentationTestRunner();
        if (instrumentation != null) {
            config.setAttribute(AndroidJUnitLaunchConfigDelegate.ATTR_INSTR_NAME, 
                    instrumentation);
        }
        // if a valid runner is not found, rely on launch delegate to log error.
        // This method is called without explicit user action to launch Android JUnit, so avoid
        // logging an error here.
        AndroidJUnitLaunchConfigDelegate.setJUnitDefaults(config);
        return config;
    }
}
