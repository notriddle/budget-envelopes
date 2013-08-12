The release system
==================

 * Only bug fixes happen directly in master: all new features are developed in separate branches and merged when they're considered ready-to-beta-test.

 * Beta releases that are uploaded to Google+ are just builds from master.

 * When a consensus is reached that the app is ready for another release (all the strings are translated into all supported languages, and no known critical bugs exist), the release branch is fast-forwarded to the tip of the master branch, the version number is incremented, and the tag is made. The release branch is just there to make finding the latest stable version easy.

 * The master branch always has an odd version number, while the actual releases always have an even version number.

 * On Google Play, I use a staggered roll-out. First day at 1%, second day at 10%, third day at 100%.

