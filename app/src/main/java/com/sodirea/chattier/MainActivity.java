package com.sodirea.chattier;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ConstraintLayout layout = findViewById(R.id.layout);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("name", Context.MODE_PRIVATE);
        String name = prefs.getString("name", "");
        if (name == "") { // user has not entered a name yet; it is their first time opening the application
            Intent intent = new Intent(MainActivity.this, NameActivity.class);
            startActivity(intent);
        } else {
            TextView nameView = new TextView(getApplicationContext());
            nameView.setText("Welcome " + name);
            layout.addView(nameView);
        }
    }
}
