package com.juanjob.app.account.client;

import static com.juanjob.app.helpers.BitmapHelper.bitmap_str;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.juanjob.app.R;
import com.juanjob.app.account.AccountVerificationPage;
import com.juanjob.app.database.ClientTable;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.database.ServiceTable;
import com.juanjob.app.helpers.DateHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;

public class ClientRegistrationAddService extends AppCompatActivity {
    private TextInputEditText service_name_text_field, service_desc_text_field,
            service_location_text_field, price_min_text_field, price_max_text_field;
    private TextView label_spinner_txt;
    private AppCompatButton select_service_img_sample;
    private AlertDialog loading_screen;
    private int price_min_int, price_max_int;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.create_service_page);
        loading_screen = new LoadingPage().loadingGif(this);

        ImageView back_btn_new_service = findViewById(R.id.back_btn_new_service);
        back_btn_new_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        img_str = findViewById(R.id.img_str);
        getKeyboardEventImg(img_str);

        price_min_text_field = findViewById(R.id.price_min_text_field);
        price_max_text_field = findViewById(R.id.price_max_text_field);

        service_name_text_field = findViewById(R.id.service_name_text_field);
        service_desc_text_field = findViewById(R.id.service_desc_text_field);
        service_location_text_field = findViewById(R.id.service_location_text_field);

        label_spinner_txt = findViewById(R.id.label_spinner_txt);
        select_service_img_sample = findViewById(R.id.select_service_img_sample);
        select_service_img_sample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_str.setText("");
                checkPermission();
            }
        });

        Button create_service_btn = findViewById(R.id.create_service_btn);
        create_service_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_screen.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isValidServiceInputs()) {
                            createAccount();
                        } else {
                            loading_screen.dismiss();
                        }
                    }
                }, 1000);
            }
        });
    }

    private void createService(String client_id, String location) {
        //String name = String.valueOf(service_name_text_field.getText());
        String name = "";
        String desc = String.valueOf(service_desc_text_field.getText());
        String category = String.valueOf(label_spinner_txt.getText());
        String sample_img = img_str.getText().toString();
        String status = "Active";
        String date_created = new DateHelper().getDateNow();
        String price_range_str = "₱" + price_min_text_field.getText().toString() + " - " + "₱" + price_max_text_field.getText().toString();

        ServiceTable service = new ServiceTable(name, desc, date_created, category, location, status, client_id, "", sample_img, price_range_str);
        new Repositories().createService(service, client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                loading_screen.dismiss();
                if (result.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Registration Success", Toast.LENGTH_LONG).show();
                    accountVerificationPage();
                } else {
                    Toast.makeText(getApplicationContext(), "Please try again!.", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    public void checkPermission() {
        String permission = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        int requestCode = 923;

        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ClientRegistrationAddService.this,
                    new String[]{permission},
                    requestCode);

            Toast.makeText(getApplicationContext(), "Please allow permission to access media files.", Toast.LENGTH_LONG)
                    .show();
        } else {
            selectImage();
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, 169);
    }

    private static int RESULT_LOAD_IMG = 169;
    String imgDecodableString;
    private TextView img_str;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = this.getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                String bitmap_str_img = bitmap_str(getApplicationContext(), imgDecodableString);
                img_str.setText(bitmap_str_img);

            } else {
                Toast.makeText(this, "You haven't picked an Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void getKeyboardEventImg(TextView textview) {
        textview.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!textview.getText().toString().trim().isEmpty()) {
                    select_service_img_sample.setText("Image Selected");
                    select_service_img_sample.setTextColor(Color.parseColor("#008000"));
                    select_service_img_sample.setBackgroundResource(R.drawable.green_border);
                } else {
                    select_service_img_sample.setText("*Select Sample Service Image");
                    select_service_img_sample.setTextColor(Color.parseColor("#ADABAB"));
                    select_service_img_sample.setBackgroundResource(R.drawable.gray_border);
                }
            }
        });
    }

    private boolean isValidServiceInputs() {
        if (!isValidServiceName()) {
            String error_msg = "Service Name should not be empty.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidDescription()) {
            String error_msg = "Service Description should not be empty.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidLocation()) {
            String error_msg = "Service Location should not be empty.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidImage()) {
            String error_msg = "Please select a sample image of your service";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidPriceRange()){
            return false;
        }

        return true;

    }

    private boolean isValidServiceName() {
        //return !String.valueOf(service_name_text_field.getText()).trim().isEmpty();
        return true;
    }

    private boolean isValidDescription() {
        return !String.valueOf(service_desc_text_field.getText()).trim().isEmpty();
    }

    private boolean isValidLocation() {
        //return !String.valueOf(service_location_text_field.getText()).trim().isEmpty();
        return true;
    }

    private boolean isValidImage() {
        return !String.valueOf(img_str.getText()).trim().isEmpty();
    }

    private boolean isValidPriceRange(){
        price_min_int = price_min_text_field.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(price_min_text_field.getText().toString().trim());
        price_max_int = price_max_text_field.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(price_max_text_field.getText().toString().trim());

        if (price_min_int > price_max_int) {
            String error_msg = "Price min should not be greater than price max.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (price_min_int == price_max_int) {
            String error_msg = "Price min and price max should no be equal.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        return true;
    }


    private void createAccount() {
        Bundle b = new Bundle();
        b = getIntent().getExtras();
        assert b != null;
        String username = b.getString("username") + "_client@juanjob.com";
        String password = b.getString("password");
        FirebaseAuth firebase_auth = FirebaseAuth.getInstance();
        firebase_auth.createUserWithEmailAndPassword(username.trim(), password.trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebase_auth.getCurrentUser();
                            createUserClient(user.getUid());
                        } else {
                            Toast.makeText(ClientRegistrationAddService.this, "Username already exist.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createUserClient(String user_id) {
        Bundle b = new Bundle();
        b = getIntent().getExtras();
        String firstname = b.getString("firstname");
        String middle_name = b.getString("middle_name");
        String lastname = b.getString("lastname");
        String address = b.getString("address");
        String mobile = b.getString("mobile");
        String email = b.getString("email");
        String username = b.getString("username");
        String password = b.getString("password");
        String birthday = b.getString("birthday");
        int age = b.getInt("age");
        String gender = b.getString("gender");
        String profile_id_base64 = b.getString("profile_id_base64");
        String category = b.getString("category");
        String subcategory = b.getString("subcategory");
        String other_category = b.getString("other_category");
        String service_location = b.getString("service_location");
        String front_id_base64 = b.getString("front_id_base64");
        String back_id_base64 = b.getString("back_id_base64");
        String brgy_clearance_base64 = b.getString("brgy_clearance_base64");
        String police_clearance_base64 = b.getString("police_clearance_base64");
        String recent_job_base64 = b.getString("recent_job_base64");

        new Repositories().createClient(new ClientTable(firstname, middle_name, lastname, address,
                mobile, username, password, birthday, age, front_id_base64, back_id_base64, email,
                gender, profile_id_base64, category, other_category, service_location, "Unverified",
                "Inactive", brgy_clearance_base64, police_clearance_base64, recent_job_base64,
                "true", "0", "0", subcategory, 0), user_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("success")) {
                    createService(user_id, service_location);
                } else {
                    Toast.makeText(ClientRegistrationAddService.this, "Username already exist.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void accountVerificationPage() {
        Intent i = new Intent(this, AccountVerificationPage.class);
        startActivity(i);
        finish();
    }
}
