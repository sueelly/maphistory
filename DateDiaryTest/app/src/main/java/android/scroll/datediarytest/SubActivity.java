package android.scroll.datediarytest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SubActivity extends AppCompatActivity {

    Fragment1 fragment1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        fragment1 = new Fragment1();

        getSupportFragmentManager().beginTransaction().replace(R.id.subContainer, fragment1).commit();


    }
}