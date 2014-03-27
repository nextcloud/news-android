ownCloud News Reader - Android App
==================================
The ownCloud News Reader Android App is under AGPLv3 License terms.
Status: Beta
Bugs and enhancements can be reported under: https://github.com/owncloud/News-Android-App/issues

<p>
<a href="https://play.google.com/store/apps/details?id=de.luhmer.owncloudnewsreader" alt="Download from Google Play">
  <img src="http://www.android.com/images/brand/android_app_on_play_large.png">
</a>

<a href="https://f-droid.org/repository/browse/?fdfilter=ownCloud%20News&fdid=de.luhmer.owncloudnewsreader" alt="ownCloud News App on fdroid.org">
  <img src="https://f-droid.org/wiki/images/fdroid-135.png" width="80" height="80" >
</a>
</p>


Donate
==================================
[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=david-dev&url=https://github.com/owncloud/News-Android-App&title=News-Android-App&language=JAVA&tags=github&category=software)


How to use the Beta App via Google Play ?
==================================
Join the following Google Group:
https://groups.google.com/d/forum/owncloud-news-android-app
there you can find a link (after I accepted you in the group) to Google Play for registering as beta tester of the app.



FAQ:
==================================
The app shows an empty Toast when trying to sync or sign-in (little dark box at the button of the screen)
- Make sure you're using the latest version of the news app and the appframework from GitHub. Otherwise you can use the Android version <= 0.3.3 (via Google Play or GitHub <a href="https://github.com/owncloud/News-Android-App/commits/master/News-Android-App.apk">Choose version</a>).


How to compile the App
==================================
Requirements:
-----------------------
>1) Android Studio
>2) Git installed (you can do the following stuff without git but I'm not going to show how).

Download and install:
-----------------------
>1) Open cmd/terminal
>2) Navigate to your workspace
>3) Then type in:
><pre>
git clone --recursive https://github.com/owncloud/News-Android-App.git
></pre>

for the dev branch:
><pre>
git clone --recursive https://github.com/owncloud/News-Android-App.git -b dev
></pre>


>Import the Project and start coding!


>That's all. I hope it works for you! If something is not working, please send me an email to david-dev@live.de


Updates
==================================
0.5.8 (in development)
---------------------


0.5.7 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/213">#213 When using the dark theme websites with no background color are unreadable</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/211">#211 Links within articles</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/198">#198 enable auto sync configuration</a>


0.5.6 (Google Play)
---------------------
- Fixed flickering of the screen when changing Feeds (in dark Theme)
- New Pull-To-Refresh Style
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/200">#200 Clicking article in widget makes app crash</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/196">#196 Stutter with "mark as read while scrolling" turned on</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/189">#189 Read mouse-over</a>

0.5.5
---------------------
- Improve Changelog View
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/186">#186 Missing "clear cache" in the settings (on Tablets)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/189">#189 Read mouse-over</a>
- Improvement - Fix Layout problems in DetailView
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/195">#195 Mark as read when opened in browser</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/194">#194 favIcons and imgCache show up in Gallery</a>

0.5.4
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/184">#184 Option to disable notification</a>

0.5.3
---------------------
- Update star/checkbox icons for devices with lower screen size
- Update language support
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/176">#176 Open directly in Browser</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/179">#179 Widget items not clickable</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/183">#183 Widget items color stripe position</a>

0.5.2
---------------------
- Improvement - Notification when background sync is enabled and new items are received
- Improvement - Fix high CPU-Load in Detail-View
- Improvement - Speed up image caching

0.5.0
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/162">#162 New items available notification pops up when there really aren't</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/160">#160 Widget font size</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/161">#161 'Send via' should be removed from sharing </a>

0.4.11
---------------------
- Critical Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/158">#158 0.4.10 instantly crashes when opening</a>

0.4.10 (unpublished)
---------------------
- Improvement - New Changelog Design
- Improvement - AppRater Plugin added
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/155">#155 Feed view isn't refreshed on sync</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/153">#153 Sidebar should be darker</a>
- Bug fixes

0.4.9
---------------------
- Update German Language support
- Readded full support of Android 2.2+ (was broken since 0.4.4)
- Improvement - In Landscape Mode on Tablets (7inch+) the Feed/Folder pane is always visible.
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/77">#77 There should be icons for folders and special categories</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/139">#139 Article list jumps after having article opened</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/137">#137 Back button shouldn't close app when app displays a specific feed or folder</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/151">#151 Reload slide pane when open event is triggered</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/136">#136 Fix that the translated app name is used as the folder name</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/134">#134 Sidebar - "Loading ..." font color should be brighter</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/133">#133 Refreshing after adding server data results in unauthorized</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/57">#57 Background synchronization</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/152">#152 Changing sorting direction</a>

0.4.8
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/140">#140 Open in browser and Share with controls always act on original article</a>
- Update Language support

0.4.7
---------------------
- fixed app crash when sync on startup is enabled
- faster favIcon pre-caching

0.4.6
---------------------
- Fixed app freeze when sync is finished
- Small improvements


0.4.5 (unpublished)
---------------------
- Fixed critical app crash after sync finished
- Improved security for self signed certificates. Special thanks to Dominik Schürmann (@dschuermann) <a href="https://github.com/owncloud/News-Android-App/issues/130">#130 (Implement MemorizingTrustManager to prevent MitM attacks)</a>
- Small bug fixes
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/128">#128 (»Mark all as read« is sometimes disabled)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/125">#125 (Feed list entries flash when unread count changes)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/129">#129 (Line height needs to be increased for better readability)</a>


0.4.4 (unpublished)
---------------------
- Fixed Security issue - thank you for the hint @davivel <a href="https://github.com/owncloud/News-Android-App/issues/47">#47 (can't connect to my ownCloud)</a>
- Fixed issue - <a href="https://github.com/owncloud/News-Android-App/issues/105">#105 (Androids back button does not hide empty feeds)</a>
- Fixed issue - <a href="https://github.com/owncloud/News-Android-App/issues/119">#119 ("mark all read" button has some bugs)</a>
- Fixed issue - <a href="https://github.com/owncloud/News-Android-App/issues/103">#103 (Favicons not shown)</a>
- Fixed issue - <a href="https://github.com/owncloud/News-Android-App/issues/112">#112 (Click on wrong item)</a>
- Fixed issue - <a href="https://github.com/owncloud/News-Android-App/issues/115">#115 (Database lock issue)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/117">#117 (Rearange the icons in the detail view)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/118">#118 (Add author to the new detail view header)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/120">#120 (Add coloured line to the feeds view in the average coulour of the favicon)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/107">#107 (Keep unread)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/60">#60 (Sync from unread items (or any feed view))</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/113">#113 (Long press on image to show title text)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/108">#108 (Mark as read once post is beyond the screen)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/34">#34 (Widget)</a>
- Layout improvement - <a href="https://github.com/owncloud/News-Android-App/issues/106">#106 (Option to skip list view)</a>
- Layout improvement - <a href="https://github.com/owncloud/News-Android-App/issues/55">#55 (collapsible feeds list to maximize item space on phablets)</a>
- Layout improvement - <a href="https://github.com/owncloud/News-Android-App/issues/15">#15 (make column bar between folders and newslist movable)</a>
- Layout improvement - <a href="https://github.com/owncloud/News-Android-App/issues/116">#116 (Remove About/Changelog Menu Item from the List Detail View (Second view))</a>
- Improved german translation - <a href="https://github.com/owncloud/News-Android-App/issues/88">#88 (Bad german translation)</a>

0.4.3
---------------------
- Fixed issue <a href="https://github.com/owncloud/News-Android-App/issues/104">#104 (0.4.2 does not sync)</a>
- Fix issue that sometimes Exeptions are not shown
- Update F-Droid (merge dev with master)
- Update Language Support from master branch

0.4.2
---------------------
- critical bug fix that sync was broken <a href="https://github.com/owncloud/News-Android-App/issues/102">#102 (0.4.1 doesn't sync anymore)</a>

0.4.1
---------------------
- Font settings are also applied to the item detail view now
- Fix issue that the Button "Download more items" was not working

0.4.0
---------------------
- Fixed app crash when image cache is enabled and the dialog appear which asks the user if download via roaming is allowed.
- Fixed app crash reports.
- Fixed Issue <a href="https://github.com/owncloud/News-Android-App/issues/96">#96 (Can't sync feeds - using a bad URL)</a>
- Improved <a href="https://github.com/owncloud/News-Android-App/issues/95">#95 Make font/font size user selectable</a>
- Improved <a href="https://github.com/owncloud/News-Android-App/issues/86">#86 clearing the cache while having read items prevents them from being synced</a>
- Implemented Feature <a href="https://github.com/owncloud/News-Android-App/issues/99">#99 Option to change item order new-old/old-new</a>

0.3.9
---------------------
- Support for APIv1 and APIv2. (That means the app on Google Play will be updated, too!)
- Small fixes
- Improved memory usage while synchronizing
- Auto sync of item state every 5 minutes
- Changed font style to Roboto Light

0.3.8
---------------------
- Fixed Issue when trying to download more items in "all unread" and "starred items" view.
- Added option to set up the maximum cache size.
- Fixed app crash on tablets (could crash somtimes since v.0.3.7 when trying to download more items).
- Fixed Issue <a href="https://github.com/owncloud/News-Android-App/issues/78">#78 (The cache should be cleared in the background)</a>
- Improved feature <a href="https://github.com/owncloud/News-Android-App/issues/84">#84 (Buttons to toggle the folders are hard to hit and not descriptive)</a>
- Improved feature <a href="https://github.com/owncloud/News-Android-App/issues/76">#76 (There should me more spacing between feeds and folders)</a>
- Speed optimizations in the Folder/Feed Overview
- About/Changelog added

0.3.7
---------------------
- Option to mark articles as read while scrolling <a href="https://github.com/owncloud/News-Android-App/issues/14">#14 ("mark as read" on scroll)</a>
- Rich list theme layout (WebView) <a href="https://github.com/owncloud/News-Android-App/issues/6">#6</a>
- Fixed issue <a href="https://github.com/owncloud/News-Android-App/issues/46">#46 (Android 3.2.1 crash)</a>
- Fixed issue <a href="https://github.com/owncloud/News-Android-App/issues/68">#68 (Special folder "all unread articles" shows all articles)</a>
- Fixed issue <a href="https://github.com/owncloud/News-Android-App/issues/69">#69 (Crash when image cache enabled)</a>

0.3.6
---------------------
- Option to scroll through articles with Volume rockers <a href="https://github.com/owncloud/News-Android-App/issues/61">#61 (Use volume rocker to browse through articles)</a>
- Option to download old items for feed/folder <a href="https://github.com/owncloud/News-Android-App/issues/63">#63 (Allow dowloading old items)</a>
- Light Theme for item view <a href="https://github.com/owncloud/News-Android-App/issues/59">#59 (White Theme doesn't apply to articles)</a>
- Image offline caching function asks now if you want to download if you're not connected with wifi
- Item detail optimizations

0.3.5
---------------------
- Fixed issue <a href="https://github.com/owncloud/News-Android-App/issues/52">#52 (Folders visible multiple times)</a>
- Fixed issue <a href="https://github.com/owncloud/News-Android-App/issues/53">#53 (New items get added at the bottom)</a>
- Added default feed favIcon
- Theme is now also applied in the settings screen
- Implemented <a href="https://github.com/owncloud/News-Android-App/issues/56">#56 (Click on header to open article in browser)</a>

0.3.4
---------------------
- Offline reading (Only when you sync items the marked/starred/unread/unstarred items get synchronized. This save a lot of network traffic
- Offline image caching
- Login is getting verified when you click sign-in
- Strict-Hostname-Verification (Important Security Fix)
- Simple or extended list view
- Light or dark app Theme
- Implemented <a href="https://github.com/owncloud/News-Android-App/issues/29">#29 Mark all Article in one Column as readed</a>
- A lot of other new stuff and fixes

0.3.3
---------------------
- Dark/Light App Theme
- Feed List Design Simple/Extended
- many new languages have been added

0.3.2
---------------------
- Fixed app crash when leaving item detail view.

0.3.1
---------------------
- Polish language support added (thank you for translating Cyryl)
- App crash fixed when no item header text is available
- Go back in the item view if you press the home button
- Added Up Button in detail view as fix for GitHub Issue #13
- Other small fixes

0.3.0
---------------------
- Android 2.2+ Support added
- small bugfixes
