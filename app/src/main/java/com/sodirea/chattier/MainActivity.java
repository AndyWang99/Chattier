package com.sodirea.chattier;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private Socket socket;

    {
        try {
            socket = IO.socket("http://192.168.0.111:8080");
        } catch (URISyntaxException e) {
        }
    }

    public void configureSocketEvents() {
        final SharedPreferences prefs = getApplicationContext().getSharedPreferences("name", Context.MODE_PRIVATE);
        ConstraintLayout layout = findViewById(R.id.layout);
        final LinearLayout chat = findViewById(R.id.chat);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit("newPlayerName",  prefs.getString("name", ""));
            }
        }).on("newPlayerJoined", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        String newPlayerName = "";
                        try {
                            newPlayerName = data.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        TextView newPlayerJoinedView = new TextView(MainActivity.this);
                        newPlayerJoinedView.setText(newPlayerName + " has joined the chat.");
                        chat.addView(newPlayerJoinedView);
                    }
                });
            }
        }).on("playerLeft", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        String disconnectedName = "";
                        try {
                            disconnectedName = data.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        TextView playerLeftView = new TextView(MainActivity.this);
                        playerLeftView.setText(disconnectedName + " has left the chat.");
                        chat.addView(playerLeftView);
                    }
                });
            }
        }).on("broadcastNewName", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        String oldName = "";
                        String newName = "";
                        try {
                            oldName = data.getString("oldName");
                            newName = data.getString("newName");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        TextView nameChanged = new TextView(MainActivity.this);
                        nameChanged.setText(oldName + " has changed their name to " + newName + ".");
                        chat.addView(nameChanged);
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("name", Context.MODE_PRIVATE);
        String name = prefs.getString("name", "");
        ConstraintLayout layout = findViewById(R.id.layout);
        LinearLayout chat = findViewById(R.id.chat);

        if (name == "") { // user has not entered a name yet; it is their first time opening the application
            Intent intent = new Intent(MainActivity.this, NameActivity.class);
            startActivity(intent);
        } else {
            // this only applies if they just changed their name
            Intent previousIntent = getIntent();
            String oldName = previousIntent.getStringExtra("nameChanged");
            if (oldName != null) {
                socket.emit("updatePlayerName", name);
            }

            socket.connect();
            configureSocketEvents();
            TextView nameView = findViewById(R.id.name);
            nameView.setText(name);

            // button to transition to NameActivity to change your name
            Button changeName = findViewById(R.id.change_name);
            changeName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, NameActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
