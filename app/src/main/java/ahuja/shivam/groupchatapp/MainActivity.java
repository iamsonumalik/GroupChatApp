package ahuja.shivam.groupchatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuWrapperFactory;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.http.Url;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_SIGN_IN =1 ;
    private static final int PHOTO_REQUEST_CODE =007 ;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFirebaseDatabaseReference;
    private String mUsername;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ChildEventListener mChildEventListener;
    private SharedPreferences msharedPreferences;
    private SharedPreferences.Editor mEditor;
    private StorageReference mFirebaseStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);
        //sharedPreferences
        msharedPreferences=getSharedPreferences("USER",MODE_PRIVATE);
        mEditor=msharedPreferences.edit();



        //firebase
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mFirebaseStorageReference= FirebaseStorage.getInstance().getReference().child("chat_photos");
        mFirebaseAuth= FirebaseAuth.getInstance();

        mFirebaseDatabaseReference=mFirebaseDatabase.getReference().child("message");

        mAuthStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               FirebaseUser firebaseuser=firebaseAuth.getCurrentUser();
                if(firebaseuser!=null)
                {
                    mEditor.putString("username",firebaseuser.getDisplayName());
                    mEditor.commit();
                    Log.d("shivan ahuja","logged in");


                loginInitialize(firebaseuser.getDisplayName());
                    Log.d("shivan ahuja","after login initialize");
                }
                else{
                    Log.d("shivan ahuja","logged out");
                    logedoutRemove();
                    Log.d("shivan ahuja","after logout remove");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,AuthUI.GOOGLE_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };




        // Initialize message ListView and its adapter
        List<MessagePOJOClass> messagePOJOClasses = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_layout, messagePOJOClasses);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(intent,"choose one"),PHOTO_REQUEST_CODE);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});



        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                MessagePOJOClass messagePOJOClass =new MessagePOJOClass(mMessageEditText.getText().toString(),mUsername,null);
                mFirebaseDatabaseReference.push().setValue(messagePOJOClass);
                // Clear input box
                mMessageEditText.setText("");
            }
        });
    }

    private void logedoutRemove() {
    mUsername=ANONYMOUS;
        stopReading();
    }

    private void loginInitialize(String displayName) {
        Log.d("shivan ahuja"," in login initialize");
        mUsername=displayName;
        Log.d("shivan ahuja","after setting username");
        readMsgs();
        Log.d("shivan ahuja"," after reading msgs");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.sign_out_menu)
        {
            AuthUI.getInstance().signOut(MainActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("shivan ahuja","in on Pause");
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        stopReading();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN)
        {
            if(resultCode==RESULT_OK)
            {
                Toast.makeText(MainActivity.this,"thanks for signing in",Toast.LENGTH_LONG).show();

            }
            else if(resultCode==RESULT_CANCELED)
            {
                finish();
            }
        }
        else if(requestCode==PHOTO_REQUEST_CODE && resultCode==RESULT_OK)
        {
            Uri imageuri=data.getData();
            String imagename=imageuri.getLastPathSegment();
            StorageReference photostoragereference = mFirebaseStorageReference.child(imagename);
            photostoragereference.putFile(imageuri).addOnSuccessListener(this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadurl= taskSnapshot.getDownloadUrl();
                    MessagePOJOClass message=new MessagePOJOClass(null,mUsername,downloadurl.toString());
                    mFirebaseDatabaseReference.push().setValue(message);
                    Toast.makeText(MainActivity.this,"image sent successfully",Toast.LENGTH_SHORT).show();

                }
            });


        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("shivan ahuja","in on reasume");
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }
    void readMsgs()
    {Log.d("shivan ahuja"," in readmsgs");
        if(mChildEventListener==null) {
            Log.d("shivan ahuja","childevent  listener is null inread msgs");
            mChildEventListener=new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d("shivan ahuja","in child added ");
                    MessagePOJOClass messagePOJOClass =dataSnapshot.getValue(MessagePOJOClass.class);
                    mMessageAdapter.add(messagePOJOClass);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            mFirebaseDatabaseReference.addChildEventListener(mChildEventListener);
            Log.d("shivan ahuja"," after attaching child event listener");
        }
    }

    void stopReading()
    {Log.d("shivan ahuja","in stop reading");
        if(mChildEventListener!=null)
        {Log.d("shivan ahuja"," if child event listener is not null");
            mMessageAdapter.clear();
            mFirebaseDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener=null;
        }
    }
}
