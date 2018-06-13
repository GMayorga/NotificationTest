package com.example.mayorgag4.notificationtest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    private ImageView imageView;
    public static int NOTIFICATION_ID = 1;
    private Button button;
    public static final String KEY_NOTIFICATION_REPLY = "KEY_NOTIFICATION_REPLY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }


    private void openGallery() {
        Intent gallery =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }



    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Create PendingIntent to take us to DetailsActivity
            // as a result of notification action
            Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
            detailsIntent.putExtra("EXTRA_DETAILS_ID", 42);
            PendingIntent detailsPendingIntent = PendingIntent.getActivity(
                    MainActivity.this,
                    0,
                    detailsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Define PendingIntent for Reply action
            PendingIntent replyPendingIntent = null;
            // Call Activity on platforms that don't support DirectReply natively
            if (Build.VERSION.SDK_INT < 24) {
                replyPendingIntent = detailsPendingIntent;
            } else { // Call BroadcastReceiver on platforms supporting DirectReply
                replyPendingIntent = PendingIntent.getBroadcast(
                        MainActivity.this,
                        0,
                        new Intent(MainActivity.this, ReplyReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
            }


            Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.images);

            // NotificationCompat Builder takes care of backwards compatibility and
            // provides clean API to create rich notifications
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)

                    //LargeIcon and setStyle needs to be updated to pull from app
                    //setContentTitle needs to be updated to info about match
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setLargeIcon(pic)
                    .setContentTitle("Something important happened")
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(pic))
                    .setAutoCancel(true)
                    .setContentIntent(detailsPendingIntent)
                    .addAction(android.R.drawable.ic_menu_compass, "Details", detailsPendingIntent);

            // Obtain NotificationManager system service in order to show the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        }
    };
}

