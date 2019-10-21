package com.ramyhelow.dailyplanner.createActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ramyhelow.dailyplanner.R;
import com.ramyhelow.dailyplanner.objects.Project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.dailyplanner.Utils.getCurrentDateAndTime;
import static com.ramyhelow.dailyplanner.Utils.getDatabase;
import static com.ramyhelow.dailyplanner.Utils.isEmpty;

public class CreateProjectActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_save_project)
    FloatingActionButton fabSaveProject;
    @BindView(R.id.et_project_title) EditText projectTitleEditText;
    private String uId;
    private String edit = "edit";
    private String key = "key";
    private String projectKey;
    private int isEdit = 0;
    private String title = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_project);

        getDatabase();

        ButterKnife.bind(this);

        Intent intent = getIntent();

        // Checking if user is editing existing task
        if (intent.hasExtra(edit)){
            isEdit = 1;

            if (intent.getStringExtra(title)!=null) {
                String projectTitle = intent.getStringExtra(title);
                projectTitleEditText.setText(projectTitle);
            }

            if (intent.getStringExtra(key)!=null){
                projectKey = intent.getStringExtra(key);
            }
        }

        // If clicked and project title is not empty, save project
        fabSaveProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEmpty(projectTitleEditText)){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.no_project_title, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    saveProject();
                    finish();
                }
            }
        });

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (isEdit == 0) {
            getSupportActionBar().setTitle(getString(R.string.create_new_project));
        } else {
            getSupportActionBar().setTitle(getString(R.string.edit_project));
        }
    }


    // get the project data to save in our firebase db
    void saveProject() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            uId = user.getUid();
        } else {
            Log.v("Auth", "User ID is null");
        }

        if (isEdit == 1) {
            database.getReference("users")
                    .child(uId)
                    .child("projects")
                    .child(projectKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    database.getReference("users")
                                            .child(uId)
                                            .child("projects")
                                            .child(projectKey)
                                            .child("title")
                                            .setValue(projectTitleEditText.getText().toString());

                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            R.string.project_edit_confirmation, Toast.LENGTH_SHORT);
                                    toast.show();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {

            String key = database.getReference("taskList").push().getKey();
            String projectId = getCurrentDateAndTime();

            final Project project = new Project();
            project.setTitle(projectTitleEditText.getText().toString());
            project.setId(projectId);

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put( key, project.toFirebaseObject());
            database.getReference("users").child(uId).child("projects").updateChildren(childUpdates);
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.project_created, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
