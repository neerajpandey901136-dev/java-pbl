# Android Integration Guide

To complete the full-stack system and have the Android app read real SMS messages, follow these steps to integrate Android with our Spring Boot backend.

## 1. Add Permissions to `AndroidManifest.xml`
Your Android app needs permission to read SMS and access the internet. Add these inside the `<manifest>` tag:

```xml
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.INTERNET" />
```

## 2. Create the Broadcast Receiver
Create a class that extends `BroadcastReceiver` to listen for incoming SMS messages.

```java
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String messageBody = smsMessage.getMessageBody();
                        String sender = smsMessage.getDisplayOriginatingAddress();

                        // Typical bank SMS come from identifiers like VM-HDFCBK
                        if (sender != null && sender.matches(".*[A-Z]{2}-.*")) {
                            Log.d("SmsReceiver", "Bank SMS detected: " + messageBody);
                            sendSmsToBackend(messageBody);
                        }
                    }
                }
            }
        }
    }

    private void sendSmsToBackend(String smsBody) {
        // You should implement the API call here using Retrofit, Volley, or OkHttp.
        // Example logic:
        // POST http://YOUR_BACKEND_IP:8080/api/sms/parse/{userId}
        // Body: { "sms": smsBody }
    }
}
```

## 3. Register the Receiver in `AndroidManifest.xml`
Register the receiver so it runs in the background.

```xml
<receiver android:name=".SmsReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

## 4. Request Permissions at Runtime
In your `MainActivity.java`, ensure you ask the user for permission when the app starts:

```java
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
        }
    }
}
```

## 5. Send API Request (Using OkHttp Example)

Add OkHttp to your app's `build.gradle`:
`implementation("com.squareup.okhttp3:okhttp:4.10.0")`

Implement the network call in your `sendSmsToBackend` method:

```java
import okhttp3.*;
import java.io.IOException;

public void sendSmsToBackend(String smsBody, Long userId) {
    OkHttpClient client = new OkHttpClient();
    
    String json = "{\"sms\":\"" + smsBody.replace("\"", "\\\"") + "\"}";
    RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
    
    // REPLACE WITH YOUR COMPUTER'S LOCAL IP ADDRESS
    String url = "http://192.168.1.X:8080/api/sms/parse/" + userId;

    Request request = new Request.Builder()
        .url(url)
        .post(body)
        .build();

    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e("API", "Failed to send SMS to backend", e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Log.d("API", "Backend response: " + response.body().string());
        }
    });
}
```

This completes the Android component! For your college submission, if you can't easily set up the Android app, simply use the **Simulate SMS Auto-Read** feature in the web dashboard, which tests the exact same backend logic!
