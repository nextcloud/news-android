# Nextcloud News for Android

[![Android CI](https://github.com/nextcloud/news-android/workflows/Android%20CI/badge.svg)](https://github.com/nextcloud/news-android/actions)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2bb65782750445c99e80dab29f6701a6)](https://www.codacy.com/app/Nextcloud/news-android?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=nextcloud/news-android&amp;utm_campaign=Badge_Grade)
[![GitHub issues](https://img.shields.io/github/issues/nextcloud/news-android.svg)](https://github.com/nextcloud/news-android/issues)
[![GitHub stars](https://img.shields.io/github/stars/nextcloud/news-android.svg)](https://github.com/nextcloud/news-android/stargazers)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![CHANGELOG.md](https://img.shields.io/badge/Changelog-CHANGELOG.md-informational)](CHANGELOG.md)
[![Flattr this git repo](https://img.shields.io/badge/Flattr-Donate-success)](https://flattr.com/submit/auto?user_id=david-dev&url=https://github.com/owncloud/News-Android-App&title=News-Android-App&language=JAVA&tags=github&category=software)

An Android client for [Nextcloud News App](https://github.com/nextcloud/news/).

## ▶️ Access

[![Latest Release](https://img.shields.io/github/v/tag/nextcloud/news-android?label=latest+release&sort=semver)](https://github.com/nextcloud/news-android/releases)
[![F-Droid Release](https://img.shields.io/f-droid/v/de.luhmer.owncloudnewsreader)](https://f-droid.org/de/packages/de.luhmer.owncloudnewsreader/)
[![Beta channel](https://img.shields.io/badge/Play%0DStore-Beta%0Dchannel-informational)](https://play.google.com/apps/testing/de.luhmer.owncloudnewsreader)

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
      alt="Get it on Play Store"
      height="80">](https://play.google.com/store/apps/details?id=de.luhmer.owncloudnewsreader&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1)
[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/app/de.luhmer.owncloudnewsreader)
[<img src="https://raw.githubusercontent.com/stefan-niedermann/paypal-donate-button/master/paypal-donate-button.png"
      alt="Donate with PayPal"
      height="80">](https://www.paypal.com/donate?hosted_button_id=5TJ6LTEVTDF5J)


## 👀 Screenshots

| 1 | 2 | 3 |
| --- | --- | --- |
| ![Feed list](https://raw.githubusercontent.com/nextcloud/news-android/master/fastlane/metadata/android/en-US/images/phoneScreenshots/1_en-US.png) | ![Articles](https://raw.githubusercontent.com/nextcloud/news-android/master/fastlane/metadata/android/en-US/images/phoneScreenshots/2_en-US.png) | ![Details](https://raw.githubusercontent.com/nextcloud/news-android/master/fastlane/metadata/android/en-US/images/phoneScreenshots/4_en-US.png) |

## 👪 Join the team
* Test the app with different devices
* Report issues in the [issue tracker](https://github.com/nextcloud/news-android/issues)
* [Pick a good first issue](https://github.com/nextcloud/news-android/labels/good%20first%20issue) :notebook:
* Create a [Pull Request](https://opensource.guide/how-to-contribute/#opening-a-pull-request)
* Help increasing the test coverage by contributing unit tests
* Translate the app on [Transifex](https://www.transifex.com/nextcloud/nextcloud/)
* Send me a bottle of your favorite beer :beers: :wink:

## How to compile the App

### Requirements

1. [Android Studio](https://developer.android.com/studio/)

### Download and install

1. Open cmd/terminal
2. Navigate to your workspace
3. Then type in: `git clone https://github.com/nextcloud/news-android.git`
4. Import the Project in Android Studio and start coding!
   
### Testing with Android Auto

1. Open Android Studio, click on "Tools" -> "SDK Manager"
2. Select and install "Android Auto API Simulators"
3. Open terminal, go to <android-sdk>/extras/google/simulators (e.g. `cd ~/Library/Android/sdk/extras/google/simulators`)
4. Install apk using adb (`../../../platform-tools/adb install media-browser-simulator.apk`)
5. Install apk using adb (`../../../platform-tools/adb install messaging-simulator.apk`)

### Advanced Testing Android Auto (e.g. Voice Features) [Link](https://developer.android.com/training/cars/testing)

1. Install Android 9 in an Emulator (Android 10 didn't work with the Android Auto App)
2. Install Android Auto from PlayStore
3. Follow instructions on training website (see link above)
4. `~/Library/Android/sdk/platform-tools/adb forward tcp:5277 tcp:5277
5. `cd ~/Library/Android/sdk/extras/google/auto`
6. `./desktop-head-unit`
7. From inside the terminal type: `mic play ./voice/pause.wav`

That's all. I hope it works for you! If something is not working, please send me an email to david-dev@live.de


## Contributors

* [David Luhmer](https://github.com/David-Development) (Maintainer)
* [Daniel Schaal](https://github.com/schaal)
* [otrichet](https://github.com/otrichet)
* [cemrich](https://github.com/cemrich)
* [Benjamin Stephan](https://github.com/b3nson)
* [Stefan Niedermann](https://github.com/stefan-niedermann)
* [Nils Griebner](https://github.com/NilsGriebner)
* [AnotherDaniel](https://github.com/AnotherDaniel)
* [Unpublished](https://github.com/Unpublished)
* [emasty](https://github.com/emasty)
