package com.example.maphistory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class NewAndListActivity extends AppCompatActivity implements AutoPermissionsListener, OnTabItemSelectedListener {

    Fragment1 fragment1;
    Fragment2 fragment2;

    //    public static NoteDatabase mDatabase = null;
    private static final String TAG = "MainActivity";

    File file;
    Uri uri;
    Bitmap resultPhotoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_and_list);

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

        NavigationBarView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem itemBar) {

                switch (itemBar.getItemId()) {
                    case R.id.tab1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment1).commit();
                        return true;

                    case R.id.tab2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment2).commit();
                        return true;
                }

                return false;
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this, 101);

    }


    public void takePicture() {
        try {
            file = createFile();
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);
        } else {
            uri = Uri.fromFile(file);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        startActivityForResult(intent, 101);
    }

    private File createFile() {
        String filename = "capture.jpg";
        File outFile = new File(getFilesDir(), filename);
        Log.d("Main", "File path : " + outFile.getAbsolutePath());

        return outFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            try {

                resultPhotoBitmap = decodeSampledBitmapFromResource(file, fragment1.pictureImageView.getWidth(), fragment1.pictureImageView.getHeight());
                fragment1.pictureImageView.setImageBitmap(resultPhotoBitmap);

//                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
//                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(File res, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res.getAbsolutePath(),options);

//        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(res.getAbsolutePath(),options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height;
            final int halfWidth = width;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onTabSelected(int position) {

    }

    @Override
    public void showFragment1(Note item) {
        fragment1 = new Fragment1();
        fragment1.setItem(item);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment1).commit();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, @NotNull String[] permissions) {
        Toast.makeText(this, "permissions denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, @NotNull String[] permissions) {
        Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }




}