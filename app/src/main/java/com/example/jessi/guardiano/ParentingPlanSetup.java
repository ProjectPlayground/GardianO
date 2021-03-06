package com.example.jessi.guardiano;


import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.jessi.guardiano.DataObjects.Children;
import com.example.jessi.guardiano.DataObjects.Plan;
import com.example.jessi.guardiano.DataObjects.UnderSchoolAgeScheduleWeekday;
import com.example.jessi.guardiano.DataObjects.UnderSchoolAgeScheduleWeekend;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.jessi.guardiano.R.id.button_drop_off;
import static com.example.jessi.guardiano.R.id.button_drop_off_weekday;
import static com.example.jessi.guardiano.R.id.button_pick_up;
import static com.example.jessi.guardiano.R.id.button_pick_up_weekday;
import static com.example.jessi.guardiano.R.id.editPlanName;
import static com.example.jessi.guardiano.R.id.spinner_weekday_options;
import static com.example.jessi.guardiano.R.id.spinner_weekend_options;
import static com.example.jessi.guardiano.R.id.text_child_DOB;
import static com.example.jessi.guardiano.R.id.text_child_name;
import static com.example.jessi.guardiano.R.id.yes_radio_button;
import static java.security.AccessController.doPrivilegedWithCombiner;
import static java.security.AccessController.getContext;

//TODO: code clean up
public class ParentingPlanSetup extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView tvweekendDropOffTimeText, tvweekendPickUpTimeText, tvweekdayDropOffTimeText, tvweekdayPickUpTimeText;
    private TextView tvPlanName, tvChildName, tvChildDOB;
    private EditText mPlanName, mChildName, mChildDOB;
    private Spinner sweekendFrequency, sweekdayFrequency, spinnerWeekend, spinnerWeekday;
    private TimePicker timePicker;
    private Button weekendDropoffTimeButton, weekendPickUpTimeButton, buttonNext;
    private int hour, minute;
    private String planName, planStart, childName, childDOB, weekendFrequency, weekdayFrequency;
    private String weekendDropOffTime, weekendPickUpTime, weekdayDropOffTime, weekdayPickUpTime;
    static final int TIME_DIALOG_ID = 999;

    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mDatabaseReferenceP, mDatabaseReferenceC, mDatabaseReference;
    private ChildEventListener mChildEventListenerP, mChildEventListenerC, mChildEventListener;
    private LinearLayout llUnderSchoolAge, llSameSchedule1, llSameSchedule2, llWeekdaySelection, llTimePicker1, llTimePicker2 ;
    private RadioButton yesRadioButton, yesRadioButton2, noRadioButton, noRadioButton2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parenting_plan_setup);

        //instantiate text views
        tvPlanName = (TextView) findViewById(R.id.textView_planName);
        tvChildName = (TextView) findViewById(R.id.textView_childName);
        tvChildDOB = (TextView) findViewById(R.id.textView_childDOB);
        tvweekdayDropOffTimeText = (TextView) findViewById(R.id.text_drop_off_weekday);
        tvweekdayPickUpTimeText = (TextView) findViewById(R.id.text_pick_up_weekday);
        tvweekendDropOffTimeText = (TextView) findViewById(R.id.text_drop_off);
        tvweekendPickUpTimeText = (TextView) findViewById(R.id.text_pick_up);

        //Initiate layouts
        llUnderSchoolAge = (LinearLayout) findViewById(R.id.layout_q_same_schedule);
        llSameSchedule1 = (LinearLayout) findViewById(R.id.layout_weekend_schedule);
        llSameSchedule2 = (LinearLayout) findViewById(R.id.layout_weekday_schedule);
        llWeekdaySelection = (LinearLayout) findViewById(R.id.layout_weekday_selection);
        llTimePicker1 = (LinearLayout) findViewById(R.id.layout_weekend_time_picker);
        llTimePicker2 = (LinearLayout) findViewById(R.id.layout_weekday_time_picker);

        //initiate buttons
        buttonNext = (Button) findViewById(R.id.button_first_page);

        //Initialize RadioButtons
        yesRadioButton = (RadioButton) findViewById(R.id.yes_radio_button);
        yesRadioButton2 = (RadioButton) findViewById(R.id.yes2_radio_button);
        noRadioButton = (RadioButton) findViewById(R.id.no_radio_button);
        noRadioButton2 = (RadioButton) findViewById(R.id.no2_radio_button);

        // Spinner element
        spinnerWeekend = (Spinner) findViewById(R.id.spinner_weekend_options);
        this.initializeSpinner(spinnerWeekend);

        spinnerWeekday = (Spinner) findViewById(R.id.spinner_weekday_options);
        this.initializeSpinner(spinnerWeekday);

        //Database access
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mfirebaseDatabase.getReference();

        //attach db child listener
        dbChildEventListener();

        //Read and save text inputs
        mPlanName = (EditText) findViewById(R.id.editPlanName);
        this.editorActionListener(mPlanName);

        //TODO: Add planStart as input
        //EditText mPlanStart = (EditText) findViewById(R.id.editPlanStart);
        //planStart = mPlanName.getText().toString();
        mChildName = (EditText) findViewById(R.id.text_child_name);
        this.editorActionListener(mChildName);
        mChildDOB = (EditText) findViewById(R.id.text_child_DOB);
        this.editorActionListener(mChildDOB);

        //call bottom navigation
        this.bottomNavigationViewListener();
    }

    private void initializeSpinner(Spinner s) {

        List<String> categories = new ArrayList<String>();

        // Spinner click listener
        s.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        switch (s.getId()) {
            case spinner_weekend_options:

                categories.add("Every");
                categories.add("Every other");
                categories.add("First wknd of mos");
                categories.add("Second wknd of mos");
                categories.add("Third wknd of mos");
                categories.add("Fourth wknd of mos");
                categories.add("Fifth wknd of mos");
                categories.add("None");
                break;

            case spinner_weekday_options:
                //List<String> categories = new ArrayList<String>();
                categories.add("Every");
                categories.add("Every other");
                categories.add("First week of mos");
                categories.add("Second week of mos");
                categories.add("Third week of mos");
                categories.add("Fourth week of mos");
                categories.add("Fifth week of mos");
                categories.add("None");
                break;
        }
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        s.setAdapter(dataAdapter);
    }

    private void editorActionListener(EditText et) {

        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    //TODO: Temperarly save state here
                boolean done = false;
                Context context = getApplicationContext();
                if (actionId == EditorInfo.IME_ACTION_NEXT) {

                    String text = v.getText().toString();
                    //Todo: Remove this toast
                    //Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

                    switch (v.getId()) {
                        case editPlanName:
                            planName = text;
                            break;

                        /*case editPlanStart:
                            planStart = text;
                            break;*/

                        case text_child_name:
                            childName = text;
                            break;

                        case text_child_DOB:
                            childDOB = text;
                            break;
                    }
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = v.getText().toString();
                    //TODO: remove this toast
                    // Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    done = true;

                    switch (v.getId()) {
                        case text_child_DOB:
                            childDOB = text;
                            break;
                    }
                }

                if (done) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.RESULT_HIDDEN);
                }

                return done;
            }
        });
    }
    private void dbChildEventListener() {

        mChildEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //hideSoftKeyboard();
                //tvChildDOB.setVisibility(View.VISIBLE);
                if(dataSnapshot.getKey().equals("Children")) {
                    mChildName.setVisibility(View.GONE);
                    mChildDOB.setVisibility(View.GONE);

                    tvChildName.setVisibility(View.VISIBLE);
                    tvChildDOB.setVisibility(View.VISIBLE);

                    Children children = dataSnapshot.getValue(Children.class);
                    tvChildName.setText(children.getChildName());
                    tvChildDOB.setText(children.getChildDOB());
                }
                if(dataSnapshot.getKey().equals("Plan")) {
                    mPlanName.setVisibility(View.GONE);
                    tvPlanName.setVisibility(View.VISIBLE);

                    Plan plan = dataSnapshot.getValue(Plan.class);
                    tvPlanName.setText(plan.getPlanName());
                    //tvChildDOB.setText(children.getChildDOB());
                }
                if(dataSnapshot.getKey().equals("UnderSchoolAgeScheduleWeekend")) {
                    yesRadioButton.setChecked(true);
                    noRadioButton.setChecked(false);
                    llUnderSchoolAge.setVisibility(View.VISIBLE);
                    yesRadioButton2.setChecked(false);
                    noRadioButton2.setChecked(true);
                    llSameSchedule1.setVisibility(View.VISIBLE);
                    llTimePicker1.setVisibility(View.VISIBLE);

                    UnderSchoolAgeScheduleWeekend underSchoolAgeScheduleWeekend = dataSnapshot.getValue(UnderSchoolAgeScheduleWeekend.class);
                    int selection = getSpinnerValueIndex(spinnerWeekend, underSchoolAgeScheduleWeekend.getFrequency());

                    spinnerWeekend.setSelection(selection);
                    tvweekendDropOffTimeText.setText(underSchoolAgeScheduleWeekend.getDropOffTime());
                    tvweekendPickUpTimeText.setText(underSchoolAgeScheduleWeekend.getPickUpTime());
                }
                //TODO: Set questions visible, and set weekday frequency, dropoff and pick up times
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseReference = mfirebaseDatabase.getReference().child("User");
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    private int getSpinnerValueIndex(Spinner spinner, String value) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++){
            if (spinner.getItemAtPosition(i).equals(value)){
                index = i;
                break;
            }
        }
        return index;
    }

    private void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.RESULT_HIDDEN);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        switch (parent.getId()) {
            case spinner_weekday_options:
                weekdayFrequency = item;
                break;
            case spinner_weekend_options:
                weekendFrequency = item;
                break;
        }
        // Showing selected spinner item/ add spinner selection
        // to DB
        //Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // when nothing is selected do nothing. Empty on purpose.
    }

    public void showTimePickerDialog(View v) {
        //DialogFragment newFragment = new TimePickerFragment();
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");

        switch (v.getId()) {
            case button_drop_off:
                newFragment.setTvText(tvweekendDropOffTimeText);
                //weekendDropOffTime = tvweekendDropOffTimeText.getText().toString();
                break;
            case button_pick_up:
                newFragment.setTvText(tvweekendPickUpTimeText);
                //weekdayDropOffTime = time;
                break;
            case button_drop_off_weekday:
                newFragment.setTvText(tvweekdayDropOffTimeText);
                //weekdayDropOffTime = time;
                break;
            case button_pick_up_weekday:
                newFragment.setTvText(tvweekdayPickUpTimeText);
                //weekdayPickUpTime = time;
                break;
        }
    }

    public void onNextButtonClicked(View v) {

        //Set values to views
        weekendDropOffTime = tvweekendDropOffTimeText.getText().toString();
        weekendPickUpTime = tvweekendPickUpTimeText.getText().toString();

        //TODO: Call all DB transactions here. Figure out save state in case app closes local save
        Children children = new Children(childName, childDOB);
        Plan plan = new Plan(planName);
        //UnderSchoolAgeScheduleWeekday underSchoolAgeScheduleWeekday = new UnderSchoolAgeScheduleWeekday();
        UnderSchoolAgeScheduleWeekend underSchoolAgeScheduleWeekend = new UnderSchoolAgeScheduleWeekend(weekendFrequency,weekendPickUpTime, weekendDropOffTime);
        //can restore the data. WHen next is clicked all transactions are pushed to db.
        //Call to save Plan name
        mDatabaseReference = mfirebaseDatabase.getReference().child("User/Plan");
        mDatabaseReference.setValue(plan);

        //Call to save child info
        mDatabaseReference = mfirebaseDatabase.getReference().child("User/Children");
        mDatabaseReference.setValue(children);

        //Call to save Under age weekend schedule
        mDatabaseReference = mfirebaseDatabase.getReference().child("User/UnderSchoolAgeScheduleWeekend");
        mDatabaseReference.setValue(underSchoolAgeScheduleWeekend);

        //Call to save under age weekday schedule
        //mDatabaseReference = mfirebaseDatabase.getReference().child("User/UnderSchoolAgeScheduleWeekday");
        //mDatabaseReference.setValue(underSchoolAgeScheduleWeekday);
    }
    public void bottomNavigationViewListener() {

        final Context context = this;

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(

                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_calendar:
                                Intent intent1 = new Intent(context, Calendar.class);
                                startActivity(intent1);
                                break;
                            case R.id.action_plan:
                                Intent intent2 = new Intent(context, ParentingPlanSetup.class);
                                startActivity(intent2);
                                break;
                            case R.id.action_settings:
                                Intent intent3 = new Intent(context, SettingsActivity.class);
                                startActivity(intent3);
                                break;
                        }
                        return true;
                    }
                });
    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        //Check which radio button was clicked
        switch (view.getId()) {
            case R.id.yes_radio_button:
                if (checked) {
                    //Make use school schedule  Q visible
                    llUnderSchoolAge.setVisibility(View.VISIBLE);
                    //Show next button
                    buttonNext.setVisibility(View.GONE);
                }
                break;
            case R.id.yes2_radio_button:
                if (checked) {
                    //Using the same school schedule, no futher action is needed
                    llSameSchedule1.setVisibility(View.GONE);
                    llTimePicker1.setVisibility(View.GONE);
                    llSameSchedule2.setVisibility(View.GONE);
                    llWeekdaySelection.setVisibility(View.GONE);
                    llTimePicker2.setVisibility(View.GONE);
                    //Show next button
                    buttonNext.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.no_radio_button:
                if (checked) {
                    //No futher action is needed, proceed to the next form, child is not under
                    //school age
                    llUnderSchoolAge.setVisibility(View.GONE);

                    llSameSchedule1.setVisibility(View.GONE);
                    llTimePicker1.setVisibility(View.GONE);
                    llSameSchedule2.setVisibility(View.GONE);
                    llWeekdaySelection.setVisibility(View.GONE);
                    llTimePicker2.setVisibility(View.GONE);

                    //Show next button
                    buttonNext.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.no2_radio_button:
                if (checked) {
                    //Child under school age and not using school schedule
                    llSameSchedule1.setVisibility(View.VISIBLE);
                    llTimePicker1.setVisibility(View.VISIBLE);
                    llSameSchedule2.setVisibility(View.VISIBLE);
                    llWeekdaySelection.setVisibility(View.VISIBLE);
                    llTimePicker2.setVisibility(View.VISIBLE);

                    //Show next button
                    buttonNext.setVisibility(View.VISIBLE);
                }
                break;
        }

    }

    public void addImageButtonClicked(View view) {

        //ImageButton ibAdd = (ImageButton) this.findViewById(R.id.image_button_add_remove);


    }
}
