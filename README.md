Nextcloud News Reader – Android App
==================================
The Nextcloud News Reader Android App is under [AGPLv3](https://www.gnu.org/licenses/license-list.html#AGPLv3.0) License terms.

<p>
<a href="https://play.google.com/store/apps/details?id=de.luhmer.owncloudnewsreader" alt="Download from Google Play">
  <img src="http://www.android.com/images/brand/android_app_on_play_large.png">
</a>

<a href="https://f-droid.org/repository/browse/?fdid=de.luhmer.owncloudnewsreader" alt="ownCloud News App on fdroid.org">
  <img src="https://camo.githubusercontent.com/7df0eafa4433fa4919a56f87c3d99cf81b68d01c/68747470733a2f2f662d64726f69642e6f72672f77696b692f696d616765732f632f63342f462d44726f69642d627574746f6e5f617661696c61626c652d6f6e2e706e67">
</a>
</p>

*Bugs and enhancements can be reported under*: https://github.com/owncloud/News-Android-App/issues


Donate
==================================
[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=david-dev&url=https://github.com/owncloud/News-Android-App&title=News-Android-App&language=JAVA&tags=github&category=software)


How to use the Beta App via Google Play?
==================================
Join the following Google Group:
https://groups.google.com/d/forum/owncloud-news-android-app

There you can find a link (after I accepted you in the group) to Google Play for registering as beta tester of the app.



Help to translate
==================================
Have you found a translation error? Or would you like to use ownCloud News Reader in your
native language?

You can [join the translation team](https://www.transifex.com/owncloud-org/owncloud/) and improve one of over 100 languages (it's the android-news app).


How to compile the App
==================================
Requirements:
-----------------------
  1. Android Studio

Download and install:
-----------------------
  1. Open cmd/terminal

  2. Navigate to your workspace

  3. Then type in:

    
    git clone --recursive https://github.com/owncloud/News-Android-App.git
    
    for the dev branch:
    
    git clone --recursive https://github.com/owncloud/News-Android-App.git -b dev
    

  5. Import the Project and start coding!

  6. If you should ever get a Gradle error `Configuration with name 'default' not found`
    while building, execute following commands in the projects root directory:

   
    git submodule init
    git submodule update
   

That's all. I hope it works for you! If something is not working, please send me an email to david-dev@live.de


Contributors
==================================
* [David Luhmer](https://github.com/David-Development) (Maintainer)
* [Daniel Schaal](https://github.com/schaal)
* [otrichet](https://github.com/otrichet)
* [cemrich](https://github.com/cemrich)
* [Benjamin Stephan](https://github.com/b3nson)
* [Stefan Niedermann](https://github.com/stefan-niedermann)


Changelog
==================================
- see [CHANGELOG.md](CHANGELOG.md)