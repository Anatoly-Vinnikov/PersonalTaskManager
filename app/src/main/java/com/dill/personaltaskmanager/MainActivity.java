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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences savedTasks;
    private SharedPreferences savedStats;
    private ArrayList<String> tasks = new ArrayList<>();
    private ArrayList<String> checkedTasks = new ArrayList<>();
    private ArrayList<String> search = new ArrayList<>();
    private ArrayAdapter<String> adapter, adapter2;
    private ListView listView;
    private TextView textView;
    private Context context;
    private static long back_pressed;
    private boolean searchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listView.smoothScrollToPosition(0);
            }
        });

        context = this;
        textView = (TextView) findViewById(R.id.textView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);

                alert.setTitle(R.string.new_task);
                final EditText input = new EditText(context);
                alert.setView(input);

                alert.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        if (!value.equals("")) {
                            tasks.add(0, value);
                            adapter.notifyDataSetChanged();
                            setChecked();
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }
                });

                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        }
                    }
                });

                alert.show();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
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
        if (tasks.size() > 0) {
            textView.setVisibility(View.INVISIBLE);
            Collections.sort(tasks);
        }
        adapter.notifyDataSetChanged();

        savedStats = getSharedPreferences(getString(R.string.sharedTasks), Context.MODE_PRIVATE);
        Set<String> retStats = savedStats.getStringSet(getString(R.string.stats), new HashSet<String>());
        for(String r : retStats) {
            checkedTasks.add(0, r);
        }
        setChecked();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                if (listView.isItemChecked(position)) {
                    listView.setItemChecked(position, true);
                    if (searchMode) {
                        checkedTasks.add(search.get(position));
                    } else {
                        checkedTasks.add(tasks.get(position));
                    }
                } else {
                    listView.setItemChecked(position, false);
                    if (searchMode) {
                        checkedTasks.remove(search.get(position));
                    } else {
                        checkedTasks.remove(tasks.get(position));
                    }
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

    private void setChecked() {
        for (int i = 0; i < tasks.size(); i++) {
            listView.setItemChecked(i, false);
        }
        for (int i = 0; i < checkedTasks.size(); i++) {
            if (!searchMode) {
                listView.setItemChecked(tasks.indexOf(checkedTasks.get(i)), true);
            } else {
                listView.setItemChecked(search.indexOf(checkedTasks.get(i)), true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (searchMode) {
            try {
                getSupportActionBar().setTitle(R.string.app_name);
                getSupportActionBar().setHomeButtonEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            } catch (NullPointerException e) {
                Log.d("", "Action bar is not supported");
            }
            searchMode = false;
            listView.setAdapter(adapter);
            search.clear();
            setChecked();
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis())
                super.onBackPressed();
            else
                Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
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
                setChecked();
                if (tasks.size() == 0) {
                    textView.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setCancelable(true);
        return builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sortA) {
            Collections.sort(tasks);
            adapter.notifyDataSetChanged();
            setChecked();
            Toast.makeText(this, R.string.sort_done, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_sortZ) {
            Collections.sort(tasks);
            Collections.reverse(tasks);
            adapter.notifyDataSetChanged();
            setChecked();
            Toast.makeText(this, R.string.sort_done, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_search) {
            searchMode = true;
            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            alert.setTitle(R.string.search);
            final EditText input = new EditText(context);
            alert.setView(input);

            alert.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();
                    search.clear();
                    try {
                        getSupportActionBar().setTitle(R.string.search);
                        getSupportActionBar().setHomeButtonEnabled(true);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    } catch (NullPointerException e) {
                        Log.d("", "Action bar is not supported");
                    }
                    for (int i = 0; i < tasks.size(); i++) {
                        if (tasks.get(i).contains(value)) {
                            search.add(tasks.get(i));
                        }
                    }

                    adapter2 = new ArrayAdapter<>(context,
                            android.R.layout.simple_list_item_checked, search);
                    listView.setAdapter(adapter2);
                    setChecked();
                    Toast.makeText(context, R.string.sort_done, Toast.LENGTH_SHORT).show();
                }
            });

            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    }
                }
            });

            alert.show();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
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