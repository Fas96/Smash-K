package com.fas.smash_k;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fas.smash_k.ui.RequestHandler;
import com.fas.smash_k.ui.models.app.Constants;
import com.fas.smash_k.ui.models.app.UserLocal;
import com.fas.smash_k.ui.models.chatItems.User;
import com.fas.smash_k.ui.sharedPrefManager.SharedPrefManager;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends Activity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    public static final int REQUEST_CODE_BIRTHDATE = 2;
    public static final int REQUST_CODE_EXTERNAL_PERMISSION = 1;
    public static final int REQUST_CODE_PICK_IMAGE = 3;
    TextView birthDayView; EditText emailView; EditText lastNameView; EditText nameView;
    EditText passwordView; EditText phoneView;CircleImageView profileImageView;EditText reEnterView;
    Button registerButton;User user;CardView image_profilem;
    ProgressDialog progressDialog;

    /* access modifiers changed from: protected */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if(SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }


        profileImageView = (CircleImageView) findViewById(R.id.image_profile);
        //image_profilem=findViewById(R.id.image_profile_cv);
        this.birthDayView = (TextView) findViewById(R.id.dob);
        this.nameView = (EditText) findViewById(R.id.name);
        this.lastNameView = (EditText) findViewById(R.id.last_name);
        this.emailView = (EditText) findViewById(R.id.email);
        this.phoneView = (EditText) findViewById(R.id.phone);
        this.passwordView = (EditText) findViewById(R.id.password);
        this.reEnterView = (EditText) findViewById(R.id.reenter_password);
        this.registerButton = (Button) findViewById(R.id.register_btn);
        //progress dailog
        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Registering....");

        //set listener
        this.profileImageView.setOnClickListener(this);
        this.birthDayView.setOnClickListener(this);
        this.registerButton.setOnClickListener(this);
       // image_profilem.setOnClickListener(this);
    }
    public void onClick(View v) {

        int id = v.getId();
        if(id==R.id.image_profile){
            //Toast.makeText(getApplicationContext(),"image_profile",Toast.LENGTH_SHORT).show();
           // pickPhoto();
            if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                pickPhoto();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            }
        }
        if (id == R.id.dob) {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, calendar.get(1), calendar.get(2), calendar.get(5));
            datePickerDialog.show();
        } else if (id != R.id.image_profile) {
            if (id == R.id.register_btn) {
                if (checkPassword(this.passwordView.getText().toString(), this.reEnterView.getText().toString())) {
                    String name = this.nameView.getText().toString();
                    String lastname = this.lastNameView.getText().toString();
                    String emial = this.emailView.getText().toString();
                    String phone = this.phoneView.getText().toString();

                    //register user
                    registerUser();


                }
                Toast.makeText(this, "Confirmation faild", Toast.LENGTH_LONG).show();
            }
        } else if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            pickPhoto();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (requestCode == 1 && permissions.length > 0 && results[0] == 0) {
            pickPhoto();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == -1 && requestCode == 3 && intent != null) {
            Uri imageUri = intent.getData();
            this.profileImageView.setImageURI(imageUri);
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("images/*");
        Intent intentPicker = new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentPicker.setType("image/*");
        Intent chooserIntent = Intent.createChooser(intent, "Select Image");
        chooserIntent.putExtra("android.intent.extra.INITIAL_INTENTS", new Intent[]{intentPicker});
        startActivityForResult(chooserIntent, 3);
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = managedQuery(contentUri, new String[]{"_data"}, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        StringBuilder sb = new StringBuilder();
        sb.append(dayOfMonth);
        sb.append("/");
        sb.append(0);
        sb.append(month+1);
        sb.append("/");
        sb.append(year);
        String stringdate = sb.toString();
        this.birthDayView.setText(stringdate);
    }

    public boolean checkPassword(String password, String reenterPass) {
        if (password == null || password.isEmpty() || reenterPass == null || reenterPass.isEmpty() || !password.equals(reenterPass)) {
            return false;
        }
        return true;
    }

    private void registerUser() {
        final String email = emailView.getText().toString().trim();
        final String username = nameView.getText().toString().trim();
        final String password = passwordView.getText().toString().trim();

        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_REGISTER, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Gson gson = new Gson();
                        UserLocal b = gson.fromJson(response.toString().substring(1,response.toString().length()),UserLocal.class);
                        if(b.getError().toString().equals("false")){

                            SharedPrefManager.getInstance(getApplicationContext())
                                    .userLogin(
                                            b.getId(),
                                            b.getUsername(),
                                            b.getEmail()
                                    );
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish(); }else{
                            Toast.makeText(
                                    getApplicationContext(),
                                    b.getUsername()+"fas",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // progressDialog.hide();
                        Toast.makeText(getApplicationContext(), error.getMessage()+"register error", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };


        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }
}