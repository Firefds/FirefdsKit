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

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class XSystemProp {

	private XSystemProp() {

	}

	// Get the value for the given key
	// @param key: key to lookup
	// @return null if the key isn't found
	public static String get(String key) {
		String ret;

		try {
			Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
			ret = (String) callStaticMethod(classSystemProperties, "get", key);
		} catch (Throwable t) {
			t.printStackTrace();
			ret = null;
		}
		return ret;
	}

	// Get the value for the given key
	// @param key: key to lookup
	// @param def: default value to return
	// @return if the key isn't found, return def if it isn't null, or an
	// empty string otherwise
	public static String get(String key, String def) {
		String ret = def;

		try {
			Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
			ret = (String) callStaticMethod(classSystemProperties, "get", key, def);
		} catch (Throwable t) {
			t.printStackTrace();
			ret = def;
		}
		return ret;
	}

	// Get the value for the given key, and return as an integer
	// @param key: key to lookup
	// @param def: default value to return
	// @return the key parsed as an integer, or def if the key isn't found
	// or cannot be parsed
	public static Integer getInt(String key, Integer def) {
		Integer ret = def;

		try {
			Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
			ret = (Integer) callStaticMethod(classSystemProperties, "getInt", key, def);
		} catch (Throwable t) {
			t.printStackTrace();
			ret = def;
		}
		return ret;
	}

	// Get the value for the given key, and return as a long
	// @param key: key to lookup
	// @param def: default value to return
	// @return the key parsed as a long, or def if the key isn't found or
	// cannot be parsed
	public static Long getLong(String key, Long def) {
		Long ret = def;

		try {
			Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
			ret = (Long) callStaticMethod(classSystemProperties, "getLong", key, def);
		} catch (Throwable t) {
			t.printStackTrace();
			ret = def;
		}
		return ret;
	}

	// Get the value (case insensitive) for the given key, returned as a
	// boolean
	// Values 'n', 'no', '0', 'false' or 'off' are considered false
	// Values 'y', 'yes', '1', 'true' or 'on' are considered true
	// If the key does not exist, or has any other value, then the default
	// result is returned
	// @param key: key to lookup
	// @param def: default value to return
	// @return the key parsed as a boolean, or def if the key isn't found or
	// cannot be parsed
	public static Boolean getBoolean(String key, boolean def) {
		Boolean ret = def;

		try {
			Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
			ret = (Boolean) callStaticMethod(classSystemProperties, "getBoolean", key, def);
		} catch (Throwable t) {
			t.printStackTrace();
			ret = def;
		}
		return ret;
	}

	// Set the value for the given key
	public static void set(String key, String val) {
		try {
			Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
			callStaticMethod(classSystemProperties, "set", key, val);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}