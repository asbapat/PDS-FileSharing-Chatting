package com.example.aniket.pds.demo;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import com.example.aniket.pds.R;

public class MainActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        tabHost.setup();


        //creating tab menu
        TabHost.TabSpec Tab1 = tabHost.newTabSpec("Messenger");
        TabHost.TabSpec Tab2 = tabHost.newTabSpec("Files Sharing");



        //setting tab2 name
        Tab1.setIndicator("Messenger");
        //set activity

        Tab1.setContent(new Intent(this, TabActivity_1.class));


        //setting tab2 name
        Tab2.setIndicator("File Sharing");
        //set activity
        Tab2.setContent(new Intent(this, TabActivity_2.class));

        //adding tabs

        tabHost.addTab(Tab1);
        tabHost.addTab(Tab2);


    }



}