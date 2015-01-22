package com.tues.sando.equalizator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity implements ExitDialog.Comunicator {

    private static final int SELECT_PICTURE = 1;
    private ImageView image;
    private int opened = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.mainImage);
    }

    @Override
    public void onBackPressed() {
        ExitDialog dialog = new ExitDialog();
        dialog.show(getFragmentManager(), "whatever");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_saveImage:
                if (opened == 1) {
                    image.buildDrawingCache();
                    Bitmap bm = image.getDrawingCache();
                    addImageToGallery(bm);
                    opened = 0;
                }else {
                    Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_equalize:
                //todo aaa
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onPositive() {
        this.finish();
    }

    public void openGallery(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                try {
                    image.setImageBitmap(getBitmapFromUri(selectedImageUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                opened = 1;
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public void addImageToGallery(final Bitmap bm) {
        String extr = Environment.getExternalStorageDirectory().toString();
        File mFolder = new File(extr + "/MyApp");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }
        String strF = mFolder.getAbsolutePath();
        File mSubFolder = new File(strF + "/MyApp-SubFolder");
        if (!mSubFolder.exists()) {
            mSubFolder.mkdir();
        }
        String s = "myfile.png";
        File f = new File(mSubFolder.getAbsolutePath(), s);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 70, fos);
            fos.flush();
            fos.close();
            //MediaStore.Images.Media.insertImage(getContentResolver(), bm, "Screen", "screen");
            MediaStore.Images.Media.insertImage(getContentResolver(), bm, "HistogramPic", "BULLSHLAKA");
            Toast.makeText(this, "Good game bro", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "Just shit", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
