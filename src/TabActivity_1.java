package com.example.aniket.pds.demo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aniket.pds.R;
import com.example.aniket.pds.platform.Descriptor;
import com.example.aniket.pds.platform.PecService;

import java.util.ArrayList;
import java.util.HashMap;

public class TabActivity_1 extends Activity {

    private static final int APP_ID = 28396;
    private ServiceConnection serviceConnection;
    private Messenger serviceMessenger;
    private Messenger rMessenger;
    private Thread newThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_1);


        final TextView textView = (TextView) findViewById(R.id.text1);
        final EditText editText = (EditText) findViewById(R.id.edit1);
        final Button btn_send = (Button) findViewById(R.id.btn1);
        Button btn_request = (Button) findViewById(R.id.btn3);
        Button button = (Button) findViewById(R.id.btn2);
        Button clear = (Button) findViewById(R.id.clear);

        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextColor(Color.BLACK);
        new MyClass().execute();


        // Click this button to bind and register the application to PecService.
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TabActivity_1.this, PecService.class);

                // Make sure PecService is started before binding. Otherwise, the service is going
                // to die when the caller application is dead.
                ComponentName cn = startService(intent);
                if (cn == null) {
                    Toast.makeText(TabActivity_1.this, "Failed to start Service", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean ret = bindService(intent, serviceConnection, Context.BIND_IMPORTANT);
                if (ret) {
                    Toast.makeText(TabActivity_1.this, "Bound to Service", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TabActivity_1.this, "Failed to Bind", Toast.LENGTH_SHORT).show();
                }
            }
        });

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(TabActivity_1.this, "Service Connected", Toast.LENGTH_SHORT).show();
                serviceMessenger = new Messenger(service);
                new MyClass().execute();


                // Register as soon as bound to PecService. This gives PecService the messenger to
                // talk to the application.
                Message msg = Message.obtain(null, PecService.APP_MSG_REGISTER_APP, APP_ID, 0, rMessenger);
                try {
                    Toast.makeText(TabActivity_1.this, "Register to PecService", Toast.LENGTH_SHORT).show();
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(TabActivity_1.this, "Service Disconnected", Toast.LENGTH_SHORT).show();
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

                        bundle = msg.getData();
                        bundle.setClassLoader(Descriptor.class.getClassLoader());
                        HashMap<Descriptor, byte[]> map = (HashMap<Descriptor, byte[]>) bundle.getSerializable("data");
                        for (Descriptor descriptor : map.keySet()) {
                            byte[] data = map.get(descriptor);
                            textView.append("\nReceive data from PecService: Descriptor=" + descriptor.toString() + " Data=" + new String(data) + "\n");
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
                        }

                        break;
                }
            }
        });


        // Click this button to add test data to PecService.
        btn_send.setOnClickListener(new Button.OnClickListener() {
            @Override
            /*public void onClick(View v) {
                @Override*/
            public void onClick(View v) {
                if (serviceMessenger == null) {
                    textView.append("Service is not connected.\n");
                    return;
                }
                new MyClass().execute();

                HashMap<Descriptor, byte[]> map = new HashMap<>();
                String testdata = editText.getText().toString();
                for (int i = 0; i < 3; ++i) {
                    Descriptor descriptor = new Descriptor(i, 1, 1);
                    map.put(descriptor, (testdata).getBytes());
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("data", map);

                Message msg = Message.obtain(null, PecService.APP_MSG_PROVIDE_DATA, APP_ID, 0);
                msg.setData(bundle);
                try {
                    serviceMessenger.send(msg);
                    textView.append(testdata + "\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    textView.append("Failed to add test data to PecService.\n");
                }

            }
        });

        // Click this button to request test data from PecService.
        // If the requested data have been added to PecService on either this device or another
        // device connected in the same network, PecService should be able to return them.
        // If only metadata of the requested data have been added to PecService on either this
        // device or another connected device, PecService should ask the application that added
        // corresponding metadata to provide data, and return that data when provided.
        btn_request.setOnClickListener(new Button.OnClickListener() {
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


        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
            }
        });
    }

    public class MyClass extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            if (serviceMessenger == null) {
                return null;
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
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}