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
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.juanjob.app.R;
import com.juanjob.app.client.profile.ClientProfilePage;
import com.juanjob.app.database.Repositories;
import com.juanjob.app.database.ServiceTable;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;

public class EditServicePage extends Fragment {
    private String service_id, client_id;
    private AlertDialog loading_screen;
    private TextInputEditText service_name_text_field, service_desc_text_field, service_location_text_field, price_min_text_field, price_max_text_field;;
    private AppCompatButton select_service_img_sample;
    private TextView img_tv, label_spinner_txt, status_spinner_txt;
    private int price_min_int, price_max_int;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_service_page, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        loading_screen = new LoadingPage().loadingGif(getActivity());
        label_spinner_txt = v.findViewById(R.id.label_spinner_txt);
        status_spinner_txt = v.findViewById(R.id.status_spinner_txt);

        price_min_text_field = v.findViewById(R.id.price_min_text_field);
        price_max_text_field = v.findViewById(R.id.price_max_text_field);

        SharedPreferences sp = requireActivity().getSharedPreferences("Service", MODE_PRIVATE);
        service_id = sp.getString("service_id", "");

        SharedPreferences login_sp = requireActivity().getSharedPreferences("Login", MODE_PRIVATE);
        client_id = login_sp.getString("login_id", "");

        getServiceInfo();

        ImageView back_btn_new_service = v.findViewById(R.id.back_btn_new_service);
        back_btn_new_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
            }
        });

        img_tv = v.findViewById(R.id.img_tv);

        service_name_text_field = v.findViewById(R.id.service_name_text_field);
        service_desc_text_field = v.findViewById(R.id.service_desc_text_field);
        service_location_text_field = v.findViewById(R.id.service_location_text_field);

        select_service_img_sample = v.findViewById(R.id.select_service_img_sample);
        select_service_img_sample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        AppCompatButton edit_service_btn = v.findViewById(R.id.edit_service_btn);
        edit_service_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidServiceInputs()) {
                    updateStatus();
                }
            }
        });

        getUserInfo();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
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
                img_tv.setText(bitmap_str_img);
                select_service_img_sample.setText("New Image Selected");
                select_service_img_sample.setTextColor(Color.parseColor("#008000"));
                select_service_img_sample.setBackgroundResource(R.drawable.green_border);
            } else {
                Toast.makeText(getActivity(), "You haven't picked an Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void updateStatus() {
        loading_screen.show();
        new Repositories().getService(service_id, client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject sku_obj = new JSONObject(result);
                    String img_str = img_tv.getText().toString();
                    //String service_name_str = Objects.requireNonNull(service_name_text_field.getText()).toString();
                    String service_name_str = "";
                    String service_desc_str = Objects.requireNonNull(service_desc_text_field.getText()).toString();
                    //String service_location_str = Objects.requireNonNull(service_location_text_field.getText()).toString();
                    String service_location_str = String.valueOf(sku_obj.get("location"));;
                    String service_status_str = status_spinner_txt.getText().toString().replace("Status: ", "").toUpperCase();
                    String category_str = label_spinner_txt.getText().toString();
                    String customer_id_str = String.valueOf(sku_obj.get("customer_id"));
                    String date_created = String.valueOf(sku_obj.get("date_created"));
                    String price_range_str = "₱" + price_min_text_field.getText().toString() + " - " + "₱" + price_max_text_field.getText().toString();

                    if (status_spinner_txt.getText().toString().toLowerCase().contains("active")) {
                        customer_id_str = "";
                    }

                    ServiceTable service_table = new ServiceTable(service_name_str, service_desc_str, date_created, category_str, service_location_str,
                            service_status_str, client_id, customer_id_str, img_str, price_range_str);

                    new Repositories().updateService(service_table, service_id, client_id, new Repositories.RepoCallback() {
                        @Override
                        public void onSuccess(String result) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loading_screen.dismiss();
                                    if (result.equals("success")) {
                                        reloadProfilePage();
                                        getParentFragmentManager().popBackStack();
                                    } else {
                                        Toast.makeText(getContext(), "Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, 1000);
                        }
                    });

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void updateStatus1(AlertDialog prompt) {
        loading_screen.show();
        new Repositories().getService(service_id, client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject sku_obj = new JSONObject(result);
                    String img_str = img_tv.getText().toString();
                    //String service_name_str = Objects.requireNonNull(service_name_text_field.getText()).toString();
                    String service_name_str = "";
                    String service_desc_str = Objects.requireNonNull(service_desc_text_field.getText()).toString();
                    //String service_location_str = Objects.requireNonNull(service_location_text_field.getText()).toString();
                    String service_location_str = String.valueOf(sku_obj.get("location"));;
                    String service_status_str = status_spinner_txt.getText().toString().replace("Status: ", "").toUpperCase();
                    String category_str = label_spinner_txt.getText().toString();
                    String customer_id_str = String.valueOf(sku_obj.get("customer_id"));
                    String date_created = String.valueOf(sku_obj.get("date_created"));
                    String price_range_str = "₱" + price_min_text_field.getText().toString() + " - " + "₱" + price_max_text_field.getText().toString();

                    if (status_spinner_txt.getText().toString().toLowerCase().contains("active")) {
                        customer_id_str = "";
                    }

                    ServiceTable service_table = new ServiceTable(service_name_str, service_desc_str, date_created, category_str, service_location_str,
                            service_status_str, client_id, customer_id_str, img_str, price_range_str);

                    new Repositories().updateService(service_table, service_id, client_id, new Repositories.RepoCallback() {
                        @Override
                        public void onSuccess(String result) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loading_screen.dismiss();
                                    prompt.dismiss();
                                    if (result.equals("success")) {
                                        prompt.dismiss();
                                        loading_screen.dismiss();
                                        getParentFragmentManager().popBackStack();
                                        reloadProfilePage();
                                    } else {
                                        Toast.makeText(getContext(), "Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, 1000);
                        }
                    });

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void reloadMyServicesPage() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.client_fragmentContainerView, new MyServicesPage(), "services_page").commit();
    }

    private void reloadProfilePage() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.client_fragmentContainerView, new ClientProfilePage(), "profile_page").commit();
    }

    private void getServiceInfo() {
        loading_screen.show();
        new Repositories().getService(service_id, client_id, new Repositories.RepoCallback() {
            @Override
            public void onSuccess(String result) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loading_screen.dismiss();
                        try {
                            JSONObject sku_obj = new JSONObject(result);
                            String url = String.valueOf(sku_obj.get("img"));
                            String service_name = String.valueOf(sku_obj.get("name"));
                            String service_desc = String.valueOf(sku_obj.get("description"));
                            String service_location = String.valueOf(sku_obj.get("location"));
                            String service_status = String.valueOf(sku_obj.get("status"));
                            String service_customer = String.valueOf(sku_obj.get("customer_id"));
                            String client_id = String.valueOf(sku_obj.get("client_id"));
                            String category = String.valueOf(sku_obj.get("category"));
                            String price_range = String.valueOf(sku_obj.get("price_range")).replace("₱", "");
                            List<String> price_range_list = Arrays.asList(price_range.trim().split("-"));
                            String price_min = price_range_list.get(0);
                            String price_max = price_range_list.get(1);

                            price_min_text_field.setText(price_min);
                            price_max_text_field.setText(price_max);
                            price_min_int = price_min_text_field.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(price_min_text_field.getText().toString().trim());
                            price_max_int = price_max_text_field.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(price_max_text_field.getText().toString().trim());

                            img_tv.setText(url);
                            //service_name_text_field.setText(service_name);
                            service_desc_text_field.setText(service_desc);
                            //service_location_text_field.setText(service_location);
                            status_spinner_txt.setText("Status: " + service_status.toUpperCase());
                            label_spinner_txt.setText(category);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, 1000);
            }
        });
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
                        //service_location_text_field.setText(service_location);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {

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

        if (!isValidPriceRange()) {
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
                updateStatus1(dialog);
            }
        });

        TextView logout_text = mview.findViewById(R.id.prompt_text);
        logout_text.setText("Do you want to save the changes?");
    }
}
