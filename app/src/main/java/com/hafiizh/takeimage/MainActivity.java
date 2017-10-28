package com.hafiizh.takeimage;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btn_take_image;
    ImageView image;
    Intent intent;
    Uri uri;
    Bitmap bitmap, decode;

    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_FILE = 1;

    int bitmap_size = 40;
    int max_resolution = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_take_image = (Button) findViewById(R.id.btn_take_image);
        image = (ImageView) findViewById(R.id.image_view);

        btn_take_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeImage();
            }
        });
    }

    private void takeImage() {
        final String[] items = {"Take photo", "Take from gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Take image").setIcon(R.mipmap.ic_launcher).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Take photo")) {
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    uri = Uri.fromFile(getMediaFile());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[which].equals("Take from gallery")) {
                    intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_FILE);
                } else if (items[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                bitmap = BitmapFactory.decodeFile(uri.getPath());
                setImageView(resizeBitmap(bitmap, max_resolution));
            } else if (requestCode == REQUEST_FILE && data != null && data.getData() != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    setImageView(resizeBitmap(bitmap, max_resolution));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setImageView(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, bitmap_size, bytes);
        decode = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));
        image.setImageBitmap(decode);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int max_resolution) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmap_ratio = (float) width / (float) height;

        if (bitmap_ratio > 1) {
            width = max_resolution;
            height = (int) (width / bitmap_ratio);
        } else {
            height = max_resolution;
            width = (int) (height * bitmap_ratio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private File getMediaFile() {
        File storage_dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "image");
        if (!storage_dir.exists())
            if (!storage_dir.mkdirs())
                return null;
        String time_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(storage_dir.getPath() + File.separator + "image" + time_stamp + ".jpg");
    }
}
