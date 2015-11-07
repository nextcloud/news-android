window.onload = function ()
{
    var images = document.getElementsByTagName('img');

    for (var i = 1; i < images.length; i++) {// i = 1 because of the feed image which has no caption
        if(images[i].getAttribute('title') != "" &&
                images[i].getAttribute('title') != null &&
                images[i].getAttribute('title') != "null") {
            (function (image) {
                var timer;
                image.addEventListener('mouseup', function() {
                    clearTimeout(timer);
                });


                image.addEventListener('mousedown', function (e) {
                    timer = window.setTimeout(function() {
                        e.preventDefault();
                        alert(image.getAttribute('title'));
                        //alert("fired - mousedown");
                    }, 1000);
                });

                image.addEventListener("touchstart", function(e){
                    timer = window.setTimeout(function() {
                        e.preventDefault();
                        alert(image.getAttribute('title'));
                        //alert("fired - touchstart");
                    }, 1000);
                });

                image.addEventListener("touchmove", function(e){
                    clearTimeout(timer);
                });

                image.addEventListener('touchend', function() {
                    clearTimeout(timer);
                });
            })(images[i]);
        }
    }
}
