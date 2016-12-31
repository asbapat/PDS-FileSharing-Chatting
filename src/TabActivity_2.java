package com.example.aniket.pds.demo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aniket.pds.R;
import com.example.aniket.pds.core.Query;
import com.example.aniket.pds.platform.Descriptor;
import com.example.aniket.pds.platform.PecService;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Object;

public class TabActivity_2 extends Activity {

    private static final int APP_ID = 28396;
    private ServiceConnection serviceConnection;
    private Messenger serviceMessenger;
    private Messenger rMessenger;
    private StringBuilder data = new StringBuilder();
    //private String ext;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_2);

        final TextView textView = (TextView) findViewById(R.id.txtFile);
        final TextView name = (TextView) findViewById(R.id.txt);
        final EditText editText = (EditText) findViewById(R.id.edit1);
        final Button add_metadata = (Button) findViewById(R.id.btn_send);
        Button request_metadata = (Button) findViewById(R.id.btn_request);
        Button button = (Button) findViewById(R.id.btnreg);
        Button display = (Button) findViewById(R.id.display);
        Button request_data = (Button) findViewById(R.id.add_data);
        Button clear = (Button) findViewById(R.id.clear);
        final Button send = (Button) findViewById(R.id.sendfile);

        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextColor(Color.BLACK);
        name.setMovementMethod(new ScrollingMovementMethod());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TabActivity_2.this, PecService.class);
                ComponentName ch = startService(intent);
                if (ch == null) {
                    Toast.makeText(TabActivity_2.this, "Failed to start Service", Toast.LENGTH_LONG).show();
                    return;
                }
                boolean ret = bindService(intent, serviceConnection, Context.BIND_IMPORTANT);
                if (ret) {

                    Toast.makeText(TabActivity_2.this, "Bound to Service", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TabActivity_2.this, "Failed to Bind", Toast.LENGTH_SHORT).show();
                }
            }
        });

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(TabActivity_2.this, "Service Connected", Toast.LENGTH_SHORT).show();

                serviceMessenger = new Messenger(service);

                // Register as soon as bound to PecService. This gives PecService the messenger to
                // talk to the application.
                Message msg = Message.obtain(null, PecService.APP_MSG_REGISTER_APP, APP_ID, 0, rMessenger);
                try {
                    Toast.makeText(TabActivity_2.this, "Register to PecService", Toast.LENGTH_SHORT).show();
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();

                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(TabActivity_2.this, "Service Disconnected", Toast.LENGTH_SHORT).show();
                serviceMessenger = null;

            }
        };

        rMessenger = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle;
                switch (msg.what) {

                    // PecService provide data to application.
                    // This is probably because the the application requested the data earlier.
                    case PecService.SRV_MSG_PROVIDE_DATA:

                        String ext = "";
                        bundle = msg.getData();
                        bundle.setClassLoader(Descriptor.class.getClassLoader());
                        HashMap<Descriptor, byte[]> map = (HashMap<Descriptor, byte[]>) bundle.getSerializable("data");
                        for (Descriptor descriptor : map.keySet()) {
                            byte[] data = map.get(descriptor);
                            byte[] data1 = map.get(descriptor);
                            textView.append("Receive data from PecService: Descriptor=" + descriptor.toString() + "\n");

                            File filep = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PDS");
                            if (!filep.exists()) {
                                filep.mkdirs();
                            }
                            if (ext.equals(".txt")) {
                                File filepath = new File(filep + "/TestFile.txt");
                                try {
                                    if (!filepath.exists()) {
                                        filepath.createNewFile();
                                        System.out.println("New file created");
                                    }
                                    FileOutputStream out = new FileOutputStream(filepath);
                                    FileWriter fw = new FileWriter(filepath.getAbsoluteFile());
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    bw.write(new String(data));
                                    bw.close();
                                    System.out.println("Written successfully");
                                } catch (IOException ioe) {
                                    System.out.println("IOException : " + ioe);
                                } catch (NullPointerException e) {
                                    System.out.println("Error");
                                }
                            } else {


                                File filepath = new File(filep + "/TestImage.jpeg");
                                try {
                                    if (!filepath.exists()) {
                                        filepath.createNewFile();
                                        System.out.println("New file created");
                                    }
                                    FileOutputStream out1 = new FileOutputStream(filepath);
                                    out1.write(data1);
                                    out1.close();


                                } catch (IOException ioe) {
                                    System.out.println("IOException : " + ioe);
                                } catch (NullPointerException e) {
                                    System.out.println("Error");
                                }
                            }
                        }

                        break;

                    // PecService requests data from the application.
                    // In the following example, the application replies all the data requested by
                    // PecService.
                    case PecService.SRV_MSG_REQUEST_DATA:

                        // Get the descriptors of data requested by PecService.

                        bundle = msg.getData();
                        bundle.setClassLoader(Descriptor.class.getClassLoader());
                        ArrayList<Descriptor> descriptors = (ArrayList<Descriptor>) bundle.getSerializable("descriptors");
                        for (Descriptor descriptor : descriptors) {
                            textView.append("PecService requesting data: Descriptor=" + descriptor.toString() + "\n");
                        }

                        // Provide data to PecService.

                        if (serviceMessenger == null) {
                            textView.append("Service is not connected.\n");
                            return;
                        }

                        HashMap<Descriptor, byte[]> data = new HashMap<>();
                        String testdata = "TESTDATA_";

                        for (Descriptor descriptor : descriptors) {
                            data.put(descriptor, (testdata + descriptor.getDataType()).getBytes());
                        }

                        Bundle replyBundle = new Bundle();
                        replyBundle.putSerializable("data", data);

                        Message replyMsg = Message.obtain(null, PecService.APP_MSG_PROVIDE_DATA, APP_ID, 0);
                        replyMsg.setData(replyBundle);
                        try {
                            serviceMessenger.send(replyMsg);
                            textView.append("Provide requested data to PecService.\n");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            textView.append("Failed to provide requested data to PecService.\n");
                        }

                        break;

                    // PecService provide metadata to application.
                    case PecService.SRV_MSG_PROVIDE_METADATA:

                        bundle = msg.getData();
                        bundle.setClassLoader(Descriptor.class.getClassLoader());
                        ArrayList<Descriptor> metadata = (ArrayList<Descriptor>) bundle.getSerializable("metadata");
                        for (Descriptor descriptor : metadata) {
                            textView.append("Receive metadata from PecService: Descriptor=" + descriptor.toString() + "\n");
                            String s1 = descriptor.getAttributes().toString();
                        }

                        break;
                }
            }
        });

        // Click this button to add test metadata to PecService.
        add_metadata.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serviceMessenger == null) {
                    textView.append("Service is not connected.\n");
                    return;
                }

                // String data = editText.getText().toString();

                ArrayList<Descriptor> metadata = new ArrayList<>();
                HashMap<String, String> testdata = new HashMap<String, String>();
                for (int i = 0; i < 1; ++i) {
                    String s1 = name.getText().toString();
                    testdata.put("FileName:", s1);
                    Descriptor descriptor = new Descriptor(i, 1, 1, testdata);
                    metadata.add(descriptor);
                    System.out.println(descriptor.toString());
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("metadata", metadata);

                Message msg = Message.obtain(null, PecService.APP_MSG_PROVIDE_METADATA, APP_ID, 0);
                msg.setData(bundle);
                try {
                    serviceMessenger.send(msg);
                    textView.append("Test metadata added to PecService.\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    textView.append("Failed to add test metadata to PecService.\n");
                }


            }
        });

        // Click this button to request metadata from PecService
        request_metadata.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceMessenger == null) {
                    textView.append("Service is not connected.\n");
                    return;
                }

                Message msg = Message.obtain(null, PecService.APP_MSG_REQUEST_METADATA, APP_ID, 0);
                try {
                    serviceMessenger.send(msg);
                    textView.append("Requesting metadata from PecService.\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    textView.append("Failed to request metadata to PecService.\n");
                }
            }
        });

        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TabActivity_2.this, FileSelection.class);
                startActivityForResult(intent, 1);

            }
        });

        request_data.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceMessenger == null) {
                    textView.append("Service is not connected.\n");
                    return;
                }

                ArrayList<Descriptor> descriptors = new ArrayList<>();
                for (int i = 0; i < 3; ++i) {
                    Descriptor descriptor = new Descriptor(i, 1, 1);
                    descriptors.add(descriptor);
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("descriptors", descriptors);

                Message msg = Message.obtain(null, PecService.APP_MSG_REQUEST_DATA, APP_ID, 0);
                msg.setData(bundle);
                try {
                    serviceMessenger.send(msg);
                    textView.append("Requesting data from PecService.\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    textView.append("Failed to request data to PecService.\n");
                }

            }
        });

        send.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Project/" + name.getText().toString());
                StringBuilder data = new StringBuilder();
                int size = (int) file.length();
                byte[] bytes = new byte[size];
                String ext;
                ext = file.toString().substring(file.toString().lastIndexOf("."));

                if (ext.equals(".txt")) {
                    try {
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, bytes.length);
                        buf.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                    Bitmap bmp = BitmapFactory.decodeFile(file.toString());

                    bmp.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                    bytes = stream.toByteArray();

                }

                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        data.append(line);
                        data.append("\n");
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (serviceMessenger == null) {
                    textView.append("Service is not connected.\n");
                    return;
                }

                HashMap<Descriptor, byte[]> map = new HashMap<>();
                for (int i = 0; i < 3; ++i) {
                    Descriptor descriptor = new Descriptor(i, 1, 1);
                    map.put(descriptor, bytes);
                    System.out.println(bytes);
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("data", map);

                Message msg = Message.obtain(null, PecService.APP_MSG_PROVIDE_DATA, APP_ID, 0);
                msg.setData(bundle);
                try {
                    serviceMessenger.send(msg);
                    textView.append("File sent successfully" + "\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    textView.append("Failed to add test data to PecService.\n");
                }

            }
        });


        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                name.setText("");
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        //super.onActivityResult(requestCode, resultCode, data);
        File fileName;

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri returnUri = returnIntent.getData();
                File f = new File("" + returnUri);
                String data = f.getName();
                TextView nameView = (TextView) findViewById(R.id.txt);
                nameView.setText(data);

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                TextView nameView = (TextView) findViewById(R.id.txt);
                nameView.setText("No file found");
            }
        }

    }
}
