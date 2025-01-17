package UITestFramework;

import Reports.ExtentReportManager;
import com.aventstack.extentreports.ExtentReports;

import com.google.common.collect.ImmutableMap;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Properties;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import Reports.Log;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.*;



/**
 * contains all the methods to create a new session and destroy the 
 * session after the test(s) execution is over. Each test extends
 *  this class.
 */
public class CreateSession  {

	public WebDriver driver = null;
	Properties configFile ;
	protected static Properties lobConfigProp = new Properties();
	protected static Properties localeConfigProp = new Properties();
	protected FileInputStream configFis, lobConfigFis, localeConfigFis;	
	public Properties testDataFile;
	protected ExtentReports extent = ExtentReportManager.extent;
	private final String CONFIG_FILE_PATH="//src//main//java//config//config.properties";
	protected File file = new File("");
	Properties configProp = new Properties();
	String OS;
	/** 
	 * Returns the instance of the webdriver. 
	 * @return webdriver instance 
	 */
	public WebDriver getDriver() {
        return driver;
}
	

	/** 
	 * this method starts Appium server. Calls startAppiumServer method to start the session depending upon your OS.
	 * @throws Exception Unable to start appium server
	 */

	@BeforeSuite
	public void invokeAppium() throws Exception
	{
		String OS = System.getProperty("os.name").toLowerCase();
		try{
			startAppiumServer(OS);
			Log.info("Appium server started successfully");
		}
		catch (Exception e) {
			Log.error("Unable to start appium server");
			throw new Exception(e.getMessage());
		}

	}

	/** 
	 * this method stops Appium server.Calls stopAppiumServer method to 
	 * stop session depending upon your OS.
	 * @throws Exception Unable to stop appium server
	 */

	@AfterTest
	public void stopAppium() throws Exception {
		try{
			stopAppiumServer(OS);
			Log.info("Appium server stopped successfully");

		}
		catch (Exception e) {
			Log.error("Unable to stop appium server");
			throw new Exception(e.getMessage());
		}
	}


	/** 
	 * this method creates the driver depending upon the passed parameter (android or iOS)
	 *  and loads the properties files (config and test data properties files).
	 * @param os android or iOS
	 * @param methodName - name of the method under execution  
	 * @throws Exception issue while loading properties files or creation of driver.
	 */
	@Parameters({"os"})
	@BeforeClass
	public  void createDriver(String os, Method methodName) throws Exception{

		propertiesFileLoad(os);

		File propertiesFile = new File(file.getAbsoluteFile() + "//src//test//resources//log4j2.properties");
		PropertyConfigurator.configure(propertiesFile.toString());
		Log.info("Creating driver");



		if (os.equalsIgnoreCase("android")){
			String buildPath = choosebuild(os);
			androidDriver(buildPath, methodName);
			Log.info("Android driver created");

		}																		         
		else if (os.equalsIgnoreCase("iOS")){
			String buildPath = choosebuild(os);
			iOSDriver(buildPath, methodName);
			Log.info("iOS driver created");
		}


	}


	/** 
	 * this method quit the driver after the execution of test(s) 
	 */
	@AfterClass
	public void teardown(){
		Log.info("Shutting down driver");
		driver.quit();
	}



	/** 
	 *  this method creates the android driver
	 *  @param buildPath - path to pick the location of the app
	 *  @param methodName - name of the method under execution 
	 * @throws MalformedURLException Thrown to indicate that a malformed URL has occurred.
	 */
	public synchronized void androidDriver(String buildPath, Method methodName) throws MalformedURLException{
		File app = new File(buildPath);
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("deviceName", "Realme");
		capabilities.setCapability("platformName","Android");
		capabilities.setCapability("appPackage", "xunison.com.smarthomeapp");
		capabilities.setCapability("appActivity", "xunison.com.smarthomeapp.screen.main.activity.MainActivity");
		capabilities.setCapability("noReset", "true");
		capabilities.setCapability("fullReset","false");
//		capabilities.setCapability("name", methodName.getName());
		capabilities.setCapability("app", app.getAbsolutePath());
		capabilities.setCapability("automationName", "UiAutomator2");
		capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "");
		capabilities.setCapability("chromedriverExecutable","C:\\software\\Chromedriver\\chromedriver90.exe");
		capabilities.setCapability("appium:chromeOptions", ImmutableMap.of("w3c", false));
		capabilities.setCapability("autoGrantPermissions", true);
		driver = new AndroidDriver( appiumService.getUrl(), capabilities);

	}

	/** 
	 *  this method creates the iOS driver
	 *  @param buildPath- path to pick the location of the app
	 *  @param methodName- name of the method under execution
	 * @throws MalformedURLException Thrown to indicate that a malformed URL has occurred.
	 */
	public void iOSDriver(String buildPath, Method methodName) throws MalformedURLException {
		File app = new File(buildPath);
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("platformName","iOS");
		capabilities.setCapability("platformVersion", "8.2");
		capabilities.setCapability("appiumVersion", "1.3.7");
		capabilities.setCapability("name", methodName.getName());
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME,"iPhone 5s"); 
		capabilities.setCapability("app", app.getAbsolutePath());
		driver  = new IOSDriver( appiumService.getUrl(), capabilities);

	}



	/** 
	 *  this method starts the appium  server depending on your OS.
	 * @param os your machine OS (windows/linux/mac)
	 * @throws IOException Signals that an I/O exception of some sort has occurred
	 * @throws ExecuteException An exception indicating that the executing a subprocesses failed
	 * @throws InterruptedException Thrown when a thread is waiting, sleeping, 
	 * or otherwise occupied, and the thread is interrupted, either before
	 *  or during the activity.
	 */

	private static AppiumDriverLocalService appiumService;
		private static AppiumServiceBuilder builder;
	public void startAppiumServer(String os) throws ExecuteException, IOException, InterruptedException{
		if (os.contains("windows")){
			String appiumJSPath = "/Users/HP/AppData/Roaming/npm/node_modules/appium/build/lib/main.js";
			 builder = new AppiumServiceBuilder()
					.withAppiumJS(new File(appiumJSPath))
//                .usingDriverExecutable(new File(Appium_Node_Path))
					.withIPAddress("0.0.0.0")
					.usingAnyFreePort()
					.withArgument(GeneralServerFlag.SESSION_OVERRIDE)
					.withArgument(GeneralServerFlag.LOG_LEVEL, "error");
			appiumService = builder.build();
			appiumService.start();


			Log.info("Appium started on " + appiumService.getUrl());

		}
		else if (os.contains("mac os x")){
			CommandLine command = new CommandLine("/Applications/Appium.app/Contents/Resources/node/bin/node");  
			command.addArgument("/Applications/Appium.app/Contents/Resources/node_modules/appium/bin/appium.js", false);  
			command.addArgument("--address", false);  
			command.addArgument("127.0.0.1");  
			command.addArgument("--port", false);  
			command.addArgument("4723");  
			command.addArgument("--full-reset", false);  
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();  
			DefaultExecutor executor = new DefaultExecutor();  
			executor.setExitValue(1);  
			executor.execute(command, resultHandler);  
			Thread.sleep(5000);  
		}
		else if (os.contains("linux")){
			//Start the appium server
			System.out.println("ANDROID_HOME : ");
			System.getenv("ANDROID_HOME");
			//	System.out.println("PATH :" +System.getenv("PATH"));
			CommandLine command = new CommandLine("/bin/bash");
			command.addArgument("-c");
			command.addArgument("~/.linuxbrew/bin/node");
			command.addArgument("~/.linuxbrew/lib/node_modules/appium/lib/appium.js", true);
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			DefaultExecutor executor = new DefaultExecutor();
			executor.setExitValue(1);
			executor.execute(command, resultHandler);
			Thread.sleep(5000); //Wait for appium server to start	

		}
		else{
			Log.info(os + "is not supported yet");
		}
	}

	/** 
	 *  this method stops the appium  server.
	 * @param os your machine OS (windows/linux/mac).
	 * @throws IOException Signals that an I/O exception of some sort has occurred. 
	 * @throws ExecuteException An exception indicating that the executing a subprocesses failed.
	 */
	public void stopAppiumServer(String os) throws ExecuteException, IOException {
			if (appiumService != null) {
				appiumService.stop();
				appiumService = null;
				Log.info("Appium server stopped");
			} else {
				Log.error("Appium server fail to stopped");
			}
	}

	/** 
	 *  this method loads properties files config and file having test data.
	 * @param platform android or ios, to load specific test data file.
	 * @throws Exception property files are not loaded successfully
	 */
	public void propertiesFileLoad(String platform) throws Exception{
		configFis = new FileInputStream(file.getAbsoluteFile()
				+ CONFIG_FILE_PATH);
		configProp .load(configFis);


		File f = new File(file.getAbsoluteFile() + "/src/main/java/config/" + platform
				+ "_config.properties");


		if (f.exists() && !f.isDirectory()) {
			lobConfigFis = new FileInputStream(file.getAbsoluteFile()
					+ "/src/main/java/config/" + platform + "_config.properties");
			lobConfigProp.load(lobConfigFis);

			String locale = lobConfigProp.getProperty("LOCALE");

			localeConfigFis = new FileInputStream(file.getAbsoluteFile()
					+ "/src/main/java/testData/" + locale + "_" + platform  + ".properties");
			localeConfigProp.load(localeConfigFis);
		} 
		else {
			throw new Exception("Properties files loading failed ");
		}}

	public String choosebuild(String invokeDriver){
		String appPath = null;
		if(invokeDriver.equals("android")){
			appPath = configProp.getProperty("AndroidAppPath");
			return appPath;
		}
		else if(invokeDriver.equals("iOS")){
			appPath = configProp.getProperty("iOSAppPath");
			return appPath;
		}

		return appPath;
	}


	@AfterSuite
	void flushReport() throws Exception {
		extent.flush();

	}



}

