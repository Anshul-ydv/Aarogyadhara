package com.example.Aarogyadhara.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Aarogyadhara.R;
import com.example.Aarogyadhara.adapters.MessageAdapter;
import com.example.Aarogyadhara.models.ChatObject;
import com.example.Aarogyadhara.models.MessageObject;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mChat;
    private RecyclerView.Adapter mChatAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;
    EditText mMessage;
    DatabaseReference mChatMessagesDb;
    ChatObject mChatObject;
    String chatID;

    ArrayList<MessageObject> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatObject = (ChatObject) getIntent().getSerializableExtra("chatObject");
        Button mSend = findViewById(R.id.send);
        chatID = getIntent().getExtras().getString("chatID");
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        initializeMessage();
        //initializeMedia();
        getChatMessages();

    }

    @SuppressLint("WrongConstant")
    private void initializeMessage() {
        messageList = new ArrayList<>();
        mChat= findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayout.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(messageList);
        mChat.setAdapter(mChatAdapter);
    }

    private void sendMessage() {
        mMessage = findViewById(R.id.messageInput);

        String messageId = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).child("messages").push().getKey();
        final DatabaseReference newMessageDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).child("messages").child(messageId);

        final Map newMessageMap = new HashMap<>();

        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

        if(!mMessage.getText().toString().isEmpty())
            newMessageMap.put("text", mMessage.getText().toString());

        if(!mMessage.getText().toString().isEmpty())
            updateDatabaseWithNewMessage(newMessageDb, newMessageMap);

    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap){
        newMessageDb.updateChildren(newMessageMap);
        mMessage.setText(null);
        String message;
        if(newMessageMap.get("text") != null)
            message = newMessageMap.get("text").toString();
        else
            Toast.makeText(ChatActivity.this,"Try again",Toast.LENGTH_SHORT).show();

    }

    private void getChatMessages() {
        FirebaseDatabase.getInstance().getReference().child("chat").child(chatID).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){
                    String  text = "",
                            creatorID = "";
                    ArrayList<String> mediaUrlList = new ArrayList<>();

                    if(dataSnapshot.child("text").getValue() != null)
                        text = dataSnapshot.child("text").getValue().toString();
                    if(dataSnapshot.child("creator").getValue() != null)
                        creatorID = dataSnapshot.child("creator").getValue().toString();

                    if(dataSnapshot.child("media").getChildrenCount() > 0)
                        for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren())
                            mediaUrlList.add(mediaSnapshot.getValue().toString());

                    MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, text);
                    messageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(messageList.size()-1);
                    mChatAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

}
