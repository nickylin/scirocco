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

package com.android.ide.eclipse.scirocco.internal.launch.junit.runtime;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.runner.FirstRunExecutionListener;
import org.eclipse.jdt.internal.junit.runner.MessageIds;
import org.eclipse.jdt.internal.junit.runner.RemoteTestRunner;
import org.eclipse.jdt.internal.junit.runner.TestExecution;
import org.eclipse.jdt.internal.junit.runner.TestReferenceFailure;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.android.ide.eclipse.scirocco.AdtPlugin;
import com.android.ide.eclipse.scirocco.internal.launch.LaunchMessages;
import com.android.ide.eclipse.scirocco.preference.SciroccoPreferenceInitializer;
import com.android.util.HTTPUtil;
import com.android.util.ZipUtil;

/**
 * Supports Eclipse JUnit execution of Android tests.
 * <p/>
 * Communicates back to a Eclipse JDT JUnit client via a socket connection.
 * 
 * @see org.eclipse.jdt.internal.junit.runner.RemoteTestRunner for more details
 *      on the protocol
 */
@SuppressWarnings("restriction")
public class RemoteAdtTestRunner extends RemoteTestRunner {

	private static final String DELAY_MSEC_KEY = "delay_msec";
	/** the delay between each test execution when in collecting test info */
	private static final String COLLECT_TEST_DELAY_MS = "15";

	private AndroidJUnitLaunchInfo mLaunchInfo;
	private TestExecution mExecution;
	private StringBuffer mSciroccoTestResult = new StringBuffer();
	private boolean mIsTestFailed = false;
	private static final String EVIDENCE_FOLDER_NAME = "evidence";
	private static final String PARAM_SCIROCCO_TEST_RESULT = "$SCIROCCO_TEST_RESULT";
	private static final String PARAM_SCIROCCO_PROJECT_NAME = "$PROJECT_NAME";
	private static final String PARAM_SCIROCCO_DEVICE = "$DEVICE";
	private static final String PARAM_SCIROCCO_CLASS_NAME = "$CLASS_NAME";
	private static final String PARAM_SCIROCCO_TEST_DATE = "$TEST_DATE";
	private static final String PARAM_SCIROCCO_TEST_PASS_COUNT = "$PASS";
	private static final String PARAM_SCIROCCO_TEST_FAIL_COUNT = "$FAIL";
	private static final String PARAM_PROJECT_NAME = "project";
	private static final String PARAM_CLASS_NAME = "class_name";
	private static final String PARAM_DEVICE_NAME = "device";
	private static final String PARAM_DATE = "date";
	private static final String TEMPLATES_DIRECTORY = "templates/"; //$NON-NLS-1$

	private static final String TEMPLATE_SCIROCCO_TEST_RESULT = TEMPLATES_DIRECTORY
			+ "scirocco_test_result.template";

	private int mTestPassCount = 0;
	private int mTestFailCount = 0;

	/**
	 * Initialize the JDT JUnit test runner parameters from the {@code args}.
	 * 
	 * @param args
	 *            name-value pair of arguments to pass to parent JUnit runner.
	 * @param launchInfo
	 *            the Android specific test launch info
	 */
	protected void init(String[] args, AndroidJUnitLaunchInfo launchInfo) {
		defaultInit(args);
		mLaunchInfo = launchInfo;
	}
	

	    

	public void test(){
		
	}

	/**
	 * Runs a set of tests, and reports back results using parent class.
	 * <p/>
	 * JDT Unit expects to be sent data in the following sequence:
	 * <ol>
	 * <li>The total number of tests to be executed.</li>
	 * <li>The test 'tree' data about the tests to be executed, which is
	 * composed of the set of test class names, the number of tests in each
	 * class, and the names of each test in the class.</li>
	 * <li>The test execution result for each test method. Expects individual
	 * notifications of the test execution start, any failures, and the end of
	 * the test execution.</li>
	 * <li>The end of the test run, with its elapsed time.</li>
	 * </ol>
	 * <p/>
	 * In order to satisfy this, this method performs two actual Android
	 * instrumentation runs. The first is a 'log only' run that will collect the
	 * test tree data, without actually executing the tests, and send it back to
	 * JDT JUnit. The second is the actual test execution, whose results will be
	 * communicated back in real-time to JDT JUnit.
	 * 
	 * @param testClassNames
	 *            ignored - the AndroidJUnitLaunchInfo will be used to determine
	 *            which tests to run.
	 * @param testName
	 *            ignored
	 * @param execution
	 *            used to report test progress
	 */
	@Override
	public void runTests(String[] testClassNames, String testName,
			TestExecution execution) {
		// hold onto this execution reference so it can be used to report test
		// progress
		mExecution = execution;

		RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
				mLaunchInfo.getAppPackage(), mLaunchInfo.getRunner(),
				mLaunchInfo.getDevice());

		if (mLaunchInfo.getTestClass() != null) {
			if (mLaunchInfo.getTestMethod() != null) {
				runner.setMethodName(mLaunchInfo.getTestClass(),
						mLaunchInfo.getTestMethod());
			} else {
				runner.setClassName(mLaunchInfo.getTestClass());
			}
		}

		if (mLaunchInfo.getTestPackage() != null) {
			runner.setTestPackageName(mLaunchInfo.getTestPackage());
		}

		// set log only to first collect test case info, so Eclipse has correct
		// test case count/
		// tree info
		runner.setLogOnly(true);
		// add a small delay between each test. Otherwise for large test suites
		// framework may
		// report Binder transaction failures
		runner.addInstrumentationArg(DELAY_MSEC_KEY, COLLECT_TEST_DELAY_MS);
		TestCollector collector = new TestCollector();
		try {
			AdtPlugin.printToConsole(mLaunchInfo.getProject(),
					"Collecting test information");

			runner.run(collector);
			if (collector.getErrorMessage() != null) {
				// error occurred during test collection.
				reportError(collector.getErrorMessage());
				// abort here
				notifyTestRunEnded(0);
				return;
			}
			notifyTestRunStarted(collector.getTestCaseCount());
			AdtPlugin.printToConsole(mLaunchInfo.getProject(),
					"Sending test information to Eclipse");

			collector.sendTrees(this);

			// now do real execution
			runner.setLogOnly(false);
			runner.removeInstrumentationArg(DELAY_MSEC_KEY);
			if (mLaunchInfo.isDebugMode()) {
				runner.setDebug(true);
			}
			AdtPlugin.printToConsole(mLaunchInfo.getProject(),
					"Running tests...");
			runner.run(new TestRunListener());
		} catch (TimeoutException e) {
			reportError(LaunchMessages.RemoteAdtTestRunner_RunTimeoutException);
		} catch (IOException e) {
			reportError(String.format(
					LaunchMessages.RemoteAdtTestRunner_RunIOException_s,
					e.getMessage()));
		} catch (AdbCommandRejectedException e) {
			reportError(String
					.format(LaunchMessages.RemoteAdtTestRunner_RunAdbCommandRejectedException_s,
							e.getMessage()));
		} catch (ShellCommandUnresponsiveException e) {
			reportError(LaunchMessages.RemoteAdtTestRunner_RunTimeoutException);
		}
	}

	/**
	 * Main entry method to run tests
	 * 
	 * @param programArgs
	 *            JDT JUnit program arguments to be processed by parent
	 * @param junitInfo
	 *            the {@link AndroidJUnitLaunchInfo} containing info about this
	 *            test ru
	 */
	public void runTests(String[] programArgs, AndroidJUnitLaunchInfo junitInfo) {
		init(programArgs, junitInfo);
		run();
	}

	
	static boolean mConnected = false;
	/**
	 * Connects to the remote ports and runs the tests.
	 */
	protected void run() {
		if (mConnected == false){
			if (!connect()){
				return;
			}
			mConnected = true;
		}
		
		FirstRunExecutionListener listener= firstRunExecutionListener();
		mExecution= new TestExecution(listener, getClassifier());
		runTests(mExecution);
	}
	
	/**
	 * Stop the current test run.
	 */
	public void terminate() {
		stop();
	}

	@Override
	protected void stop() {
		if (mExecution != null) {
			mExecution.stop();
		}
	}

	private void notifyTestRunEnded(long elapsedTime) {
		// copy from parent - not ideal, but method is private
		sendMessage(MessageIds.TEST_RUN_END + elapsedTime);
		try{
			flush();
		}catch(Exception e){
			System.out.println(e);
		}
		AdtPlugin.testRunEnded();
		// shutDown();
	}

	/**
	 * @param errorMessage
	 */
	private void reportError(String errorMessage) {
		AdtPlugin.printErrorToConsole(mLaunchInfo.getProject(), String
				.format(LaunchMessages.RemoteAdtTestRunner_RunFailedMsg_s,
						errorMessage));
		// is this needed?
		// notifyTestRunStopped(-1);
	}

	public static final String SCIROCCO_ANNOTATION_NAME = "Scirocco";

	/**
	 * TestRunListener that communicates results in real-time back to JDT JUnit
	 */
	private class TestRunListener implements ITestRunListener {

		private IAnnotation getSciroccoAnnotation(TestIdentifier test) {
			try {
				IProject project = mLaunchInfo.getProject();
				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragmentRoot root;

				root = javaProject.getPackageFragmentRoots()[0];

				String testClassName = test.getClassName();
				String[] classNameAry = testClassName.split("\\.");
				String className = classNameAry[classNameAry.length - 1];
				String packageName = test.getClassName().substring(0,
						testClassName.length() - className.length() - 1);

				IPackageFragment packageFragment = root
						.getPackageFragment(packageName);
				ICompilationUnit cu = packageFragment
						.getCompilationUnit(className + ".java");
				IType type = cu.getTypes()[0];
				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					if (method.getElementName().equals(test.getTestName())) {
						method.getAnnotations();
						return method.getAnnotation(SCIROCCO_ANNOTATION_NAME);
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			return null;
		}
		

		/**
		 * テスト結果を格納するHTMLテーブルにスクリーンショットを追加する
		 * 
		 * @param test
		 */
		public void appendMemoryInfo(TestIdentifier test) {
			mSciroccoTestResult.append("<td class='col'>\n");
			int MAX_NUMBER_OF_MEMORY_INFO = 50;
			try {
				for (int sequenceNo = 0; sequenceNo <= MAX_NUMBER_OF_MEMORY_INFO; sequenceNo++) {
					String memoryInfoFileName = getMemoryInfoFileName(test,sequenceNo);
					String absoluteMemoryInfoPath = getScreenShotFolder().getRawLocation() + "/" + memoryInfoFileName;
					File file = new File(absoluteMemoryInfoPath);
					if (file.exists() == false) {
						break;
					}
					BufferedReader br = new BufferedReader(
											new InputStreamReader(
												new FileInputStream(absoluteMemoryInfoPath),
												"UTF-8"));
		            String line;
		            while ((line = br.readLine()) != null) {
		            	mSciroccoTestResult.append(line);
		            }
	            	mSciroccoTestResult.append("<br>");
		            br.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSciroccoTestResult.append("</td>\n");
		}
		
		private static final String MEMORY_INFO_FILENAME= "memory_info";
		private String getMemoryInfoFileName(TestIdentifier test,int sequenceNo) {
			return MEMORY_INFO_FILENAME + "_" +
					 test.getTestName() + "_"
					+ Integer.toString(sequenceNo) + ".txt";
		}
		
		/**
		 * テスト結果を格納するHTMLテーブルにスクリーンショットを追加する
		 * @param test
		 */
		public void appendScreenShot(TestIdentifier test){
			mSciroccoTestResult.append("<td class='col'><table id='screenshot'><tr>\n");
			// TODO　この数を可変にする方法を考える
			// スクリーンショットの取得情報をSDカードに保存しておくか？
			int MAX_NUMBER_OF_SCREEN_SHOTS = 50;
			for (int screenShotSequenceNo = 0; screenShotSequenceNo <= MAX_NUMBER_OF_SCREEN_SHOTS; screenShotSequenceNo++) {
				String screenShotName =
					 test.getTestName() + "_"
					+ Integer.toString(screenShotSequenceNo) + ".jpg";

				String relativeScreenShotPath = EVIDENCE_FOLDER_NAME + "/" + screenShotName;
				String AbsoluteScreenShotPath = getScreenShotFolder()
						.getRawLocation() + "/" + screenShotName;

				System.out.println("screenShotPath= " + relativeScreenShotPath);
				File file = new File(AbsoluteScreenShotPath);
				if (file.exists() == false) {
					break;
				}
				mSciroccoTestResult.append("<td><Img Src="
						+ relativeScreenShotPath
						+ " Border='0' ></td>\n");
			}
			mSciroccoTestResult.append("</tr></table></td>\n");
			mSciroccoTestResult.append("</tr>");
		}
		
		
		public void testEnded(TestIdentifier test,
				Map<String, String> ignoredTestMetrics) {
			
			
			if (mIsTestFailed == false) {
				writeSciroccoTestResult(test, null);
				getTestEvidenceFromDevicesSDCard();
				appendMemoryInfo(test);
				appendScreenShot(test);
				mTestPassCount++;
			}
			mIsTestFailed = false;
			mExecution.getListener().notifyTestEnded(
					new TestCaseReference(test));
		}

		
		private IFolder getSciroccoFolder() {
			IProject project = mLaunchInfo.getProject();
			IFolder sciroccoFolder = project.getFolder(new Path(
					"scirocco"));
			return sciroccoFolder;
		}
		
		private IFolder getTestProjectFolder() {
			try {
				// プロジェクトディレクトリを作成
				IFolder testProjectFolder = getSciroccoFolder()
						.getFolder(mLaunchInfo.getProject().getName());
				if (!testProjectFolder.exists()) {
					testProjectFolder.create(false, true, null);
				}
				return testProjectFolder;
			} catch (CoreException e1) {
				e1.printStackTrace();
				return null;
			}
		}
		
		private IFolder getTestTimeFolder() {
			try {
				// プロジェクトディレクトリを作成
				IFolder testProjectFolder = this.getTestProjectFolder(); 

				// クラスディレクトリを作成
				IFolder testClassFolder = testProjectFolder
						.getFolder(mLaunchInfo.getTestClass());
				if (!testClassFolder.exists()) {
					testClassFolder.create(false, true, null);
				}

				// デバイスディレクトリを作成
				IFolder deviceFolder = testClassFolder.getFolder(mLaunchInfo
						.getDevice().getSerialNumber());
				if (!deviceFolder.exists()) {
					deviceFolder.create(false, true, null);
				}

				// テスト時刻ディレクトリを作成
				IFolder testTimeFolder = deviceFolder.getFolder(AdtPlugin
						.getTestTime());
				if (!testTimeFolder.exists()) {
					testTimeFolder.create(false, true, null);
				}
				return testTimeFolder;
			} catch (CoreException e1) {
				e1.printStackTrace();
				return null;
			}
		}

		/**
		 * ScreenShotFolderを取得する ディレクトリ構成
		 * scirocco/<テストクラス名>/<デバイス名>/<テスト時刻>/test_result.html
		 * screenshots/abc.jpg
		 */
		private IFolder getScreenShotFolder() {
			try {
				// テスト時刻ディレクトリを作成
				IFolder testTimeFolder = getTestTimeFolder();

				// スクリーンショットディレクトリを作成
				IFolder screenShotFolder = testTimeFolder
						.getFolder(EVIDENCE_FOLDER_NAME);
				if (!screenShotFolder.exists()) {
					screenShotFolder.create(false, true, null);
				}
				return screenShotFolder;
			} catch (CoreException e1) {
				e1.printStackTrace();
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.android.ddmlib.testrunner.ITestRunListener#testFailed(com.android
		 * .ddmlib.testrunner.ITestRunListener.TestFailure,
		 * com.android.ddmlib.testrunner.TestIdentifier, java.lang.String)
		 */
		public void testFailed(TestFailure status, TestIdentifier test,
				String trace) {
			mIsTestFailed = true;
			mTestFailCount++;
			String statusString;
			if (status == TestFailure.ERROR) {
				statusString = MessageIds.TEST_ERROR;
			} else {
				statusString = MessageIds.TEST_FAILED;
			}
			TestReferenceFailure failure = new TestReferenceFailure(
					new TestCaseReference(test), statusString, trace, null);
            try{
            	mExecution.getListener().notifyTestFailed(failure);
            }catch(Exception e){
            	e.printStackTrace();
            }
			writeSciroccoTestResult(test, trace);
			appendScreenShot(test);
			AdtPlugin.printToConsole(mLaunchInfo.getProject(),
					"=scirocco=testFailed+" + test.getTestName() + trace);
		}

		private void writeSciroccoTestResult(TestIdentifier test, String trace) {
			String testProcedure = "";
			String confirmationContents = "";

			// アノテーションのパラメータを取得
			IAnnotation annotation = getSciroccoAnnotation(test);
			if (annotation != null && annotation.exists() != false) {
				IMemberValuePair[] values;
				try {
					values = annotation.getMemberValuePairs();
					for (IMemberValuePair value : values) {
						if (value.getMemberName().equals("testProcedure")) {
							testProcedure = value.getValue().toString();
						} else if (value.getMemberName().equals(
								"confirmationContents")) {
							confirmationContents = value.getValue().toString();
						}
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
			
			
			mSciroccoTestResult.append("<tr class='row'>");
			if (mIsTestFailed) {
				mSciroccoTestResult
						.append("<td class='col'><font color='red'>Fail</font></td>\n");
			} else {
				mSciroccoTestResult
						.append("<td class='col'><font color='green'>Pass</font></td>\n");
			}
			mSciroccoTestResult.append("<td class='col'>" + test.getTestName() + "</td>\n");
			mSciroccoTestResult.append("<td class='col'>" + testProcedure + "</td>\n");
			mSciroccoTestResult.append("<td class='col'>" + confirmationContents
					+ "</td>\n");
			if (trace == null) {
				trace = "";
			}
			mSciroccoTestResult.append("<td class='col'>" + trace + "</td>\n");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.android.ddmlib.testrunner.ITestRunListener#testRunEnded(long,
		 * Map<String, String>)
		 */
		public void testRunEnded(long elapsedTime,
				Map<String, String> runMetrics) {
			notifyTestRunEnded(elapsedTime);
			AdtPlugin.printToConsole(mLaunchInfo.getProject(),
					LaunchMessages.RemoteAdtTestRunner_RunCompleteMsg);
			createSciroccoReportFile();
			String cgi_url = AdtPlugin.getDefault().getPreferenceStore().getString(SciroccoPreferenceInitializer.SCIROCCO_TMS_URL);
			if (cgi_url != null && cgi_url != ""){
				sendSciroccoReportToServer();
			}
			//scirocco
			removeFileFromSDCard();
		}


		
		private void sendSciroccoReportToServer() {
			IFolder testTimeFolder = this.getTestTimeFolder();
			String srcPath = testTimeFolder.getRawLocation().toString();
			String destPath = getSciroccoFolder().getRawLocation().toString() + "/temp.zip";
			String sciroccoServerUrl =  AdtPlugin.getDefault().getPreferenceStore().getString(SciroccoPreferenceInitializer.SCIROCCO_TMS_URL);
			if (!sciroccoServerUrl.endsWith("/")){
				sciroccoServerUrl = sciroccoServerUrl + "/";
			}
			
			String upload_cgi_url = sciroccoServerUrl + "upload_done";
			final Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(PARAM_PROJECT_NAME,mLaunchInfo.getProject().getName());
			parameters.put(PARAM_CLASS_NAME,mLaunchInfo.getTestClass());
			parameters.put(PARAM_DEVICE_NAME,mLaunchInfo.getDevice().getSerialNumber());
			parameters.put(PARAM_DATE,AdtPlugin.getTestTime());
			
			try {
				// 送信ファイルをzip化
				ZipUtil.zip(srcPath,destPath);
				HTTPUtil.postUploadFile(upload_cgi_url,destPath,parameters);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.android.ddmlib.testrunner.ITestRunListener#testRunFailed(java
		 * .lang.String)
		 */
		public void testRunFailed(String errorMessage) {
			// System.out.println("testRunFailed");
			AdtPlugin.printToConsole(mLaunchInfo.getProject(),
					"<scirocco>testRunFailed+" + errorMessage + "</scirocco>");

			reportError(errorMessage);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.android.ddmlib.testrunner.ITestRunListener#testRunStarted(int)
		 */
		public void testRunStarted(String runName, int testCount) {
			AdtPlugin.setTestTime();
			mTestPassCount = 0;
			mTestFailCount = 0;

			// ignore
			AdtPlugin.printToConsole(mLaunchInfo.getProject(),
					"<scirocco>testRunStarted</scirocco>");

			// テスト実行前にSDカードのスクリーンショットディレクトリを削除
			// + " -s " + mLaunchInfo.getDevice().getSerialNumber()
			// + " shell "
			// + " rm -r /mnt/sdcard/scirocco");
			AdtPlugin.execCommand(mLaunchInfo.getProject().getName(),AdtPlugin.getOsAbsoluteAdb() + " -s "
					+ mLaunchInfo.getDevice().getSerialNumber() + " shell "
					+ " rm -r /sdcard/scirocco");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.android.ddmlib.testrunner.ITestRunListener#testRunStopped(long)
		 */
		public void testRunStopped(long elapsedTime) {
			System.out.println("testRunStopped");
			notifyTestRunStopped(elapsedTime);
			AdtPlugin.printToConsole(mLaunchInfo.getProject(),
					LaunchMessages.RemoteAdtTestRunner_RunStoppedMsg);

		}

		private static final String DEVICE_SDCARD_DIR0 = "/sdcard/scirocco";
		private static final String DEVICE_SDCARD_DIR1 = "/mnt/sdcard/scirocco";

		// private static final String SCIROCCO_SCREENSHOT_DIR =
		// "/mnt/sdcard/scirocco";
		/*
		 * Sciroccoのテスト結果ファイル(.html)をテストプロジェクト内に作成する
		 */
		private void createSciroccoReportFile() {
			final Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(PARAM_SCIROCCO_PROJECT_NAME,
					mLaunchInfo.getProject().getName());
			parameters.put(PARAM_SCIROCCO_CLASS_NAME,
					mLaunchInfo.getTestClass());
			parameters.put(PARAM_SCIROCCO_DEVICE, mLaunchInfo.getDevice()
					.getSerialNumber());
			parameters.put(PARAM_SCIROCCO_TEST_DATE,
					getFormattedTestDate());
			parameters.put(PARAM_SCIROCCO_TEST_PASS_COUNT,
					Integer.toString(mTestPassCount));
			parameters.put(PARAM_SCIROCCO_TEST_FAIL_COUNT,
					Integer.toString(mTestFailCount));
			parameters.put(PARAM_SCIROCCO_TEST_RESULT,
					
					mSciroccoTestResult.toString());

			IFolder testTimeFolder = getTestTimeFolder();
			IFile file = testTimeFolder.getFile(new Path(
					"report.html"));
			String scirocoTestResultTemplate = AdtPlugin
					.readEmbeddedTextFile(TEMPLATE_SCIROCCO_TEST_RESULT);
			// Replace all keyword parameters
			scirocoTestResultTemplate = replaceParameters(
					scirocoTestResultTemplate, parameters);
			InputStream is;
			try {
				is = new ByteArrayInputStream(
						scirocoTestResultTemplate.getBytes("UTF-8"));
				file.delete(true, null);
				file.create(is, true, null);
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		private String getFormattedTestDate() {
			String d = AdtPlugin.getTestTime();
			String year = d.substring(0,4);
			String month = d.substring(4,6);
			String day = d.substring(6,8);
			String h = d.substring(8,10);
			String m = d.substring(10,12);
			String s = d.substring(12,d.length());
			return year + "/" + month + "/" + day +"/" + " " + h + ":" + m + ":" + s ;
		}

		/**
		 * Replaces placeholders found in a string with values.
		 * 
		 * @param str
		 *            the string to search for placeholders.
		 * @param parameters
		 *            a map of <placeholder, Value> to search for in the string
		 * @return A new String object with the placeholder replaced by the
		 *         values.
		 */
		private String replaceParameters(String str,
				Map<String, Object> parameters) {

			if (parameters == null) {
				AdtPlugin
						.log(IStatus.ERROR,
								"NPW replace parameters: null parameter map. String: '%s'", str); //$NON-NLS-1$
				return str;
			} else if (str == null) {
				AdtPlugin.log(IStatus.ERROR,
						"NPW replace parameters: null template string"); //$NON-NLS-1$
				return str;
			}

			for (Entry<String, Object> entry : parameters.entrySet()) {
				if (entry != null && entry.getValue() instanceof String) {
					Object value = entry.getValue();
					if (value == null) {
						AdtPlugin
								.log(IStatus.ERROR,
										"NPW replace parameters: null value for key '%s' in template '%s'", //$NON-NLS-1$
										entry.getKey(), str);
					} else {
						// str = str.replaceAll(entry.getKey(), (String) value);
						str = str.replace(entry.getKey(), (String) value);
					}
				}
			}

			return str;
		}

		private String getDeviceSDCardDir(int idx){
			if (idx ==0){
				return DEVICE_SDCARD_DIR0;
			}else{
				return DEVICE_SDCARD_DIR1;
			}
		}
		
		private void removeFileFromSDCard(){
			for (int idx = 0; idx <= 1; idx++) {
				// SDカードの内容をクリア
				AdtPlugin.execCommand(mLaunchInfo.getProject().getName(),
						AdtPlugin.getOsAbsoluteAdb() + " -s "
								+ mLaunchInfo.getDevice().getSerialNumber()
								+ " shell " + " rm -r "
								+ getDeviceSDCardDir(idx));
			}
		}
		
		private void getTestEvidenceFromDevicesSDCard() {
			IFolder screenShotFolder = getScreenShotFolder();
			for (int idx = 0; idx <= 1; idx++) {
				// 端末のSDカードに保存された証跡(スクリーンショットとメモリ情報)をテストプロジェクト内にコピー
				String pullCmd = AdtPlugin.getOsAbsoluteAdb() + " -s "
						+ mLaunchInfo.getDevice().getSerialNumber() + " pull "
						+ getDeviceSDCardDir(idx) + " "
						+ screenShotFolder.getLocation();
				AdtPlugin.printToConsole(mLaunchInfo.getProject(),"pullCmd = " + pullCmd);
				AdtPlugin.execCommand(mLaunchInfo.getProject().getName(),
						pullCmd);
			}
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.android.ddmlib.testrunner.ITestRunListener#testStarted(com.android
		 * .ddmlib.testrunner.TestIdentifier)
		 */
		public void testStarted(TestIdentifier test) {
			TestCaseReference testId = new TestCaseReference(test);
			try{
				mExecution.getListener().notifyTestStarted(testId);
			}catch(Exception e){
				System.out.println(e);
			}
		}
	}

	/**
	 * Override parent to get extra logs.
	 */
	@Override
	protected boolean connect() {
		boolean result = super.connect();
		if (!result) {
			AdtPlugin.printErrorToConsole(mLaunchInfo.getProject(),
					"Connect to Eclipse test result listener failed");
		}
		return result;
	}

	/**
	 * Override parent to dump error message to console.
	 */
	@Override
	public void runFailed(String message, Exception exception) {
		if (exception != null) {
			AdtPlugin.logAndPrintError(exception, mLaunchInfo.getProject()
					.getName(), "Test launch failed: %s", message);
		} else {
			AdtPlugin.printErrorToConsole(mLaunchInfo.getProject(),
					"Test launch failed: %s", message);
		}
	}
}

