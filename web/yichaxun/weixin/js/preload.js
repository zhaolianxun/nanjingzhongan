/**
 * Created by xinxin on 5/11/2017.
 */



function preloadImage(){
    var preloadImages = new Array()
    for (i = 0; i < preloadImage.arguments.length; i++) {
        preloadImages[i] = new Image()
        preloadImages[i].src = preloadImage.arguments[i]
    }
}

function preloadResource(){
    for (i = 0; i < preloadResource.arguments.length; i++) {
        var o =   document.createElement('object');
        o.data = preloadResource.arguments[i];

        document.body.appendChild(o);
        o.style.display='none';
    }


}


