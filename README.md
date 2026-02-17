# Busy Reply

When you're on a call and someone else rings, this app sends them an SMS with your saved message and rejects the call.

**You don't need to keep the app open.** Once you've set it as the call screening app and saved your message, the system will run it automatically when a call comes in.

## Will it work?

Yes, if you:

1. **Grant permissions** when the app asks for **Phone** and **SMS** (needed to detect “busy” and send the text).
2. **Set the app as “Call screening” app** (Android 10+): in the app, tap **“Enable call screening”** and choose this app in the system screen. Without this, the system never notifies the app about incoming calls.
3. **Save a message** in the text field and tap **Save**.

Then, when your line is busy (you’re on another call) and a new call arrives, the app will send that message to the caller and reject the call.

## How to test

### What you need

- This phone (with the app installed).
- Two ways to call it: e.g. **another phone** (Phone B) and a **second line** (e.g. work number, friend’s phone, or another SIM).

### Steps

1. **Install and open** the app.
2. **Allow** the requested permissions (Phone, SMS).
3. **Type a message** (e.g. “I’m on another call, I’ll ring you back”) and tap **Save**.
4. **Tap “Enable call screening”** and in the system dialog **select this app** as the call screening app.
5. **Start a first call** to your phone (e.g. from Phone B) and **answer** it so the line is busy.
6. **From the second line**, call your phone again while the first call is still active.
7. **Expected:**  
   - The second call is rejected (busy/voicemail).  
   - The second caller receives an **SMS** with the message you saved.

### If it doesn’t work

- Confirm you tapped **“Enable call screening”** and selected this app (Settings → Apps → Default apps or “Call screening” depending on device).
- Confirm **Phone** and **SMS** permissions are allowed in app settings.
- On some devices, “busy” is only detected when the **first call is answered**. Try again with a clear sequence: answer first call, then trigger the second call.
- If the second caller doesn’t get an SMS, check that the saved message is not empty and that the number isn’t hidden/unknown (some carriers don’t provide the number).

## Build and install

```bash
./gradlew installDebug
```

Or build and run from Android Studio.
