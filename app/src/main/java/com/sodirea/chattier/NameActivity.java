package com.sodirea.chattier;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        final EditText enterName = findViewById(R.id.enter_name);
        Button submit = findViewById(R.id.submit);

        // upon clicking submit, go back to main activity with the newly inputted name
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check that their name is not empty
                String name = enterName.getText().toString();
                if (name.matches("")) {
                    Toast toast = Toast.makeText(NameActivity.this, "You must enter a name!", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    // put new name in prefs, send old name back to main activity
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("name", Context.MODE_PRIVATE);
                    String oldName = prefs.getString("name", "");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("name", name);
                    editor.apply();
                    Intent intent = new Intent(NameActivity.this, MainActivity.class);
                    intent.putExtra("nameChanged", oldName);
                    startActivity(intent);
                }
            }
        });
    }
}
