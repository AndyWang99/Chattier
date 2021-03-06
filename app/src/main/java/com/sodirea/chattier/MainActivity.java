package com.sodirea.chattier;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
        final SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("name", Context.MODE_PRIVATE);
        final LinearLayout chat = findViewById(R.id.chat);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit("newUserName", prefs.getString("name", ""));              // when the user first connects, tell the server the user's name
            }
        }).on("newUserJoined", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {                                    // run on UI thread so that views can be added or modified
                    @Override
                    public void run() {
                        String newUserName = args[0].toString();                                    // get the name of the user who just joined
                        TextView newUserJoinedView = new TextView(MainActivity.this);
                        newUserJoinedView.setText(newUserName + " has joined the chat.");           // notifying other users of the new user's name
                        newUserJoinedView.setGravity(Gravity.CENTER);
                        chat.addView(newUserJoinedView);
                        scrollChatToBottom();
                    }
                });
            }
        }).on("userLeft", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        String disconnectedName = "";

                        try {
                            disconnectedName = data.getString("name");                        // get the name of the user who just left
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        TextView userLeftView = new TextView(MainActivity.this);
                        userLeftView.setText(disconnectedName + " has left the chat.");             // notifying other users of the leaving user's name
                        userLeftView.setGravity(Gravity.CENTER);
                        chat.addView(userLeftView);
                        scrollChatToBottom();
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
                            oldName = data.getString("oldName");                                 // get the user's old name from which they changed from
                            newName = data.getString("newName");                                 // get the user's new name
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        TextView nameChanged = new TextView(MainActivity.this);
                        nameChanged.setText(oldName + " has changed their name to " + newName + ".");   // notifying other users of the change in names
                        nameChanged.setGravity(Gravity.CENTER);
                        chat.addView(nameChanged);
                        scrollChatToBottom();
                    }
                });
            }
        }).on("receiveMessage", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        String name = "";
                        String message = "";

                        try {
                            name = data.getString("name");                                    // get the sending user's name
                            message = data.getString("message");                              // get the message that was sent
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        TextView displayMessage = new TextView(MainActivity.this);
                        displayMessage.setText(name + ": " + message);                              // display the message to the receiving client
                        chat.addView(displayMessage);
                        scrollChatToBottom();
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("name", Context.MODE_PRIVATE);
        final String name = prefs.getString("name", "");
        final LinearLayout chat = findViewById(R.id.chat);

        if (name == "") {
            // user has not entered a name yet, so make them enter a name
            Intent intent = new Intent(MainActivity.this, NameActivity.class);
            startActivity(intent);
        } else {
            // check if the user changed their name, and tell the server to update accordingly
            Intent previousIntent = getIntent();
            String oldName = previousIntent.getStringExtra("nameChanged");
            if (oldName != null) {
                socket.emit("updateUserName", name);
            }

            socket.connect();                                      // connecting client to server
            configureSocketEvents();                               // creating emitter listeners

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

            // button to submit the text inputted in the EditText to the chat
            final EditText inputText = findViewById(R.id.input_text);
            Button enterText = findViewById(R.id.submit_text);
            enterText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String myInput = inputText.getText().toString();
                    if (!myInput.matches("")) {                                   // only submit if EditText is not empty
                        TextView message = new TextView(MainActivity.this);
                        message.setText(name + ": " + myInput);
                        chat.addView(message);                                           // showing the message on the sender's client
                        scrollChatToBottom();
                        socket.emit("sendMessage", myInput);                       // send message to server to relay to other clients
                        inputText.setText("");                                           // clearing the text after the user sends their message
                    }
                }
            });
        }
    }

    public void scrollChatToBottom() {
        final ScrollView chatScroll = findViewById(R.id.chat_scroll);
        chatScroll.post(new Runnable() {
            @Override
            public void run() {
                chatScroll.fullScroll(ScrollView.FOCUS_DOWN);           // scrolls the chat to the bottom
            }
        });
    }
}
