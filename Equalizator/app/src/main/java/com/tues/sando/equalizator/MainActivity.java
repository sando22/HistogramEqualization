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
import android.widget.Button;
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
    private Bitmap equalizedPicture = null;
    private Bitmap grayscaledPicture = null;
    private Bitmap normalPicture = null;
    public Button undoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.mainImage);
        undoButton = (Button) findViewById(R.id.undo_button);
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
                switch (opened){
                    case 1:
                        addImageToGallery(grayscaledPicture);
                        opened = 0;
                        undoButton.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        addImageToGallery(equalizedPicture);
                        opened = 0;
                        undoButton.setVisibility(View.INVISIBLE);
                        break;
                    default:
                        Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            case R.id.menu_equalize:
                if (opened == 3) {
                    image.buildDrawingCache();
                    Bitmap bmToEqualize = image.getDrawingCache();
                    equalizedPicture = HistogramEqualizator.histogram_equalization(bmToEqualize);
                    image.destroyDrawingCache();
                    image.setImageBitmap(equalizedPicture);
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    opened = 2;
                    undoButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Nothing to work on", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_grayscale:
                if (opened == 3) {
                    image.buildDrawingCache();
                    Bitmap bmToGreyscale = image.getDrawingCache();
                    grayscaledPicture = GreyScaleConverter.toGrayscale(bmToGreyscale);
                    image.destroyDrawingCache();
                    image.setImageBitmap(grayscaledPicture);
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    opened = 1;
                    undoButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Nothing to work on", Toast.LENGTH_SHORT).show();
                }
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
                    normalPicture = getBitmapFromUri(selectedImageUri);
                    image.setImageBitmap(normalPicture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                opened = 3;
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
        File mFolder = new File(extr + "/Histogram equalizer");
        if (!mFolder.exists()) {
            mFolder.mkdir();
        }
        String strF = mFolder.getAbsolutePath();
        File mSubFolder = new File(strF + "/TEMP");
        if (!mSubFolder.exists()) {
            mSubFolder.mkdir();
        }
        String s = "temp.png";
        File f = new File(mSubFolder.getAbsolutePath(), s);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 70, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), bm, "CustomizedPic", "BULLSHLAKA");
            Toast.makeText(this, "Picture saved", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void undoAction(View view) {
        if (opened == 1 || opened == 2){
            image.setImageBitmap(normalPicture);
            opened = 3;
            undoButton.setVisibility(View.INVISIBLE);
        }
    }
}
