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

import static it.gmariotti.android.apps.dashclock.extensions.battery.LogUtils.LOGD;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.os.AsyncTask;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;
import  	java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BatteryExtension extends DashClockExtension {

    public static final String REFRESH_INTENT_FILTER = "it.gmariotti.android.apps.dashclock.extensions.battery.RefreshChanel";
    public static Intent REFRESH_INTENT = new Intent(REFRESH_INTENT_FILTER);

	private static final String TAG = "BatteryExtension";

	public static final String PREF_BATTERY = "pref_Battery";
	public static final String PREF_BATTERY_CHARGE = "pref_battery_charge";
	public static final String PREF_BATTERY_VOLTAGE = "pref_battery_voltage";
	public static final String PREF_BATTERY_TEMP = "pref_battery_temp";
	public static final String PREF_BATTERY_HEALTH = "pref_battery_health";
	public static final String PREF_BATTERY_REALTIME = "pref_battery_realtime";

	
	// Prefs
	protected boolean prefCharge = true;
	protected boolean prefTemp = true;
	protected boolean prefVoltage = true;
	protected boolean prefHealth = true;
	protected boolean prefRealtime = true;

	// Value
	private int level;
	private String charging;
	private String charge;
	private int voltage;
	private int temperature;
	private String umTemp;
	private String umVoltage = "";
	private String health;

    private OnClickReceiver onClickReceiver;

	@Override
	protected void onInitialize(boolean isReconnect) {
		super.onInitialize(isReconnect); 
		if (!isReconnect) {
			readPreferences();

            if (onClickReceiver != null) {
                try {
                    unregisterReceiver(onClickReceiver);
                } catch (Exception e) {
                }
            }

            IntentFilter intentFilter = new IntentFilter(REFRESH_INTENT_FILTER);
            onClickReceiver = new OnClickReceiver();
            registerReceiver(onClickReceiver, intentFilter);


            IntentFilter filterScreen=new IntentFilter();
            filterScreen.addAction(Intent.ACTION_SCREEN_ON);

            getApplicationContext().registerReceiver(mScreenOnReceiver,
                    filterScreen);
            //scheduleRefresh(0);
		}
	}



    private class DownloadChanelData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            String response = "";
            String temperature = "";
            String updateDateTime = "";
            String channelName = "";
            Date date = null;
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {

                    HttpResponse execute = client.execute(httpGet);
                    HttpEntity entity = execute.getEntity();

                    if(entity != null){
                        response = EntityUtils.toString(entity);
                    }
                    else{
                    }

                } catch (Exception e) {
                    //response = "ddd" + e.getMessage();
                    e.printStackTrace();
                }
            }


            if(response.length() > 0){
                try {
                    JSONObject jObj = new JSONObject(response);

                    JSONObject subObj_chanel = jObj.getJSONObject("channel");

                    channelName = subObj_chanel.getString("field1");

                    JSONArray jArr = jObj.getJSONArray("feeds");

                    JSONObject subObj_feeds = jArr.getJSONObject(jArr.length()-1);

                    temperature = subObj_feeds.getString("field1");
                    updateDateTime = subObj_feeds.getString("created_at");

                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ");
                    date = dateFormat1.parse(updateDateTime);

                } catch (Exception e) {
                    temperature = "ERR";
                }
            }


            SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            updateDateTime = dateFormat2.format(date);

            // Publish the extension data update.
            publishUpdate(new ExtensionData()
                    .visible(true)
                    .icon(R.drawable.ic_thermometer)
                    .status(temperature+"°")
                    .expandedTitle(temperature+"°C")
                    .expandedBody(channelName + "\n" + updateDateTime + "\nResonse: " + response)
                    .clickIntent(REFRESH_INTENT));

            return response;
        }
    }

    public void readWebpage()
    {
        DownloadChanelData task = new DownloadChanelData();
        //task.execute(new String[] { "https://thingspeak.com/channels/27592/feeds/last.json?timezone=Europe/Warsaw" });
        task.execute(new String[] { "https://api.thingspeak.com/channels/27592/feeds.json?results=1&timezone=Europe/Warsaw" });

    }

	@Override
	protected void onUpdateData(int reason) {
		LOGD(TAG, "onUpdate "+reason);
		// Read Preferences
		//readPreferences();

        readWebpage();
    }
	



	/**
	 * Read preference
	 */
	private void readPreferences() {
		// Get preference value.
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefVoltage = sp.getBoolean(PREF_BATTERY_VOLTAGE, true);
		prefCharge = sp.getBoolean(PREF_BATTERY_CHARGE, true);
		prefTemp = sp.getBoolean(PREF_BATTERY_TEMP, true);
		prefHealth = sp.getBoolean(PREF_BATTERY_HEALTH, true);
		prefRealtime = sp.getBoolean(PREF_BATTERY_REALTIME, true);
	}


    private Intent prepareClickIntent() {
        Intent clickIntent = REFRESH_INTENT;

        return clickIntent;
    }

    private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
        }
    };

    class OnClickReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
        }
    }
}