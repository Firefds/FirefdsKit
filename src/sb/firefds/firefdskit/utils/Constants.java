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
package sb.firefds.firefdskit.utils;

public class Constants {

	public static final String BACKUP_DIR = ".XTouchWiz";
	public static final String PREFS = Packages.XTOUCHWIZ + "_preferences";

	public static final String FEATURE_XML = "feature.xml";
	public static final String REBOOT_DEVICE = "reboot";

	public static final String SYSTEM_CSC_FEATURE_XML = "/system/csc/feature.xml";
	public static final String SYSTEM_CSC_OTHER_XML = "/system/csc/others.xml";
	public static final String SYSTEM_CSC_FEATURE_BKP = "/system/csc/feature.xml.bak";
	public static final String SYSTEM_OTHER_FEATURE_BKP = "/system/csc/others.xml.bak";
	public static final String SYSTEM_CSC_FEATURE_DIR = "/system/csc/";
	public static final String FEATURES_LIST_HEADER1 = "<?xml  version=\"1.0\" encoding=\"UTF-8\" ?>\n"
			+ "<!-- It can be added for each operators like below form\n" +

			"<ABC>                       -> sales code\n" + "<sapient>true</sapient>         -> feature1\n"
			+ "<burton>false</burton>          -> feature2\n" + "<libtech>true</libtech>         -> feature3\n" + ".\n"
			+ ".\n" + "</ABC>\n\n" +

			"each name of tags are will be environment variable with \"ro.csc.\"\n"
			+ "ex) \"ro.csc.sapient\", \"ro.csc.burton\", \"ro.csc.libtech\" <== you could use them at any section.\n"
			+ "also, should be added between <FeatureSet>\n" + " and </FeatureSet> .\n" + "jhwan.kim@hdlnc.com\n"
			+ "by Wanam  -->\n" + "<SamsungMobileFeature>\n\n" +

			"<Version>";
	public static final String FEATURES_LIST_HEADER2 = "</Version>\n" + "<Country>";
	public static final String FEATURES_LIST_HEADER3 = "</Country>\n" + "<CountryISO>";
	public static final String FEATURES_LIST_HEADER4 = "</CountryISO>\n" + "<SalesCode>";
	public static final String FEATURES_LIST_HEADER5 = "</SalesCode>\n\n" +

			"<!-- KOR -->\n" + "<FeatureSet>\n";
	public static final String FEATURES_LIST_FOOTER = "</FeatureSet>\n" + "</SamsungMobileFeature>";

}
