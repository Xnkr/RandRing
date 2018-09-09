package com.xnkr.randomring;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    public static final String DEBUG = "DEBUG";
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String SEVERE = "SEVERE";
    public static final String ringtonesPath = Environment.getExternalStorageDirectory().toString() + "/Ringtones";

    Button nitrogen;
    TextView currentlyPlaying;
    ListView sourceList, destList;
    List<String> sList = new ArrayList<>();
    List<String> dList = new ArrayList<>();
    TreeMap<String, Ringtone> sMap = new TreeMap<>();
    TreeMap<String, Ringtone> dMap = new TreeMap<>();
    TreeMap<String, Ringtone> allMap = new TreeMap<>();
    Ringtone currentlyPlayingRingtone;
    ArrayAdapter<String> dListAdapter;
    ArrayAdapter<String> sListAdapter;
    Random random = new Random(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: DB and Persistance
        nitrogen = (Button) findViewById(R.id.nitrogen);
        currentlyPlaying = (TextView) findViewById(R.id.text);
        sourceList = (ListView) findViewById(R.id._source);
        destList = (ListView) findViewById(R.id._dest);

        sListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                sList
        );
        dListAdapter = new ArrayAdapter<>(
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
                if (randomizeRingtone()) {
                    Toast.makeText(MainActivity.this, "Ringtone set", Toast.LENGTH_SHORT).show();
                    currentlyPlaying.setText("Currently set: " + currentlyPlayingRingtone.getFileName());
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

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

                } catch (Exception e) {
                    Log.e(SEVERE, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getMenuInflater().inflate(R.menu.switch_menu, menu);
        MenuItem item = menu.findItem(R.id.switch_rand);
        item.setActionView(R.layout.switch_item);
        final Switch sw = (Switch) menu.findItem(R.id.switch_rand).getActionView().findViewById(R.id.action_switch);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO: Switch scheduler
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "Checked", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Unchecked", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        if (item.getItemId() == R.id.reset) {
            try {
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
                Toast.makeText(this, "Lists reset", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(SEVERE, e.getMessage());
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.reload) {

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
        } else if (item.getItemId() == R.id.select_all) {
            if (!sMap.isEmpty()) {
                sMap.clear();
                dMap = (TreeMap<String, Ringtone>) allMap.clone();
                dList.addAll(sList);
                sList.clear();
                sListAdapter.notifyDataSetChanged();
                dListAdapter.notifyDataSetChanged();
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean randomizeRingtone() {
        try {
            Object[] dMapKeys = dMap.keySet().toArray();
            Object dMapRandom = dMapKeys[random.nextInt(dMapKeys.length)];
            if (currentlyPlayingRingtone != null) {
                while (dMapRandom == currentlyPlayingRingtone.getFileName())
                    dMapRandom = dMapKeys[random.nextInt(dMapKeys.length)];
            }
            currentlyPlayingRingtone = dMap.get(dMapRandom);
            setRingtone();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void setRingtone() {
        File ringtoneFile = new File(currentlyPlayingRingtone.getFilePath());
        if (currentlyPlayingRingtone.getFileName() != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, ringtoneFile.getName());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.MediaColumns.SIZE, ringtoneFile.length());
            values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());
            // TODO: Add permissions check
            getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtoneFile.getAbsolutePath() + "\"", null);
            Uri newUri = getContentResolver().insert(uri, values);

            try {
                RingtoneManager.setActualDefaultRingtoneUri(MainActivity.this, RingtoneManager.TYPE_RINGTONE, newUri);
            } catch (Exception e) {
                Log.e(SEVERE, e.getMessage());
                e.printStackTrace();
            }
        }
    }
}