package com.joson.progressbarpro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * @Auther: hepiao
 * @CreatTime: 2020/9/25
 * @Description: 主活动类
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                switch (itemId) {
                    case R.id.navigation_circle:
                        navController.navigate(R.id.circleFragment);
                        break;
                    case R.id.navigation_horizontal:
                        navController.navigate(R.id.horizontalFragment);
                        break;
                    case R.id.navigation_node:
                        navController.navigate(R.id.nodeFragment);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }
}