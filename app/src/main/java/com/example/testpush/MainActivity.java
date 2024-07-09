package com.example.testpush;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.clevertap.android.geofence.CTGeofenceSettings.ACCURACY_HIGH;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.clevertap.android.geofence.CTGeofenceAPI;
import com.clevertap.android.geofence.CTGeofenceSettings;
import com.clevertap.android.geofence.Logger;
import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener;
import com.clevertap.android.geofence.interfaces.CTLocationUpdatesListener;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CTInboxStyleConfig;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.CleverTapInstanceConfig;
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CTInboxStyleConfig;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.Constants;
import com.clevertap.android.sdk.InAppNotificationButtonListener;
import com.clevertap.android.sdk.inapp.CTLocalInApp;
import com.clevertap.android.sdk.inbox.CTInboxMessage;
import com.clevertap.android.sdk.interfaces.NotificationHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.CleverTapInstanceConfig;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements CTInboxListener, InAppNotificationButtonListener {

    CleverTapAPI clevertapDefaultInstance;
    private WebView webView;
    private TextView inboxMessageCountValueTextView,inboxUnreadMessageCountValueTextView;
    private FirebaseAnalytics mFirebaseAnalytics;


    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        CleverTapAPI cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(this);
        cleverTapDefaultInstance.enablePersonalization();
//        String name = (String) clevertapDefaultInstance.profile.getProperty("Name");
//        if (name != null) {
//            // Assuming you have a TextView named customerTypeTextView in your layout
//            TextView nameTextView = findViewById(R.id.nameTextView);
//            nameTextView.setText("Name is: " + name);
//        }


        if (cleverTapDefaultInstance != null) {
            //Set the Notification Inbox Listener
            cleverTapDefaultInstance.setCTNotificationInboxListener(this);
            //Initialize the inbox and wait for callbacks on overridden methods
            cleverTapDefaultInstance.initializeInbox();
            clevertapDefaultInstance.showAppInbox();
        }
        // each of the below mentioned fields are optional
//        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
//        profileUpdate.put("Name", "Elena Gilbert");    // String
//        profileUpdate.put("Identity", 2134059);      // String or number
//        profileUpdate.put("Email", "elena@gmail.com"); // Email address of the user
//        profileUpdate.put("Phone", "+15679403608");   // Phone (with the country code, starting with +)
//        profileUpdate.put("Gender", "F");             // Can be either M or F
//        //profileUpdate.put("DOB", new Date());         // Date of Birth. Set the Date object to the appropriate value first
//// optional fields. controls whether the user will be sent email, push etc.
//        profileUpdate.put("MSG-email", false);        // Disable email notifications
//        profileUpdate.put("MSG-push", true);          // Enable push notifications
//        profileUpdate.put("MSG-sms", false);          // Disable SMS notifications
//        profileUpdate.put("MSG-whatsapp", true);      // Enable WhatsApp notifications
//        ArrayList<String> stuff = new ArrayList<String>();
//        stuff.add("bag");
//        stuff.add("shoes");
//        profileUpdate.put("MyStuff", stuff);                        //ArrayList of Strings
//        String[] otherStuff = {"Jeans","Perfume","Tops","Mobile"};
//        profileUpdate.put("MyStuff", otherStuff);                   //String Array
//
//        clevertapDefaultInstance.onUserLogin(profileUpdate);
        clevertapDefaultInstance.pushEvent("Sample push triggered!!!");
        CleverTapAPI.createNotificationChannel(getApplicationContext(), "123456", "Your Channel Name", "Your Channel Description", NotificationManager.IMPORTANCE_MAX, true);
        clevertapDefaultInstance.setInAppNotificationButtonListener(this);


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setUserProperty("ct_objectId", Objects.requireNonNull(CleverTapAPI.getDefaultInstance(this)).getCleverTapID());
        CTGeofenceSettings ctGeofenceSettings = new CTGeofenceSettings.Builder()
                .enableBackgroundLocationUpdates(true)//boolean to enable background location updates
                .setLogLevel(Logger.VERBOSE)//Log Level
                .setLocationAccuracy(ACCURACY_HIGH)//byte value for Location Accuracy
                .setLocationFetchMode(CTGeofenceSettings.FETCH_LAST_LOCATION_PERIODIC)//byte value for Fetch Mode
                .setGeofenceMonitoringCount(45)//int value for number of Geofences CleverTap can monitor
                .setSmallestDisplacement(200)//float value for smallest Displacement in meters
                .setGeofenceNotificationResponsiveness(0)// int value for geofence notification responsiveness in milliseconds
                .build();
        CTGeofenceAPI.getInstance(getApplicationContext()).init(ctGeofenceSettings,clevertapDefaultInstance);

        try {
            CTGeofenceAPI.getInstance(getApplicationContext()).triggerLocation();
        } catch (IllegalStateException e){
            // thrown when this method is called before geofence SDK initialization
        }

        CTGeofenceAPI.getInstance(getApplicationContext())
                .setOnGeofenceApiInitializedListener(new CTGeofenceAPI.OnGeofenceApiInitializedListener() {
                    @Override
                    public void OnGeofenceApiInitialized() {
                        //App is notified on the main thread that CTGeofenceAPI is initialized
                    }
                });

        CTGeofenceAPI.getInstance(getApplicationContext())
                .setCtGeofenceEventsListener(new CTGeofenceEventsListener() {
                    @Override
                    public void onGeofenceEnteredEvent(JSONObject jsonObject) {
                        //Callback on the main thread when the user enters Geofence with info in jsonObject
                        System.out.println("onGeofenceEnteredEvent: " + jsonObject.toString());
                    }

                    @Override
                    public void onGeofenceExitedEvent(JSONObject jsonObject) {
                        //Callback on the main thread when user exits Geofence with info in jsonObject
                        System.out.println("onGeofenceExitedEvent: " + jsonObject.toString());
                    }
                });

        CTGeofenceAPI.getInstance(getApplicationContext())
                .setCtLocationUpdatesListener(new CTLocationUpdatesListener() {
                    @Override
                    public void onLocationUpdates(Location location) {
                        //New location on the main thread as provided by the Android OS
                    }
                });
//        CleverTapAPI.setNotificationHandler((NotificationHandler)new PushTemplateNotificationHandler());
//        if (clevertapDefaultInstance != null) {
//            //Set the Notification Inbox Listener
//            clevertapDefaultInstance.setCTNotificationInboxListener(this);
//            //Initialize the inbox and wait for callbacks on overridden methods
//            clevertapDefaultInstance.initializeInbox();
//        }


//        Bundle extras = intent.getExtras();
//        if (extras != null) {
//            String actionId = extras.getString("actionId");
//            if (actionId != null) {
//                Log.d("ACTION_ID", actionId);
//                boolean autoCancel = extras.getBoolean("autoCancel", true);
//                int notificationId = extras.getInt("notificationId", -1);
//                if (autoCancel && notificationId > -1) {
//                    NotificationManager notifyMgr =
//                            (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                    notifyMgr.cancel(notificationId);  // the bit that cancels the notification
//                }
//                Toast.makeText(getBaseContext(),"Action ID is: "+actionId,
//                        Toast.LENGTH_SHORT).show();
//            }
//        }

        EditText name = findViewById(R.id.editTextName);
        EditText email = findViewById(R.id.editTextEmail);
        EditText phone = findViewById(R.id.editTextPhone);
        EditText i = findViewById(R.id.editText);
        Button b1 = findViewById(R.id.button1);
        Button b2 = findViewById(R.id.button3);
        Button b3 = findViewById(R.id.button4);
        Button b4 = findViewById(R.id.button5);
        Button b5 = findViewById(R.id.button6);
        Button b6 = findViewById(R.id.button7);
        Button b7 = findViewById(R.id.button8);
        Button b8 = findViewById(R.id.button9);
        Button b9 = findViewById(R.id.button2);
        //Button b10=findViewById(R.id.button10);
        Button b11 = findViewById(R.id.button11);
        Button b12 = findViewById(R.id.button12);
        Button b13 = findViewById(R.id.button13);
        Button b14 = findViewById(R.id.button14);

        RadioGroup genderRadioGroup = findViewById(R.id.radioGroupGender);
        RadioButton maleRadioButton = findViewById(R.id.radioButtonMale);
        RadioButton femaleRadioButton = findViewById(R.id.radioButtonFemale);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Push event button triggered");
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Manual Carousel Push event triggered");
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Ratings Push event triggered");
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Product Catalog Push event triggered");
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Product Catalog Push event triggered");
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Five Icons Push event triggered");
            }
        });

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Timer Push event triggered");
            }
        });

        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("\n" +
                        "Zero Bezel Push event triggered");
            }
        });

        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Input Box Push event triggered");
            }
        });


        b11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredName = name.getText().toString();
                String enteredEmail = email.getText().toString();
                String enteredPhone = phone.getText().toString();
                String enteredIdentity = i.getText().toString();
                String selectedGender = "";
                int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                if (selectedGenderId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedGenderId);
                    selectedGender = selectedRadioButton.getText().toString();
                }

                HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
                profileUpdate.put("Name", enteredName);    // String
                profileUpdate.put("Identity", enteredIdentity);      // String or number
                profileUpdate.put("Email", enteredEmail); // Email address of the user
                profileUpdate.put("Phone", enteredPhone);   // Phone (with the country code, starting with +)
                profileUpdate.put("Gender", selectedGender);             // Can be either M or F
                profileUpdate.put("DOB", new Date());         // Date of Birth. Set the Date object to the appropriate value first
// optional fields. controls whether the user will be sent email, push etc.
                profileUpdate.put("MSG-email", false);        // Disable email notifications
                profileUpdate.put("MSG-push", true);          // Enable push notifications
                profileUpdate.put("MSG-sms", false);          // Disable SMS notifications
                profileUpdate.put("MSG-whatsapp", true);      // Enable WhatsApp notifications
//                ArrayList<String> stuff = new ArrayList<String>();
//                stuff.add("bag");
//                stuff.add("shoes");
//                profileUpdate.put("MyStuff", stuff);                        //ArrayList of Strings
//                String[] otherStuff = {"Jeans","Perfume","Tops","Mobile"};
//                profileUpdate.put("MyStuff", otherStuff);                   //String Array

                clevertapDefaultInstance.onUserLogin(profileUpdate);
                Toast.makeText(getBaseContext(), "User " + enteredName + " saved", Toast.LENGTH_SHORT).show();
            }
        });


        b12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open Inbox(Customised, with tabs)
                ArrayList<String> inboxTabs = new ArrayList<>();
                inboxTabs.add("Promotions");
                inboxTabs.add("Offers");
                inboxTabs.add("Others"); // Anything after the first 2 will be ignored

                CTInboxStyleConfig inboxStyleConfig = new CTInboxStyleConfig();
                inboxStyleConfig.setTabs(inboxTabs); // Do not use this if you don't want to use tabs
                inboxStyleConfig.setTabBackgroundColor("#FF0000");
                inboxStyleConfig.setSelectedTabIndicatorColor("#0000FF");
                inboxStyleConfig.setSelectedTabColor("#000000");
                inboxStyleConfig.setUnselectedTabColor("#FFFFFF");
                inboxStyleConfig.setBackButtonColor("#FF0000");
                inboxStyleConfig.setNavBarTitleColor("#FF0000");
                inboxStyleConfig.setNavBarTitle("MY INBOX");
                inboxStyleConfig.setNavBarColor("#FFFFFF");
                inboxStyleConfig.setInboxBackgroundColor("#00FF00");
                inboxStyleConfig.setFirstTabTitle("Basic Inbox");

                if (clevertapDefaultInstance != null) {
                    clevertapDefaultInstance.showAppInbox(inboxStyleConfig); // Opens activity With Tabs
                }


            }
        });

        b13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Custom inapp event triggered!!!");
                System.out.println("This is custom event call-abc");
            }
        });

        b14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clevertapDefaultInstance.pushEvent("Push primer event triggered!!!");
                System.out.println("This is push primer event-abc");
                JSONObject jsonObject = CTLocalInApp.builder()
                        .setInAppType(CTLocalInApp.InAppType.HALF_INTERSTITIAL)
                        .setTitleText("Get Notified")
                        .setMessageText("Please enable notifications on your device to use Push Notifications.")
                        .followDeviceOrientation(true)
                        .setPositiveBtnText("Allow")
                        .setNegativeBtnText("Cancel")
                        .setBackgroundColor("#FFFFFF")
                        .setBtnBorderColor("#0000FF")
                        .setTitleTextColor("#0000FF")
                        .setMessageTextColor("#000000")
                        .setBtnTextColor("#FFFFFF")
                        .setImageUrl("https://icon-library.com/images/push-notification-icon/push-notification-icon-14.jpg")
                        .setBtnBackgroundColor("#0000FF")
                        .build();
                clevertapDefaultInstance.promptPushPrimer(jsonObject);
                System.out.println(jsonObject.toString());
                Toast.makeText(getBaseContext(),"Push Primer Button clicked!!!",Toast.LENGTH_SHORT).show();
            }
        });

        System.out.println("Permission granted or not for push primer?"+clevertapDefaultInstance.isPushPermissionGranted());
        // Initialize TextView
        inboxMessageCountValueTextView = findViewById(R.id.inboxMessageCountValueTextView);
        inboxUnreadMessageCountValueTextView=findViewById(R.id.inboxUnreadMessageCountValueTextView);

        // Fetch inbox message count from backend and update UI
        fetchInboxMessageCountAndUpdateUI();
        fetchUnreadInboxMessageCountAndUpdateUI();

        // Total inbox message count
        System.out.println("Total inbox message count = " + clevertapDefaultInstance.getInboxMessageCount());

// Unread inbox message count
        System.out.println("Unread inbox message count = " + clevertapDefaultInstance.getInboxMessageUnreadCount());

        System.out.println("All inbox messages = " + clevertapDefaultInstance.getAllInboxMessages());
        System.out.println("All inbox messages = " + clevertapDefaultInstance.getUnreadInboxMessages());

//        List<CTInboxMessage> inboxMessages = clevertapDefaultInstance.getAllInboxMessages();
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("All inbox messages = [");
//        for (CTInboxMessage message : inboxMessages) {
//            stringBuilder.append(message.toString()).append(", ");
//        }
//        stringBuilder.append("]");
//        System.out.println(stringBuilder.toString());

        List<CTInboxMessage> inboxMessages = clevertapDefaultInstance.getAllInboxMessages();
        if (inboxMessages != null) {
            for (CTInboxMessage message : inboxMessages) {
                System.out.println("All inbox messages ID = " + message.getMessageId());
            }
        }


        List<CTInboxMessage> unreadMessages = clevertapDefaultInstance.getUnreadInboxMessages();
        if (unreadMessages != null) {
            for (CTInboxMessage message : unreadMessages) {
                System.out.println("All unread inbox messages ID = " + message.getMessageId());
            }
        }

        //Retrieve the 1st message with its corresponding message id
        String firstMessageId = null;
        List<CTInboxMessage> allInboxMessages = clevertapDefaultInstance.getAllInboxMessages();
        if (allInboxMessages != null && !allInboxMessages.isEmpty()) {
            firstMessageId = allInboxMessages.get(0).getMessageId();
        }

        if (firstMessageId != null) {
            CTInboxMessage inboxMessageForId = clevertapDefaultInstance.getInboxMessageForId(firstMessageId);
            if (inboxMessageForId != null) {
                System.out.println("inboxMessage For Id " + firstMessageId + " = " + inboxMessageForId.getData());
            } else {
                System.out.println("inboxMessage For Id " + firstMessageId + " is null");
            }
        } else {
            System.out.println("inboxMessage Id is null");
        }

        //This Java code retrieves the first inbox message from the list of inbox messages, deletes it if it exists, and prints its message ID. If the inbox message is null or the list is empty, it prints "inboxMessage is null".
        CTInboxMessage firstMessage = null;
        List<CTInboxMessage> inbox_Messages = clevertapDefaultInstance.getAllInboxMessages();
        if (inbox_Messages != null && !inbox_Messages.isEmpty()) {
            firstMessage = inbox_Messages.get(0);
        }

        if (firstMessage != null) {
            clevertapDefaultInstance.deleteInboxMessage(firstMessage);
            System.out.println("Deleted inboxMessage = " + firstMessage.getMessageId());
        } else {
            System.out.println("inboxMessage is null");
        }


        List<String> messageIDs = new ArrayList<>();
        List<CTInboxMessage> unread_Messages = clevertapDefaultInstance.getUnreadInboxMessages();
        if (unread_Messages != null) {
            for (CTInboxMessage message : unread_Messages) {
                messageIDs.add(message.getMessageId());
            }
        }

        if (!messageIDs.isEmpty()) {
            clevertapDefaultInstance.deleteInboxMessagesForIDs((ArrayList<String>) messageIDs);
            System.out.println("Deleted list of inboxMessages For IDs = " + messageIDs);
        } else {
            System.out.println("No unread inbox messages to delete");
        }

        // Fetch custom data from the first inbox message
        CTInboxMessage first_Message = clevertapDefaultInstance.getAllInboxMessages().isEmpty() ? null : clevertapDefaultInstance.getAllInboxMessages().get(0);
        String customData = (firstMessage != null) ? String.valueOf(first_Message.getCustomData()) : null;

// Print the custom data
        System.out.println("inboxMessage customData = " + customData);


    }

    private void fetchInboxMessageCountAndUpdateUI() {
        // Make a network request to fetch inbox message count from backend
        // For demonstration purposes, let's assume inboxMessageCount is obtained from a hypothetical API
        int inboxMessageCount = clevertapDefaultInstance.getInboxMessageCount(); // This is a placeholder, replace it with actual value obtained from API

        // Update UI with inboxMessageCount
        inboxMessageCountValueTextView.setText("Inbox Count: "+inboxMessageCount);
    }

    private void fetchUnreadInboxMessageCountAndUpdateUI() {
        // Make a network request to fetch inbox message count from backend
        // For demonstration purposes, let's assume inboxMessageCount is obtained from a hypothetical API
        int unreadinboxMessageCount = clevertapDefaultInstance.getInboxMessageUnreadCount(); // This is a placeholder, replace it with actual value obtained from API

        // Update UI with inboxMessageCount
        inboxUnreadMessageCountValueTextView.setText("Unread Inbox Count: "+unreadinboxMessageCount);
    }




    @Override
    public void inboxDidInitialize() {
        Button b10=findViewById(R.id.button10);
        b10.setOnClickListener(v -> {
            clevertapDefaultInstance.pushEvent("Android simple inbox event pushed!");
            Toast.makeText(getBaseContext(),"Button clicked!!!",Toast.LENGTH_SHORT).show();
            ArrayList<String> tabs = new ArrayList<>();
            tabs.add("Promotions");
            tabs.add("Offers");//We support upto 2 tabs only. Additional tabs will be ignored

            CTInboxStyleConfig styleConfig = new CTInboxStyleConfig();
            styleConfig.setFirstTabTitle("First Tab");
            styleConfig.setTabs(tabs);//Do not use this if you don't want to use tabs
            styleConfig.setTabBackgroundColor("#FF0000");
            styleConfig.setSelectedTabIndicatorColor("#0000FF");
            styleConfig.setSelectedTabColor("#0000FF");
            styleConfig.setUnselectedTabColor("#FFFFFF");
            styleConfig.setBackButtonColor("#FF0000");
            styleConfig.setNavBarTitleColor("#FF0000");
            styleConfig.setNavBarTitle("MY INBOX");
            styleConfig.setNavBarColor("#FFFFFF");
            styleConfig.setInboxBackgroundColor("#ADD8E6");
            if (clevertapDefaultInstance != null) {
                clevertapDefaultInstance.showAppInbox(styleConfig); //With Tabs
            }
            //ct.showAppInbox();//Opens Activity with default style configs
        });
    }

    @Override
    public void inboxMessagesDidUpdate() {

    }

    @Override
    public void onInAppButtonClick(HashMap<String, String> hashMap) {
        if(hashMap != null){
            // Iterate over the key-value pairs in the hashMap
            for (String key : hashMap.keySet()) {
                String value = hashMap.get(key);
                System.out.println("Key: " + key + ", Value: " + value);
            }
        }
    }


//    @Override
//    public void inboxDidInitialize() {
//        b10.setOnClickListener(v -> {
//            ArrayList<String> tabs = new ArrayList<>();
//            tabs.add("Promotions");
//            tabs.add("Offers");//We support upto 2 tabs only. Additional tabs will be ignored
//
//            CTInboxStyleConfig styleConfig = new CTInboxStyleConfig();
//            styleConfig.setFirstTabTitle("First Tab");
//            styleConfig.setTabs(tabs);//Do not use this if you don't want to use tabs
//            styleConfig.setTabBackgroundColor("#FF0000");
//            styleConfig.setSelectedTabIndicatorColor("#0000FF");
//            styleConfig.setSelectedTabColor("#0000FF");
//            styleConfig.setUnselectedTabColor("#FFFFFF");
//            styleConfig.setBackButtonColor("#FF0000");
//            styleConfig.setNavBarTitleColor("#FF0000");
//            styleConfig.setNavBarTitle("MY INBOX");
//            styleConfig.setNavBarColor("#FFFFFF");
//            styleConfig.setInboxBackgroundColor("#ADD8E6");
//            if (clevertapDefaultInstance != null) {
//                clevertapDefaultInstance.showAppInbox(styleConfig); //With Tabs
//            }
//            //ct.showAppInbox();//Opens Activity with default style configs
//        });
//    }
//
//    @Override
//    public void inboxMessagesDidUpdate() {
//
//    }
}