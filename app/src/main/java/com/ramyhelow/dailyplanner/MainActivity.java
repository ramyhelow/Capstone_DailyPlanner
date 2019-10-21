package com.ramyhelow.dailyplanner;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.ramyhelow.dailyplanner.adapters.ViewPagerAdapter;
import com.ramyhelow.dailyplanner.createActivities.CreateNoteActivity;
import com.ramyhelow.dailyplanner.createActivities.CreateProjectActivity;
import com.ramyhelow.dailyplanner.createActivities.CreateTaskActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.dailyplanner.Utils.getDatabase;
import static com.ramyhelow.dailyplanner.Utils.updateWidget;

public class MainActivity extends AppCompatActivity {

    // MainActivity is used to display two main screen fragments - Tasks and Notes

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.fab_main)
    FloatingActionButton fabMain;
    @BindView(R.id.fab_project) FloatingActionButton fabProject;
    @BindView(R.id.fab_note) FloatingActionButton fabNote;
    @BindView(R.id.fab_task) FloatingActionButton fabTask;
    @BindView(R.id.layout_fab_project) LinearLayout layoutFabProject;
    @BindView(R.id.layout_fab_note) LinearLayout layoutFabNote;
    @BindView(R.id.layout_fab_task) LinearLayout layoutFabTask;

    @SuppressLint("StaticFieldLeak")
    public static ViewPager viewPager;

    private Animation rotate_forward,rotate_backward;

    private String currentFragmentIndex = "current_fragment";

    boolean isFABOpen=false;

    private SharedPreferences pref;
    public static final String myPreference = "mypref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);

        pref = getSharedPreferences(myPreference,
                Context.MODE_PRIVATE);

        // Gets FirebaseDatabase and sets offline persistence to true
        getDatabase();

        Context context = getApplicationContext();
        updateWidget(context);

        // Binding views
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Setting up fragments
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(TasksFragment.getInstance(), getString(R.string.tasks));
        adapter.addFragment(NotesFragment.getInstance(), getString(R.string.notes));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

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
                startActivity(new Intent(MainActivity.this, CreateTaskActivity.class));
            }
        });

        fabNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMain.startAnimation(rotate_backward);
                closeFABMenu();
                startActivity(new Intent(MainActivity.this, CreateNoteActivity.class));
            }
        });

        fabProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMain.startAnimation(rotate_backward);
                closeFABMenu();
                startActivity(new Intent(MainActivity.this, CreateProjectActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pref.contains(currentFragmentIndex)) {
            int currentItem = pref.getInt(currentFragmentIndex,0);
            viewPager.setCurrentItem(currentItem);
            Log.v("MainActivity", "sharedPreferences set to: " + currentItem);
        } else {
            Log.v("MainActivity", "sharedPreferences are null.");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        int currentFragmentInt = tabLayout.getSelectedTabPosition();
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(currentFragmentIndex,currentFragmentInt);
        editor.apply();

        Log.v("MainActivity", "current fragment is " + currentFragmentInt);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState){
        super.onRestoreInstanceState(outState);
            viewPager.setCurrentItem(outState.getInt(currentFragmentIndex));
        String scrollPositionString = "scroll_position";
        tabLayout.setScrollY(outState.getInt(scrollPositionString));
            Log.v("MainActivity", "last saved fragment is: " + outState.getInt(currentFragmentIndex));
    }

    @Override
    public void onBackPressed(){
        int currentFragment = viewPager.getCurrentItem();

        if (currentFragment > 0) {
            viewPager.setCurrentItem(0);
        } else {
            this.moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        if (itemThatWasClickedId == R.id.log_out_button) {
            AuthUI.getInstance()
                    .signOut(getApplicationContext())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
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
