package com.example.mayorgag4.notificationtest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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
    Bitmap bitmapSelectGallery;
    Bitmap bitmapAutoGallery;
    Bitmap finalBitmapPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This is required to open the gallery to select a photo:
        imageView = (ImageView) findViewById(R.id.image_view);
        Button pickImageButton = (Button) findViewById(R.id.pick_image_button);
        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        //This is required to have the most recent photo appear on the app:
        lastPhotoInGallery();

    }




    public void lastPhotoInGallery () {

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
                String imageLocation  = cursor.getString(1);
                File imageFile = new File(imageLocation);

                if (imageFile.exists()) {
                    bitmapAutoGallery = BitmapFactory.decodeFile(imageLocation);

                    if (bitmapAutoGallery != null) {
                        imageView.setImageBitmap(bitmapAutoGallery);

                        //This is required in order to make notification appear automatically
                        //However, a delay is required because if it appears to soon on phone, it will not appear on Glass
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                notifications();
                            }
                        }, 2000);

                    }
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
        //This code is to display the selected image from Gallery and convert to Bitmap
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();

            //This is required to make a bitmap out of URI. Notifications can only display bitmap
            try {
                bitmapSelectGallery = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(), imageUri);

            } catch (Exception e) {

            }
            imageView.setImageBitmap(bitmapSelectGallery);

            //This is required in order to make notification appear automatically:
            notifications();
        }
    }



    //This can be used for when I have the code that checks if a new image has been added to gallery:
public void attemptToRefresh(){

    Intent intent = getIntent();
    finish();
    startActivity(intent);


}
    public void notifications(){
        //This code is required to send notifications to the phone and Google Glass
        //Google Glass automatically will display phone notifications as part of its design

            //This is used to open the new screen when the notification is clicked on the phone:
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
            if (bitmapSelectGallery !=null){

                //bitmapSelectGallery is for images selected from Gallery on phone
                //Need to resize bitmaps otherwise app will crash and/or not display photo correctly
                finalBitmapPic = Bitmap.createScaledBitmap(bitmapSelectGallery, 500, 800, false);
            }
            else{
                //bitmapAutoGallery is for the image that auto loads on app since it is latest image in Gallery
                //Need to resize bitmaps otherwise app will crash and/or not display photo correctly
                finalBitmapPic = Bitmap.createScaledBitmap(bitmapAutoGallery, 500, 800, false);
            }

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)

                    //LargeIcon needs to be updated to pull from app
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
}

