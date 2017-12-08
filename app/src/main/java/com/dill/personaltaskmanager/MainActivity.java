package com.dill.personaltaskmanager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /*listView.smoothScrollToPosition(0);*/
    private SharedPreferences savedTasks;
    private SharedPreferences savedStats;
    private ArrayList<String> tasks = new ArrayList<>();
    private ArrayList<String> checkedTasks = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private Context context;
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);

                alert.setTitle(R.string.newTask);
                final EditText input = new EditText(context);
                alert.setView(input);

                alert.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        tasks.add(0, value);
                        adapter.notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.show();
                //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        });

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_checked, tasks);

        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        savedTasks = getSharedPreferences(getString(R.string.sharedTasks), Context.MODE_PRIVATE);
        Set<String> ret = savedTasks.getStringSet(getString(R.string.tasks), new HashSet<String>());
        for(String r : ret) {
            tasks.add(0, r);
        }
        adapter.notifyDataSetChanged();

        savedStats = getSharedPreferences(getString(R.string.sharedTasks), Context.MODE_PRIVATE);
        Set<String> retStats = savedStats.getStringSet(getString(R.string.stats), new HashSet<String>());
        for(String r : retStats) {
            checkedTasks.add(0, r);
            listView.setItemChecked(Integer.parseInt(r), true);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                if (listView.isItemChecked(position)) {
                    listView.setItemChecked(position, true);
                    checkedTasks.add(String.valueOf(position));
                } else {
                    listView.setItemChecked(position, false);
                    checkedTasks.remove(String.valueOf(position));
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                showDialog(position);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    @Override
    protected Dialog onCreateDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] choose ={getString(R.string.delete)};
        builder.setItems(choose, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (listView.isItemChecked(position)) {
                    listView.setItemChecked(position, false);
                    checkedTasks.remove(String.valueOf(position));
                }
                tasks.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onStop() {
        Set<String> tasks = new HashSet<String>();
        tasks.addAll(this.tasks);
        SharedPreferences.Editor tasksEditor = savedTasks.edit();
        tasksEditor.putStringSet(getString(R.string.tasks), tasks);
        tasksEditor.apply();

        Set<String> checks = new HashSet<String>();
        checks.addAll(checkedTasks);
        SharedPreferences.Editor statsEditor = savedStats.edit();
        statsEditor.putStringSet(getString(R.string.stats), checks);
        statsEditor.apply();
        super.onStop();
    }
}