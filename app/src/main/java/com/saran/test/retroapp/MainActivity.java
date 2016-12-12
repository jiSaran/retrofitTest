package com.saran.test.retroapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private final String url = "http://ci.draftserver.com/hornsbyapp/webservice/";
    private OkHttpClient okHttpClient;
    private static int IMAGE_CAPTURE_REQUEST = 1;
    private List<String> spinnerItems = new ArrayList<>();
    private Spinner spinner;
    private final String string = "Type of problem";
    private static Uri imgUri;
    private ImageView view;
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private CheckBox checkBox;
    private Button sendbtn, selectimgbtn;
    private EditText name_field, location_field, notes_field, email_field, phone_field;
    private final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private final String phonePattern = "^[+]?[0-9]{8,20}$";
    private String image_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.image_url = "";
        checkBox = (CheckBox) findViewById(R.id.check);
        spinner = (Spinner) findViewById(R.id.spinner);
        sendbtn = (Button) findViewById(R.id.send);
        name_field = (EditText) findViewById(R.id.name);
        location_field = (EditText) findViewById(R.id.location);
        notes_field = (EditText) findViewById(R.id.notes);
        email_field = (EditText) findViewById(R.id.email);
        phone_field = (EditText) findViewById(R.id.phone);
        selectimgbtn = (Button) findViewById(R.id.imgbtn);
        view = (ImageView) findViewById(R.id.imgview);

        if(checkDB()){
            addProblemFromDB();
        } else {
            getProblemsTitle();
        }
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFields()) {
                    sendData(image_url);
                }
            }
        });

        selectimgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });

    }

    private void addProblemFromDB() {
        final DBHelper db = new DBHelper(MainActivity.this);
        spinnerItems = db.getProblem();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private boolean checkDB() {
        String path = getDatabasePath(DBHelper.getDBName()).toString();
        SQLiteDatabase checkDB = null;
        try{
            checkDB = SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
            return true;
        } catch (Exception e){
            Log.d("Error",e.toString());
            return false;
        }
    }

    private void sendData(String fileUri) {
        retrofitClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);

//        String str = fileUri.toString();

        File file = new File(fileUri);

        RequestBody requestFile = RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        RequestBody name = createPartFromString(name_field.getText().toString());
        RequestBody location = createPartFromString(location_field.getText().toString());
        RequestBody notes = createPartFromString(notes_field.getText().toString());
        RequestBody email = createPartFromString(email_field.getText().toString());
        RequestBody phone = createPartFromString(phone_field.getText().toString());
        RequestBody problem = createPartFromString(spinner.getSelectedItem().toString());

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("problem_type_id", problem);
        map.put("phone_no", phone);
        map.put("location", location);
        map.put("notes", notes);

        Call<ResponseBody> call = service.submitForm(map, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Sucess", response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Failed", t.toString());
            }
        });

    }

    private void retrofitClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }

    public boolean checkFields() {
        if (checkBox.isChecked()) {
            if (!spinner.getSelectedItem().toString().equals(string)) {
                if (!location_field.getText().toString().trim().isEmpty()) {
                    if (!notes_field.getText().toString().trim().isEmpty()) {
                        if (!name_field.getText().toString().trim().isEmpty()) {
                            if (!email_field.getText().toString().trim().isEmpty() && email_field.getText().toString().trim().matches(emailPattern)) {
                                if (!phone_field.getText().toString().trim().isEmpty() && phone_field.getText().toString().trim().matches(phonePattern)) {
                                    return true;
                                } else {
                                    Toast.makeText(MainActivity.this, "Please enter valid phone no", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Please enter valid email", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter name", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter notes", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter location", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please select problem type", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Please check the checkbox", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void getProblemsTitle() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build();

        ApiService problemList = retrofit.create(ApiService.class);
        Call<ProblemObject> listCall = problemList.getProblemDetails("1");

        final DBHelper db = new DBHelper(MainActivity.this);

        spinnerItems.add(string);

        listCall.enqueue(new Callback<ProblemObject>() {
            @Override
            public void onResponse(Call<ProblemObject> call, Response<ProblemObject> response) {

                if (response.isSuccessful()) {
                    db.addProblem(1,string);
                    for (int i = 0; i < response.body().problemTypes.size(); i++) {
                        db.addProblem(i+2,response.body().problemTypes.get(i).title);
                        spinnerItems.add(response.body().problemTypes.get(i).title);
                    }
                }

            }

            @Override
            public void onFailure(Call<ProblemObject> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    public void captureImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,IMAGE_CAPTURE_REQUEST);
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, IMAGE_CAPTURE_REQUEST);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        try {

            if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK && data != null) {
//                String photoPath = "";
//                Bundle bundle = data.getExtras();
//                Bitmap bitmap = (Bitmap) bundle.get("data");
//
//                view.setImageBitmap(bitmap);
//                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.ImageColumns.ORIENTATION}, MediaStore.Images.Media.DATE_ADDED, null, "date_added ASC");
//                if (cursor != null && cursor.moveToFirst()) {
//                    do {
//                        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
//                        photoPath = uri.toString();
//                    } while (cursor.moveToNext());
//                    cursor.close();
//                }
//
//                this.image_url = photoPath;

                Uri uri = data.getData();
                this.image_url = getPath(uri);
                view.setImageURI(uri);




//                imgUri = getImageUri(MainActivity.this, bitmap);

//                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
//                String photoPath = "";
//
//                Bundle extras = data.getExtras();
//                Bitmap mImageBitmap = (Bitmap) extras.get("data");
//                this.imgview.setImageBitmap(mImageBitmap);
//
//                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.ImageColumns.ORIENTATION}, MediaStore.Images.Media.DATE_ADDED, null, "date_added ASC");
//                if (cursor != null && cursor.moveToFirst()) {
//                    do {
//                        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
//                        photoPath = uri.toString();
//                    } while (cursor.moveToNext());
//
//                    cursor.close();
//                }
//                image_url = photoPath;

//                Uri uri = data.getData();
//                String[] file = {MediaStore.Images.Media.DATA};
//
//                Bundle extras = data.getExtras();
//                Bitmap mImageBitmap = (Bitmap) extras.get("data");
//
//                Cursor cursor = getContentResolver().query(uri, file, null, null, null);
//                cursor.moveToFirst();
//
//                int columnIndex = cursor.getColumnIndex(file[0]);
//                imageDecode = cursor.getString(columnIndex);
//                cursor.close();
//
//                File imgfile = new File(imageDecode);
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 2;
//                Bitmap myBitmap = BitmapFactory.decodeFile(imgfile.getAbsolutePath(),options);

            }

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Unable to capture image", Toast.LENGTH_LONG).show();
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        String s = "";
        Cursor cursor = getContentResolver().query(uri,projection,null,null,null);
        if(cursor == null){
            return null;
        }else{
            int columnIndex = cursor.getColumnIndex(projection[0]);
            cursor.moveToFirst();
            s = cursor.getString(columnIndex);
            cursor.close();
            return s;
        }
    }

}
