package com.mc.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mrapp.android.dialog.MaterialDialog;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchMaterial;
    private Toolbar toolbar;
    private TextView textView;
    private boolean busValues[];
    private List<String> buses;
    private Set<String> checkedBuses = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        switchMaterial = findViewById(R.id.switchMaterial);
        toolbar = findViewById(R.id.toolbarSetting);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(toolbarClicked());
        textView = findViewById(R.id.textViewBusSetting);
        //Checking if the switch is already checked or not.
        final SharedPreferences sharedPreferences = getSharedPreferences("location",MODE_PRIVATE);
        if(sharedPreferences.getBoolean("getLocation",false)){
            switchMaterial.setChecked(true);
        }else{
            switchMaterial.setChecked(false);
        }
        buses = new ArrayList<String>();
        //Storing the switch data with every change.
        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("getLocation",true);
                    editor.commit();
                }else{
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("getLocation",false);
                    editor.commit();
                }
            }
        });

        textView.setOnClickListener(onBusClick());

    }

    public View.OnClickListener onBusClick() {
        //If the select bus textview is clicked.
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select the buses");
                loadData();
                builder.setMultiChoiceItems(R.array.bus,busValues,clickListener());
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getSharedPreferences("buses",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if(checkedBuses.size() == 0){
                            editor.remove("buses").apply();
                        }else {
                            editor.putStringSet("buses", checkedBuses).apply();
                        }
                        checkedBuses = new HashSet<>();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedBuses = new HashSet<>();
                    }
                });
                MaterialDialog materialDialog = builder.create();

                materialDialog.show();
            }
        };
    }


    public DialogInterface.OnMultiChoiceClickListener clickListener(){
        return new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked){
                    checkedBuses.add(buses.get(which));
                }else{
                    checkedBuses.remove(buses.get(which));
                }
            }
        };
    }

    public void loadData(){
        //Loading the already selected bus from the user. If none is selected then all the buses will be shown in the app
        //If any one is selected then only one bus will be shown.
        buses = Arrays.asList(getResources().getStringArray(R.array.bus));
        busValues = new boolean[buses.size()];

        SharedPreferences sharedPreferences = getSharedPreferences("buses",MODE_PRIVATE);
        Set<String> storedBuses = sharedPreferences.getStringSet("buses",null);
        if(storedBuses == null){
            Arrays.fill(busValues,false);
        }else{
            for(int i=0;i<buses.size();i++){
                if(storedBuses.contains(buses.get(i))){
                    busValues[i] = true;
                    checkedBuses.add(buses.get(i));
                }else{
                    busValues[i]= false;
                }
            }
        }

    }

    public View.OnClickListener toolbarClicked(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }
}

