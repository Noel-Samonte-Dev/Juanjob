package com.juanjob.app.account;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import com.juanjob.app.R;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;

public class ForgotPasswordPage extends AppCompatActivity {
    private AlertDialog loading;
    private TextInputEditText username_text_field, change_pw_otp_text_field, change_pw_pw_text_field, change_pw_confirm_pw_text_field;
    private TextView old_password_tv, user_id_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.forgot_password_page);

        mAuth = FirebaseAuth.getInstance();

        loading = new LoadingPage().loadingGif(this);

        user_id_tv = findViewById(R.id.user_id_tv);
        old_password_tv = findViewById(R.id.old_password_tv);
        username_text_field = findViewById(R.id.username_text_field);
        change_pw_otp_text_field = findViewById(R.id.change_pw_otp_text_field);
        change_pw_pw_text_field = findViewById(R.id.change_pw_pw_text_field);
        change_pw_confirm_pw_text_field = findViewById(R.id.change_pw_confirm_pw_text_field);

        ImageView back_btn_new_pw = findViewById(R.id.back_btn_new_pw);
        back_btn_new_pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView get_code = findViewById(R.id.get_code);
        get_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.show();
                if (isWorker()) {
                    getClientMobileNumber();
                } else {
                    getCustomerMobileNumber();
                }
            }
        });

        Button change_pw_btn = findViewById(R.id.change_pw_btn);
        change_pw_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidPassword()) {
                    String code = String.valueOf(change_pw_otp_text_field.getText());
                    verifyCode(code);
                }

                if (!isValidPassword()) {
                    ToastMsg.error_toast(getApplicationContext(), "Password should be at least 8 characters long and contain uppercase, lowercase, special character, number.");
                }
            }
        });
    }

    private boolean isValidPassword() {
        String password = String.valueOf(change_pw_pw_text_field.getText());
        String confirm_password = String.valueOf(change_pw_confirm_pw_text_field.getText());
        String password_pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\/\";<>:\\\'(){}\\[\\],._!@#$%^&*+=?-]).{8,}$";
        return password.matches(password_pattern) && password.equals(confirm_password);
    }

    private boolean isWorker() {
        SharedPreferences sp = getSharedPreferences("Module Selected", MODE_PRIVATE);
        String module_selected = sp.getString("module_selected", "");
        return module_selected.equals("worker");
    }

    private void changeFirebaseUserPassword(String new_password) {
        String username_str = String.valueOf(username_text_field.getText()).trim();
        String username = username_str + (isWorker() ? "_client@juanjob.com" : "_customer@juanjob.com");
        String old_password = String.valueOf(old_password_tv.getText());
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithEmailAndPassword(username, old_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    firebaseUser.updatePassword(new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                String user_id = String.valueOf(user_id_tv.getText());
                                if (isWorker()) {
                                    new Repositories().updateClientPassword(user_id, new_password, new Repositories.RepoCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            loading.dismiss();
                                            Toast.makeText(getApplicationContext(), "Successfully Updated Password!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                } else {
                                    new Repositories().updateCustomerPassword(user_id, new_password, new Repositories.RepoCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            loading.dismiss();
                                            Toast.makeText(getApplicationContext(), "Successfully Updated Password!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                }
                            } else {
                                loading.dismiss();
                                Toast.makeText(getApplicationContext(), "Something went wrong please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    loading.dismiss();
                    Toast.makeText(getApplicationContext(), "Something went wrong please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private FirebaseAuth mAuth;

    private void otpSend(String phoneNumber) {
        String phonse_number = "+63" + phoneNumber.substring(1);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phonse_number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallBack)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private String verificationId;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            Toast.makeText(getApplicationContext(), "An OTP was sent to your registered mobile number.", Toast.LENGTH_SHORT).show();
            loading.dismiss();
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verificationId = code;
                Toast.makeText(getApplicationContext(), "An OTP was sent to your registered mobile number.", Toast.LENGTH_SHORT).show();
                loading.dismiss();
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            loading.dismiss();
        }
    };

    private void verifyCode(String code) {
        loading.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String new_password = String.valueOf(change_pw_confirm_pw_text_field.getText());
                            changeFirebaseUserPassword(new_password);
                        } else {
                            loading.dismiss();
                            Toast.makeText(ForgotPasswordPage.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getClientMobileNumber() {
        new Repositories().getAllClient(new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject client_id_obj = obj.getJSONObject("client_id");
                        JSONArray client_id_array = client_id_obj.names();
                        boolean is_user_exist = false;
                        for (int a = 0; a < Objects.requireNonNull(client_id_array).length(); a++) {
                            String client_id = String.valueOf(client_id_array.get(a));
                            JSONObject client_info_obj = new JSONObject(String.valueOf(client_id_obj.get(client_id)));
                            String mobile = client_info_obj.getString("mobile");
                            String username_str = client_info_obj.getString("username");
                            String password = client_info_obj.getString("password");
                            old_password_tv.setText(password);
                            user_id_tv.setText(client_id);
                            if (String.valueOf(username_text_field.getText()).trim().equals(username_str.trim())) {
                                is_user_exist = true;
                                otpSend(mobile);
                            }

                            if (a + 1 == Objects.requireNonNull(client_id_array).length() && !is_user_exist) {
                                Toast.makeText(getApplicationContext(), "Username does not exist.", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }
            }
        });
    }

    private void getCustomerMobileNumber() {
        new Repositories().getAllCustomer(new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject client_id_obj = obj.getJSONObject("customer_id");
                        JSONArray client_id_array = client_id_obj.names();
                        boolean is_user_exist = false;
                        for (int a = 0; a < Objects.requireNonNull(client_id_array).length(); a++) {
                            String client_id = String.valueOf(client_id_array.get(a));
                            JSONObject client_info_obj = new JSONObject(String.valueOf(client_id_obj.get(client_id)));
                            String mobile = client_info_obj.getString("mobile");
                            String username_str = client_info_obj.getString("username");
                            String password = client_info_obj.getString("password");
                            old_password_tv.setText(password);
                            user_id_tv.setText(client_id);
                            if (String.valueOf(username_text_field.getText()).trim().equals(username_str.trim())) {
                                is_user_exist = true;
                                otpSend(mobile);
                            }

                            if (a + 1 == Objects.requireNonNull(client_id_array).length() && !is_user_exist) {
                                Toast.makeText(getApplicationContext(), "Username does not exist.", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }
            }
        });
    }
}
