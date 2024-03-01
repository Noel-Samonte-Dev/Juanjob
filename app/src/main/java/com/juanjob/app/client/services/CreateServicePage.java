package com.juanjob.app.client.services;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.juanjob.app.helpers.BitmapHelper.bitmap_str;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.juanjob.app.R;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.database.ServiceTable;
import com.juanjob.app.helpers.DateHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;

public class CreateServicePage extends Fragment {
    private TextInputEditText service_name_text_field, service_desc_text_field,
            service_location_text_field, price_min_text_field, price_max_text_field;
    private TextView label_spinner_txt;
    private AppCompatButton select_service_img_sample;
    private AlertDialog loading_screen;
    private String client_id;
    private int price_min_int, price_max_int;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_service_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading_screen = new LoadingPage().loadingGif(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        client_id = sp.getString("login_id", "");

        ImageView back_btn_new_service = v.findViewById(R.id.back_btn_new_service);
        back_btn_new_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        img_str = v.findViewById(R.id.img_str);
        getKeyboardEventImg(img_str);

        price_min_text_field = v.findViewById(R.id.price_min_text_field);
        price_max_text_field = v.findViewById(R.id.price_max_text_field);

        service_name_text_field = v.findViewById(R.id.service_name_text_field);
        service_desc_text_field = v.findViewById(R.id.service_desc_text_field);
        service_location_text_field = v.findViewById(R.id.service_location_text_field);

        label_spinner_txt = v.findViewById(R.id.label_spinner_txt);
        select_service_img_sample = v.findViewById(R.id.select_service_img_sample);
        select_service_img_sample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_str.setText("");
                checkPermission();
            }
        });

        Button create_service_btn = v.findViewById(R.id.create_service_btn);
        create_service_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_screen.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isValidServiceInputs()) {
                            createService();
                        } else {
                            loading_screen.dismiss();
                        }
                    }
                }, 1000);
            }
        });

        //categoryPicker(v);
        getUserInfo();
    }

    private void getUserInfo() {
        new Repositories().getClient(client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                if (!result.equals("user_not_found")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        String category = String.valueOf(obj.get("category"));
                        String other_category = String.valueOf(obj.get("other_category"));
                        String service_location = String.valueOf(obj.get("service_location"));

                        String category_str = category.equals("Others") ? "Others: " + other_category : category;
                        label_spinner_txt.setText(category_str);
                        service_location_text_field.setText(service_location);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {

                }
            }
        });
    }

    private void createService() {
        SharedPreferences sp = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        String client_id = sp.getString("login_id", "");
        String name = String.valueOf(service_name_text_field.getText());
        String desc = String.valueOf(service_desc_text_field.getText());
        String location = String.valueOf(service_location_text_field.getText());
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
                    Toast.makeText(getContext(), "Service successfully added!", Toast.LENGTH_LONG)
                            .show();
                    getParentFragmentManager().popBackStack();
                    reloadMyServicesPage();
                } else {
                    Toast.makeText(getContext(), "Please try again!.", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private void reloadMyServicesPage() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.client_fragmentContainerView, new MyServicesPage(), "services_page").commit();
    }

    private void categoryPicker(View view) {
        List<String> category_dropDown_list = new ArrayList<>();
        category_dropDown_list.add("");
        category_dropDown_list.add("Cleaning");
        category_dropDown_list.add("Mechanical Maintenance");
        category_dropDown_list.add("Electrical Maintenance");
        category_dropDown_list.add("Electronic Maintenance");
        category_dropDown_list.add("Construction");
        category_dropDown_list.add("Spa and Massage");
        category_dropDown_list.add("Salon Services");

        Spinner dropdown_category = view.findViewById(R.id.category_dropdown);
        ConstraintLayout category_btn = view.findViewById(R.id.category_btn);
        category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropdown_category.performClick();
            }
        });

        label_spinner_txt.setOnClickListener(new View.OnClickListener() {
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
                    label_spinner_txt.setText(category_dropDown_list.get(index));
                    label_spinner_txt.setTextColor(Color.parseColor("#000000"));
                    category_btn.setBackgroundResource(R.drawable.gray_border);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
                Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                String bitmap_str_img = bitmap_str(requireContext(), imgDecodableString);
                img_str.setText(bitmap_str_img);

            } else {
                Toast.makeText(getActivity(), "You haven't picked an Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG)
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
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidDescription()) {
            String error_msg = "Service Description should not be empty.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidLocation()) {
            String error_msg = "Service Location should not be empty.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

//        if (!isValidCategory()) {
//            String error_msg = "Please select a category of your service.";
//            ToastMsg.error_toast(getContext(), error_msg);
//            return false;
//        }

        if (!isValidImage()) {
            String error_msg = "Please select a sample image of your service";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (!isValidPriceRange()){
            return false;
        }

        return true;

    }

    private boolean isValidServiceName() {
        return !String.valueOf(service_name_text_field.getText()).trim().isEmpty();
    }

    private boolean isValidDescription() {
        return !String.valueOf(service_desc_text_field.getText()).trim().isEmpty();
    }

    private boolean isValidLocation() {
        return !String.valueOf(service_location_text_field.getText()).trim().isEmpty();
    }

    private boolean isValidCategory() {
        String category = String.valueOf(label_spinner_txt.getText());
        //return !category.equals("*Select Category");
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
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        if (price_min_int == price_max_int) {
            String error_msg = "Price min and price max should no be equal.";
            ToastMsg.error_toast(getContext(), error_msg);
            return false;
        }

        return true;
    }
}