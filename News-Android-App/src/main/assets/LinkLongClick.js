/*
 Enables long click functionality on links ('open in browser', etc.).
 Loaded in NewsDetailFragment.
 Don't use single line comments as they will break this javascript code.
*/

var links = document.getElementsByTagName('a');

for (var i = 0; i < links.length; i++) {

    (function (link) {
        var timer;

        function clearTimer() {
            clearTimeout(timer);
        }

        function onLongClickStart(e) {
            clearTimer();
            timer = window.setTimeout(function() {
                e.preventDefault();
                Android.openLinkInBrowser(link.getAttribute('href'));
            }, 500);
        }

        if(link.children.length > 0 && link.children[0].nodeName === 'IMG') {
            /* disable for image links because title will be shown instead */
        } else {
            link.addEventListener('mousedown', onLongClickStart);
            link.addEventListener('mousemove', clearTimer);
            link.addEventListener('mouseup', clearTimer);
        }
    })(links[i]);

}