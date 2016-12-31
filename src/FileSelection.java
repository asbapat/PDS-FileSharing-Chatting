package com.example.aniket.pds.demo;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.aniket.pds.R;

import java.io.File;
import java.security.spec.EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class FileSelection extends ListActivity {
    private File file;
    private ArrayList<String> myList;
    private static final int PERMS_REQUEST_CODE = 123;
    private Uri fileUri;
    private String[] mFileList = new String[100];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      setContentView(R.layout.activity_file_selection);
        final ListView list1 = getListView();
        Button done = (Button) findViewById(R.id.done);
        if (hasPermission()) {
            display();
        } else {
            requestPerms();
        }
        //setResult(Activity.RESULT_CANCELED, null);

        list1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Object itemClicked = list1.getAdapter().getItem(position);
                Toast.makeText(getApplicationContext(), "Item Clicked: " + itemClicked + " \nPosition: " + id,

                        Toast.LENGTH_SHORT).show();

                File requestFile = new File(itemClicked.toString());

                try {
                    Uri fileUri = Uri.fromFile(requestFile);
                    Intent mReturnIntent = new Intent();
                    if (fileUri != null) {
                        // Grant temporary read permission to the content URI

                        mReturnIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        mReturnIntent.setData(fileUri);
                        setResult(Activity.RESULT_OK, mReturnIntent);
                    } else {
                        setResult(Activity.RESULT_CANCELED, mReturnIntent);

                    }

                } catch (IllegalArgumentException e) {
                    Log.e("File Selector",
                            "The selected file can't be shared: " +
                                    itemClicked);
                }

            }

        });
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMS_REQUEST_CODE);
        }
    }

    private boolean hasPermission() {
        int res = 0;
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allowed = true;

        switch (requestCode) {

            case PERMS_REQUEST_CODE:

                for (int res : grantResults) {
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                allowed = false;
        }
        if (allowed) {
            display();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "Access denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void display() {
        myList = new ArrayList<String>();

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "Error! No SDCARD Found!", Toast.LENGTH_LONG)
                    .show();
            Log.i("LOg", "notnfonunf");
        } else {
            // Locate the image folder in your SD Card
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Project");
            if (!file.exists()) {
                file.mkdirs();
            }
            String s = file.toString();
            Log.i("Log:", "path" + s);


            File list[] = file.listFiles();

            for (int i = 0; i < list.length; i++) {
                myList.add(list[i].getName());
                mFileList[i] = list[i].getAbsolutePath().toString();
            }
            setListAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, myList));
        }
    }


}

