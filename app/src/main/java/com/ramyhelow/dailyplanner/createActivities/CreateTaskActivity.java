package com.ramyhelow.dailyplanner.createActivities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ramyhelow.dailyplanner.R;
import com.ramyhelow.dailyplanner.objects.Project;
import com.ramyhelow.dailyplanner.objects.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.dailyplanner.Utils.getCurrentDateAndTime;
import static com.ramyhelow.dailyplanner.Utils.getDatabase;
import static com.ramyhelow.dailyplanner.Utils.getUserId;
import static com.ramyhelow.dailyplanner.Utils.isEmpty;
import static com.ramyhelow.dailyplanner.Utils.updateWidget;

public class CreateTaskActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.due_date_tv)
    TextView dueDate;
    @BindView(R.id.pick_date_button)
    Button pickDate;
    @BindView(R.id.fab_save_task)
    FloatingActionButton fabSave;
    @BindView(R.id.et_task_title)
    EditText taskTitleEditText;
    @BindView(R.id.et_task_details)
    EditText taskDetailsEditText;
    @BindView(R.id.pick_project_button)
    Button pickProject;
    @BindView(R.id.selected_project_tv)
    TextView selectedProjectTv;

    private List<Project> projectItemList = new ArrayList<>();
    private List<String> projectTitleList = new ArrayList<>();
    private List<Project> projects;
    private DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance()
            .getReference();
    private Project selectedProject;
    private String projectId = "0";
    private String uId =getUserId();

    private SharedPreferences pref;
    private String savedTitle = "saved_title";
    private String savedContent = "saved_content";
    private String savedDate = "saved_date";
    private String savedDateDb = "saved_date_db";
    private String savedProject = "saved_project";
    private String savedProjectTitle = "saved_project_title";
    private String savedProjectKey = "saved_project_key";
    private String savedId = "saved_id";
    private String nothing = "nothing";
    private String savedTitleStr;
    private String savedContentStr;
    private String savedDateStr;
    private String savedDateDbStr;
    private String savedProjectStr;
    private String savedIdStr;
    private String savedProjectKeyStr;
    public static final String myPreference = "myPref";
    private String edit = "edit";
    private int isEdit = 0;
    private String title = "title";
    private String date = "date";
    private String projectTitle;
    private String dateDb = "dateDb";
    private String content = "content";
    private String key = "key";
    private String projectKey = "projectKey";
    private String selectedProjectKey;


    private String taskTitle;
    private String taskContent;
    private String taskDate;
    private String taskDateDb;
    private String taskKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task);

        getDatabase();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String projectTitle = dataSnapshot.child("title").getValue(String.class);
                selectedProjectTv.setText(projectTitle);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        pref = getSharedPreferences(myPreference,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        pickDate.setOnClickListener(this);
        pickProject.setOnClickListener(this);
        fabSave.setOnClickListener(this);

        Intent intent = getIntent();

        // Checking if user is editing existing task
        if (intent.hasExtra(edit)){
            isEdit = 1;

            if (intent.getStringExtra(title)!=null) {
                taskTitle = intent.getStringExtra(title);
                taskTitleEditText.setText(taskTitle);
            }

            if (intent.getStringExtra(date)!=null) {
                taskDate = intent.getStringExtra(date);
                dueDate.setText(taskDate);
            }

            if (intent.getStringExtra(dateDb)!=null) {
                taskDateDb = intent.getStringExtra(dateDb);
            }

            if (intent.getStringExtra(projectKey)!=null) {
                selectedProjectKey = intent.getStringExtra(projectKey);
                mFirebaseDatabase
                        .child("users")
                        .child(uId)
                        .child("projects")
                        .child(selectedProjectKey).addValueEventListener(valueEventListener);
            }

            if (intent.getStringExtra(content)!=null) {
                taskContent = intent.getStringExtra(content);
                taskDetailsEditText.setText(taskContent);
            }

            if (intent.getStringExtra(key)!=null) {
                taskKey = intent.getStringExtra(key);
            }
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (isEdit == 0) {
            getSupportActionBar().setTitle(R.string.create_new_task);
        } else {
            getSupportActionBar().setTitle(R.string.edit_task);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if (pref.contains(savedTitle)){
            taskTitleEditText.setText(pref.getString(savedTitle, nothing));
        }

        if (pref.contains(savedContent)){
            taskDetailsEditText.setText(pref.getString(savedContent, nothing));
        }

        if (pref.contains(savedDate)){
            dueDate.setText(pref.getString(savedDate, nothing));
        }

        if (pref.contains(savedProject)){
            selectedProjectTv.setText(pref.getString(savedProject, nothing));
        }

        if (pref.contains(savedId)){
            projectId = pref.getString(savedId, nothing);
        }

        if (pref.contains(savedProjectKey)){
            selectedProjectKey = pref.getString(savedProjectKey, nothing);
        }


        mFirebaseDatabase.child("users").child(getUserId())
                .child("projects").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    projectItemList.clear();
                    List<String> values;
                    @SuppressWarnings("unchecked") Map<String, String> td = (HashMap<String,String>) dataSnapshot.getValue();

                    if (td != null) {
                        values = new ArrayList<>(td.values());
                        projectTitleList.clear();

                        JSONArray jsonArray = new JSONArray(values);
                        String jsonArrayStr = jsonArray.toString();

                        Type listType = new TypeToken<List<Project>>(){}.getType();
                        projects = new Gson().fromJson(jsonArrayStr, listType);

                        final int N = projects.size();
                        for (int i = 0; i < N; i++) {
                            Project projectItem = projects.get(i);
                            String projectStr = projectItem.getTitle();
                            projectTitleList.add(projectStr);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        String dateStr = "";
        String dateDbStr = "";
        String projectTitleStr = "";
        String idStr ="";
        String projectKeyStr ="";

        SharedPreferences.Editor editor = pref.edit();

        if (!isEmpty(taskTitleEditText)){
            savedTitleStr = taskTitleEditText.getText().toString();
            editor.putString(savedTitle,savedTitleStr);
        }

        if (!isEmpty(taskDetailsEditText)){
            savedContentStr = taskDetailsEditText.getText().toString();
            editor.putString(savedContent, savedContentStr);
        }

        if (dueDate.getText() != null){
            savedDateStr = dueDate.getText().toString();
            dateStr = savedDateStr;
            editor.putString(savedDate, savedDateStr);

            savedDateDbStr = taskDateDb;
            dateDbStr = savedDateDbStr;
            editor.putString(savedDateDb, savedDateDbStr);
        }

        if (selectedProjectTv.getText() != null){
            savedProjectStr = projectTitle;
            projectTitleStr = savedProjectStr;
            editor.putString(savedProject, savedProjectStr);

            savedProjectKeyStr = selectedProjectKey;
            projectKeyStr = savedProjectKeyStr;
            editor.putString(savedProjectKey, savedProjectKeyStr);
        }

        if (!projectId.equals("0")){
            savedIdStr = projectId;
            idStr = savedIdStr;
            editor.putString(savedId, savedIdStr);
        }

        if (!dateStr.equals("")){
            outState.putString(savedDate, dateStr);
            outState.putString(savedDateDb, dateDbStr);
        }

        if (projectTitleStr != null){
            outState.putString(savedProject, projectTitleStr);
            outState.putString(savedProjectKey, projectKeyStr);
        }

        if (!idStr.equals("")){
            outState.putString(savedId, idStr);
        }

        editor.apply();
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState){
        super.onRestoreInstanceState(outState);

        if (outState.containsKey(savedDate)) {
            dueDate.setText(outState.getString(savedDate, nothing));
            taskDateDb = outState.getString(savedDateDb, nothing);
        }

        if (outState.containsKey(savedId)){
            projectId = outState.getString(savedId, nothing);
        }

        if (outState.containsKey(savedProjectKey)) {
            selectedProjectKey = outState.getString(savedProjectKey, nothing);
            projectTitle = outState.getString(savedProject, nothing);
            selectedProjectTv.setText(projectTitle);
        }

        if (pref.contains(savedContent)){
            taskDetailsEditText.setText(pref.getString(savedContent, nothing));
        }

        if (pref.contains(savedDate)){
            dueDate.setText(pref.getString(savedDate, nothing));
            taskDateDb = pref.getString(savedDateDb, nothing);
        }

        if (pref.contains(savedProject)){
            selectedProjectTv.setText(pref.getString(savedProject, nothing));
        }

        if (pref.contains(savedProjectKey)){
            selectedProjectKey = pref.getString(savedProjectKey, nothing);
        }

        if (pref.contains(savedId)){
            projectId = pref.getString(savedId, nothing);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == pickDate) {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateTaskActivity.this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            String selectedDate = dayOfMonth +"."+ (monthOfYear + 1) + "." + year ;

                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, monthOfYear);
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            Date dateRepresentation = cal.getTime();

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
                            taskDateDb = sdf.format(dateRepresentation);
                            dueDate.setText(selectedDate);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        } else if(view == fabSave) {
            if (isEmpty(taskTitleEditText)) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.no_task_title, Toast.LENGTH_LONG);
                toast.show();
            } else {
                saveTodo();
                finish();
            }
        } else if(view == pickProject) {
            new MaterialDialog.Builder(this)
                    .title(R.string.select_project)
                    .items(projectTitleList)
                    .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int index, CharSequence text) {
                            if (index!=-1) {
                                selectedProject = projects.get(index);
                                projectId = selectedProject.getId();

                                mFirebaseDatabase
                                        .child("users")
                                        .child(uId)
                                        .child("projects")
                                        .orderByChild("id")
                                        .equalTo(projectId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                            selectedProjectKey = childSnapshot.getKey();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                projectTitle = selectedProject.getTitle();
                                selectedProjectTv.setText(projectTitle);
                            }
                            return true;
                        }
                    })
                    .negativeText(R.string.cancel)
                    .positiveText(R.string.ok)
                    .show();
        }
    }

    // get the note data to save in our firebase db
    void saveTodo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (user != null) {
            uId = user.getUid();
        } else {
            Log.v("Auth", "User ID is null");
        }

        if (isEdit == 1) {
            database.getReference("users")
                    .child(uId)
                    .child("tasks")
                    .child(taskKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            dataSnapshot.getRef().child("title")
                                    .setValue(taskTitleEditText.getText().toString());

                            dataSnapshot.getRef().child("content")
                                    .setValue(taskDetailsEditText.getText().toString());

                            dataSnapshot.getRef().child("projectKey").setValue(selectedProjectKey);

                            dataSnapshot.getRef().child("date")
                                    .setValue(taskDateDb);

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    R.string.task_edit_confirmation, Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            String dateString = taskDateDb;
            String key = database.getReference("taskList").push().getKey();
            String taskID = getCurrentDateAndTime();

            Task task = new Task();
            task.setTitle(taskTitleEditText.getText().toString());
            task.setContent(taskDetailsEditText.getText().toString());
            task.setDate(dateString);
            task.setState("0");
            task.setProjectKey(selectedProjectKey);
            task.setTaskId(taskID);

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put( key, task.toFirebaseObject());
            database.getReference("users").child(uId).child("tasks").updateChildren(childUpdates);

            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.task_created, Toast.LENGTH_SHORT);
            toast.show();
        }
        updateWidget(getApplicationContext());
    }
}
