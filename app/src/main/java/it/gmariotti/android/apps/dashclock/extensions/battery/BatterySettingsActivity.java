/*******************************************************************************
 * Copyright 2013 Gabriele Mariotti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package it.gmariotti.android.apps.dashclock.extensions.battery;

import android.os.Bundle;

public class BatterySettingsActivity extends BaseSettingsActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setIcon(R.drawable.ic_thermometer);
    }

    @Override
    protected void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.prefs_battery);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
//        bindPreferenceSummaryToValue(findPreference(BatteryExtension.PREF_CHANNEL_ID));
        bindPreferenceSummaryToValue(findPreference(BatteryExtension.PREF_FIELD_ID));
        bindPreferenceSummaryToValue(findPreference(BatteryExtension.PREF_FIELD_DATE));
        bindPreferenceSummaryToValue(findPreference(BatteryExtension.PREF_FIELD_NAME));
        bindPreferenceSummaryToValue(findPreference(BatteryExtension.PREF_FIELD_TIME));

    }
	
	

}
