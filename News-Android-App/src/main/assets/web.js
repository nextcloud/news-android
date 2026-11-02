// Images that are at least as wide as the text column fill the whole screen width
(function () {
    var content = document.getElementById('content');

    function markFullWidth(img) {
        // measured per image as the page might not have a width yet when this script runs
        var contentWidth = content.clientWidth;
        if (contentWidth > 0 && img.naturalWidth >= contentWidth) {
            img.classList.add('nextcloud-news-img-full');
        }
    }

    document.querySelectorAll('#content img').forEach(function (img) {
        if (img.complete) {
            markFullWidth(img);
        } else {
            img.addEventListener('load', function () { markFullWidth(img); });
        }
    });
})();
