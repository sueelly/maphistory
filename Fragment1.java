package android.scroll.tlllllll;

import static android.scroll.tlllllll.SelectDateFragment.DATE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;
import java.util.zip.Inflater;


public class Fragment1 extends Fragment {

    ImageView imageView;
    Uri uri;
    SQLiteDatabase database;
    String table1 = "tableString";
    String table2 = "tableImage";
    public Context context;
    ViewGroup rootView;
    DatePickerDialog datePickerDialog;
    FloatingActionButton floatingActionButton;
    public static EditText date;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_1, container, false);

        date = (EditText) rootView.findViewById(R.id.date);
        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getActivity().getSupportFragmentManager(),"datePicker");

            }
        });

        return rootView;
//        imageView = findViewById(R.id.imageView); // 이거 무조건 setContentView 뒤에 써야함!!!!!!!!!!!!
    }


//    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if(result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        uri =result.getData().getData();
//
//                        try {
//                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                            imageView.setImageBitmap(bitmap);
//                        }
//                        catch (Exception e) {
//                            Toast.makeText( getApplicationContext(),"실패", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            });
//
//    public void getImage(View view) {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        startActivityResult.launch(intent);
//    }

    public void saveFile(View v) {

    }

//    private void createDatabase(String databaseName) {
//        database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
//    }
//
//    private void createTable(String tableName) {
//        database.execSQL("create table if not exists " + tableName + "("
//                + " title text, "
//                + " date text, "
//                + " wheres text "
//                + " article text)"
//        );
//    }
//
//    private void insertRecord() {
//        database.execSQL("insert into " + table1
//                + "(title, date, wheres, article)"
//                + " values "
//                + "('Jhon', '20', 'asdasd', 'asdasdas')"
//
//        );
//    }

    public void deletePage(View v) {

    }

}
