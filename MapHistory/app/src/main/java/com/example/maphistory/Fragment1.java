package com.example.maphistory;

import static android.app.Activity.RESULT_OK;
import static com.example.maphistory.SelectDateFragment.DATE;

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
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.pedro.library.AutoPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;


public class Fragment1 extends Fragment {

    private static final String TAG = "Fragment1";

    Context context;
    OnTabItemSelectedListener listener;
    ImageView pictureImageView;
    int selectPhotoMenu;
    Button save, delete;
    ImageButton writePlace;
    DBManager dbHelper;
    Note item;

    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;
    File file;
    Bitmap resultPhotoBitmap;
    Uri uri;
    SimpleDateFormat todayDateFormat;


    SQLiteDatabase database;
    EditText where, title, article;
    String table1 = "tableString";
    String table2 = "tableImage";
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_1, container, false);

        date = (EditText) rootView.findViewById(R.id.date);
        pictureImageView = rootView.findViewById(R.id.pictureImageView);
        where = rootView.findViewById(R.id.where);
        title = rootView.findViewById(R.id.title);
        article = rootView.findViewById(R.id.article);
        delete = rootView.findViewById(R.id.delete);
        save = rootView.findViewById(R.id.save);
        writePlace = rootView.findViewById(R.id.writePlace);

        dbHelper = new DBManager(getActivity(), 1);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.insert(title.getText().toString(), date.getText().toString(), where.getText().toString(),
                        " ", " ", " " , article.getText().toString() );

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                article.setText(dbHelper.getResult());

            }
        });

        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");

            }
        });
//        imageView = (ImageView) rootView.findViewById(R.id.imageView); // 이거 무조건 setContentView 뒤에 써야함!!!!!!!!!!!!
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
            List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS_COMPONENTS,
                    Place.Field.LAT_LNG, Place.Field.VIEWPORT);

            // Build the autocomplete intent with field, county, and type filters applied
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .setHint("장소를 검색하세요")
                    .build(getActivity());
            startActivityForResult(intent, 1);

        });

//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                saveNote();
//
//            }
//        });

        //AutoPermissions.Companion.loadAllPermissions(this, 101);

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
        String filename = "capture.jpg";
        File storageDir = Environment.getExternalStorageDirectory();
        File outFile = new File(storageDir, filename);

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

                    Uri fileUri = intent.getData();

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

    public void setItem(Note item) {
        this.item = item;
    }

    public void applyItem() {

        if (item != null) {

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

            Date currentDate = new Date();
            if (todayDateFormat == null) {
                todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
            }

        }

    }

}