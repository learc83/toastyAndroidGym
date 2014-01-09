package com.example.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BedSelection extends Activity implements ActionBar.TabListener {
    // global so they are easily available to fragments
    public static int customerID;
    public static int customerLevel;
    public static String customerName;
    public static BedsAdapter adapter;

    public int getBedsByLevel(int lvl) {
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("Level", Integer.toString(lvl)));

        // post to customer_login
        ToastyHttpResponse response = ToastyHTTPHandler.Post("bed_status", params);

        if (response.Error != 0) {
            return response.Error;
        }

        try {
            // Parse JSON
            JSONObject jObject = new JSONObject(response.Body);

            // Check for error response
            if (!jObject.isNull("error_code")) {
                /* generic error code for error response from bed_status--shouldn't happen unless
                something really went wrong */
                return 11;
            }

            JSONArray array = jObject.getJSONArray("beds");

            // Convert JSON array to array of Bed objects
            ArrayList<Bed> beds = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);

                String name = row.getString("Name");
                String num = row.getString("Bed_num");
                int maxTime = row.getInt("Max_time");

                beds.add(new Bed(name, num, maxTime));

                /* Toast for dev purposes */
                //Context context = getApplicationContext();
                //Toast.makeText(context, bed.Name + " " + bed.Number, Toast.LENGTH_SHORT).show();
                /* end toast */
            }

            adapter.clear();
            adapter.addAll(beds);

        } catch (JSONException e) {
            e.printStackTrace();
            /* TODO This is a hack to clear the beds even if no beds are returned
               Fix this by checking for null beds and handling it there */
            adapter.clear();
            return 10;
        }

        return 0; // No Error
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bed_selection);

        Intent intent = getIntent();
        customerID = intent.getIntExtra(MainActivity.CUSTOMER_ID, 0);
        customerLevel=  5;// TODO remove comments intent.getIntExtra(MainActivity.CUSTOMER_LEVEL, 0);
        customerName =  intent.getStringExtra(MainActivity.CUSTOMER_NAME);


        /* Create new adapter to handle populating beds, and attach to ListView
           This has to come before setting up the tabs, so that an adapter exists when a tab is selected */
        adapter = new BedsAdapter(this);
        ListView listView = (ListView) findViewById(R.id.bedList);
        listView.setAdapter(adapter);

        ActionBar bar = getActionBar();
        // Set up tabs for each level the customer has access to except highest one
        for (int i=1; i<customerLevel; i++) {
            bar.addTab(bar.newTab().setText("Level " + i).setTabListener(this), i-1, false);
        }

        // Set up tab for the highest level customer has access to and select it by default
        bar.addTab(bar.newTab().setText("Level " + customerLevel).setTabListener(this), customerLevel-1, true);

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        /* Toast for dev purposes */
        Context context = getApplicationContext();
        Toast.makeText(context, customerName, Toast.LENGTH_LONG).show();
        /* end toast */
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        int t = tab.getPosition();

        getBedsByLevel(t + 1);
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // Do Nothing
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // Do Nothing
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.bed_selection, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}

class Bed {
    public String Name;
    public String Number;
    public int MaxTime;

    public Bed(String name, String number, int maxTime) {
        this.Name = name;
        this.Number = number;
        this.MaxTime = maxTime;
    }
}
