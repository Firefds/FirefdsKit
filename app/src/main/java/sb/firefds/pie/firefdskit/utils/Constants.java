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
package sb.firefds.pie.firefdskit.utils;

import sb.firefds.pie.firefdskit.BuildConfig;
import sb.firefds.pie.firefdskit.utils.Utils.CscType;

public class Constants {

    public static final String BACKUP_DIR = ".FirefdsKit";
    public static final String PREFS = BuildConfig.APPLICATION_ID + "_preferences";

    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";

    public static final String FEATURE_XML = Utils.getCSCType().equals(CscType.CSC) ?
            "feature.xml" : "cscfeature.xml";
    public static final String REBOOT_DEVICE = "reboot";

    public static final String SYSTEM_CSC_FEATURE_XML = Utils.getCSCType().equals(CscType.OMC_OMC) ?
            Utils.getOMCPath() + "/" + FEATURE_XML : "/system/csc/" + FEATURE_XML;
    public static final String SYSTEM_CSC_FEATURE_BKP = Utils.getCSCType().equals(CscType.OMC_OMC) ?
            Utils.getOMCPath() + "/" + FEATURE_XML + ".bak" : "/system/csc/" + FEATURE_XML + ".bak";

    public static final String SYSTEM_CSC_OTHER_XML = "/system/csc/others.xml";
    public static final String SYSTEM_OTHER_FEATURE_BKP = "/system/csc/others.xml.bak";

    public static final String FEATURES_LIST_HEADER1 =
            "<?xml  version=\"1.0\" encoding=\"UTF-8\" ?>\n" + "<SamsungMobileFeature>\n" + "\t<Version>";
    public static final String FEATURES_LIST_HEADER2 = "</Version>\n" + "\t<Country>";
    public static final String FEATURES_LIST_HEADER3 = "</Country>\n" + "\t<CountryISO>";
    public static final String FEATURES_LIST_HEADER4 = "</CountryISO>\n" + "\t<SalesCode>";
    public static final String FEATURES_LIST_HEADER5 = "</SalesCode>\n" + "\t<FeatureSet>\n";
    public static final String FEATURES_LIST_FOOTER = "\t</FeatureSet>\n" + "</SamsungMobileFeature>";

    public static final String FORCE_CONNECT_MMS = "CscFeature_RIL_ForceConnectMMS";
    public static final String DISABLE_SMS_TO_MMS_CONVERSION_BY_TEXT_INPUT =
            "CscFeature_Message_DisableSmsToMmsConversionByTextInput";
    public static final String SMS_MAX_BYTE = "CscFeature_Message_SmsMaxByte";
    public static final String MAX_RECIPIENT_LENGTH_AS = "CscFeature_Message_MaxRecipientLengthAs";
    public static final String DISABLE_PHONE_NUMBER_FORMATTING = "CscFeature_Common_DisablePhoneNumberFormatting";

    public static final String SHORTCUT_STATUSBAR = "STATUSBAR";
    public static final String SHORTCUT_SYSTEM = "SYSTEM";
    public static final String SHORTCUT_PHONE = "PHONE";
    public static final String SHORTCUT_SECURITY = "SECURITY";

}
