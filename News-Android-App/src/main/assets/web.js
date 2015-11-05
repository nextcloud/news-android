/*
  Enables long click functionality on images and shows their title text.
  Loaded in NewsDetailFragment.
*/

window.onload = function () {

    var images = document.getElementsByTagName('img');

    for (var i = 1; i < images.length; i++) { // i = 1 because of the feed image which has no caption

        if(!images[i].getAttribute('title'))
            continue;

        (function (image) {

            var timer;

            function clearTimer() {
                clearTimeout(timer);
            }

            function onLongClickStart(e) {
                clearTimer();
                timer = window.setTimeout(function() {
                    e.preventDefault();
                    alert(image.getAttribute('title'));
                }, 500);
            }

            image.addEventListener('mousedown', onLongClickStart);
            image.addEventListener('mouseup', clearTimer);

            image.addEventListener('touchstart', onLongClickStart);
            image.addEventListener('touchmove', clearTimer);
            image.addEventListener('touchend', clearTimer);

        })(images[i]);
    }

}
