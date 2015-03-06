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
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.os.AsyncTask;
import android.widget.EditText;

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
import android.view.View;

public class BatteryExtension extends DashClockExtension {

    public static final String REFRESH_INTENT_FILTER = "it.gmariotti.android.apps.dashclock.extensions.battery.RefreshChanel";
    public static Intent REFRESH_INTENT = new Intent(REFRESH_INTENT_FILTER);

	private static final String TAG = "BatteryExtension";

	public static final String PREF_CHANNEL_ID = "pref_chanel_id";
    public static final String PREF_FIELD_ID = "pref_field_id";
    public static final String PREF_FIELD_NAME = "pref_field_name";
    public static final String PREF_FIELD_DATE = "pref_field_date";
    public static final String PREF_FIELD_TIME = "pref_field_time";


	
	// Prefs
    protected String  prefChannelID = "";
    protected String  prefFieldID = "";
	protected boolean prefFieldName = true;
    protected boolean prefFieldDate = true;
    protected boolean prefFieldTime = true;


    private OnClickReceiver onClickReceiver;

	@Override
	protected void onInitialize(boolean isReconnect) {
		super.onInitialize(isReconnect);

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



    private class DownloadChanelData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            String response = "";
            String updateDateTime = "";

            String fieldValue = null;
            String updateDate = null;
            String updateTime = null;
            String fieldName = null;

            Date dateUpdateDateTime = null;


            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {

                    HttpResponse execute = client.execute(httpGet);
                    HttpEntity entity = execute.getEntity();

                    if (entity != null) {
                        response = EntityUtils.toString(entity);
                    } else {
                    }

                } catch (Exception e) {
                    //response = "ddd" + e.getMessage();
                    e.printStackTrace();
                }
            }


            if (response.length() > 0) {
                try {
                    JSONObject jObj = new JSONObject(response);

                    JSONObject subObj_chanel = jObj.getJSONObject("channel");

                    fieldName = subObj_chanel.getString("field1");

                    JSONArray jArr = jObj.getJSONArray("feeds");

                    JSONObject subObj_feeds = jArr.getJSONObject(jArr.length() - 1);

                    fieldValue = subObj_feeds.getString("field1");


                    updateDateTime = subObj_feeds.getString("created_at");

                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ");
                    dateUpdateDateTime = dateFormat1.parse(updateDateTime);

                } catch (Exception e) {
                    fieldValue = "ERR";
                }



                SimpleDateFormat dateFormatterDate = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat dateFormatterTime = new SimpleDateFormat("HH:mm:ss");

                updateDate = dateFormatterDate.format(dateUpdateDateTime);
                updateTime = dateFormatterTime.format(dateUpdateDateTime);


                String and = "";
                StringBuffer sb = new StringBuffer();
                if (prefFieldName && fieldName != null) {
                    sb.append(fieldName);
                    and = "\n";
                }
                if (prefFieldDate && updateDate != null) {
                    sb.append(and);
                    sb.append(updateDate);
                    and = "\n";

                    if (prefFieldTime)
                        and = " ";
                }
                if (prefFieldTime && updateTime != null) {
                    sb.append(and);
                    sb.append(updateTime);
                    and = "\n";
                }

                // Publish the extension data update.
                publishUpdate(new ExtensionData()
                        .visible(true)
                        .icon(R.drawable.thermometer_icon_2)
                        .status(fieldValue + "°")
                        .expandedTitle(fieldValue + "°C")
                        .expandedBody(sb.toString())
                        .clickIntent(REFRESH_INTENT));

            }

            return response;
        }
    }

    public void readWebpage()
    {
        DownloadChanelData task = new DownloadChanelData();
        //task.execute(new String[] { "https://thingspeak.com/channels/27592/feeds/last.json?timezone=Europe/Warsaw" });
        task.execute(new String[]{"https://api.thingspeak.com/channels/27592/feeds.json?results=1&timezone=Europe/Warsaw"});

    }

	@Override
	protected void onUpdateData(int reason) {

		readPreferences();

        readWebpage();
    }
	



	/**
	 * Read preference
	 */
	private void readPreferences() {
		// Get preference value.
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        //prefChannelID = sp.getString(PREF_CHANNEL_ID, prefChannelID);
        prefFieldID = sp.getString(PREF_FIELD_ID, prefFieldID);

		prefFieldName = sp.getBoolean(PREF_FIELD_NAME, true);
        prefFieldDate = sp.getBoolean(PREF_FIELD_DATE, true);
        prefFieldTime = sp.getBoolean(PREF_FIELD_TIME, true);



//        SharedPreferences.Editor editor = sp.edit();
//        editor.putString(PREF_LOGIN, "ala ma kota");
//        editor.commit();
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