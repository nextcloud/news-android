/*
 Enables long click functionality on links ('open in browser', etc.) and images.
 Loaded in NewsDetailFragment.
 Don't use single line comments as they will break this javascript code.
*/

function watchLongClicks(domElement, onLongClickCallback) {

    var timer;

    domElement.addEventListener('mousedown', function (e) {
        timer = window.setTimeout(function() {
            e.preventDefault();
            onLongClickCallback(domElement);
        }, Android.getLongPressTimeout());
    });

    domElement.addEventListener('mousemove', function () {
        clearTimeout(timer);
    });

    domElement.addEventListener('mouseup', function () {
        clearTimeout(timer);
    });
}


/*
 Long press on links will open share window.
*/
var links = document.getElementsByTagName('a');

for (var i = 0; i < links.length; i++) {
    var link = links[i];

    /* disable for image links because title will be shown instead */
    if (link.children.length > 0 && link.children[0].nodeName === 'IMG')
        continue;

    watchLongClicks(link, function (link) {
        Android.openLinkInBrowser(link.getAttribute('href'));
    })
}


/*
 Long press on images will show their titles.
*/
var images = document.getElementsByTagName('img');

for (var i = 1; i < images.length; i++) { /* i = 1 because of the feed image which has no caption */
    var image = images[i];

    watchLongClicks(image, function (image) {
        var title = image.getAttribute('title') || image.getAttribute('alt');
        if (title) {
            Android.showImage(title, image.getAttribute('src'));
        }
    });
}