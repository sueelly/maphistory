package android.scroll.tlllllll;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    Fragment1 fragment1;
    Fragment2 fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }




}