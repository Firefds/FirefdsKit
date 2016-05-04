/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sb.firefds.xtouchwizS5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemProperties;
import sb.firefds.xtouchwizS5.bean.FeatureDTO;
import sb.firefds.xtouchwizS5.utils.Constants;
import sb.firefds.xtouchwizS5.utils.Utils;
import sb.firefds.xtouchwizS5.utils.Utils.SuTask;

import com.sec.android.app.CscFeature;

public class XCscFeaturesManager {

	private static final String RO_BUILD_PDA = "ro.build.PDA";
	private static final String SYSTEM_CSC_SALES_CODE_DAT = "/system/csc/sales_code.dat";
	private static final String SYSTEM_CSC_VERSION_TXT = "/system/CSCVersion.txt";
	private static ArrayList<FeatureDTO> defaultFeatureDTOs;
	private static String version = "";
	private static String country = "";
	private static String countryISO = "";
	private static String salesCode = "";

	public static void applyCscFeatures(SharedPreferences prefs) {
		StringBuffer features = new StringBuffer();
		ArrayList<FeatureDTO> featuresDTOList = new ArrayList<FeatureDTO>();

		try {

			setCameraAndBootSounds(prefs);

			setVolumeControlSounds(prefs);

			setLowBatterySounds(prefs);

			featuresDTOList = getCscFeaturesList(prefs);

			for (FeatureDTO featureDTO : featuresDTOList) {
				features.append("<" + featureDTO.getfeatureCode() + ">" + featureDTO.getFeatureValue() + "</"
						+ featureDTO.getfeatureCode() + ">\n");
			}

			if (defaultFeatureDTOs != null && defaultFeatureDTOs.size() > 0) {
				for (FeatureDTO defaultFeatureDTO : defaultFeatureDTOs) {
					if (features.indexOf(defaultFeatureDTO.getfeatureCode()) < 0) {
						features.append("<" + defaultFeatureDTO.getfeatureCode() + ">"
								+ defaultFeatureDTO.getFeatureValue() + "</" + defaultFeatureDTO.getfeatureCode()
								+ ">\n");
					}
				}
			}

			applyCSCFeatures(Constants.FEATURES_LIST_HEADER1 + (version.isEmpty() ? "ED0001" : version)
					+ Constants.FEATURES_LIST_HEADER2 + (country.isEmpty() ? "UNITED KINGDOM" : country)
					+ Constants.FEATURES_LIST_HEADER3 + (countryISO.isEmpty() ? "GB" : countryISO)
					+ Constants.FEATURES_LIST_HEADER4 + (salesCode.isEmpty() ? "BTU" : salesCode)
					+ Constants.FEATURES_LIST_HEADER5 + features + Constants.FEATURES_LIST_FOOTER);
			new SuTask().execute("echo " + (salesCode.isEmpty() ? "BTU" : salesCode) + " > "
					+ SYSTEM_CSC_SALES_CODE_DAT);

			String pda = SystemProperties.get(RO_BUILD_PDA);
			if (!pda.isEmpty() && !(new File(SYSTEM_CSC_VERSION_TXT).exists())) {
				new SuTask().execute("echo " + pda + " > " + SYSTEM_CSC_VERSION_TXT);
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static void setCameraAndBootSounds(SharedPreferences prefs) {
		if (prefs.getBoolean("disableBootSound", false)) {
			Utils.disableBootSounds(MainApplication.getAppContext());
		} else {
			Utils.enableBootSounds(MainApplication.getAppContext());
		}

	}

	private static void setVolumeControlSounds(SharedPreferences prefs) {
		if (prefs.getBoolean("disableVolumeControlSound", false)) {
			Utils.disableVolumeControlSounds(MainApplication.getAppContext());
		} else {
			Utils.enableVolumeControlSounds(MainApplication.getAppContext());
		}
	}

	private static void setLowBatterySounds(SharedPreferences prefs) {
		if (prefs.getBoolean("disableLowBatterySound", false)) {
			Utils.disableLowBatterySounds(MainApplication.getAppContext());
		} else {
			Utils.enableLowBatterySounds(MainApplication.getAppContext());
		}
	}

	/**
	 * @param prefs
	 * @param languageDTOList
	 */
	private static ArrayList<FeatureDTO> getCscFeaturesList(SharedPreferences prefs) {
		ArrayList<FeatureDTO> featuresDTOList = new ArrayList<FeatureDTO>();

		// Call recording
		if (prefs.getBoolean("enableCallRecordingMenu", false) || !prefs.getBoolean("enableCallAdd", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_VoiceCall_ConfigRecording", "RecordingAllowed"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_VoiceCall_ConfigRecording", "RecordingNotAllowed"));

		// Call button
		if (prefs.getBoolean("enableCallButtonLogs", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_Contact_EnableCallButtonInList", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_Contact_EnableCallButtonInList", "FALSE"));

		// Account icons
		if (prefs.getBoolean("disableAccountIconsList", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_Contact_DisableAccountIconsInContactList", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_Contact_DisableAccountIconsInContactList", "FALSE"));
		
		// Email App BG
		if (prefs.getBoolean("enableWhiteEmailBackgroung", true)) {
			featuresDTOList.add(new FeatureDTO("CscFeature_Email_UseFixedBgColorAsWhite", "TRUE"));
			featuresDTOList.add(new FeatureDTO("CscFeature_Email_BackgroundColorWhite", "TRUE"));
		} else {
			featuresDTOList.add(new FeatureDTO("CscFeature_Email_UseFixedBgColorAsWhite", "FALSE"));
			featuresDTOList.add(new FeatureDTO("CscFeature_Email_BackgroundColorWhite", "FALSE"));
		}

		// Messaging
		if (prefs.getBoolean("automaticInputMode", true)) {
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_SmsInputMode", "automatic"));
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_DisableMenuSmsInputMode", "FALSE"));
		} else {
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_SmsInputMode", ""));
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_SmsInputMode", "TRUE"));
		}

		if (prefs.getBoolean("forceMMSConnect", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_RIL_ForceConnectMMS", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_RIL_ForceConnectMMS", "FALSE"));

		if (prefs.getBoolean("enableSaveRestore", true))
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_EnableSaveRestoreSDCard", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_EnableSaveRestoreSDCard", "FALSE"));

		if (prefs.getBoolean("disableSmsToMmsConversion", false)) {
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_DisableSmsToMmsConversionByTextInput", "TRUE"));
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_SmsMaxByte", "999"));
		} else {
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_DisableSmsToMmsConversionByTextInput", "FALSE"));
			featuresDTOList.add(new FeatureDTO("CscFeature_Message_SmsMaxByte", "140"));
		}
		featuresDTOList.add(new FeatureDTO("CscFeature_Message_MaxRecipientLengthAs", "999"));

		// Browser Terminate button
		if (prefs.getBoolean("addBrowserTerminateButton", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_Web_AddOptionToTerminate", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_Web_AddOptionToTerminate", "FALSE"));

		// Wifi AP Clients
		featuresDTOList
				.add(new FeatureDTO("CscFeature_Wifi_MaxClient4MobileAp", "" + prefs.getInt("wifiAPClients", 4)));

		// Contacts
		if (prefs.getBoolean("unlimittedContactsJoining", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_Contact_SetLinkCountMaxAs", "999"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_Contact_SetLinkCountMaxAs", "5"));

		if (prefs.getBoolean("useSeparateAddressField", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_Contact_UseSeparateAddressField", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_Contact_UseSeparateAddressField", "FALSE"));

		if (prefs.getBoolean("disableNumberFormating", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_Common_DisablePhoneNumberFormatting", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_Common_DisablePhoneNumberFormatting", "FALSE"));

		// Lockscreen
		if (prefs.getBoolean("disableLockedAdb", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_LockScreen_DisableADBConnDuringSecuredLock", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_LockScreen_DisableADBConnDuringSecuredLock", "FALSE"));

		// Camera
		if (prefs.getBoolean("enableCameraDuringCall", false))
			featuresDTOList.add(new FeatureDTO("CscFeature_Camera_EnableCameraDuringCall", "TRUE"));
		else
			featuresDTOList.add(new FeatureDTO("CscFeature_Camera_EnableCameraDuringCall", "FALSE"));

		featuresDTOList.add(new FeatureDTO("CscFeature_Camera_CamcoderForceShutterSoundDuringSnapShot", "FALSE"));

		featuresDTOList.add(new FeatureDTO("CscFeature_Camera_ShutterSoundMenu", "TRUE"));

		if (!prefs.getString("selectedMwApps", "").trim().equalsIgnoreCase("")) {
			featuresDTOList.add(new FeatureDTO("CscFeature_MultiWindow_AddOnApp", prefs.getString("selectedMwApps", "")
					.replace(";", ",")));

			featuresDTOList.add(new FeatureDTO("CscFeature_Framework_AddOnApp4MultiWindow", prefs.getString(
					"selectedMwApps", "").replace(";", ",")));

			featuresDTOList.add(new FeatureDTO("CscFeature_Framework_AddOnApp4PenWindow", prefs.getString(
					"selectedMwApps", "").replace(";", ",")));
		}
		return featuresDTOList;
	}

	private static void applyCSCFeatures(String langList) {
		new CSCFeaturesTask().execute(langList);
	}

	private static class CSCFeaturesTask extends AsyncTask<String, Void, Void> {
		private OutputStream out = null;
		private File featureXML = new File(MainApplication.getAppContext().getCacheDir(), Constants.FEATURE_XML);

		protected Void doInBackground(String... params) {
			try {

				out = new FileOutputStream(featureXML);
				out.write(((String) params[0]).getBytes());

			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (featureXML.isFile()) {
				try {
					Utils.applyCSCFeatues(MainApplication.getAppContext());
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}

	}

	public static void getDefaultCSCFeatures() {

		defaultFeatureDTOs = new ArrayList<FeatureDTO>();

		try {

			List<FeatureDTO> cscDTOList = new ArrayList<FeatureDTO>();
			CscFeature mCscFeature = null;
			Field fieldFeatureList = null;

			mCscFeature = CscFeature.getInstance();

			version = mCscFeature.getString("Version");
			country = mCscFeature.getString("Country");
			countryISO = mCscFeature.getString("CountryISO");
			salesCode = mCscFeature.getString("SalesCode");

			String[] classes = MainApplication.getAppContext().getResources().getStringArray(R.array.categories_list);

			fieldFeatureList = CscFeature.class.getDeclaredField("mFeatureList");
			fieldFeatureList.setAccessible(true);

			for (String classFeatureName : classes) {
				try {
					Class<?> cscClass = Class.forName("com.sec.android.app.CscFeatureTag" + classFeatureName);
					Object someclass = cscClass.newInstance();

					Field[] fields = cscClass.getDeclaredFields();

					for (Field field : fields) {
						try {
							field.setAccessible(true);
							if (field.getType().equals(String.class)) {
								String fieldValue = (String) field.get(someclass);
								String value = mCscFeature.getString(fieldValue);
								if (fieldValue != null && fieldValue.startsWith("CscFeature_") && !value.isEmpty()) {
									cscDTOList.add(new FeatureDTO(fieldValue, value));
								}
							}

						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				} catch (ClassNotFoundException e) {
					// ignore it
				} catch (Throwable e) {
					e.printStackTrace();
				}

			}

			defaultFeatureDTOs.addAll(cscDTOList);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	public static void getDefaultCSCFeaturesFromFiles() {

		String files[] = { Constants.SYSTEM_CSC_FEATURE_XML, Constants.SYSTEM_CSC_OTHER_XML,
				Constants.SYSTEM_OTHER_FEATURE_BKP, Constants.SYSTEM_CSC_FEATURE_BKP };

		XmlPullParserFactory factory = null;
		XmlPullParser p = null;
		DefaultCSCCollector tc = new DefaultCSCCollector();
		InputStream is = null;

		try {

			for (String cscFile : files) {
				if (new File(cscFile).isFile()) {
					try {
						factory = XmlPullParserFactory.newInstance();
						p = factory.newPullParser();
						is = new FileInputStream(cscFile);
						p.setInput(is, "UTF-8");

						skipToTag(p, XmlPullParser.START_TAG);
						processTag(p, p.getName(), tc);

					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}

			defaultFeatureDTOs.addAll(tc.defaultFeatures());
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (Throwable e2) {
					e2.printStackTrace();
				}

		}

	}

	private static void processTag(XmlPullParser p, String tag, TagProcessor tp) throws IOException,
			XmlPullParserException {
		tp.processTag(p, tag);

		// this check is necessary since getNextText() pushes the current event
		// to END_TAG!
		int nextEvent = (p.getEventType() == XmlPullParser.END_TAG) ? p.getEventType() : p.next();
		while (nextEvent != XmlPullParser.END_TAG) {
			if (nextEvent == XmlPullParser.START_TAG) {
				processTag(p, p.getName(), tp); // recursive call
			}
			nextEvent = p.next();
		}
		p.require(XmlPullParser.END_TAG, null, tag);
	}

	private static int skipToTag(XmlPullParser p, int tag) throws IOException, XmlPullParserException {
		int nextEvent = p.next();
		while (nextEvent != tag && nextEvent != XmlPullParser.END_DOCUMENT) {
			nextEvent = p.next();
		}
		return nextEvent;
	}
}

interface TagProcessor {
	public void processTag(XmlPullParser p, String tag) throws IOException, XmlPullParserException;
}

class DefaultCSCCollector implements TagProcessor {
	private ArrayList<FeatureDTO> defaultFeatures;

	public DefaultCSCCollector() {
		defaultFeatures = new ArrayList<FeatureDTO>();
	}

	public void processTag(XmlPullParser p, String tag) throws IOException, XmlPullParserException {
		if (tag.startsWith("CscFeature")) {
			defaultFeatures.add(new FeatureDTO(tag, p.nextText()));
		}
	}

	public ArrayList<FeatureDTO> defaultFeatures() {
		return defaultFeatures;
	}
}
