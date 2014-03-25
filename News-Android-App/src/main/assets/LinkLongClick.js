var links = document.getElementsByTagName('a');

for (var i = 0; i < links.length; i++) {

    (function (link) {
        var timer;
        link.addEventListener('mouseup', function() {
            clearTimeout(timer);
        });


        link.addEventListener('mousedown', function (e) {
            timer = window.setTimeout(function() {
                e.preventDefault();
                Android.openLinkInBrowser(link.getAttribute('href'));
            }, 1000);
        });

        link.addEventListener("touchstart", function(e){
            timer = window.setTimeout(function() {
                e.preventDefault();
                Android.openLinkInBrowser(link.getAttribute('href'));
            }, 1000);
        });

        link.addEventListener('touchend', function() {
            clearTimeout(timer);
        });
    })(links[i]);

}