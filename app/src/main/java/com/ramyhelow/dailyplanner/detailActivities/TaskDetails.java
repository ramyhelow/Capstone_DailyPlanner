package com.ramyhelow.dailyplanner.detailActivities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ramyhelow.dailyplanner.R;
import com.ramyhelow.dailyplanner.createActivities.CreateNoteActivity;
import com.ramyhelow.dailyplanner.createActivities.CreateProjectActivity;
import com.ramyhelow.dailyplanner.createActivities.CreateTaskActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.dailyplanner.Utils.formatDate;
import static com.ramyhelow.dailyplanner.Utils.getDatabase;
import static com.ramyhelow.dailyplanner.Utils.getUserId;

public class TaskDetails extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_main)
    FloatingActionButton fabMain;
    @BindView(R.id.fab_project) FloatingActionButton fabProject;
    @BindView(R.id.fab_note) FloatingActionButton fabNote;
    @BindView(R.id.fab_task) FloatingActionButton fabTask;
    @BindView(R.id.layout_fab_project) LinearLayout layoutFabProject;
    @BindView(R.id.layout_fab_note) LinearLayout layoutFabNote;
    @BindView(R.id.layout_fab_task) LinearLayout layoutFabTask;
    @BindView(R.id.task_title_tv) TextView taskTitleTextView;
    @BindView(R.id.task_content_tv) TextView taskContentTextView;
    @BindView(R.id.task_date) TextView taskDateTextView;
    @BindView(R.id.task_project) TextView taskProjectTextView;

    private Animation rotate_forward,rotate_backward;

    boolean isFABOpen=false;
    private String taskTitle;
    private String taskDate;
    private String dateDb;
    private String uId =getUserId();
    private String taskProject;
    private String taskContent;
    private String taskKey;
    private String projectKey;

    private ValueEventListener valueEventListener;
    private ValueEventListener projectValueEventListener;
    private DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance()
            .getReference();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_details);

        getDatabase();

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.task_details));

        final Intent intent = getIntent();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskTitle =  dataSnapshot.child("title").getValue(String.class);
                if (taskTitle != null) {
                    taskTitleTextView.setText(taskTitle);
                }
                dateDb = dataSnapshot.child("date").getValue(String.class);
                if (dateDb != null) {
                    taskDateTextView.setText(formatDate(dateDb));
                }
                taskContent = dataSnapshot.child("content").getValue(String.class);
                if (taskContent != null) {
                    taskContentTextView.setText(taskContent);
                }
                projectKey = dataSnapshot.child("projectKey").getValue(String.class);

                if (projectKey != null) {
                    mFirebaseDatabase
                            .child("users")
                            .child(uId)
                            .child("projects")
                            .child(projectKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String taskProjectTitle = dataSnapshot.child("title").getValue(String.class);
                            taskProject = taskProjectTitle;
                            taskProjectTextView.setText(taskProject);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Getting task data from intent
        String mTaskTitle = "title";
        if (intent.getStringExtra(mTaskTitle)!=null) {
            taskTitle = intent.getStringExtra(mTaskTitle);
            taskTitleTextView.setText(taskTitle);
        }

        String mTaskContent = "content";
        if (intent.getStringExtra(mTaskContent)!=null) {
            taskContent = intent.getStringExtra(mTaskContent);
            taskContentTextView.setText(taskContent);
        }

        String mTaskDate = "date";
        if (intent.getStringExtra(mTaskDate)!=null) {
            taskDate = intent.getStringExtra(mTaskDate);
            taskDateTextView.setText(taskDate);
        }

        String mTaskDateDb = "dateDb";
        if (intent.getStringExtra(mTaskDateDb)!=null){
            dateDb = intent.getStringExtra(mTaskDateDb);
        }

        String mTaskProjectKey = "projectKey";
        if (intent.getStringExtra(mTaskProjectKey)!=null) {
            projectKey = intent.getStringExtra(mTaskProjectKey);

            mFirebaseDatabase
                    .child("users")
                    .child(uId)
                    .child("projects")
                    .child(projectKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    taskProject = dataSnapshot.child("title").getValue(String.class);
                    taskProjectTextView.setText(taskProject);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        taskProject = taskProjectTextView.getText().toString();

        String mTaskKey = "taskKey";
        if (intent.getStringExtra(mTaskKey)!=null) {
            taskKey = intent.getStringExtra(mTaskKey);
        }

        // Loading FAB animations
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        // Sets up click listener on main FAB and attaches opening/closing animations
        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                    fabMain.startAnimation(rotate_forward);
                }else{
                    closeFABMenu();
                    fabMain.startAnimation(rotate_backward);
                }
            }
        });

        // Sets up click listeners on "menu" FABs
        // Click opens new activity and closes FAB menu
        fabTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMain.startAnimation(rotate_backward);
                closeFABMenu();
                startActivity(new Intent(getApplicationContext(), CreateTaskActivity.class));
            }
        });

        fabNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMain.startAnimation(rotate_backward);
                closeFABMenu();
                startActivity(new Intent(getApplicationContext(), CreateNoteActivity.class));
            }
        });

        fabProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMain.startAnimation(rotate_backward);
                closeFABMenu();
                startActivity(new Intent(getApplicationContext(), CreateProjectActivity.class));
            }
        });

        DatabaseReference projectsRef = mFirebaseDatabase
                .child("users")
                .child(getUserId())
                .child("projects");

    }

    protected void onResume(){
        super.onResume();

        Log.v("TaskDetails", "onResume triggered. Project key: " + projectKey + "Task key:" + taskKey);

        mFirebaseDatabase
                .child("users")
                .child(uId)
                .child("tasks")
                .child(taskKey).addValueEventListener(valueEventListener);
    }

    protected void onStop(){
        super.onStop();

        mFirebaseDatabase.removeEventListener(valueEventListener);
    }

    // Sets isFABOpen to TRUE, views to visible and creates opening animation
    private void showFABMenu(){
        isFABOpen=true;

        layoutFabProject.setVisibility(View.VISIBLE);
        layoutFabNote.setVisibility(View.VISIBLE);
        layoutFabTask.setVisibility(View.VISIBLE);

        layoutFabProject.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        layoutFabNote.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
        layoutFabTask.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
    }

    // Sets isFABOpen to FALSE and creates closing animations
    // After last animation is finished, sets visibility of views to GONE
    private void closeFABMenu(){
        isFABOpen=false;

        layoutFabProject.animate().translationY(0);
        layoutFabNote.animate().translationY(0);
        layoutFabTask.animate().translationY(0).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    layoutFabProject.setVisibility(View.GONE);
                    layoutFabNote.setVisibility(View.GONE);
                    layoutFabTask.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        if (itemThatWasClickedId == R.id.edit_option) {
            Intent intent = new Intent(getApplicationContext(), CreateTaskActivity.class);
            intent.putExtra("title", taskTitle);
            intent.putExtra("date", taskDate);
            intent.putExtra("dateDb", dateDb);
            intent.putExtra("project", taskProject);
            intent.putExtra("content", taskContent);
            intent.putExtra("projectKey", projectKey);
            intent.putExtra("key", taskKey);
            intent.putExtra("edit", "1");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
