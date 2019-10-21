package com.ramyhelow.dailyplanner.detailActivities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ramyhelow.dailyplanner.R;
import com.ramyhelow.dailyplanner.createActivities.CreateNoteActivity;
import com.ramyhelow.dailyplanner.createActivities.CreateProjectActivity;
import com.ramyhelow.dailyplanner.createActivities.CreateTaskActivity;
import com.ramyhelow.dailyplanner.objects.Task;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.dailyplanner.Utils.getDatabase;
import static com.ramyhelow.dailyplanner.Utils.getUserId;

public class ProjectDetails extends AppCompatActivity {

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
    @BindView(R.id.rv_tasks)
    RecyclerView recyclerView;

    private Animation rotate_forward,rotate_backward;

    boolean isFABOpen=false;
    private String projectKey;
    private String projectTitle;
    private String projectKeyStr = "projectKey";
    private String title = "title";

    private Query query;

    private DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseRecyclerAdapter<Task, ProjectDetails.TaskHolder> mAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_details);

        // Gets FirebaseDatabase and sets offline persistence to true
        getDatabase();

        // Binding views
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();

        // Getting project title and ID from intent
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            String supportActionBarTitle = intent.getStringExtra(Intent.EXTRA_TEXT);
            getSupportActionBar().setTitle(supportActionBarTitle);
        }

        if (intent.hasExtra(projectKeyStr)) {
            projectKey = intent.getStringExtra(projectKeyStr);
        }

        if (intent.hasExtra(title)) {
            projectTitle = intent.getStringExtra(title);
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Setting up query details
        query = mFirebaseDatabase
                .child("users")
                .child(getUserId())
                .child("tasks")
                .orderByChild("projectKey")
                .equalTo(projectKey);

        FirebaseRecyclerOptions<Task> options =
                new FirebaseRecyclerOptions.Builder<Task>()
                        .setQuery(query, Task.class)
                        .build();

        // Creating new FirebaseRecyclerAdapter for displaying tasks in selected project
        mAdapter = new FirebaseRecyclerAdapter<Task, TaskHolder>(options) {
            @NonNull
            @Override
            final public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.task_item, parent, false);

                return new TaskHolder(view);
            }

            @Override
            public void onBindViewHolder(TaskHolder holder, final int position, final Task task) {
                final TaskHolder viewHolder = holder;
                // Sets task title if its not null
                if (task.getTitle() != null){
                    viewHolder.taskTitle.setText(task.getTitle());
                }
                // Sets formatted deadline date if its not null
                if (task.getDate() != null) {
                    viewHolder.taskDate.setText(task.getFormattedDate());
                }

                if (task.getState().equals("1")) {
                    viewHolder.taskCheckbox.setChecked(true);
                } else {
                    viewHolder.taskCheckbox.setChecked(false);
                }

                // onClickListener used for setting task state to 1 (completed)
                viewHolder.taskCheckbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (task.getState().equals("1")) {
                            mAdapter.getRef(viewHolder.getAdapterPosition()).child("state").setValue("0");
                        } else {
                            mAdapter.getRef(viewHolder.getAdapterPosition()).child("state").setValue("1");
                        }
                    }
                });

                // onClickListener used for opening details of clicked task
                // First it gets data of clicked task, puts them inside the intent
                // and then starts TaskDetails activity
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), TaskDetails.class);
                        intent.putExtra("title", task.getTitle());
                        intent.putExtra("content", task.getContent());
                        if (task.getDate() != null) {
                            intent.putExtra("date", task.getFormattedDate());
                        }
                        intent.putExtra("projectKey", task.getProjectKey());
                        intent.putExtra("taskId",task.getTaskId());
                        intent.putExtra("taskKey", mAdapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        startActivity(intent);
                    }
                });

                // onClickListener used for removing task from DB
                viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAdapter.getRef(viewHolder.getAdapterPosition()).removeValue();
                    }
                });
            }
        };

        mAdapter.notifyDataSetChanged();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mAdapter);

        mAdapter.startListening();
    }

    public static class TaskHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.task_name)
        TextView taskTitle;
        @BindView(R.id.task_due_date)
        TextView taskDate;
        @BindView(R.id.delete_btn)
        Button deleteButton;
        @BindView(R.id.task_check_box)
        CheckBox taskCheckbox;

        private TaskHolder(View itemView) {
            super(itemView);
            try {
                ButterKnife.bind(this, itemView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.startListening();
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
            Intent intent = new Intent(getApplicationContext(), CreateProjectActivity.class);
            intent.putExtra("key", projectKey);
            intent.putExtra("title",projectTitle);
            intent.putExtra("edit", "1");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
