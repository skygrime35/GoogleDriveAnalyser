# Google Drive Analyser - Authentication Setup Guide

To successfully authenticate and connect your Google Drive account, you must register this app build in the Google Cloud Console. Follow these steps:

---

## Step 1: Create a Google Cloud Project
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Click the project dropdown in the top-left and select **New Project**.
3. Give your project a name (e.g., `Google Drive Analyser`) and click **Create**.

---

## Step 2: Configure the OAuth Consent Screen
1. In the left sidebar, navigate to **APIs & Services > OAuth consent screen**.
2. Select **External** (or Internal if you are using a Google Workspace account) and click **Create**.
3. Fill in the required app information:
   * **App name:** `Google Drive Analyser`
   * **User support email:** (Your email address)
   * **Developer contact information:** (Your email address)
4. Click **Save and Continue**.
5. In the **Scopes** step, click **Add or Remove Scopes**:
   * Search for and select: `.../auth/drive.readonly` (Google Drive API - Read-only access).
   * Click **Update** and then **Save and Continue**.
6. In the **Test Users** step, click **Add Users**:
   * Add the email address of the Google account you want to connect and scan.
   * Click **Save and Continue** and then **Back to Dashboard**.

---

## Step 3: Enable the Google Drive API
1. Navigate to **APIs & Services > Library** via the sidebar.
2. Search for **Google Drive API**.
3. Click on **Google Drive API** and click **Enable**.

---

## Step 4: Register the Android Client Credentials
1. Navigate to **APIs & Services > Credentials**.
2. Click **+ Create Credentials** at the top and select **OAuth client ID**.
3. Select **Android** as the Application type.
4. Fill in the following exact configuration parameters:
   * **Name:** `Android Debug Client`
   * **Package Name:** `com.skygrime.googledriveanalyser`
   * **SHA-1 Certificate Fingerprint:** `74:30:89:B7:1A:5A:8A:56:65:CC:6D:B9:CB:F5:75:F3:D3:40:E8:8E`
5. Click **Create**.

---

## Step 5: Install the APK and Run
1. Install the built APK on your device:
   ```bash
   termux-open ~/app-debug.apk
   ```
2. Open the **Drive Analyser** app, click **Sign in with Google**, select the test account you registered in Step 2, and authorize access!
