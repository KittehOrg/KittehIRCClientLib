Using KICL on Android
=====================

KICL can be used within an Android application. This requires some configuration, because KICL uses features only available in Java 8:

### Add the library dependency

In `app/build.gradle` `dependencies` section:

```kotlin
implementation("org.kitteh.irc:client-lib:VERSIONHERE")
```

### Ensure minSdkVersion >= 24

In `app/build.gradle`:

```groovy
android {
    // ...
    defaultConfig {
        applicationId "com.example.kiclandroidtest"
        minSdkVersion 34
        // ...
    }
}
```

(Use KICL 9.0.0 if you need to go earlier than Android 14 - you can then use minSdkVersion of 24)

### Ensure source and target compatibility are set for Java 17

In `app/build.gradle`:

```groovy
android {
    // ...
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

(If using KICL 9.0.0, set this to Java 8)

### Filter out duplicate META-INF files

In `app/build.gradle`:

```groovy
android {
    // ...
    packagingOptions {
        exclude 'META-INF/INDEX.LIST'
        exclude 'META-INF/io.netty.versions.properties'
    }
}
```

### Add internet permission

In `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.kiclandroidtest">
    <uses-permission android:name="android.permission.INTERNET"/>
    <!-- ... -->
</manifest>
```

### Write some code

Simple asynchronous task to connect to an IRC network and send a message:

```java
import android.os.AsyncTask;

import org.kitteh.irc.client.library.Client;

public class ConnectIrcTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        Client client = Client.builder().nick("KittehAndroid").server().host("localhost").then().buildAndConnect();

        client.addChannel("#kittehandroid");
        client.sendMessage("#kittehandroid", "Hello World!");
        return null;
    }
}
```


And run it somewhere in an activity listener:

```java
FloatingActionButton fab = findViewById(R.id.fab);
fab.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Snackbar.make(view, "Connecting to IRC", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        new ConnectIrcTask().execute();
    }
});
```

Run the app, and trigger the task:

![IRC screenshot showing successful connection and message](android_screenshot.png)
