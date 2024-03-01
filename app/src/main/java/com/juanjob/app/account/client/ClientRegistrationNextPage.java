package com.juanjob.app.account.client;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.juanjob.app.R;
import com.juanjob.app.helpers.BitmapHelper;
import com.juanjob.app.helpers.LoadingPage;
import com.juanjob.app.helpers.ToastMsg;

public class ClientRegistrationNextPage extends AppCompatActivity {
    private AlertDialog loading_screen;
    private Button valid_id_btn, brgy_btn, police_clearance_btn, recent_job_btn;
    private TextView invalid_fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.client_registration_next_page);
        loading_screen = new LoadingPage().loadingGif(this);

        invalid_fields = findViewById(R.id.invalid_fields);

        valid_id_btn = findViewById(R.id.valid_id_btn);
        valid_id_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RESULT_LOAD_IMG = 169;
                selectImagePage();
            }
        });

        brgy_btn = findViewById(R.id.brgy_btn);
        brgy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RESULT_LOAD_IMG = 170;
                selectImage();
            }
        });

        police_clearance_btn = findViewById(R.id.police_clearance_btn);
        police_clearance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RESULT_LOAD_IMG = 171;
                selectImage();
            }
        });

        recent_job_btn = findViewById(R.id.recent_job_btn);
        recent_job_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RESULT_LOAD_IMG = 172;
                selectImage();
            }
        });

        Button create_account_btn = findViewById(R.id.create_account_btn1);
        create_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInputs()) {
                    goToAddServicePage();
                }
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, RESULT_LOAD_IMG);
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
                    Toast.makeText(ClientRegistrationNextPage.this, "Please select both front and back image of your valid ID.", Toast.LENGTH_LONG).show();
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
    private String brgy_clearance_str = "";
    private String police_clearance_str = "";
    private String recent_job_str = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 169 && resultCode == RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                if (is_front) {
                    front_id_str = imgDecodableString;
                } else {
                    back_id_str = imgDecodableString;
                }

                if (isValidFrontID() && isValidBackID()) {
                    valid_id_btn.setText("Valid ID Submitted");
                    valid_id_btn.setTextColor(Color.parseColor("#008000"));
                    valid_id_btn.setBackgroundResource(R.drawable.green_border);
                }

                ImageView imgView = is_front ? front_id_img : back_id_img;
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));
            }

            else if (requestCode == 170 && resultCode == RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                brgy_clearance_str = imgDecodableString;

                brgy_btn.setText("Barangay Clearance Submitted");
                brgy_btn.setTextColor(Color.parseColor("#008000"));
                brgy_btn.setBackgroundResource(R.drawable.green_border);
            }

            else if (requestCode == 171 && resultCode == RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                police_clearance_str = imgDecodableString;

                police_clearance_btn.setText("Police Clearance Submitted");
                police_clearance_btn.setTextColor(Color.parseColor("#008000"));
                police_clearance_btn.setBackgroundResource(R.drawable.green_border);
            }

            else if (requestCode == 172 && resultCode == RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                recent_job_str = imgDecodableString;

                recent_job_btn.setText("Recent Job Proof Submitted");
                recent_job_btn.setTextColor(Color.parseColor("#008000"));
                recent_job_btn.setBackgroundResource(R.drawable.green_border);
            }

            else {
                Toast.makeText(this, "You haven't picked an Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private boolean isValidFrontID() {
        return !front_id_str.trim().isEmpty();
    }

    private boolean isValidBackID() {
        return !back_id_str.trim().isEmpty();
    }

    private boolean isValidInputs() {
        setErrors();

        if (front_id_str.trim().isEmpty() || back_id_str.trim().isEmpty()) {
            String error_msg = "Please upload your valid id's front and back image.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (brgy_clearance_str.trim().isEmpty()) {
            String error_msg = "Please upload your barangay clearance.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (police_clearance_str.trim().isEmpty()) {
            String error_msg = "Please upload your police clearance.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        if (recent_job_str.trim().isEmpty()) {
            String error_msg = "Please upload proof of your recent job.";
            ToastMsg.error_toast(getApplicationContext(), error_msg);
            return false;
        }

        return true;
    }

    private void setErrors() {
        String invalid_fields_str = "";

        if (front_id_str.trim().isEmpty() || back_id_str.trim().isEmpty()) {
            invalid_fields_str = "*Invalid valid ID.\n";
        }

        if (brgy_clearance_str.trim().isEmpty()) {
            invalid_fields_str = invalid_fields_str + "*Invalid barangay clearance.\n";
        }

        if (police_clearance_str.trim().isEmpty()) {
            invalid_fields_str = invalid_fields_str + "*Invalid police clearance.\n";
        }

        if (recent_job_str.trim().isEmpty()) {
            invalid_fields_str = invalid_fields_str + "*Invalid proof of recent job.";
        }

        invalid_fields.setText(invalid_fields_str);
    }

    private void goToAddServicePage() {
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
        String front_id_base64 = BitmapHelper.bitmap_str(getApplicationContext(), front_id_str);
        String back_id_base64 = BitmapHelper.bitmap_str(getApplicationContext(), back_id_str);
        String brgy_clearance_base64 = BitmapHelper.bitmap_str(getApplicationContext(), brgy_clearance_str);
        String police_clearance_base64 = BitmapHelper.bitmap_str(getApplicationContext(), police_clearance_str);
        String recent_job_base64 = BitmapHelper.bitmap_str(getApplicationContext(), recent_job_str);

        Intent i = new Intent(this, ClientRegistrationAddService.class);
        i.putExtra("username", username);
        i.putExtra("password", password);
        i.putExtra("email", email);
        i.putExtra("firstname", firstname);
        i.putExtra("middle_name", middle_name);
        i.putExtra("lastname", lastname);
        i.putExtra("mobile", mobile);
        i.putExtra("address", address);
        i.putExtra("birthday", birthday);
        i.putExtra("gender", gender);
        i.putExtra("age", age);
        i.putExtra("service_location", service_location);
        i.putExtra("category", category);
        i.putExtra("subcategory", subcategory);
        i.putExtra("other_category", other_category);
        i.putExtra("profile_id_base64", profile_id_base64);
        i.putExtra("front_id_base64", front_id_base64);
        i.putExtra("back_id_base64", back_id_base64);
        i.putExtra("brgy_clearance_base64", brgy_clearance_base64);
        i.putExtra("police_clearance_base64", police_clearance_base64);
        i.putExtra("recent_job_base64", recent_job_base64);
        startActivity(i);
    }
}