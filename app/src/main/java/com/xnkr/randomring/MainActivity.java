package com.xnkr.randomring;

import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String SEVERE = "SEVERE";

    Button nitrogen, resetButton, reloadButton;
    TextView currentlyPlaying;
    ListView sourceList, destList;
    List<String> sList = new ArrayList<>();
    List<String> dList = new ArrayList<>();
    TreeMap<String, Ringtone> sMap = new TreeMap<>();
    TreeMap<String, Ringtone> dMap = new TreeMap<>();
    TreeMap<String, Ringtone> allMap = new TreeMap<>();
    public static final String ringtonesPath = Environment.getExternalStorageDirectory().toString() + "/Ringtones";
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nitrogen = (Button) findViewById(R.id.nitrogen);
        currentlyPlaying = (TextView) findViewById(R.id.text);
        resetButton = (Button) findViewById(R.id.resetButton);
        reloadButton = (Button) findViewById(R.id.reloadButton);
        sourceList = (ListView) findViewById(R.id._source);
        destList = (ListView) findViewById(R.id._dest);

        final ArrayAdapter<String> sListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                sList
        );
        final ArrayAdapter<String> dListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                dList
        );


        sourceList.setAdapter(sListAdapter);
        destList.setAdapter(dListAdapter);
        currentlyPlaying.setText("Hello");
        nitrogen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentlyPlaying.setText(Integer.toString(i++));
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    i = 0;
                    currentlyPlaying.setText(Integer.toString(i++));

                    Log.i(INFO, "Resetting selections");
                    dMap.clear();
                    dList.clear();
                    sMap.clear();
                    sList.clear();
                    sMap = (TreeMap<String, Ringtone>) allMap.clone();
                    for (String sKey : sMap.keySet()) {
                        sList.add(sKey);
                    }
                    sListAdapter.notifyDataSetChanged();
                    dListAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(SEVERE, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("Are you sure you want to reload? This will reset all selections!");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                currentlyPlaying.setText("Hello World");
                                try {
                                    Toast.makeText(MainActivity.this, "Searching for *.mp3 in /Ringtones directory", Toast.LENGTH_LONG).show();
                                    if (reloadRingtones()) {
                                        sListAdapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Something went wrong. Search failed!", Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    Log.e(SEVERE, e.getMessage());
                                    e.printStackTrace();
                                }
                                dialog.cancel();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        currentlyPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, currentlyPlaying.getText(), Toast.LENGTH_LONG).show();
            }
        });

        sourceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Object o = sourceList.getItemAtPosition(position);
                    String chosenRingtone = (String) o;
                    currentlyPlaying.setText("Currently Playing: " + chosenRingtone);
                    dMap.put(
                            chosenRingtone,
                            sMap.get(chosenRingtone)
                    );
                    dList.add(chosenRingtone);

                    sMap.remove(chosenRingtone);
                    sList.remove(chosenRingtone);

                    sListAdapter.notifyDataSetChanged();
                    dListAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e(SEVERE, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        destList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Object o = destList.getItemAtPosition(position);
                    String unChosenRingtone = (String) o;
                    sMap.put(
                            unChosenRingtone,
                            dMap.get(unChosenRingtone)
                    );
                    sList.add(unChosenRingtone);

                    dMap.remove(unChosenRingtone);
                    dList.remove(unChosenRingtone);

                    sListAdapter.notifyDataSetChanged();
                    dListAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Ringtone unchosen" + unChosenRingtone, Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Log.e(SEVERE, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    private boolean reloadRingtones() {
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            Log.i(INFO, "Clearing lists");
            sMap.clear();
            dMap.clear();
            sList.clear();
            dList.clear();
            allMap.clear();
            File directory = new File(ringtonesPath);
            File[] files = directory.listFiles();
            Ringtone ringtone = null;
            mediaMetadataRetriever = new MediaMetadataRetriever();
            for (File file : files) {
                if (file.getName().endsWith(".mp3")) {
                    mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
                    String ringtoneName = file.getName();
                    String ringtonePath = file.getAbsolutePath();
                    long ringtoneDuration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    ringtone = new Ringtone(ringtoneName, ringtonePath, ringtoneDuration);
                    Log.i(INFO, "Found ringtone: " + ringtone.toString());
                    sMap.putIfAbsent(ringtoneName, ringtone);
                    allMap.putIfAbsent(ringtoneName, ringtone);
                    sList.add(ringtoneName);
                }
            }
        } catch (Exception e) {
            Log.e(SEVERE, e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== R.id.reset){
            Toast.makeText(this, "Reset", Toast.LENGTH_SHORT).show();
        } else if(item.getItemId() == R.id.reload){
            Toast.makeText(this, "Reload", Toast.LENGTH_SHORT).show();
        } else{
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
