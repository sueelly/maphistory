package com.example.maphistory;

import static com.example.maphistory.SelectedPlaceFragment.place_name;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.maphistory.SelectDateFragment.DATE;
import static com.example.maphistory.AppConstants.SAVE_MODIFY;
import static com.example.maphistory.AppConstants.X;
import static com.example.maphistory.AppConstants.Y;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.maphistory.database.DBManager;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.pedro.library.AutoPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

public class Fragment1 extends Fragment {

    private static final String TAG = "Fragment1";
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;

    Context context;
    OnTabItemSelectedListener listener;
    ImageView pictureImageView;
    int selectPhotoMenu;
    Button save, delete, gallery, camera;
    ImageButton writePlace;
    DBManager dbHelper;
    Note item;
    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;
    File file;
    Bitmap resultPhotoBitmap;
    Uri fileUri;
    SimpleDateFormat todayDateFormat;
    SQLiteDatabase database;
    EditText where, title, article;
    ViewGroup rootView;
    DatePickerDialog datePickerDialog;
    FloatingActionButton floatingActionButton;
    public static EditText date;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnTabItemSelectedListener) {
            listener = (OnTabItemSelectedListener) context;
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();

        if (context != null) {
            context = null;
            listener = null;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_1, container, false);
        dbHelper = new DBManager(getActivity(), 1);

        date = rootView.findViewById(R.id.date);
        pictureImageView = rootView.findViewById(R.id.pictureImageView);
        where = rootView.findViewById(R.id.where);
        title = rootView.findViewById(R.id.title);
        article = rootView.findViewById(R.id.article);
        delete = rootView.findViewById(R.id.delete);
        save = rootView.findViewById(R.id.save);
        writePlace = rootView.findViewById(R.id.writePlace);
        camera = rootView.findViewById(R.id.camera);
        gallery = rootView.findViewById(R.id.gallery);
        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.floatingActionButton);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapHistoryActivity.class);
                startActivity(intent);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                if(SAVE_MODIFY ==1) {
                    String picturePath = savePicture();
                    dbHelper.insert(title.getText().toString(), date.getText().toString(), where.getText().toString(),
                            X+"", Y+"", picturePath , article.getText().toString() );
                    Toast.makeText(getActivity(), "일기가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    dbHelper.getResult();

                    NewAndListActivity ad= (NewAndListActivity) getActivity();
                    ad.bottomNavigation.setSelectedItemId(R.id.tab2);

                }
                else if(SAVE_MODIFY ==2) {

                    resetting(item);
                    dbHelper.modifyNote(item);
                    Toast.makeText(getActivity(), "일기가 수정되었습니다.", Toast.LENGTH_SHORT).show();

                    NewAndListActivity ad= (NewAndListActivity) getActivity();
                    ad.bottomNavigation.setSelectedItemId(R.id.tab2);
                }

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.deleteNote(item);
                Toast.makeText(getActivity(), "해당 일기가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                NewAndListActivity ad= (NewAndListActivity) getActivity();
                ad.bottomNavigation.setSelectedItemId(R.id.tab2);

            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });
        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (isPhotoCaptured || isPhotoFileSaved) {
                    showDialog(AppConstants.CONTENT_PHOTO_EX);
                } else {
                    showDialog(AppConstants.CONTENT_PHOTO);
                }
            }
        });
        writePlace.setOnClickListener(v ->
        {
            //Initialize fragment
            Fragment wrt_place_fragment = new WritePlaceFragment();
            //Open fragment
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container1, wrt_place_fragment)
                    .addToBackStack(null)
                    .commit();

            if(place_name != null){
                item.address = place_name;
            }
        });

        if(place_name != null){
            //item.address = place_name;
            where.setText(place_name);
        }

        applyItem();

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showDialog(int id) {
        AlertDialog.Builder builder = null;

        switch (id) {
            case AppConstants.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectPhotoMenu = which;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectPhotoMenu == 0) {
                            showPhotoCaptureActivity();
                        } else if (selectPhotoMenu == 1) {
                            showPhotoSelectionActivity();
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                break;
            case AppConstants.CONTENT_PHOTO_EX:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo_ex, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectPhotoMenu = which;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectPhotoMenu == 0) {
                            showPhotoCaptureActivity();
                        } else if (selectPhotoMenu == 1) {
                            showPhotoSelectionActivity();
                        } else if (selectPhotoMenu == 2) {
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;

                            pictureImageView.setImageResource(R.drawable.picture1);
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                break;
            default:
                break;

        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void showPhotoCaptureActivity() {

        NewAndListActivity activity = (NewAndListActivity) getActivity();
        activity.takePicture();

//        try {
//            file = createFile();
//            if (file.exists()) {
//                file.delete();
//            }
//
//            file.createNewFile();
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//
//        if(Build.VERSION.SDK_INT >= 24) {
//            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
//        } else {
//            uri = Uri.fromFile(file);
//        }
//
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//
//        if(intent.resolveActivity(context.getPackageManager())!= null) {
//            startActivityForResult(intent, AppConstants.REQ_PHOTO_CAPTURE);
//        }
    }
    private File createFile() {
        String filename = createFilename();
        File outFile = new File(context.getFilesDir(), filename);

//        outFile = Environment.getExternalStorageDirectory();
        return outFile;
    }
    private String createFilename() {
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;
    }
    public void setPicture(String picturePath, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        resultPhotoBitmap = BitmapFactory.decodeFile(picturePath, options);

        pictureImageView.setImageBitmap(resultPhotoBitmap);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showPhotoSelectionActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, AppConstants.REQ_PHOTO_SELECTION);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (intent != null) {
            switch (requestCode) {
                case AppConstants.REQ_PHOTO_CAPTURE:  // 사진 찍는 경우
                    Log.d(TAG, "onActivityResult() for REQ_PHOTO_CAPTURE.");

                    Log.d(TAG, "resultCode : " + resultCode);


                    Bundle extras = intent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("intent");
                    pictureImageView.setImageBitmap(imageBitmap);


//                    setPicture(file.getAbsolutePath(), 8);
//                    resultPhotoBitmap = decodeSampledBitmapFromResource(file, pictureImageView.getWidth(), pictureImageView.getHeight());
//                    pictureImageView.setImageBitmap(resultPhotoBitmap);


                    break;

                case AppConstants.REQ_PHOTO_SELECTION:  // 사진을 앨범에서 선택하는 경우
                    Log.d(TAG, "onActivityResult() for REQ_PHOTO_SELECTION.");

                    fileUri = intent.getData();

                    ContentResolver resolver = context.getContentResolver();

                    try {
                        InputStream instream = resolver.openInputStream(fileUri);
                        resultPhotoBitmap = BitmapFactory.decodeStream(instream);
                        pictureImageView.setImageBitmap(resultPhotoBitmap);

                        instream.close();

                        isPhotoCaptured = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }
    }
    public static Bitmap decodeSampledBitmapFromResource(File res, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res.getAbsolutePath(), options);

//        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(res.getAbsolutePath(), options);
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    private String savePicture() {
        if (resultPhotoBitmap == null) {
            Toast.makeText(context, "No picture", Toast.LENGTH_SHORT).show();
            return "";
        }
        File photoFolder = new File(AppConstants.FOLDER_PHOTO);
        if(!photoFolder.isDirectory()) {
            Log.d(TAG, "creating photo folder : " + photoFolder);
            photoFolder.mkdirs();
        }
        String photoFilename = createFilename();
        String picturePath = photoFolder + File.separator + photoFilename;
        try {
            FileOutputStream outstream = new FileOutputStream(picturePath);
            resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
            outstream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return picturePath;
    }
    public void setItem(Note item) {
        this.item = item;
    }
    public void setDateItem(Note item, String dateOfItem) {
        this.item = item;
        item.createDateStr = dateOfItem;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void resetting(Note item) {

        String picturePath = savePicture();
        item.titleOfDiary = title.getText().toString();
        item.createDateStr = date.getText().toString();
        item.address = where.getText().toString();
        item.contents = article.getText().toString();
        item.picture = picturePath;

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void applyItem() {

        if (item != null) {

            SAVE_MODIFY = 2;
            title.setText(item.getTitleOfDiary());
            date.setText(item.getCreateDateStr());
            where.setText(item.getAddress());
            article.setText(item.getContents());

            String picturePath = item.getPicture();

            if (picturePath == null || picturePath.equals("")) {
                pictureImageView.setImageResource(R.drawable.picture1);
            } else {
                setPicture(item.getPicture(), 1);
            }

        } else {
            SAVE_MODIFY = 1;
            Date currentDate = new Date();
            if (todayDateFormat == null) {
                todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
            }
        }
    }
}