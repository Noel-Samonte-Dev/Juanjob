package com.juanjob.app.account.client;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.juanjob.app.R;
import com.juanjob.app.account.CustomerRegistrationPage;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.ToastMsg;

public class ClientRegistrationPage extends AppCompatActivity {
    private TextView birthdate_tv, gender_tv, invalid_fields, brgy_textview, category_textview, sub_category_textview;
    private Button birthdate_btn, profile_img_btn;
    private TextInputLayout other_category_box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.client_registration_page);

        other_category_box = findViewById(R.id.other_category_box);
        invalid_fields = findViewById(R.id.invalid_fields);
        sub_category_textview = findViewById(R.id.sub_category_textview);
        category_textview = findViewById(R.id.category_textview);
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
        address_text_field = findViewById(R.id.address_text_field);
        brgy_textview = findViewById(R.id.brgy_textview);
        other_category_text_field = findViewById(R.id.other_category_text_field);

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

        Button create_account_btn = findViewById(R.id.create_account_btn0);
        create_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInputs()) {
                    goToNextPage();
                }
            }
        });

        getBrgyList();
        brgyPicker();
        categoryPicker();
        getKeyboardEvent(brgy_textview, "*Select Service Location");
        getKeyboardEventOtherCategory(category_textview);
    }


    private TextInputEditText new_account_username_text_field, new_account_pw_text_field, new_account_confirm_pw_text_field, new_account_email_text_field,
            first_name_text_field, middle_name_text_field, last_name_text_field, mobile_text_field, address_text_field, other_category_text_field;

    private boolean isValidInputs() {
        String username = String.valueOf(new_account_username_text_field.getText());
        String password = String.valueOf(new_account_pw_text_field.getText());
        String confirm_password = String.valueOf(new_account_confirm_pw_text_field.getText());
        String email = String.valueOf(new_account_email_text_field.getText());
        String firstname = String.valueOf(first_name_text_field.getText());
        String middle_name = String.valueOf(middle_name_text_field.getText());
        String lastname = String.valueOf(last_name_text_field.getText());
        String mobile = String.valueOf(mobile_text_field.getText());
        String address = String.valueOf(address_text_field.getText());
        String brgy = String.valueOf(brgy_textview.getText());
        String birthday = String.valueOf(birthdate_tv.getText());
        String gender = String.valueOf(gender_tv.getText());
        String category = String.valueOf(category_textview.getText());
        String subcategory = String.valueOf(sub_category_textview.getText());
        String other_category = String.valueOf(other_category_text_field.getText());

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

        if (!isValidAddress(address)) {
            String error_msg = "Address should not be empty.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidBrgy(brgy)) {
            String error_msg = "Please select a service location.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidCategory(category)) {
            String error_msg = "Please select a service category.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (!isValidSubCategory(subcategory)) {
            String error_msg = "Please select a service subcategory.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (category.equals("Others") && !isValidOtherCategory(other_category)) {
            String error_msg = "Please enter your service offered.";
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
        String address = String.valueOf(address_text_field.getText());
        String brgy = String.valueOf(brgy_textview.getText());
        String birthday = String.valueOf(birthdate_tv.getText());
        String gender = String.valueOf(gender_tv.getText());
        String category = String.valueOf(category_textview.getText());
        String subcategory = String.valueOf(sub_category_textview.getText());
        String other_category = String.valueOf(other_category_text_field.getText());
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

        if (!isValidAddress(address)) {
            invalid_fields_str += "\n*Address Required";
        }

        if (!isValidBrgy(brgy)) {
            invalid_fields_str += "\n*Service Location Required";
        }

        if (!isValidCategory(category)) {
            invalid_fields_str += "\n*Service Category Required";
        }

        if (!isValidSubCategory(subcategory)) {
            invalid_fields_str += "\n*Service Subcategory Required";
        }

        if (category.equals("Others") && !isValidOtherCategory(other_category)) {
            invalid_fields_str += "\n*Invalid Service Offered";
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

    private boolean isValidAddress(String address) {
        return !address.trim().isEmpty();
    }

    private boolean isValidBrgy(String brgy) {
        return !brgy.equals("*Select Service Location");
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

    private boolean isValidGender(String gender) {
        return !gender.trim().isEmpty();
    }

    private boolean isValidCategory(String category) {
        return !category.equals("*Select Service Category");
    }

    private boolean isValidSubCategory(String category) {
        return !category.equals("*Select Service Subcategory") && !category.trim().isEmpty() ;
    }

    private boolean isValidOtherCategory(String other_category) {
        return !other_category.trim().isEmpty();
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, 169);
    }

    private static int RESULT_LOAD_IMG = 169;
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
                }

                if (!profile_img_str.trim().isEmpty()) {
                    profile_img_btn.setText("Profile Picture Submitted");
                    profile_img_btn.setTextColor(Color.parseColor("#008000"));
                    profile_img_btn.setBackgroundResource(R.drawable.green_border);
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
        if (ContextCompat.checkSelfPermission(ClientRegistrationPage.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(ClientRegistrationPage.this,
                    new String[]{permission},
                    requestCode);

            Toast.makeText(this, "Please allow permission to access media files.", Toast.LENGTH_LONG)
                    .show();
        } else {
            selectImage();
        }
    }


    //Date Picker
    private void datePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ClientRegistrationPage.this,
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

        ArrayAdapter<String> label_dropdown = new ArrayAdapter<String>(ClientRegistrationPage.this,
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

        ArrayAdapter<String> brgy_adapter = new ArrayAdapter<String>(ClientRegistrationPage.this,
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

    private void getKeyboardEventOtherCategory(TextView textView) {
        textView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = textView.getText().toString().trim();
                ViewGroup.LayoutParams pm = other_category_box.getLayoutParams();
                if (text.equals("Others")) {
                    pm.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    pm.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                } else {
                    pm.width = 1;
                    pm.height = 1;
                }
                other_category_box.setLayoutParams(pm);
            }
        });
    }

    private void goToNextPage() {
        checkExistingUser(new CustomerRegistrationPage.InterfaceCallback() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    goIntent();
                } else {
                    Toast.makeText(ClientRegistrationPage.this, "Username already exist.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goIntent() {
        String username = String.valueOf(new_account_username_text_field.getText());
        String password = String.valueOf(new_account_pw_text_field.getText());
        String email = String.valueOf(new_account_email_text_field.getText());
        String firstname = String.valueOf(first_name_text_field.getText());
        String middle_name = String.valueOf(middle_name_text_field.getText());
        String lastname = String.valueOf(last_name_text_field.getText());
        String mobile = String.valueOf(mobile_text_field.getText());
        String address = String.valueOf(address_text_field.getText());
        String birthday = String.valueOf(birthdate_tv.getText());
        String gender = String.valueOf(gender_tv.getText());
        int age = getAge(birthday);
        String service_location = String.valueOf(brgy_textview.getText());
        String category = String.valueOf(category_textview.getText());
        String subcategory = String.valueOf(sub_category_textview.getText());
        String other_category = String.valueOf(other_category_text_field.getText());
        String profile_id_base64 = BitmapHelper.bitmap_str(getApplicationContext(), profile_img_str);

        Intent i = new Intent(this, ClientRegistrationNextPage.class);
        i.putExtra("username", username);
        i.putExtra("password", password);
        i.putExtra("email", email);
        i.putExtra("firstname", firstname);
        i.putExtra("middle_name", middle_name);
        i.putExtra("lastname", lastname);
        i.putExtra("mobile", mobile);
        i.putExtra("address", address);
        i.putExtra("birthday", address);
        i.putExtra("gender", gender);
        i.putExtra("age", age);
        i.putExtra("service_location", service_location);
        i.putExtra("category", category);
        i.putExtra("subcategory", subcategory);
        i.putExtra("other_category", other_category);
        i.putExtra("profile_id_base64", profile_id_base64);
        startActivity(i);
    }

    private void checkExistingUser(CustomerRegistrationPage.InterfaceCallback callback) {
        new Repositories().getAllCustomer(new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(!result.contains(new_account_email_text_field.getText().toString()));
            }
        });
    }

    private List<String> sub_category_list;

    private void categoryPicker() {
        sub_category_list = new ArrayList<>();
        List<String> category_dropDown_list = new ArrayList<>();
        category_dropDown_list.add("Select a category!");
        category_dropDown_list.add("Electronics");
        category_dropDown_list.add("Personal Care");
        category_dropDown_list.add("Household Maintenance");
        category_dropDown_list.add("Mechanical");

        Spinner dropdown_category = findViewById(R.id.category_dropdown);
        ConstraintLayout category_btn = findViewById(R.id.category_box);
        category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sub_category_textview.setText("*Select Service Subcategory");
                sub_category_textview.setTextColor(Color.parseColor("#ADABAB"));
                dropdown_category.performClick();
            }
        });

        category_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sub_category_textview.setText("*Select Service Subcategory");
                sub_category_textview.setTextColor(Color.parseColor("#ADABAB"));
                dropdown_category.performClick();
            }
        });

        ArrayAdapter<String> label_dropdown = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.spinner_item, category_dropDown_list);
        label_dropdown.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_category.setAdapter(label_dropdown);
        dropdown_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = dropdown_category.getSelectedItemPosition();
                if (index > 0) {
                    category_textview.setText(category_dropDown_list.get(index));
                    category_textview.setTextColor(Color.parseColor("#000000"));
                    category_btn.setBackgroundResource(R.drawable.gray_border);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Spinner dropdown_category_sub = findViewById(R.id.sub_category_dropdown);
        ConstraintLayout category_btn_sub = findViewById(R.id.sub_category_box);
        category_btn_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!category_textview.getText().toString().trim().isEmpty()) {
                    sub_category_list = new ArrayList<>();
                    List<String> sub_category_electronics = new ArrayList<>();
                    sub_category_electronics.add("Select a subcategory!");
                    sub_category_electronics.add("PC/Laptop Repair");
                    sub_category_electronics.add("Cellphone Repair");
                    sub_category_electronics.add("Appliances Repair");

                    List<String> sub_category_salon = new ArrayList<>();
                    sub_category_salon.add("Select a subcategory!");
                    sub_category_salon.add("Haircut/Salon");
                    sub_category_salon.add("Manicure/Pedicure");
                    sub_category_salon.add("Massage");

                    List<String> sub_category_household = new ArrayList<>();
                    sub_category_household.add("Select a subcategory!");
                    sub_category_household.add("Plumbing");
                    sub_category_household.add("House Wiring");
                    sub_category_household.add("House Cleaning");
                    sub_category_household.add("Laundry");

                    List<String> sub_category_mechanical = new ArrayList<>();
                    sub_category_mechanical.add("Select a subcategory!");
                    sub_category_mechanical.add("Automobile Repair");
                    sub_category_mechanical.add("Motorcycle Repair");
                    sub_category_mechanical.add("Bicycle Repair");

                    if (category_textview.getText().toString().equals("Electronics")) {
                        sub_category_list = sub_category_electronics;
                    }

                    if (category_textview.getText().toString().equals("Personal Care")) {
                        sub_category_list = sub_category_salon;
                    }

                    if (category_textview.getText().toString().equals("Household Maintenance")) {
                        sub_category_list = sub_category_household;
                    }

                    if (category_textview.getText().toString().equals("Mechanical")) {
                        sub_category_list = sub_category_mechanical;
                    }

                    ArrayAdapter<String> label_dropdown_sub = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.spinner_item, sub_category_list);
                    label_dropdown_sub.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dropdown_category_sub.setAdapter(label_dropdown_sub);
                    dropdown_category_sub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            int index = dropdown_category_sub.getSelectedItemPosition();
                            if (index > 0) {
                                sub_category_textview.setText(sub_category_list.get(index));
                                sub_category_textview.setTextColor(Color.parseColor("#000000"));
                                category_btn_sub.setBackgroundResource(R.drawable.gray_border);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });

                    dropdown_category_sub.performClick();
                } else {
                    Toast.makeText(getApplicationContext(), "Please select a category first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sub_category_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!category_textview.getText().toString().trim().isEmpty()) {
                    sub_category_list = new ArrayList<>();
                    List<String> sub_category_electronics = new ArrayList<>();
                    sub_category_electronics.add("Select a subcategory!");
                    sub_category_electronics.add("PC/Laptop Repair");
                    sub_category_electronics.add("Cellphone Repair");
                    sub_category_electronics.add("Appliances Repair");

                    List<String> sub_category_salon = new ArrayList<>();
                    sub_category_salon.add("Select a subcategory!");
                    sub_category_salon.add("Haircut/Salon");
                    sub_category_salon.add("Manicure/Pedicure");
                    sub_category_salon.add("Massage");

                    List<String> sub_category_household = new ArrayList<>();
                    sub_category_household.add("Select a subcategory!");
                    sub_category_household.add("Plumbing");
                    sub_category_household.add("House Wiring");
                    sub_category_household.add("House Cleaning");
                    sub_category_household.add("Laundry");

                    List<String> sub_category_mechanical = new ArrayList<>();
                    sub_category_mechanical.add("Select a subcategory!");
                    sub_category_mechanical.add("Automobile Repair");
                    sub_category_mechanical.add("Motorcycle Repair");
                    sub_category_mechanical.add("Bicycle Repair");

                    if (category_textview.getText().toString().equals("Electronics")) {
                        sub_category_list = sub_category_electronics;
                    }

                    if (category_textview.getText().toString().equals("Personal Care")) {
                        sub_category_list = sub_category_salon;
                    }

                    if (category_textview.getText().toString().equals("Household Maintenance")) {
                        sub_category_list = sub_category_household;
                    }

                    if (category_textview.getText().toString().equals("Mechanical")) {
                        sub_category_list = sub_category_mechanical;
                    }

                    ArrayAdapter<String> label_dropdown_sub = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.spinner_item, sub_category_list);
                    label_dropdown_sub.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dropdown_category_sub.setAdapter(label_dropdown_sub);
                    dropdown_category_sub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            int index = dropdown_category_sub.getSelectedItemPosition();
                            if (index > 0) {
                                sub_category_textview.setText(sub_category_list.get(index));
                                sub_category_textview.setTextColor(Color.parseColor("#000000"));
                                category_btn_sub.setBackgroundResource(R.drawable.gray_border);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });

                    dropdown_category_sub.performClick();
                } else {
                    Toast.makeText(getApplicationContext(), "Please select a category first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Category Picker With Radio Buttons
    private void initCategory() {
        ConstraintLayout category_btn = findViewById(R.id.category_box);
        category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPicker();
            }
        });

        category_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPicker();
            }
        });
    }

    private void categoryPickerv2() {
        View mview = View.inflate(this, R.layout.category_selection_page, null);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        mbuilder.setView(mview);
        mbuilder.setCancelable(false);
        mbuilder.setTitle(null);
        AlertDialog dialog = mbuilder.create();
        dialog.show();

        selected_category = mview.findViewById(R.id.selected_category_tv);

        Button select_category_btn = mview.findViewById(R.id.select_category);
        select_category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selected_category.getText().toString().trim().isEmpty()) {
                    category_textview.setText(selected_category.getText().toString());
                    category_textview.setTextColor(Color.parseColor("#000000"));
                    dialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView bank_close = mview.findViewById(R.id.close_btn);
        bank_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        rg1 = new RadioGroup(getApplicationContext());
        rg2 = new RadioGroup(getApplicationContext());
        rg3 = new RadioGroup(getApplicationContext());
        rg4 = new RadioGroup(getApplicationContext());

        List<String> sub_category_electronics = new ArrayList<>();
        sub_category_electronics.add("PC/Laptop Repair");
        sub_category_electronics.add("Cellphone Repair");
        sub_category_electronics.add("Appliances Repair");
        LinearLayoutCompat radiogroup_1 = mview.findViewById(R.id.radiogroup_1);
        createRadioButton(getApplicationContext(), sub_category_electronics, radiogroup_1, rg1, 1);

        List<String> sub_category_salon = new ArrayList<>();
        sub_category_salon.add("Haircut/Salon");
        sub_category_salon.add("Manicure/Pedicure");
        sub_category_salon.add("Massage");
        LinearLayoutCompat radiogroup_2 = mview.findViewById(R.id.radiogroup_2);
        createRadioButton(getApplicationContext(), sub_category_salon, radiogroup_2, rg2, 2);

        List<String> sub_category_household = new ArrayList<>();
        sub_category_household.add("Plumbing");
        sub_category_household.add("House Wiring");
        sub_category_household.add("House Cleaning");
        sub_category_household.add("Laundry");
        LinearLayoutCompat radiogroup_3 = mview.findViewById(R.id.radiogroup_3);
        createRadioButton(getApplicationContext(), sub_category_household, radiogroup_3, rg3, 3);

        List<String> sub_category_mechanical = new ArrayList<>();
        sub_category_mechanical.add("Automobile Repair");
        sub_category_mechanical.add("Motorcycle Repair");
        sub_category_mechanical.add("Bicycle Repair");
        LinearLayoutCompat radiogroup_4 = mview.findViewById(R.id.radiogroup_4);
        createRadioButton(getApplicationContext(), sub_category_mechanical, radiogroup_4, rg4, 4);

    }

    private RadioGroup rg1, rg2, rg3, rg4;
    private TextView selected_category;

    private void createRadioButton(Context context, List<String> List, LinearLayoutCompat rg, RadioGroup radio_group, int index) {
        final RadioButton[] rb = new RadioButton[List.size()];
        radio_group.setOrientation(RadioGroup.VERTICAL);
        context.setTheme(R.style.RadioButtonStyle);
        for (int i = 0; i < List.size(); i++) {
            rb[i] = new RadioButton(context);
            String str = List.get(i);
            rb[i].setText(str);
            rb[i].setTextColor(Color.parseColor("#000000"));
            rb[i].setId(i + 101);
            rb[i].setTextSize(21);
            rb[i].setPadding(0, 0, 0, 0);
            radio_group.addView(rb[i]);
            int a = i;
            rb[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (index == 1) {
                        rg2.clearCheck();
                        rg3.clearCheck();
                        rg4.clearCheck();
                    }

                    if (index == 2) {
                        rg1.clearCheck();
                        rg3.clearCheck();
                        rg4.clearCheck();
                    }

                    if (index == 3) {
                        rg1.clearCheck();
                        rg2.clearCheck();
                        rg4.clearCheck();
                    }

                    if (index == 4) {
                        rg1.clearCheck();
                        rg2.clearCheck();
                        rg3.clearCheck();
                    }

                    selected_category.setText(List.get(a));
                }
            });
        }

        rg.addView(radio_group);
    }
}
