package com.juanjob.app.customer.profile;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.juanjob.app.helpers.BitmapHelper.bitmap_str;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import com.juanjob.app.R;
import com.juanjob.app.database.CustomerTable;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerEditProfilePage extends Fragment {
    private TextInputEditText first_name_text_field, middle_name_text_field, last_name_text_field, mobile_text_field, edit_profile_email_text_field;
    private String customer_id;
    private TextView profile_img_tv, gender_spinner_txt, gender_tv, invalid_fields, brgy_textview;
    private AlertDialog loading;
    private Button profile_img_btn, save_btn;
    private CircleImageView profile_img_circular;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.customer_edit_profile_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading = new LoadingPage().loadingGif(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        customer_id = sp.getString("login_id", "");

        brgy_textview = v.findViewById(R.id.brgy_textview);
        profile_img_btn = v.findViewById(R.id.profile_img_btn);
        gender_tv = v.findViewById(R.id.gender_tv);
        gender_spinner_txt = v.findViewById(R.id.gender_spinner_txt);
        profile_img_tv = v.findViewById(R.id.profile_img_tv);
        first_name_text_field = v.findViewById(R.id.first_name_text_field);
        middle_name_text_field = v.findViewById(R.id.middle_name_text_field);
        last_name_text_field = v.findViewById(R.id.last_name_text_field);
        invalid_fields = v.findViewById(R.id.invalid_fields);
        mobile_text_field = v.findViewById(R.id.mobile_text_field);
        edit_profile_email_text_field = v.findViewById(R.id.edit_profile_email_text_field);
        save_btn = v.findViewById(R.id.save_btn);

        ImageView back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        profile_img_circular = v.findViewById(R.id.profile_img_circular);
        profile_img_circular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptChangeImage();
            }
        });

        profile_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkPermission();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInputs()) {
                    updateProfile();
                }
            }
        });

        getProfileInfo();
        genderPicker(v);
    }

    private void getProfileInfo() {
        loading.show();
        new Repositories().getCustomer(customer_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String address_str = String.valueOf(obj.get("address"));
                        String gender_str = String.valueOf(obj.get("gender"));
                        String email_str = String.valueOf(obj.get("email"));
                        String mobile_str = String.valueOf(obj.get("mobile"));
                        String profile_img_str = String.valueOf(obj.get("profile_img"));
                        String firstname = String.valueOf(obj.get("firstname"));
                        String lastname = String.valueOf(obj.get("lastname"));
                        String middle_name = String.valueOf(obj.get("middle_name"));

                        setProfileImage(profile_img_str);
                        profile_img_tv.setText(profile_img_str);
                        first_name_text_field.setText(firstname);
                        middle_name_text_field.setText(middle_name);
                        last_name_text_field.setText(lastname);
                        gender_spinner_txt.setText(gender_str);
                        gender_tv.setText(gender_str);
                        brgy_textview.setText(address_str);
                        mobile_text_field.setText(mobile_str);
                        edit_profile_email_text_field.setText(email_str);


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                loading.dismiss();
            }
        });
    }

    private void updateProfile() {
        loading.show();
        new Repositories().getCustomer(customer_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String account_status = String.valueOf(obj.get("account_status"));
                        String online_status = String.valueOf(obj.get("online_status"));
                        String username = String.valueOf(obj.get("username"));
                        String password = String.valueOf(obj.get("password"));
                        String birthday = String.valueOf(obj.get("birthday"));
                        String front_id = String.valueOf(obj.get("front_id"));
                        String back_id = String.valueOf(obj.get("back_id"));
                        int age = obj.getInt("age");

                        String email = String.valueOf(edit_profile_email_text_field.getText());
                        String firstname = String.valueOf(first_name_text_field.getText());
                        String middle_name = String.valueOf(middle_name_text_field.getText());
                        String lastname = String.valueOf(last_name_text_field.getText());
                        String mobile = String.valueOf(mobile_text_field.getText());
                        String address = String.valueOf(brgy_textview.getText());
                        String gender = String.valueOf(gender_tv.getText());
                        String profile_img_str = String.valueOf(profile_img_tv.getText());

                        CustomerTable customer = new CustomerTable(firstname, middle_name, lastname, address, mobile,
                                username, password, birthday, age, front_id, back_id, email, gender, profile_img_str,
                                address, account_status, online_status);
                        new Repositories().updateCustomer(customer, customer_id, new Repositories.RepoCallback() {
                            @Override
                            public void onSuccess(String result) {
                                loading.dismiss();
                                getParentFragmentManager().popBackStack();
                                reloadProfilePage();
                            }
                        });

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                loading.dismiss();
            }
        });
    }

    private void updateProfile1(AlertDialog prompt) {
        loading.show();
        new Repositories().getCustomer(customer_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String account_status = String.valueOf(obj.get("account_status"));
                        String online_status = String.valueOf(obj.get("online_status"));
                        String username = String.valueOf(obj.get("username"));
                        String password = String.valueOf(obj.get("password"));
                        String birthday = String.valueOf(obj.get("birthday"));
                        String front_id = String.valueOf(obj.get("front_id"));
                        String back_id = String.valueOf(obj.get("back_id"));
                        int age = obj.getInt("age");

                        String email = String.valueOf(edit_profile_email_text_field.getText());
                        String firstname = String.valueOf(first_name_text_field.getText());
                        String middle_name = String.valueOf(middle_name_text_field.getText());
                        String lastname = String.valueOf(last_name_text_field.getText());
                        String mobile = String.valueOf(mobile_text_field.getText());
                        String address = String.valueOf(brgy_textview.getText());
                        String gender = String.valueOf(gender_tv.getText());
                        String profile_img_str = String.valueOf(profile_img_tv.getText());

                        CustomerTable customer = new CustomerTable(firstname, middle_name, lastname, address, mobile,
                                username, password, birthday, age, front_id, back_id, email, gender, profile_img_str,
                                address, account_status, online_status);
                        new Repositories().updateCustomer(customer, customer_id, new Repositories.RepoCallback() {
                            @Override
                            public void onSuccess(String result) {
                                prompt.dismiss();
                                loading.dismiss();
                                getParentFragmentManager().popBackStack();
                                reloadProfilePage();
                            }
                        });

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                loading.dismiss();
            }
        });
    }

    private void reloadProfilePage() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.customer_fragmentContainerView, new CustomerProfilePage(), "profile_page").commit();
    }

    private static int RESULT_LOAD_IMG = 169;
    private String imgDecodableString;
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, 169);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                String bitmap_str_img = bitmap_str(requireContext(), imgDecodableString);
                profile_img_tv.setText(bitmap_str_img);

                setProfileImage(bitmap_str_img);

                profile_img_btn.setText("Image Selected");
                profile_img_btn.setTextColor(Color.parseColor("#008000"));
                profile_img_btn.setBackgroundResource(R.drawable.green_border);

            } else {
                Toast.makeText(getActivity(), "You haven't picked an Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void checkPermission() {
        String permission = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        int requestCode = 923;

        if (ContextCompat.checkSelfPermission(getContext(), permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{permission},
                    requestCode);

            Toast.makeText(getContext(), "Please allow permission to access media files.", Toast.LENGTH_LONG)
                    .show();
        } else {
            selectImage();
        }
    }

    private void genderPicker(View v) {
        List<String> gender_dropDown_list = new ArrayList<>();
        gender_dropDown_list.add("");
        gender_dropDown_list.add("Male");
        gender_dropDown_list.add("Female");
        gender_dropDown_list.add("Others");

        Spinner dropdown_gender = v.findViewById(R.id.dropdown_gender);
        ConstraintLayout gender_btn = v.findViewById(R.id.gender_btn);
        gender_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropdown_gender.performClick();
            }
        });

        gender_spinner_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropdown_gender.performClick();
            }
        });

        ArrayAdapter<String> label_dropdown = new ArrayAdapter<String>(getContext(),
                R.layout.spinner_item, gender_dropDown_list);
        label_dropdown.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_gender.setAdapter(label_dropdown);
        dropdown_gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = dropdown_gender.getSelectedItemPosition();
                if (index > 0) {
                    gender_spinner_txt.setText(gender_dropDown_list.get(index));
                    gender_tv.setText(gender_dropDown_list.get(index));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getKeyboardEvent(gender_spinner_txt, "*Select Gender");
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

    private boolean isValidInputs() {
        String email = String.valueOf(edit_profile_email_text_field.getText());
        String firstname = String.valueOf(first_name_text_field.getText());
        String middle_name = String.valueOf(middle_name_text_field.getText());
        String lastname = String.valueOf(last_name_text_field.getText());
        String mobile = String.valueOf(mobile_text_field.getText());
        String brgy = String.valueOf(brgy_textview.getText());
        String gender = String.valueOf(gender_tv.getText());

        setErrors();

        if (!isValidEmail(email)) {
            String error_msg = "Invalid email format.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidFirstname(firstname)) {
            String error_msg = "Firstname should not be empty";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidMiddleName(middle_name)) {
            String error_msg = "Middle name should not be empty";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidLastname(lastname)) {
            String error_msg = "Lastname should not be empty";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidMobile(mobile)) {
            String error_msg = "Mobile number should start with \"09\" and should be 11 digits in length.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidBrgy(brgy)) {
            String error_msg = "Please select a service location.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidGender(gender)) {
            String error_msg = "Please select a gender.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        return true;
    }

    private void setErrors() {
        String email = String.valueOf(edit_profile_email_text_field.getText());
        String firstname = String.valueOf(first_name_text_field.getText());
        String middle_name = String.valueOf(middle_name_text_field.getText());
        String lastname = String.valueOf(last_name_text_field.getText());
        String mobile = String.valueOf(mobile_text_field.getText());
        String brgy = String.valueOf(brgy_textview.getText());
        String gender = String.valueOf(gender_tv.getText());
        String invalid_fields_str = "";

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
            invalid_fields_str += "\n*Invalid Service Location";
        }

        if (!isValidGender(gender)) {
            invalid_fields_str += "\n*Invalid Gender";
        }

        invalid_fields.setText(invalid_fields_str);
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
        return !firstname.trim().isEmpty();
    }

    private boolean isValidMiddleName(String middle_name) {
       // return !middle_name.trim().isEmpty();
        return true;
    }

    private boolean isValidLastname(String lastname) {
        return !lastname.trim().isEmpty();
    }

    private boolean isValidGender(String gender) {
        return !gender.trim().isEmpty();
    }

    private void popBackStack() {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(requireContext());
        View mview = getLayoutInflater().inflate(R.layout.prompt_dialog, null);
        mbuilder.setView(mview);
        AlertDialog dialog = mbuilder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button close = mview.findViewById(R.id.btn_cancel_logout);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                getParentFragmentManager().popBackStack();
            }
        });

        Button proceed = mview.findViewById(R.id.btn_proceed_logout);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile1(dialog);
            }
        });

        TextView logout_text = mview.findViewById(R.id.prompt_text);
        logout_text.setText("Do you want to save the changes?");
    }

    private void promptChangeImage() {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(requireContext());
        View mview = getLayoutInflater().inflate(R.layout.prompt_dialog, null);
        mbuilder.setView(mview);
        AlertDialog dialog = mbuilder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button close = mview.findViewById(R.id.btn_cancel_logout);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button proceed = mview.findViewById(R.id.btn_proceed_logout);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        TextView logout_text = mview.findViewById(R.id.prompt_text);
        logout_text.setText("Do you want to change your profile picture?");
    }

    private void setProfileImage(String img_str) {
        Bitmap bitmap = BitmapHelper.urlStrToBitmap(img_str);
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .priority(Priority.HIGH)
                .format(DecodeFormat.PREFER_RGB_565);

        Glide.with(requireContext())
                .applyDefaultRequestOptions(options)
                .asBitmap()
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.default_img)
                .into(profile_img_circular);
    }
}
