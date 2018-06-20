package com.example.mayorgag4.notificationtest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    private ImageView imageView;
    public static int NOTIFICATION_ID = 1;
    Bitmap bitmap;
    Bitmap bitmapGallery;
    Bitmap finalBitmapPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button;

        imageView = (ImageView) findViewById(R.id.image_view);

        Button pickImageButton = (Button) findViewById(R.id.pick_image_button);
        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(buttonClickListener);

        lastPhotoGallery();

    }

    public void lastPhotoGallery (){

        // Find the last picture
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        // Put it in the image view
        if (cursor.moveToFirst()) {
            final ImageView imageView = (ImageView) findViewById(R.id.image_view);
            String imageLocation = cursor.getString(1);
            File imageFile = new File(imageLocation);

            if (imageFile.exists()) {
                bitmapGallery = BitmapFactory.decodeFile(imageLocation);

                if (bitmapGallery != null) {

                imageView.setImageBitmap(bitmapGallery);}
            }
        }


    }




    private void openGallery() {
        //This will open the phone's gallery
        Intent gallery = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //This code is to display the selected image from Gallery and send the image to be converted to Bitmap
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            manageImageFromUri(imageUri);
            imageView.setImageURI(imageUri);
        }
    }



    private void manageImageFromUri(Uri imageUri) {
        //This will create a bitmap out of the imageURI from onActivityResult
        //so it can be used as part of notification images since they require bitmap

        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(), imageUri);

        } catch (Exception e) {

        }


    }

     private View.OnClickListener buttonClickListener = new View.OnClickListener() {


        @Override
        public void onClick(View view) {
        //This code is to create notifications

            Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
            detailsIntent.putExtra("EXTRA_DETAILS_ID", 42);
            PendingIntent detailsPendingIntent = PendingIntent.getActivity(
                    MainActivity.this,
                    0,
                    detailsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );



            //Need to increase notification id by 1 in order to have multiple notifications displayed, otherwise notifications
            //will overwrite previous notification
            NOTIFICATION_ID++;

            //To determine what needs to be displayed
            if (bitmap !=null){
                //Need to resize bitmaps otherwise app will crash and/or not display photo correctly
                finalBitmapPic = Bitmap.createScaledBitmap(bitmap, 500, 800, false);

            }
            else{
                //Need to resize bitmaps otherwise app will crash and/or not display photo correctly
                finalBitmapPic = Bitmap.createScaledBitmap(bitmapGallery, 500, 800, false);

            }

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)

                    //LargeIcon and setStyle needs to be updated to pull from app
                    //setContentTitle needs to be updated to info about match
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setLargeIcon(finalBitmapPic)
                    .setContentTitle("Database Text Once It's Built")
                    .setAutoCancel(true)
                    .setContentIntent(detailsPendingIntent)
                    .addAction(android.R.drawable.ic_menu_compass, "Details", detailsPendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        }


    };
}

