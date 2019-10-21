package com.ramyhelow.dailyplanner.detailActivities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.dailyplanner.Utils.getDatabase;
import static com.ramyhelow.dailyplanner.Utils.getUserId;

public class CompletedTasksCategory extends AppCompatActivity {

    // CompletedTasksCategory is used for displaying completed/archived tasks
    // User can set already completed tasks as not completed or permanently delete

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
    @BindView(R.id.rv_completed_tasks)
    RecyclerView recyclerView;
    @BindView(R.id.loading_indicator) ProgressBar loadingIndicator;

    private Animation rotate_forward,rotate_backward;

    boolean isFABOpen=false;

    private FirebaseRecyclerAdapter<Task, CompletedTasksCategory.TaskHolder> mAdapter;
    private DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.completed_tasks_category);

        // Gets FirebaseDatabase and sets offline persistence to true
        getDatabase();

        // Binding views
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.completed_tasks));

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

    public static class TaskHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.task_name) TextView taskTitle;
        @BindView(R.id.task_due_date) TextView taskDate;
        @BindView(R.id.delete_btn) Button deleteButton;
        @BindView(R.id.task_check_box) CheckBox taskCheckbox;

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
    protected void onStart() {
        super.onStart();

        Query query = mFirebaseDatabase
                .child("users")
                .child(getUserId())
                .child("tasks")
                .orderByChild("state")
                .equalTo("1");

        FirebaseRecyclerOptions<Task> options =
                new FirebaseRecyclerOptions.Builder<Task>()
                        .setQuery(query, Task.class)
                        .build();

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
            public void onBindViewHolder(@NonNull TaskHolder holder, final int position, @NonNull final Task task) {
                final TaskHolder viewHolder = holder;
                // Sets task title if its not null
                if (task.getTitle() != null){
                    viewHolder.taskTitle.setText(task.getTitle());
                }

                // Sets formatted deadline date if its not null
                if (task.getDate() != null) {
                    viewHolder.taskDate.setText(task.getFormattedDate());
                }

                viewHolder.taskCheckbox.setChecked(true);

                // onClickListener used for setting task state to 0 (not completed)
                viewHolder.taskCheckbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAdapter.getRef(viewHolder.getAdapterPosition()).child("state").setValue("0");
                        mAdapter.notifyDataSetChanged();
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

        mFirebaseDatabase.child("users").child(getUserId()).child("tasks").orderByChild("state").equalTo("1")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        loadingIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
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
}
