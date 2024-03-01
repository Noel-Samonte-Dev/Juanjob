package com.juanjob.app.account;

import static com.juanjob.app.helpers.BitmapHelper.bitmap_str;
import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.juanjob.app.R;
import com.juanjob.app.database.CustomerTable;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;

public class CustomerRegistrationPage extends AppCompatActivity {
    private TextView birthdate_tv, gender_tv, invalid_fields, brgy_textview;
    private Button birthdate_btn, valid_id_btn, profile_img_btn;
    private AlertDialog loading_screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.customer_registration_page);
        loading_screen = new LoadingPage().loadingGif(this);

        invalid_fields = findViewById(R.id.invalid_fields);
        gender_tv = findViewById(R.id.gender_tv);
        birthdate_tv = findViewById(R.id.birthdate_tv);
        getKeyboardEventBirthdate(birthdate_tv);

        new_account_username_text_field = findViewById(R.id.new_account_username_text_field);
        new_account_pw_text_field = findViewById(R.id.new_account_pw_text_field);
        new_account_confirm_pw_text_field = findViewById(R.id.new_account_confirm_pw_text_field);
        new_account_email_text_field = findViewById(R.id.new_account_email_text_field);
        first_name_text_field = findViewById(R.id.first_name_text_field);
        middle_name_text_field = findViewById(R.id.middle_name_text_field);
        last_name_text_field = findViewById(R.id.last_name_text_field);
        mobile_text_field = findViewById(R.id.mobile_text_field);
        brgy_textview = findViewById(R.id.brgy_textview);

        ImageView back_btn_new_acct = findViewById(R.id.back_btn_new_acct);
        back_btn_new_acct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        birthdate_btn = findViewById(R.id.birthdate_btn);
        birthdate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });

        genderPicker();

        profile_img_btn = findViewById(R.id.profile_img_btn);
        profile_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProfileImg = true;
                String permission = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
                checkPermission(permission, 68);
            }
        });

        valid_id_btn = findViewById(R.id.valid_id_btn);
        valid_id_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProfileImg = false;
                String permission = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
                checkPermission(permission, 689);
            }
        });

        Button create_account_btn = findViewById(R.id.create_account_btn);
        create_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_screen.show();
                if (isValidInputs()) {
                    createAccount();
                } else {
                    loading_screen.dismiss();
                }
            }
        });

        getBrgyList();
        brgyPicker();
        getKeyboardEvent(brgy_textview, "*Select Location");
    }


    private TextInputEditText new_account_username_text_field, new_account_pw_text_field, new_account_confirm_pw_text_field, new_account_email_text_field,
            first_name_text_field, middle_name_text_field, last_name_text_field, mobile_text_field;

    private boolean isValidInputs() {
        String username = String.valueOf(new_account_username_text_field.getText());
        String password = String.valueOf(new_account_pw_text_field.getText());
        String confirm_password = String.valueOf(new_account_confirm_pw_text_field.getText());
        String email = String.valueOf(new_account_email_text_field.getText());
        String firstname = String.valueOf(first_name_text_field.getText());
        String middle_name = String.valueOf(middle_name_text_field.getText());
        String lastname = String.valueOf(last_name_text_field.getText());
        String mobile = String.valueOf(mobile_text_field.getText());
        String brgy = String.valueOf(brgy_textview.getText());
        String birthday = String.valueOf(birthdate_tv.getText());
        String gender = String.valueOf(gender_tv.getText());

        setErrors();

        if (!isValidUsername(username)) {
            String error_msg = "Invalid Username! \nUsername should be 8 characters or more and should not contain a whitespace and any special character.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidPassword(password)) {
            String error_msg = "Invalid Password! \nUse uppercase, lowercase, number, symbol and should be atleast 8 characters in length.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidConfirmPassword(password, confirm_password)) {
            String error_msg = "Password does not match.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidEmail(email)) {
            String error_msg = "Invalid email format.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidFirstname(firstname)) {
            String error_msg = "Firstname should not be empty";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidMiddleName(middle_name)) {
            String error_msg = "Middle name should not be empty";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidLastname(lastname)) {
            String error_msg = "Lastname should not be empty";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidMobile(mobile)) {
            String error_msg = "Mobile number should start with \"09\" and should be 11 digits in length.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidBrgy(brgy)) {
            String error_msg = "Please select a service location.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidBirthday(birthday)) {
            String error_msg = "Please select your birthday.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidGender(gender)) {
            String error_msg = "Please select a gender.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (profile_img_str.isEmpty()) {
            String error_msg = "Please select a profile picture.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidFrontID() && !isValidBackID()) {
            String error_msg = "Please upload your valid ID.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidFrontID()) {
            String error_msg = "Please upload the front page of your valid ID.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidBackID()) {
            String error_msg = "Please upload the back page of your valid ID.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        return true;
    }

    private void setErrors() {
        String username = String.valueOf(new_account_username_text_field.getText());
        String password = String.valueOf(new_account_pw_text_field.getText());
        String confirm_password = String.valueOf(new_account_confirm_pw_text_field.getText());
        String email = String.valueOf(new_account_email_text_field.getText());
        String firstname = String.valueOf(first_name_text_field.getText());
        String middle_name = String.valueOf(middle_name_text_field.getText());
        String lastname = String.valueOf(last_name_text_field.getText());
        String mobile = String.valueOf(mobile_text_field.getText());
        String brgy = String.valueOf(brgy_textview.getText());
        String birthday = String.valueOf(birthdate_tv.getText());
        String gender = String.valueOf(gender_tv.getText());
        String invalid_fields_str = "";

        if (!isValidUsername(username)) {
            invalid_fields_str = "*Username Required";
        }

        if (!isValidPassword(password)) {
            invalid_fields_str += "\n*Invalid Password";
        }

        if (!isValidConfirmPassword(password, confirm_password)) {
            invalid_fields_str += "\n*Invalid Confirm Password";
        }

        if (!isValidEmail(email)) {
            invalid_fields_str += "\n*Invalid Email";
        }

        if (!isValidFirstname(firstname)) {
            invalid_fields_str += "\n*Firstname Required";
        }

        if (!isValidMiddleName(middle_name)) {
            invalid_fields_str += "\n*Invalid Middle Name";
        }

        if (!isValidLastname(lastname)) {
            invalid_fields_str += "\n*Lastname Required";
        }

        if (!isValidMobile(mobile)) {
            invalid_fields_str += "\n*Invalid Mobile";
        }

        if (!isValidBrgy(brgy)) {
            invalid_fields_str += "\n*Service Location Required";
        }

        if (!isValidBirthday(birthday)) {
            invalid_fields_str += "\n*Birth Date Required";
        }

        if (!isValidGender(gender)) {
            invalid_fields_str += "\n*Gender Required";
        }

        if (profile_img_str.isEmpty()) {
            invalid_fields_str += "\n*Profile Picture Required";
        }

        if (!isValidFrontID() && !isValidBackID()) {
            invalid_fields_str += "\n*ID Required";
        }

        invalid_fields.setText(invalid_fields_str);
    }

    private boolean isValidUsername(String username) {
        String regex_special_char_detection = "^(?=.*[\\/\";<>:\\'(){}\\[\\],._!@#$%^&*+=?-])$";
        return username.trim().length() > 7 && !username.matches(regex_special_char_detection);
    }

    private boolean isValidPassword(String password) {
        String password_pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\/\";<>:\\\'(){}\\[\\],._!@#$%^&*+=?-]).{8,}$";
        return password.matches(password_pattern);
    }

    private boolean isValidConfirmPassword(String password, String confirm_passowrd) {
        return isValidPassword(password) && password.equals(confirm_passowrd);
    }

    private boolean isValidEmail(String email) {
        String email_pattern = "^.*@\\w+([\\.-]?\\w+)(\\.\\w{2,3})+$";
        return email.matches(email_pattern);
    }

    private boolean isValidMobile(String mobile) {
        return mobile.trim().length() == 11 && mobile.trim().startsWith("09");
    }

    private boolean isValidBrgy(String brgy) {
        return !brgy.equals("*Select Location");
    }

    private boolean isValidFirstname(String firstname) {
        String regex_special_char_detection = "^(?=.*[\\/\";<>:\\'(){}\\[\\],._!@#$%^&*+=?-])$";
        return !firstname.trim().isEmpty() && !firstname.matches(regex_special_char_detection);
    }

    private boolean isValidMiddleName(String middle_name) {
        //return !middle_name.trim().isEmpty();
        return true;
    }

    private boolean isValidLastname(String lastname) {
        String regex_special_char_detection = "^(?=.*[\\/\";<>:\\'(){}\\[\\],._!@#$%^&*+=?-])$";
        return !lastname.trim().isEmpty() && !lastname.matches(regex_special_char_detection);
    }

    private boolean isValidBirthday(String birthday) {
        return !birthday.trim().isEmpty();
    }

    private boolean isValidFrontID() {
        return !front_id_str.trim().isEmpty();
    }

    private boolean isValidBackID() {
        return !back_id_str.trim().isEmpty();
    }

    private boolean isValidGender(String gender) {
        return !gender.trim().isEmpty();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, 169);
    }

    private AlertDialog select_id_dialog;
    private ImageView front_id_img, back_id_img;

    private void selectImagePage() {
        View mview = getLayoutInflater().inflate(R.layout.select_id_page, null);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(this);
        mbuilder.setView(mview);
        mbuilder.setCancelable(false);
        mbuilder.setTitle(null);
        select_id_dialog = mbuilder.create();
        select_id_dialog.show();
        select_id_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView close_select_id_btn = mview.findViewById(R.id.close_select_id_btn);
        close_select_id_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_id_dialog.dismiss();
            }
        });

        //Front Image
        front_id_img = mview.findViewById(R.id.front_id_img);
        TextView front_id_btn = mview.findViewById(R.id.front_id_btn);
        front_id_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_front = true;
                selectImage();
            }
        });

        //Back Image
        back_id_img = mview.findViewById(R.id.back_id_img);
        TextView back_id_btn = mview.findViewById(R.id.back_id_btn);
        back_id_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_front = false;
                selectImage();
            }
        });

        TextView submit_btn = mview.findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (front_id_str.trim().isEmpty() || back_id_str.trim().isEmpty()) {
                    Toast.makeText(CustomerRegistrationPage.this, "Please select both front and back image of your valid ID.", Toast.LENGTH_LONG).show();
                } else {
                    select_id_dialog.dismiss();
                }
            }
        });
    }

    private static int RESULT_LOAD_IMG = 169;
    private boolean is_front = true;
    private String front_id_str = "";
    private String back_id_str = "";
    private String profile_img_str = "";

    private boolean isProfileImg = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                if (isProfileImg) {
                    profile_img_str = imgDecodableString;
                } else if (is_front) {
                    front_id_str = imgDecodableString;
                } else {
                    back_id_str = imgDecodableString;
                }

                if (isValidFrontID() && isValidBackID()) {
                    valid_id_btn.setText("Valid ID Submitted");
                    valid_id_btn.setTextColor(Color.parseColor("#008000"));
                    valid_id_btn.setBackgroundResource(R.drawable.green_border);
                }

                if (!profile_img_str.trim().isEmpty()) {
                    profile_img_btn.setText("Profile Picture Submitted");
                    profile_img_btn.setTextColor(Color.parseColor("#008000"));
                    profile_img_btn.setBackgroundResource(R.drawable.green_border);
                }

                if (!isProfileImg) {
                    ImageView imgView = is_front ? front_id_img : back_id_img;
                    imgView.setImageBitmap(BitmapFactory
                            .decodeFile(imgDecodableString));
                }

            } else {
                Toast.makeText(this, "You haven't picked an Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }


    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(CustomerRegistrationPage.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(CustomerRegistrationPage.this,
                    new String[]{permission},
                    requestCode);

            Toast.makeText(this, "Please allow permission to access media files.", Toast.LENGTH_LONG)
                    .show();
        } else {
            if (isProfileImg) {
                is_front = false;
                selectImage();
            } else {
                selectImagePage();
            }
        }
    }


    //Date Picker
    private void datePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CustomerRegistrationPage.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        birthdate_tv.setText((monthOfYear + 1) + "-" + dayOfMonth + "-" + year);

                    }
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(setDateLimit());
        datePickerDialog.show();
    }

    private long setDateLimit() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -18);
        return calendar.getTimeInMillis();

    }

    private int getAge(String birthday) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Calendar calendar = Calendar.getInstance();
        String date_now_str = dateFormat.format(calendar.getTime());
        try {
            Date date = dateFormat.parse(birthday);
            Date date_now = dateFormat.parse(date_now_str);
            long diffInMillies = Math.abs(date_now.getTime() - date.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            int age = Math.toIntExact(diff) / 365;
            return age;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void genderPicker() {
        List<String> gender_dropDown_list = new ArrayList<>();
        gender_dropDown_list.add("");
        gender_dropDown_list.add("Male");
        gender_dropDown_list.add("Female");
        gender_dropDown_list.add("Others");

        Spinner dropdown_gender = findViewById(R.id.dropdown_gender);
        ConstraintLayout gender_btn = findViewById(R.id.gender_btn);
        TextView label_spinner_txt = findViewById(R.id.label_spinner_txt);
        gender_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropdown_gender.performClick();
            }
        });

        label_spinner_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropdown_gender.performClick();
            }
        });

        ArrayAdapter<String> label_dropdown = new ArrayAdapter<String>(CustomerRegistrationPage.this,
                R.layout.spinner_item, gender_dropDown_list);
        label_dropdown.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_gender.setAdapter(label_dropdown);
        dropdown_gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = dropdown_gender.getSelectedItemPosition();
                if (index > 0) {
                    label_spinner_txt.setText(gender_dropDown_list.get(index));
                    gender_tv.setText(gender_dropDown_list.get(index));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getKeyboardEvent(label_spinner_txt, "*Select Gender");
    }

    private void getKeyboardEventBirthdate(TextView editText) {
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!editText.getText().toString().trim().isEmpty()) {
                    birthdate_btn.setText(birthdate_tv.getText().toString());
                    birthdate_btn.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
    }

    private void createAccount() {
        checkExistingUser(new InterfaceCallback() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    checkEmailFirebase();
                } else {
                    loading_screen.dismiss();
                    Toast.makeText(CustomerRegistrationPage.this, "Username already exist.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkEmailFirebase() {
        String username = String.valueOf(new_account_username_text_field.getText()) + "_customer@juanjob.com";
        String password = String.valueOf(new_account_pw_text_field.getText());
        FirebaseAuth firebase_auth = FirebaseAuth.getInstance();
        firebase_auth.createUserWithEmailAndPassword(username.trim(), password.trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loading_screen.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebase_auth.getCurrentUser();
                            createUserCustomer(user.getUid());
                        } else {
                            Toast.makeText(CustomerRegistrationPage.this, "Username already exist.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkExistingUser(InterfaceCallback callback) {
        new Repositories().getAllCustomer(new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(!result.contains(new_account_email_text_field.getText().toString()));
            }
        });
    }

    public interface InterfaceCallback {
        void onSuccess(Boolean result);
    }

    private void createUserCustomer(String user_id) {
        String username = String.valueOf(new_account_username_text_field.getText());
        String password = String.valueOf(new_account_pw_text_field.getText());
        String email = String.valueOf(new_account_email_text_field.getText());
        String firstname = String.valueOf(first_name_text_field.getText());
        String middle_name = String.valueOf(middle_name_text_field.getText());
        String lastname = String.valueOf(last_name_text_field.getText());
        String mobile = String.valueOf(mobile_text_field.getText());
        String address = String.valueOf(brgy_textview.getText());
        String birthday = String.valueOf(birthdate_tv.getText());
        String gender = String.valueOf(gender_tv.getText());
        String front_id_base64 = bitmap_str(getApplicationContext(), front_id_str);
        String back_id_base64 = bitmap_str(getApplicationContext(), back_id_str);
        String profile_id_base64 = bitmap_str(getApplicationContext(), profile_img_str);
        int age = getAge(birthday);

        new Repositories().createCustomer(new CustomerTable(firstname, middle_name, lastname, address,
                mobile, username, password, birthday, age, front_id_base64, back_id_base64, email, gender,
                profile_id_base64, address, "Unverified", "Inactive"), user_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (result.equals("success")) {
                    Toast.makeText(getApplicationContext(), "Registration Success", Toast.LENGTH_LONG).show();
                    accountVerificationPage();
                } else {
                    Toast.makeText(CustomerRegistrationPage.this, "Username already exist.",
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

    private List<String> brgy_list = new ArrayList<>();

    private void getBrgyList() {
        brgy_list.add("");
        brgy_list.add("Bancal");
        brgy_list.add("Barangay 1");
        brgy_list.add("Barangay 2");
        brgy_list.add("Barangay 3");
        brgy_list.add("Barangay 4");
        brgy_list.add("Barangay 5");
        brgy_list.add("Barangay 6");
        brgy_list.add("Barangay 7");
        brgy_list.add("Barangay 8");
        brgy_list.add("Cabilang Baybay");
        brgy_list.add("Lantic");
        brgy_list.add("Mabuhay");
        brgy_list.add("Maduya");
        brgy_list.add("Milagrosa");
    }

    private void brgyPicker() {
        Spinner brgy_dropdown = findViewById(R.id.brgy_dropdown);
        ConstraintLayout brgy_box = findViewById(R.id.brgy_box);
        TextView brgy_textview = findViewById(R.id.brgy_textview);
        brgy_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brgy_dropdown.performClick();
            }
        });

        brgy_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                brgy_dropdown.performClick();
            }
        });

        ArrayAdapter<String> brgy_adapter = new ArrayAdapter<String>(CustomerRegistrationPage.this,
                R.layout.spinner_item, brgy_list);
        brgy_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brgy_dropdown.setAdapter(brgy_adapter);
        brgy_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = brgy_dropdown.getSelectedItemPosition();
                if (index > 0) {
                    brgy_textview.setText(brgy_list.get(index));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void getKeyboardEvent(TextView textView, String hint) {
        textView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = textView.getText().toString().trim();
                if (!text.isEmpty() && !text.equals(hint.trim())) {
                    textView.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
    }

    private void storeLogin(String login_id) {
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("login_id", login_id);
        editor.commit();
    }
}
