Nextcloud News Reader – Android App
==================================
The Nextcloud News Reader Android App is under [AGPLv3](https://www.gnu.org/licenses/license-list.html#AGPLv3.0) License terms.

<p>
<a href='https://play.google.com/store/apps/details?id=de.luhmer.owncloudnewsreader&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
    <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="100"/>
</a>

<a href="https://f-droid.org/app/de.luhmer.owncloudnewsreader">
  <img src="https://f-droid.org/badge/get-it-on.png"
        alt="Get it on F-Droid" height="100">
</a>
</p>

*Bugs and enhancements can be reported under*: https://github.com/owncloud/News-Android-App/issues


Donate
==================================
[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=david-dev&url=https://github.com/owncloud/News-Android-App&title=News-Android-App&language=JAVA&tags=github&category=software)


How to use the Beta App via Google Play?
==================================
[Nextcloud News Beta](https://play.google.com/apps/testing/de.luhmer.owncloudnewsreader)




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
