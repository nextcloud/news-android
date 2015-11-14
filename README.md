ownCloud News Reader - Android App
==================================
The ownCloud News Reader Android App is under AGPLv3 License terms.
Status: Beta
Bugs and enhancements can be reported under: https://github.com/owncloud/News-Android-App/issues

<p>
<a href="https://play.google.com/store/apps/details?id=de.luhmer.owncloudnewsreader" alt="Download from Google Play">
  <img src="http://www.android.com/images/brand/android_app_on_play_large.png">
</a>

<a href="https://f-droid.org/repository/browse/?fdid=de.luhmer.owncloudnewsreader" alt="ownCloud News App on fdroid.org">
  <img src="https://camo.githubusercontent.com/7df0eafa4433fa4919a56f87c3d99cf81b68d01c/68747470733a2f2f662d64726f69642e6f72672f77696b692f696d616765732f632f63342f462d44726f69642d627574746f6e5f617661696c61626c652d6f6e2e706e67">
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

>Run the following script "remove_invalid_languages.sh"

>Import the Project and start coding!



>That's all. I hope it works for you! If something is not working, please send me an email to david-dev@live.de


Contributors
==================================
* [David Luhmer](https://github.com/David-Development) (Maintainer)
* [Daniel Schaal](https://github.com/schaal)
* [otrichet] (https://github.com/otrichet)
* [cemrich] (https://github.com/cemrich)
* [Benjamin Stephan] (https://github.com/b3nson)


Updates
==================================
0.9.8 (in development)
---------------------


0.9.7.2 (Google Play)
---------------------
- Add profile picture support
- Bug fixes


0.9.7.1 (Google Play)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/359">#359 Feed view with full article content</a>
- UI-Tweaks - <a href="https://github.com/owncloud/News-Android-App/issues/377">#377 read and star slide</a>
- Add ShowcaseView


0.9.7 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/393">#393 Login button might get cropped / completely hidden</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/407">#407 Auto reload news after sync</a>
- Bug fixes


0.9.6.3 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/399">#399 Missing scroll indicator in article list</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/401">#401 "Open in browser" not using default browser</a>


0.9.6.2 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/394">#394 back button doesn't work correct</a>


0.9.6.1 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/381">#381 Back button doesn't work correct in articles</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/392">#392 Wrong article is shown</a>


0.9.6 (Google Play)
---------------------
- Performance improvements
- Bug fixes


0.9.5.4 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/388">#388 App crash when opening an article</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/386">#386 News always open in external browser</a>


0.9.5.3 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/383">#383 Back button doesn't close the app on tablets</a>


0.9.5.2 (Google Play)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/376">#376 Design improvements</a>


0.9.5.1 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/371">#371 App crash since 0.9.4 at startup</a>


0.9.5 (Google Play)
---------------------
- UI-Redesign (special thanks to Daniel Schaal)
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/367">#367 Widget non-functional, crashes frequently</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/366">#366 "Sync on startup" option does not sync on startup only</a>
- Bug fixes


0.9.4 (Google Play - Beta)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/363">#363 Add support for Chrome Custom Tabs</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/361">#361 Pause podcast when receiving call</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/pull/362">#362 Redesign of the login dialog (special thanks to Daniel Schaal)</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/360">#360 Fix app crash</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/364">#364 App crash on Android < 4.1</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/258">#258 login fails with long passwords</a>
- Bug fixes


0.9.3 (Google Play)
---------------------
- Several UI-Improvements (special thanks to Daniel Schaal)


0.9.2 (Google Play)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/350">#350 option to set lines-count of title</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/318">#318 Image in advanced News item</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/342">#342 Low contrast checkboxes in settings (pre Lollipop)</a>


0.9.1 (Google Play)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/343">#343 Mark as read only when scrolling past article</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/345">#345 Pause support for podcast streams</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/348">#348 Notification icon in Android 5.0 is just a white square</a>


0.9.0 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/339">#339 Does not remember position in article listing</a> 
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/338">#338 Allow App to be installed on SD-Card</a>


0.8.9.5 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/334">#334 Display error since 0.8.6 - white screen after swiping to next article</a>


0.8.8 (Google Play - Beta)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/329">#329 "Mark all as read" freezes UI for long lists.</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/333">#333 Crash when opening Settings on Android 2.3</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/332">#332 [0.8.7] Crash when opening article after mark newer item as read</a>


0.8.7 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/331">#331 Bug in 0.8.6: App crashs by opening an article in external browser</a>


0.8.6 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/303">#303 List Item opens wrong Article (off by one)</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/308">#308 Allow feeds being checked as "keep unread" instead of "read" when "mark as read while scrolling" feature is used</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/324">#324 when I read the last unread news the news are not marked as read</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/327">#327 Scrolling with volume keys produces sound...</a> (Thanks @cemrich)
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/321">#321 Disabled options menu item not greyed out in actionbar</a> (Thanks @cemrich)
- Bug fixes


0.8.5 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/311">#311 Android 5.0.2 performance</a>
- Bug fixes


0.8.4 (Google Play)
---------------------
- PLEASE NOTE: This update deletes all your un-synchronized changes. After updating you'll need to perform a manual sync.
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/288">#288 Text to Speech (TTS)</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/307">#307 App crash when favicon has a height or width of 0px</a>
- Improvement - Show dialog to share a link/open in browser on long clicking a link in the detail-view
- Improve performance


0.8.3 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/301">#301 App crashes while adding feed</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/296">#296 App crashes when cache is full</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/295">#295 Images included from relative URLs are not loaded</a>


0.8.2 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/292">#292 0.8.1: Can't save sync interval</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/282">#282 Launch by default: *rss.xml - subscribe</a>


0.8.1 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/291">#291 0.8.0: App crashes when adding new feed</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/290">#290 0.8.0: Three dot menu in light theme wrong colors (black font on dark gray background)</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/289">#289 0.8.0: Widget isn't working anymore</a>
- Bug fixes/Improvements


0.8.0 (Google Play)
---------------------
- Material Design
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/236">#236 Read items in Android News Client are not synced to server</a>
- Bug fixes/Improvements


0.7.7 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/278">#278 App broken on latest News release (4.0.1)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/277">#277 Blockquote not correctly rendered</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/272">#272 Too much loading since v0.7.x</a>
- Bug fixes/Improvements


0.7.6 (Google Play)
---------------------
- Bug fixes


0.7.5 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/269">#269 App won't start</a>


0.7.4 (Google Play)
---------------------
- Update podcast feature
- Fix podcast video view position
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/256">#256 3.001: Expected BEGIN_ARRAY but was Number at line 1 column 19</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/262">#262 Deleting a feed on the server does not delete it on the client</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/257">#257 Share "Title - url" via twitter</a>
- Lot of bug fixes


0.7.3 (Google Play - Beta)
---------------------
- Update podcast feature (Add option to download podcast)


0.7.2 (Google Play - Beta)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/252">#252 "Open in Browser" should open current page.</a> 
- New feature - <a href="https://github.com/owncloud/News-Android-App/issues/182">#182 »Read« checkbox in widget</a>
- Move "Sync Settings" option from Actionbar to Settings
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/212">#212 sort order of starred items</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/124">#124 Add download image options: Over WiFi only, Over WiFi and Mobil or ask when not connected to WiFi</a>
- Bug fixes
- Improve podcast layout


0.7.1 (Google Play)
---------------------
- Layout improvements
- Performance improvements
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/244">#244 app crashs if screen rotate</a>


0.7.0 (Google Play - Beta)
---------------------
- Layout improvements
- Bug fixes


0.6.9.9 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/245">#245 clicking on feeds under starred items gives weird result</a>
- Lot of bug fixes


0.6.9.8 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/243">#243 Readed items are not synced to owncloud</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/242">#242 Starred items aren't counted</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/241">#241 Feeds without unread items are shown</a>


0.6.9.7 (Google Play - Beta)
---------------------
- Rewrite backend - **IMPORTANT** All your data will be deleted. You'll have to make a full-sync after the update. 
- Lot of bug fixes/improvements
- Performance improvements
- Add sorting podcasts by pub-date (descending)
- Add showcase view (API 11+)


0.6.9.6 (Google Play)
---------------------
- Fix app crash on devices with Android 2.2 - 2.3.*
- Small layout improvements (podcast view)
- Automatically restart app after podcast view has been enabled/disabled (or app theme changed)
- Start podcasts from the item detail view


0.6.9.5 (Google Play - Beta)
---------------------
- Add option to delete downloaded podcasts
- Bug fixes


0.6.9.4 (Google Play - Beta)
---------------------
- Add Podcast download support
- Add Video Podcast Support
- Youtube playlists are supported <a href="http://elliottbledsoe.com/brain-drain/how-to/rss-subscribe-to-youtube-playlist/">(Subscribe to a YouTube playlist using RSS)</a>
- Fix app crash
- Other fixes and improvements


0.6.9.3 (Google Play - Beta)
---------------------
- Accept ogg podcasts
- Improve layout of podcast player

0.6.9.2 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/234">#234 Favorite items don't count in total item count</a>
- Accept mpeg podcasts (only mp3)


0.6.9.1 (Google Play - Beta)
---------------------
- Add notifications for Podcasts
- Fix app crash (tablets)
- Add option to disable podcast support
- Add podcast view to item detail view
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/231">#231 App 0.6.7 crashs on start or after closing</a>


0.6.9 (Google Play - Beta)
---------------------
- Add Podcast support (early preview)
- Bug fixes


0.6.8 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/232">#232 Sync of already read items creates duplicate items</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/230">#230 Leaving space after ownCloud address in the Login dialog produces an error</a>


0.6.7 (Google Play - Beta)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/226">#226 Poor sync performance under high count of unread articles</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/227">#227 Images appears in android gallery apps</a>


0.6.6 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/223">#223 All unread article counts are 0</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/220">#220 Wrong display of unread items</a>


0.6.5 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/223">#223 All unread article counts are 0</a>


0.6.4 (Google Play - Beta)
---------------------
- Improvement - Improved feed list scroll performance
- Improvement - Fixed that the list was blocked while updating the unread count


0.6.3 (Google Play - Beta)
---------------------
- Feature - Import Accounts from other ownCloud Apps


0.6.2 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/219">#219 No feed icons in list overview (0.6.1)</a>



0.6.1 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/216">#216 Bug in list overview in 0.6.0</a>



0.6.0 (Google Play - Beta)
---------------------
- Performance improvements
- Layout improvement
- Fix critical app crash when leaving the add new activity
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/199">#199 Change App-Logo/Icon</a>
- Improvement (better performance now) - <a href="https://github.com/owncloud/News-Android-App/issues/154">#154 Scrolling feed list is slow</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/215">#215 Adjust Colors of light and dark view to the web-interface</a>




0.5.9 (Google Play - Beta)
---------------------
- Extreme performance improvements
- Several bug fixes
- Layout improvement
- New feature - <a href="https://github.com/owncloud/News-Android-App/issues/35">#35 Subscribe to feed with app</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/208">#208 Summary: gray font on black background</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/154">#154 Scrolling feed list is slow</a>


0.5.8 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/214">#214 Scrolling within article causes unwanted tap on links</a>


0.5.7 (Google Play - Beta)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/213">#213 When using the dark theme websites with no background color are unreadable</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/211">#211 Links within articles</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/198">#198 enable auto sync configuration</a>


0.5.6
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
