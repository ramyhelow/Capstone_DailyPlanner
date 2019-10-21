package com.ramyhelow.dailyplanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.dailyplanner.MainActivity.myPreference;
import static com.ramyhelow.dailyplanner.Utils.getDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    @BindView(R.id.login_btn)
    Button loginButton;

    @BindView(R.id.login_anon_btn)
    Button loginAnonButton;

    FirebaseAuth auth;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Gets FirebaseDatabase
        getDatabase();

        // Binding views
        ButterKnife.bind(this);

        pref = getSharedPreferences(myPreference,
                Context.MODE_PRIVATE);

        auth = FirebaseAuth.getInstance();

        // If user is already logged it, he is forwarded to the main screen
        if (auth.getCurrentUser() != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            SharedPreferences.Editor editor = pref.edit();
            String currentFragmentIndex = "current_fragment";
            editor.putInt(currentFragmentIndex,0);
            editor.apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // Choose authentication providers
        final List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);
            }
        });

        // Anon login is used only for testing and project review
        loginAnonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signInAnonymously()
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("LOGIN", "signInAnonymously:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("LOGIN", "signInAnonymously:failure", task.getException());
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Log.v("AUTH", "SIGN IN FAILED");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        auth = FirebaseAuth.getInstance();

        // If user is already logged it, he is forwarded to the main screen
        if (auth.getCurrentUser() != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)
            startActivity(new Intent(this, MainActivity.class));
        }

    }

}
