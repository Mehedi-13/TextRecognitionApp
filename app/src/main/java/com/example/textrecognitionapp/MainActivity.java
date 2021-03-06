package com.example.textrecognitionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    private EditText mResultEt;
    private ImageView mPreviewIv;


    private  static final int CAMERA_REQUEST_CODE=200;
    private  static final int STORAGE_REQUEST_CODE=400;
    private  static final int IMAGE_PICK_GALLERY_CODE=1000;
    private  static final int IMAGE_PICK_CAMERA_CODE=1001;

    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setSubtitle("Click + button to insert Image");

        mResultEt=findViewById(R.id.resultEt);
        mPreviewIv=findViewById(R.id.imageIv);

        cameraPermission= new String []{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission= new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};


    }

    //actionBar Menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate menu
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //handle actionbar item clicks

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        if (id==R.id.addImage){
            showImageImportDialog();
        }
        if (id==R.id.setting){
            Toast.makeText(this, "Setting", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog() {
        //items to display in dialog

        String[] items={"Camera","Gallery"};
        AlertDialog.Builder dialog= new AlertDialog.Builder(this);
        //set title
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    //camera option clicked
//                    for os masmalo and above we need to ask runtime permission for camera and storage

                    if (!checkCamerapermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickCamera();
                    }


                }
                if (which==1){
                    //gallery option clicked
                    if (!checkStoragepermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickGallery();
                    }
                }

            }
        });
        dialog.create().show(); //show Dialog


    }

    private void pickGallery() {
        //intent to pic image from gallery
        Intent intent= new Intent(Intent.ACTION_PICK);
        //setIntent type to image
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }

    private void pickCamera() {

        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic"); //title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image To Text"); //description
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragepermission() {
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED) ;

        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCamerapermission() {

        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED) ;

        return result && result1;
    }


    //handle permission result

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                        boolean cameraAccepted= grantResults[0] ==
                                PackageManager.PERMISSION_GRANTED;
                        boolean WriteStorageAccepted =grantResults[0] ==
                                PackageManager.PERMISSION_GRANTED;
                        if (cameraAccepted && WriteStorageAccepted){
                            pickCamera();
                        }
                        else {
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                        }
                }
                break;

            case STORAGE_REQUEST_CODE:

                if (grantResults.length > 0) {

                    boolean WriteStorageAccepted =grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (WriteStorageAccepted){
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }




    }


    //handel Image result


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //got image from camera
        if (resultCode == RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
            if (requestCode==IMAGE_PICK_CAMERA_CODE){

                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        //get crop image

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                Uri resultUri= result.getUri(); //get Image uri
                //set image to image view
                mPreviewIv.setImageURI(resultUri);

                //get drawble bitmap for text recontion
                BitmapDrawable bitmapDrawable= (BitmapDrawable)mPreviewIv.getDrawable();
                Bitmap bitmap= bitmapDrawable.getBitmap();

                TextRecognizer recognizer= new TextRecognizer.Builder(getApplicationContext()).build();

                if (!recognizer.isOperational()){
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
                else {
                    Frame frame= new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items= recognizer.detect(frame);
                    StringBuilder sb= new StringBuilder();

                    for (int i=0;i<items.size();i++){
                        TextBlock myItem= items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }
                    //set text to edit text
                    mResultEt.setText(sb.toString());
                }

            }

            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                // if ther is any error then show it
                Exception error = result.getError();
                Toast.makeText(this, ""+ error, Toast.LENGTH_SHORT).show();


            }
        }


    }
}