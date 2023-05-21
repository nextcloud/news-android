0.9.9.80
---------------------
- Changed - Internal dependency updates
- Changed - <a href="https://github.com/nextcloud/news-android/pull/1212">!1212 - Nextcloud Single-Sign-On updates</a>
- Changed - <a href="https://github.com/nextcloud/news-android/pull/1200">!1200 - Bail out early on generating unread rss items notifications (thanks to @Unpublished)</a>
- Changed - <a href="https://github.com/nextcloud/news-android/pull/1199">!1199 - Housekeeping / Remove unused classes (thanks to @Unpublished)</a>
- Changed - <a href="https://github.com/nextcloud/news-android/pull/1195">!1195 - Migrate some classes to Kotlin (thanks to @Unpublished)</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/pull/1214">!1214 - Text formatting is a bit weird</a>

 
0.9.9.79
---------------------
- Changed - Internal dependency updates
- Changed - <a href="https://github.com/nextcloud/news-android/pull/1171">!1171 - Allow selecting feed URL in options dialog (thanks to @Unpublished)</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/pull/1187">!1187 - Fix crash related when trying to move a feed (thanks to @Unpublished)</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/pull/1184">!1184 - Prevent podcast view from showing up on every app start (thanks to @Unpublished)</a>

0.9.9.78
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/pull/1134">!1134 - Fix broken Notifications on Android 13 (thanks to @Unpublished)</a>

0.9.9.77
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1111">#1111 - Fix incorrect height of listview rows</a>
- Changed - <a href="https://github.com/nextcloud/news-android/pull/1115">!1115 - Switched from Universal-Image-Loader to Glide as image loading library</a>
- Added - Added support for SVG favicons
- Added - <a href="https://github.com/nextcloud/news-android/pull/1130">!1130 - Added support for external media players (thanks to @JFronny)</a>

0.9.9.76
---------------------
- Security related fixes (only F-Droid users affected): [#1109](https://github.com/nextcloud/news-android/issues/1109) / [fdroid/fdroiddata#2753](https://gitlab.com/fdroid/fdroiddata/-/issues/2753)

0.9.9.75
---------------------
- Fixed crash when relative links in articles are clicked
- Support Material You Theming with App Icon (thanks to @salixor)

0.9.9.74
---------------------
- Fixed incompatibility issues with Nextcloud News 18.1.0

0.9.9.73 (Beta)
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1061">#1061 App sometimes crashes on long tap on detail view</a>
- Fixed database crashed by reducing the number of loaded items per page
- Fixed crash when long tapping folders in navigation drawer
- Fixed app crash when ui updates
- Fixed crashes caused by swiping on articles in list view

0.9.9.72 (Beta)
---------------------
- Added - <a href="https://github.com/nextcloud/news-android/pull/1066">!1066 Support for Folder Management (Rename, Remove, Create) (thanks @proninyaroslav)</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1075">#1075 Feed name update not updating in RSS items (thanks @proninyaroslav)</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1064">#1064 Add button to exit audio/podcast player once it's open</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1048">#1048 Fix broken podcast time scrolling</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1053">#1053 Podcast player disappears after rotating device</a>

0.9.9.71 (Beta)
---------------------
- Added - <a href="https://github.com/nextcloud/news-android/issues/1060">#1060 Always show incognito mode icon if incognito mode is enabled</a>

0.9.9.70 (Beta)
---------------------
- Fixed - Try to fix more app crashes during sync (reduce number of items per sync)
- Fixed - Speedup detail view by not storing instance state of webview

0.9.9.69
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1055">#1055 App crashes during sync (OutOfMemory Error)</a>

0.9.9.68 (Beta)
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1012">#1012 Loadingbar is visible even though page is done loading</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1029">#1029 Unread list does not actualize after manual update (Only when using legacy login)</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1046">#1046 "No notification" setting still generates notifications in separate notification channel</a>
- Fixed - Fix missing images if webview has been restored (e.g. after app has been in background)
- Fixed - News App is broken after restoring it from a backup (when using SSO)

0.9.9.67 (Beta)
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1044">#1044 Colors/Theme sometimes not applied</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1042">#1042 Relative image links/URLs don't open correctly</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1039">#1039 SSO not working with Beta Version of Files App</a>

0.9.9.66 (Beta)
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1036">#1036 Fixed crashes on Android 12 devices</a> (#1032 / #1037)

0.9.9.65 (Beta)
---------------------
- Fixed - Fix broken sync due to incompatibility between latest nextcloud files app and Single Sign On Library

0.9.9.64 (Beta)
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1006">#1006 Refactor and fix sync issues</a>
- Changed - Improve OPML import dialog
- Changed - Increase the soft limit of articles in the app from 1500 to 5000 articles

0.9.9.63 (Beta)
---------------------
- Added - <a href="https://github.com/nextcloud/news-android/issues/1002">#1002 support for more granular notification settings</a>
- Changed - added file extension to downloaded/exported images
- Changed - allow clicks on notification after an image has been saved/downloaded from detail view
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/1018">#1018 Item state sync is not working correctly when many items have been changed</a>
- Fix security issue GHSL-2021-1033 (Thanks to GitHub Security Lab - with special thanks to Tony Torralba and Kevin Backhouse)

0.9.9.62
---------------------
- Changed - <a href="https://github.com/nextcloud/news-android/issues/824">#824 Enhance empty content view (thanks Stefan)</a>
- Changed - <a href="https://github.com/nextcloud/news-android/issues/976">#976 Sync Interval - settings menu is now a popup (thanks @fabienli)</a>
- Changed - <a href="https://github.com/nextcloud/news-android/issues/974">#974 only show notification if it is different to the previous unread articles list</a>

0.9.9.61
---------------------
- Changed - <a href="https://github.com/nextcloud/news-android/issues/969">#969 Remove unnecessary Notifications setting</a>
- Changed - <a href="https://github.com/nextcloud/news-android/issues/968">#968 Rename "Light/Dark (based on Daytime)" to "System Default"</a>
- Changed - <a href="https://github.com/nextcloud/news-android/issues/960">#960 Make articles respect default system font</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/964">#964 Crash when using card layout</a>

0.9.9.60
---------------------
- Changed - Major Design Update thanks to @stefan-niedermann!
- Changed - <a href="https://github.com/nextcloud/news-android/issues/944">#944 Drop dark mode based on location</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/958">#958 OPML Export Dialog is now translated </a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/945">#945 New Thubmnails list layout does not show favorite status</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/938">#938 Everincreasing Fontsize with Fontsize setting "Big"</a>

0.9.9.54
---------------------
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/918">#918 Poor scroll performance for some feeds</a>
- Fixed - <a href="https://github.com/nextcloud/news-android/issues/903">#903 Bottom part of article not visible because of action icons</a>
- Changed - <a href="https://github.com/nextcloud/news-android/issues/929">#929 Widget should respect dark / light theme</a>
- Changed - Auto-Sync is enabled by default now (every 15min)
- Changed - Minor adjustments to UI (including new default list layout)
- Changed - New list layout in the app
- Changed - Widget redesign

0.9.9.53
---------------------
- Version bump for another Google review

0.9.9.52
---------------------
- Version bump for another Google review

0.9.9.51
---------------------
- Version bump for another Google review

0.9.9.50
---------------------
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/880">#880 Starred items were not synchronized in certain situations</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/889">#889 Fast Access Functions activated on startup but settings deactivated</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/892">#892 Refresh unread items view after update</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/403">#403 Problem with syncing "old" favorites</a>
- Changed - <a href="https://github.com/nextcloud/news-android/issues/896">#896 Change User-Info API</a>

0.9.9.41
---------------------
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/887">#887 Fix crashes due to huge rss items</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/878">#878 Floating menu setting had no effect / Show either the floating or the kebab menu</a>
- Feature - <a href="https://github.com/nextcloud/news-android/issues/754">#754 Add more meaningful notifications</a>
- Feature - <a href="https://github.com/nextcloud/news-android/pull/885">!885 Enable auto sync by default (every 24h)</a>

0.9.9.40
---------------------
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/344">#344 Starred items are not synced correctly</a>
- Feature - <a href="https://github.com/nextcloud/news-android/pull/881">!881 When enabled, also use custom tabs when skipping detailed view</a>

0.9.9.39
---------------------
- Google refused update

0.9.9.38
---------------------
- Feature - <a href="https://github.com/nextcloud/news-android/issues/868">#868 - Add thumbnail support (media) articles</a>
- Feature Removal: Double-Tap-To-Star in detail view
- Fix app crashes 

0.9.9.37
---------------------
- New feature: Fast actions (Huge thanks to @emasty)

0.9.9.36
---------------------
- Reduce min-newsApi level to 17

0.9.9.35
---------------------
- Fix Single-Sign On related Issues
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/769">#769 - Nextcloud API not responding</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/830">#830 - Only ask for Location permission if auto theme enabled</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/786">#786 - Error loading xml resource in Android 4.4</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/pull/833">#833 - fix tables are too wide</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/issues/821">#821 - "Add feed"-icon is misaligned</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/pull/827">#821 - Text in drawer not bold, except of "add new feed" and "settings" (thanks @tobiasKaminsky)</a>

0.9.9.34 / 0.9.9.33
---------------------
- Fix F-Droid build issues

0.9.9.32
---------------------
- Fix bug that items containing "image/jpeg" as enclosure, are interpreted as podcasts
- Fix app crash when changing server settings
- Improve opml import / export

0.9.9.31
---------------------
- Feature - <a href="https://github.com/nextcloud/news-android/issues/787">#787 - Display profile avatar in the sidenav</a>
- Feature - <a href="https://github.com/nextcloud/news-android/issues/788">#788 - Move settings menu to sidenav as last entry (thanks @emasty)</a>
- Feature - <a href="https://github.com/nextcloud/news-android/issues/789">#789 - Add a new feed should be in sidenav (thanks @emasty)</a>
- Feature - <a href="https://github.com/nextcloud/news-android/issues/804">#804 - Support Android 10 System DayNight Modes (thanks @wbrawner)</a>
- Feature - <a href="https://github.com/nextcloud/news-android/pull/811">#811 - Android Auto Support (including Voice Control)</a>
- Feature - <a href="https://github.com/nextcloud/news-android/pull/810">#810 - Automatically add debug information when reporting github issue through the app</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/pull/807">#807 - Fixed open article in browser call (thanks @emasty)</a>
- Bug fix - <a href="https://github.com/nextcloud/news-android/pull/806">#806 - Update app icon background layer (thanks @stefan-niedermann)</a>

0.9.9.28 / 0.9.9.29 / 0.9.9.30
---------------------
- Retry rejected review by google due to new android auto support
- Fix - <a href="https://github.com/nextcloud/news-android/issues/795">#795 Adjust app icon to match new regulations</a>
- Feature - <a href="https://github.com/nextcloud/news-android/pull/799">#799 - Added podcast browser for Android Auto App</a>
- Feature - <a href="https://github.com/nextcloud/news-android/issues/798">#798 better display of code blocks</a>
- Feature - <a href="https://github.com/nextcloud/news-android/pull/791">#791 Implement incognito mode</a>

0.9.9.27
---------------------
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/795">#795 Adjust app icon to match new regulations</a>
- Fix - Fix validation of urls during manual account setup

0.9.9.26
---------------------
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/726">#726 Add new feed fails</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/744">#744 Fix issues when adding feeds (thanks @Unpublished)</a>
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/747">#747 Add option to share article when using chrome-custom-tabs</a>
- Fix - Reset database when account is stored
- Fix - Workaround for app-crashes due to widget problems
- Feature - Support for Android Auto (Podcast playback)
- Feature - Use picture-in-picture mode for video podcasts
- Fix - Fix restarts of app due to a bug in android compat library (when using dark mode)

0.9.9.25
---------------------
- Fix - app crashes

0.9.9.24
---------------------
- Fix - app crashes

0.9.9.23
---------------------
- Fix - app crashes
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/717">#717 Launch a synchronization when switching from this app to another</a>

0.9.9.22
---------------------
- Fix - app crash during startup
- Fix - app crash during sync

0.9.9.21
---------------------
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/713">#713 App hangs during sync </a>
- Fix - Sync on startup not working in some cases
- UI Improvement - Improve first app start experience

0.9.9.20
---------------------
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/702">#702 Widget not updating</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/698">#698 Black font color on all layouts</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/696">#696 Background sync (automatic sync) broken</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/693">#693 Fix background color in Settings (Thank you @AnotherDaniel)</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/683">#683 OLED bg is lost on orientation change in auto NightMode (Thank you @AnotherDaniel)</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/681">#681 Make audio podcasts controllable by Bluetooth media controls</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/678">#678 Settings section/page header text is black, also in dark theme</a>
- UI Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/704">#704 Move About/Changelog to Settings (Thank you @AnotherDaniel)</a>
- UI Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/690">#690 Settings - Disable oled setting if Light theme is selected  (Thank you @AnotherDaniel)</a>
- UI Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/688">#688 Settings - Add missing section header of first/general settings category  (Thank you @AnotherDaniel)</a>
- UI Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/680">#680 Add option to only show headlines</a>
- UI Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/677">#677 New feed items look read</a>

0.9.9.19
---------------------
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/661">#661 NightMode (Thank you @AnotherDaniel)</a>
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/658">#658 Several UI refactorings</a>
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/585">#585 Thumbnail support</a>
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/467">#467 Adjustable Font Size (Thank you @AnotherDaniel)</a>
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/596">#596 Download articles to view them offline</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/664">#664 Order of folders is confusing (Thank you @AnotherDaniel)</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/633">#633 swipe upwards to mark as read</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/667">#667 Beeping while podcast playing/downloading</a>

0.9.9.18 (Google Play)
---------------------
- Improve - <a href="https://github.com/owncloud/News-Android-App/issues/651">#651 Automatic reload of rss item list when empty</a>
- Improve - <a href="https://github.com/owncloud/News-Android-App/issues/657">#657 Rename app from "Nextcloud News" to "News"</a>
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/478">#478 Add Search Functionality (Thank you @NilsGriebner)</a>
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/644">#644 Move feed (Thank you @NilsGriebner)</a>
- Improve first sign-on experience
- Several UI improvements
- Single Sign On (first official beta!)

0.9.9.17 (Google Play)
---------------------
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/630">#630 Improve unread rss item count notification (#645)</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/646">#646 support for animated gifs on android 8.1</a>
- Fix - Login issues when using passwords with special characters
- Add Single Sign On (requires a non published version of the Nextcloud files app)

0.9.9.16 (Google Play)
---------------------
- Massive improvements to the rss-item-list scrolling performance
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/632">#632 App crash when downloading single image</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/629">#629 Error when trying to fetch more items</a>
- Add support for Android 8+ Notifications
- Added widget preview - thank you @stefan-niedermann
- Added roundIcon for API level 25 - thank you @stefan-niedermann
- Added adaptive icon and adjusted splash screen - thank you @stefan-niedermann

0.9.9.15 (Google Play)
---------------------
- Fix app crashes due to missing translations
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/622">#622 Sync fails (showrss.info feeds)</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/618">#618 No white theme in 0.9.9.13 fdroid</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/564">#564 Feed icons don't appear to be caching</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/557">#557 Reloading Icon barely visible</a>
- Add option to change podcast playback speed (thanks @jwaghetti)

0.9.9.14 (Google Play)
---------------------
- Add more translations to reduce number of app crashes
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/617">#617 Strange behaviour when marking read with scroll</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/616">#616 Improve usability - Blue links with dark OLED theme</a>

0.9.9.13 (Google Play)
---------------------
- Optimization - <a href="https://github.com/owncloud/News-Android-App/issues/614">#614 Black background color for OLED screens (New Theme)</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/612">#612 Impossible to delete or edit RSS feeds</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/607">#607 Workaround for SSL Handshake failed on Android 7.0 (thank you @svenschn)</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/606">#606 App crash when opening "Settings" on Android 4.2</a>
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/591">#591 Add option to load links in feeds in external browser</a>
- Fix app crash on android 8

0.9.9.12 (Google Play)
---------------------
- Several bug fixes
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/602">#602 crash when trying to open settings</a>

0.9.9.11 (Google Play)
---------------------
- Fix app crashes on Android 8+
- Fix - <a href="https://github.com/owncloud/News-Android-App/issues/571">#571 Move updates/changes to a separate CHANGES/CHANGELOG file</a>

0.9.9.10 (Google Play)
---------------------
- Several bug fixes
- Add support for cardview
- Optimization - <a href="https://github.com/owncloud/News-Android-App/issues/590">#590 Improve "mark as read while scrolling" feature</a>
- Optimization - <a href="https://github.com/owncloud/News-Android-App/issues/591">#591 Load links of feeds in external browser</a>

0.9.9.9 (Google Play)
---------------------
- Fix several app crashes
- Fix several widget issues
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/583">#583 App crashes on Android 8</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/579">#579 App crash due to invalid drawable tag vector</a>
- Optimization - <a href="https://github.com/owncloud/News-Android-App/issues/575">#575 Widget unusable on dark background</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/587">#587 App crashes</a>

0.9.9.8 (Google Play)
---------------------
- Fix several app crashes
- Use flavors (for proprietary newsApi calls)

0.9.9.7 (Google Play)
---------------------
- Fix several app crashes

0.9.9.6 (Google Play)
---------------------
- Rewrite of sync backend (use Retrofit, Dagger, OkHttp)
- Fix app crash (when using self signed ssl certificates)
- Several other fixes and improvements

0.9.9.5 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/559">#559 Sync is slow</a>

0.9.9.4 (Google Play)
---------------------
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/549">#549 Native YouTube video support</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/497">#497 Starts playing podcast when headphones are removed</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/546">#546 Share button has wrong color</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/540">#540 Dialog disappears on device rotation</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/532">#532 Graphical bug in landscape mode 10.1"</a>
- Optimization - <a href="https://github.com/owncloud/News-Android-App/issues/538">#538 Display total number of new items instead of last fetched in the notification</a>
- Optimization - <a href="https://github.com/owncloud/News-Android-App/issues/537">#537 display news title instead of "unread articles"</a>
- UI-Update - <a href="https://github.com/owncloud/News-Android-App/pull/542">#542 Nextcloud Theme - Thank you @stefan-niedermann</a>

0.9.9.3 (Google Play)
---------------------
- Critical bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/539">#539 Can not sync with Nextcloud 11 beta 1</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/511">#511 Prevent preload of videos</a>

0.9.9.2 (Google Play)
---------------------
- Partial bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/532">#532 Graphical bug in landscape mode 10.1"</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/530">#530 App crashes when trying to launch "Settings"</a>

0.9.9.1 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/531">#531 Design issues with longclick-dialogs</a>

0.9.9.0 (Google Play)
---------------------
- Improvement - Better error handling if API returns wrong version code
- Feature - Add Splash Screen
- Several Bug fixes and improvements

0.9.8.7 (Google Play)
---------------------
- Fix app crash - <a href="https://github.com/owncloud/News-Android-App/issues/519">#519 New versions force quit on CM11</a>

0.9.8.6 (Google Play)
---------------------
- Fix app crash - <a href="https://github.com/owncloud/News-Android-App/issues/519">#519 New versions force quit on CM11</a>

0.9.8.5 (Google Play)
---------------------
- Critical bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/518">#518 Bug in 0.9.8.3: Using the app caused marking all articles as read and starred articles are lost</a>

0.9.8.4
---------------------
- Critical bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/518">#518 Bug in 0.9.8.3: Using the app caused marking all articles as read and starred articles are lost</a>

0.9.8.3
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/502">#502 App crash when scrolling on empty list</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/509">#509 Sharing links duplicates titles</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/493">#493 Server and Username disappears</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/498">#498 Server and Username disappears</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/515">#515 Increase the padding for article content</a>
- Feature - <a href="https://github.com/owncloud/News-Android-App/issues/513">#513 Deduplicate articles</a>
- Feature - add support for video/mp4 podcasts
- Several Bug fixes and improvements

0.9.8.2 (Google Play)
---------------------
- Critical bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/492">#492 App crashes on start</a>

0.9.8.1 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/487">#487 App crashes when entering "Settings" on Android 4.4.4</a>
- Security fix - <a href="https://github.com/owncloud/News-Android-App/issues/489">#489 rfc: disable password check</a>

0.9.8 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/435">#435 Audio podcasts: icon disappears in detailed view</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/396">#396 podcasts stop playing, maybe high memory usage</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/463">#463 "Download images" stops after a few images</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/480">#480 Show ALT text (and TITLE) in image long-click menu</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/481">#481 Error with special characters in the title of feed</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/479">#479 Add a button to share article</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/445">#445 Audio Podcast: Download progress</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/434">#434 Audio Podcast: use androids media control elements</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/436">#436 Audio podcast: highlight them in detailed view</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/485">#485 Rename to "ownCloud News"</a>
- Performance improvement
- Several Bug fixes and improvements

0.9.7.6 (Google Play)
---------------------
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/349">#349 Widget always empty</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/472">#472 Starring is broken (add swipe to star again)</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/146">#146 add context menu on pressing long on an item</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/239">#239 Add support for OPML files import/export</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/343">#343 Mark as read only when scrolling past article</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/issues/374">#374 unread badge - teslaunread-newsApi</a>
- Security improvement (Prevent XSS)

0.9.7.5 (Google Play)
---------------------
- Bug fix

0.9.7.4 (Google Play)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/pull/474">#474 New Feature: Rename and remove feeds </a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/pull/465">#465 Support for right-to-left languages</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/pull/456">#456 Download-Directory-Chooser for images in webview</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/466">#466 Articles are displayed in desktop view</a>

0.9.7.3 (Google Play)
---------------------
- Improvement - <a href="https://github.com/owncloud/News-Android-App/pull/431">#431 Avoid volume change at beginning and end of feed</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/pull/430">#430 Switched collapse folder icons</a>
- Improvement - <a href="https://github.com/owncloud/News-Android-App/pull/438">#438 context menu "save image" in detail view</a>
- Bug fix - <a href="https://github.com/owncloud/News-Android-App/issues/410">#410 Latest release on google play does not sync</a>

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
