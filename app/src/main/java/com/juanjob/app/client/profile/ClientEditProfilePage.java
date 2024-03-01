package com.juanjob.app.client.profile;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.juanjob.app.helpers.BitmapHelper.bitmap_str;
import android.Manifest;
import android.content.Context;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
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
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.juanjob.app.R;
import com.juanjob.app.database.ClientTable;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClientEditProfilePage extends Fragment {
    private TextInputEditText first_name_text_field, middle_name_text_field, last_name_text_field, address_text_field,
            mobile_text_field, edit_profile_email_text_field, other_category_text_field;
    private TextInputLayout other_category_box;
    private TextView gender_spinner_txt, gender_tv, brgy_textview, category_textview, profile_img_tv, invalid_fields;
    private String client_id;
    private AlertDialog loading;
    private Button save, profile_img_btn;
    private CircleImageView profile_img_circular;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.client_edit_profile_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading = new LoadingPage().loadingGif(getActivity());
        loading.show();

        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        client_id = sp.getString("login_id", "");

        ImageView back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        profile_img_tv = v.findViewById(R.id.profile_img_tv);
        gender_spinner_txt = v.findViewById(R.id.label_spinner_txt);
        gender_tv = v.findViewById(R.id.gender_tv);
        brgy_textview = v.findViewById(R.id.brgy_textview);
        category_textview = v.findViewById(R.id.category_textview);
        other_category_text_field = v.findViewById(R.id.other_category_text_field);
        other_category_box = v.findViewById(R.id.other_category_box);

        first_name_text_field = v.findViewById(R.id.first_name_text_field);
        middle_name_text_field = v.findViewById(R.id.middle_name_text_field);
        last_name_text_field = v.findViewById(R.id.last_name_text_field);
        address_text_field = v.findViewById(R.id.address_text_field);
        mobile_text_field = v.findViewById(R.id.mobile_text_field);
        edit_profile_email_text_field = v.findViewById(R.id.edit_profile_email_text_field);
        invalid_fields = v.findViewById(R.id.invalid_fields);

        profile_img_circular = v.findViewById(R.id.profile_img_circular);
        profile_img_circular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptChangeImage();
            }
        });

        profile_img_btn = v.findViewById(R.id.profile_img_btn);
        profile_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkPermission();
            }
        });

        save = v.findViewById(R.id.save_btn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    updateServicesLocation();
                }
            }
        });

        getProfileInfo();
        genderPicker(v);
        brgyPicker(v);
        getKeyboardEventOtherCategory(category_textview);
    }

    private boolean isValidInput() {
        String email = String.valueOf(edit_profile_email_text_field.getText());
        String firstname = String.valueOf(first_name_text_field.getText());
        String middle_name = String.valueOf(middle_name_text_field.getText());
        String lastname = String.valueOf(last_name_text_field.getText());
        String mobile = String.valueOf(mobile_text_field.getText());
        String address = String.valueOf(address_text_field.getText());
        String brgy = String.valueOf(brgy_textview.getText());
        String category = String.valueOf(category_textview.getText());
        String other_category = String.valueOf(other_category_text_field.getText());

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

        if (!isValidAddress(address)) {
            String error_msg = "Address should not be empty.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidBrgy(brgy)) {
            String error_msg = "Please select a service location.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidCategory(category)) {
            String error_msg = "Please select a service category.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (category.equals("Others") && !isValidOtherCategory(other_category)) {
            String error_msg = "Please enter your service offered.";
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
        String address = String.valueOf(address_text_field.getText());
        String brgy = String.valueOf(brgy_textview.getText());
        String category = String.valueOf(category_textview.getText());
        String other_category = String.valueOf(other_category_text_field.getText());
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

        if (!isValidAddress(address)) {
            invalid_fields_str += "\n*Invalid Address";
        }

        if (!isValidBrgy(brgy)) {
            invalid_fields_str += "\n*Invalid Service Location";
        }

        if (!isValidCategory(category)) {
            invalid_fields_str += "\n*Invalid Service Category";
        }

        if (category.equals("Others") && !isValidOtherCategory(other_category)) {
            invalid_fields_str += "\n*Invalid Service Offered";
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

    private boolean isValidAddress(String address) {
        return !address.trim().isEmpty();
    }

    private boolean isValidBrgy(String brgy) {
        return !brgy.equals("*Select Service Location");
    }

    private boolean isValidFirstname(String firstname) {
        return !firstname.trim().isEmpty();
    }

    private boolean isValidMiddleName(String middle_name) {
        //return !middle_name.trim().isEmpty();
        return true;
    }

    private boolean isValidLastname(String lastname) {
        return !lastname.trim().isEmpty();
    }

    private boolean isValidCategory(String category) {
        return !category.equals("*Select Service Category");
    }

    private boolean isValidOtherCategory(String other_category) {
        return !other_category.trim().isEmpty();
    }

    private void getProfileInfo() {
        new Repositories().getClient(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String category_str = String.valueOf(obj.get("category"));
                        String other_category_str = String.valueOf(obj.get("other_category"));
                        String service_location_str = String.valueOf(obj.get("service_location"));
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
                        address_text_field.setText(address_str);
                        mobile_text_field.setText(mobile_str);
                        edit_profile_email_text_field.setText(email_str);
                        brgy_textview.setText(service_location_str);
                        category_textview.setText(category_str);
                        other_category_text_field.setText(category_str.equals("Others") ? other_category_str : "");


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
        new Repositories().getClient(client_id, new Repositories.RepoCallback() {
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
                        String brgy_clearance = String.valueOf(obj.get("brgy_clearance"));
                        String police_clearance = String.valueOf(obj.get("police_clearance"));
                        String recent_job_proof = String.valueOf(obj.get("recent_job_proof"));
                        String is_available = String.valueOf(obj.get("is_available"));
                        String rating = String.valueOf(obj.get("rating"));
                        String rating_quantity = String.valueOf(obj.get("rating_quantity"));
                        String category = String.valueOf(obj.get("category"));
                        String subcategory = String.valueOf(obj.get("subcategory"));
                        int age = obj.getInt("age");
                        int completed_orders = obj.getInt("completed_orders");

                        String email = String.valueOf(edit_profile_email_text_field.getText());
                        String firstname = String.valueOf(first_name_text_field.getText());
                        String middle_name = String.valueOf(middle_name_text_field.getText());
                        String lastname = String.valueOf(last_name_text_field.getText());
                        String mobile = String.valueOf(mobile_text_field.getText());
                        String address = String.valueOf(address_text_field.getText());
                        String gender = String.valueOf(gender_tv.getText());
                        String service_location = String.valueOf(brgy_textview.getText());
                        String other_category = String.valueOf(other_category_text_field.getText());
                        String profile_img_str = String.valueOf(profile_img_tv.getText());

                        ClientTable client = new ClientTable(firstname, middle_name, lastname, address,
                                mobile, username, password, birthday, age, front_id, back_id, email,
                                gender, profile_img_str, category, other_category, service_location,
                                account_status, online_status, brgy_clearance, police_clearance, recent_job_proof, is_available, rating, rating_quantity, subcategory, completed_orders);
                        new Repositories().updateClient(client, client_id, new Repositories.RepoCallback() {
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
        new Repositories().getClient(client_id, new Repositories.RepoCallback() {
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
                        String brgy_clearance = String.valueOf(obj.get("brgy_clearance"));
                        String police_clearance = String.valueOf(obj.get("police_clearance"));
                        String recent_job_proof = String.valueOf(obj.get("recent_job_proof"));
                        String is_available = String.valueOf(obj.get("is_available"));
                        String rating = String.valueOf(obj.get("rating"));
                        String rating_quantity = String.valueOf(obj.get("rating_quantity"));
                        String category = String.valueOf(obj.get("category"));
                        String subcategory = String.valueOf(obj.get("subcategory"));
                        int age = obj.getInt("age");
                        int completed_orders = obj.getInt("completed_orders");

                        String email = String.valueOf(edit_profile_email_text_field.getText());
                        String firstname = String.valueOf(first_name_text_field.getText());
                        String middle_name = String.valueOf(middle_name_text_field.getText());
                        String lastname = String.valueOf(last_name_text_field.getText());
                        String mobile = String.valueOf(mobile_text_field.getText());
                        String address = String.valueOf(address_text_field.getText());
                        String gender = String.valueOf(gender_tv.getText());
                        String service_location = String.valueOf(brgy_textview.getText());
                        String other_category = String.valueOf(other_category_text_field.getText());
                        String profile_img_str = String.valueOf(profile_img_tv.getText());

                        ClientTable client = new ClientTable(firstname, middle_name, lastname, address,
                                mobile, username, password, birthday, age, front_id, back_id, email,
                                gender, profile_img_str, category, other_category, service_location,
                                account_status, online_status, brgy_clearance, police_clearance, recent_job_proof, is_available, rating, rating_quantity, subcategory, completed_orders);
                        new Repositories().updateClient(client, client_id, new Repositories.RepoCallback() {
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
        fragmentTransaction.replace(R.id.client_fragmentContainerView, new ClientProfilePage(), "profile_page").commit();
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

    private void brgyPicker(View v) {
        getBrgyList();
        Spinner brgy_dropdown = v.findViewById(R.id.brgy_dropdown);
        ConstraintLayout brgy_box = v.findViewById(R.id.brgy_box);
        TextView brgy_textview = v.findViewById(R.id.brgy_textview);
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

        ArrayAdapter<String> brgy_adapter = new ArrayAdapter<String>(getContext(),
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

    private void categoryPicker1(View v) {
        List<String> category_dropDown_list = new ArrayList<>();
        category_dropDown_list.add("");
        category_dropDown_list.add("Cleaning");
        category_dropDown_list.add("Mechanical Maintenance");
        category_dropDown_list.add("Electrical Maintenance");
        category_dropDown_list.add("Electronic Maintenance");
        category_dropDown_list.add("Construction");
        category_dropDown_list.add("Spa and Massage");
        category_dropDown_list.add("Salon Services");
        category_dropDown_list.add("Others");

        Spinner dropdown_category = v.findViewById(R.id.category_dropdown);
        ConstraintLayout category_btn = v.findViewById(R.id.category_box);
        category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropdown_category.performClick();
            }
        });

        category_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropdown_category.performClick();
            }
        });

        ArrayAdapter<String> label_dropdown = new ArrayAdapter<String>(getContext(),
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
    }

    private void initCategory(View view) {
        ConstraintLayout category_btn = view.findViewById(R.id.category_box);
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

    private void categoryPicker() {
        View mview = View.inflate(getContext(), R.layout.category_selection_page, null);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(getContext(), android.R.style.Theme_NoTitleBar_Fullscreen);
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
                    Toast.makeText(getContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
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

        rg1 = new RadioGroup(getContext());
        rg2 = new RadioGroup(getContext());
        rg3 = new RadioGroup(getContext());
        rg4 = new RadioGroup(getContext());

        List<String> sub_category_electronics = new ArrayList<>();
        sub_category_electronics.add("PC/Laptop Repair");
        sub_category_electronics.add("Cellphone Repair");
        sub_category_electronics.add("Appliances Repair");
        LinearLayoutCompat radiogroup_1 = mview.findViewById(R.id.radiogroup_1);
        createRadioButton(getContext(), sub_category_electronics, radiogroup_1, rg1, 1);

        List<String> sub_category_salon = new ArrayList<>();
        sub_category_salon.add("Haircut/Salon");
        sub_category_salon.add("Manicure/Pedicure");
        sub_category_salon.add("Massage");
        LinearLayoutCompat radiogroup_2 = mview.findViewById(R.id.radiogroup_2);
        createRadioButton(getContext(), sub_category_salon, radiogroup_2, rg2, 2);

        List<String> sub_category_household = new ArrayList<>();
        sub_category_household.add("Plumbing");
        sub_category_household.add("House Wiring");
        sub_category_household.add("House Cleaning");
        sub_category_household.add("Laundry");
        LinearLayoutCompat radiogroup_3 = mview.findViewById(R.id.radiogroup_3);
        createRadioButton(getContext(), sub_category_household, radiogroup_3, rg3, 3);

        List<String> sub_category_mechanical = new ArrayList<>();
        sub_category_mechanical.add("Automobile Repair");
        sub_category_mechanical.add("Motorcycle Repair");
        sub_category_mechanical.add("Bicycle Repair");
        LinearLayoutCompat radiogroup_4 = mview.findViewById(R.id.radiogroup_4);
        createRadioButton(getContext(), sub_category_mechanical, radiogroup_4, rg4, 4);

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

    private void updateServicesLocation() {
        new Repositories().getAllClientServices(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("services_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject service_id_obj = obj.getJSONObject("service_id");
                        JSONArray array = service_id_obj.toJSONArray(service_id_obj.names());
                        for (int i = 0; i < Objects.requireNonNull(array).length(); i++) {
                            String service_id = Objects.requireNonNull(service_id_obj.names()).get(i).toString();
                            String service_location = String.valueOf(brgy_textview.getText());
                            int finalI = i;
                            new Repositories().updateServiceLocation(service_id, client_id, service_location, new Repositories.RepoCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    String category_str = String.valueOf(category_textview.getText());
                                    String other_category = String.valueOf(other_category_text_field.getText());
                                    String category = category_str.equals("Others") ? "Others: " + other_category : category_str;
                                    new Repositories().updateServiceCategory(service_id, client_id, category, new Repositories.RepoCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            if (finalI + 1 == array.length()) {
                                                updateProfile();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(getContext(), "Please Try Again!", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }
            }
        });
    }
    private void updateServicesLocation1(AlertDialog prompt) {
        loading.show();
        new Repositories().getAllClientServices(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("services_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject service_id_obj = obj.getJSONObject("service_id");
                        JSONArray array = service_id_obj.toJSONArray(service_id_obj.names());
                        for (int i = 0; i < Objects.requireNonNull(array).length(); i++) {
                            String service_id = Objects.requireNonNull(service_id_obj.names()).get(i).toString();
                            String service_location = String.valueOf(brgy_textview.getText());
                            int finalI = i;
                            new Repositories().updateServiceLocation(service_id, client_id, service_location, new Repositories.RepoCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    String category_str = String.valueOf(category_textview.getText());
                                    String other_category = String.valueOf(other_category_text_field.getText());
                                    String category = category_str.equals("Others") ? "Others: " + other_category : category_str;
                                    new Repositories().updateServiceCategory(service_id, client_id, category, new Repositories.RepoCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            if (finalI + 1 == array.length()) {
                                                updateProfile1(prompt);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Toast.makeText(getContext(), "Please Try Again!", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                }
            }
        });
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
                updateServicesLocation1(dialog);
            }
        });

        TextView logout_text = mview.findViewById(R.id.prompt_text);
        logout_text.setText("Do you want to save the changes?");
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
}
