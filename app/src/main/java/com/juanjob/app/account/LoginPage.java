package com.juanjob.app.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import com.juanjob.app.account.client.ClientRegistrationPage;
import com.juanjob.app.client.ClientNavigationPage;
import com.juanjob.app.customer.CustomerNavigationPage;
import com.juanjob.app.R;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.LoadingPage;

public class LoginPage extends AppCompatActivity {
    private AlertDialog loading_screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login_page);
        loading_screen = new LoadingPage().loadingGif(this);

        TextInputEditText login_username_text_field = findViewById(R.id.login_username_text_field);
        TextInputEditText login_pw_text_field = findViewById(R.id.login_pw_text_field);

        TextView forgot_pw = findViewById(R.id.forgot_pw);
        forgot_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GotoForgotPasswordPage();
            }
        });

        Button login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_screen.show();
                String username = String.valueOf(login_username_text_field.getText()).trim();
                String password = String.valueOf(login_pw_text_field.getText()).trim();
                if (username.trim().isEmpty() || password.trim().isEmpty()) {
                    Toast.makeText(LoginPage.this,"Username and Password are Required!", Toast.LENGTH_SHORT).show();
                    loading_screen.dismiss();
                } else {
                    login(username, password);
                }
            }
        });

        Button register_btn = findViewById(R.id.register_btn);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GotoRegisterPage();
            }
        });

        Button find_btn = findViewById(R.id.find_btn);
        find_btn.setText(!isWorker() ? "Find Jobs" : "Find Workers");
        find_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findModule();
            }
        });
    }

    private void GotoForgotPasswordPage() {
        Intent i = new Intent(this, ForgotPasswordPage.class);
        startActivity(i);
    }

    private void GotoRegisterPage() {
        if (isWorker()) {
            Intent i = new Intent(this, ClientRegistrationPage.class);
            startActivity(i);
        } else {
            Intent i = new Intent(this, CustomerRegistrationPage.class);
            startActivity(i);
        }
    }

    private void findModule() {
        if (isWorker()) {
            moduleSelected(false);
            GotoLoginPage();
        } else {
            moduleSelected(true);
            GotoLoginPage();
        }
    }

    private void moduleSelected(boolean is_worker) {
        SharedPreferences sp = getSharedPreferences("Module Selected", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("module_selected", is_worker ? "worker" : "employer");
        editor.commit();
    }

    private void GotoLoginPage() {
        Intent i = new Intent(this, LoginPage.class);
        startActivity(i);
        finish();
    }

    private void GotoHomePage() {
        if (isWorker()) {
            Intent i = new Intent(this, ClientNavigationPage.class);
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(this, CustomerNavigationPage.class);
            startActivity(i);
            finish();
        }
    }

    private void accountVerificationPage() {
        Intent i = new Intent(this, AccountVerificationPage.class);
        startActivity(i);
        finish();
    }

    private boolean isWorker() {
        SharedPreferences sp = getSharedPreferences("Module Selected", MODE_PRIVATE);
        String module_selected = sp.getString("module_selected", "");
        return module_selected.equals("worker");
    }

    private void login(String username, String password) {
        String user = username + (isWorker() ? "_client@juanjob.com" : "_customer@juanjob.com");
        FirebaseAuth firebase_auth = FirebaseAuth.getInstance();
        firebase_auth.signInWithEmailAndPassword(user, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loading_screen.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebase_auth.getCurrentUser();
                            getUser(user.getUid());
                        } else {
                            Toast.makeText(LoginPage.this, "Username or password does not exist.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getUser(String user_id) {
        if (isWorker()) {
            new Repositories().getClient(user_id, new Repositories.RepoCallback() {
                @Override
                public void onSuccess(String result) {
                    if (!result.equals("user_not_found")) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            String account_status = String.valueOf(obj.get("account_status"));
                            if (account_status.equals("Verified")) {
                                Toast.makeText(LoginPage.this, "Login Success!",
                                        Toast.LENGTH_LONG).show();
                                storeLogin(user_id);
                                GotoHomePage();
                            } else {
                                accountVerificationPage();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Toast.makeText(LoginPage.this, "Something went wrong, please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            new Repositories().getCustomer(user_id, new Repositories.RepoCallback() {
                @Override
                public void onSuccess(String result) {
                    if (!result.equals("user_not_found")) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            String account_status = String.valueOf(obj.get("account_status"));
                            if (account_status.equals("Verified")) {
                                Toast.makeText(LoginPage.this, "Login Success!",
                                        Toast.LENGTH_LONG).show();
                                storeLogin(user_id);
                                GotoHomePage();
                            } else {
                                accountVerificationPage();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Toast.makeText(LoginPage.this, "Something went wrong, please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void storeLogin(String login_id) {
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("login_id", login_id);
        editor.commit();
    }
}
