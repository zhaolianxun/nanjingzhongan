var rxw1={};
rxw1.relitivePath='';
rxw1.errorpad=function (content) {
    if ($("#errorpad").length <= 0) {
        $("body").append('<div  id="errorpad" style="width:200px;padding:0 40px;position:absolute;position:fixed;top:30%;left:50%;transform:translateX(-50%);margin-top:-1.25rem;z-index:999999999;font-size:0.8rem;color:#fff;background:rgba(0,0,0,0.8);line-height:2.25rem;border-radius:0.25rem;text-align:center;">' + content + '</div>');
    }
    else {
        $("#errorpad p").html(content);
    }
    $("#errorpad").fadeIn();
    setTimeout(function () {
        $("#errorpad").fadeOut("slow");
    }, 2000);
}

rxw1.confirmpad=function(param) {
    if ($("#confirmpad").length <= 0) {
        $("body").append('<div id = "confirmpad" style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');
        $('#confirmpad [name=cancel]').click(function(){
            $("#confirmpad").remove()
        });
        $('#confirmpad [name=confirm]').click(function(){
            $("#confirmpad").remove()
            if(param.confirm){
                param.confirm(param.data);
            }
        });

    }else{
        $("#confirmpad").show()
    }


}

rxw1.inputpad=function input(param) {
    if ($("#inputpad").length <= 0) {
        $("body").append('<div id = "inputpad" style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div  style="width:90%;text-align:center;margin:10px auto;"><input type="text" value="'+param.default+'" style="text-align: center;width:100%"/></div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');


        $('#inputpad [name=cancel]').click(function(){
            $("#inputpad").remove()});
        $('#inputpad [name=confirm]').click(function(){
            var val = $("#inputpad input").val()
            $("#inputpad").remove()
            if(param.confirm){
                param.confirm(val);
            }
        });
    }else{
        $("#inputpad").show()
    }
}


rxw1.imgPreview=function (url){
    $("body").append('<div onclick="$(this).remove();$(\'body\').css(\'overflow\',\'auto\')" style="z-index:999999;position: absolute;left: 0;top:0;background-color: rgba(0,0,0,0.1);width:100%;height:500px;"> <img style="display:block;margin:auto;position:relative;" src="'+url+'"> /*<img style="display:block;position:absolute;top:50%;left:50%;transform: translate(-50%,-50%)" src="'+url+'">*/ </div>')
}



rxw1.windowTouch = function(params){
    window.addEventListener('touchstart', function(event) {
        var touch = event.targetTouches[0];
        var touchData ={};
        window.touchData=touchData

        touchData.touchStart = touch.pageY;
        touchData.touchDis=0;

        var img=document.createElement("img");

        img.style.display='none';
        img.style.width='12%';
        img.style.position='absolute';
        img.style.top='10%';
        img.style.left='50%';
        img.style.transform='translateX(-50%)';
        img.src=rxw1.relitivePath+'rxw/img/wait.gif';

        window.touchData.waitImg=img;
        document.body.appendChild(img);
    }, false);
    window.addEventListener('touchmove', function(event) {
        var touch = event.targetTouches[0];

        window.touchData.touchDis=touch.pageY-window.touchData.touchStart;

        if(window.touchData.touchDis>10){
            window.touchData.waitImg.style.display='';
        }else if(window.touchData.touchDis<10){

        }
    }, false);
    window.addEventListener('touchend', function(event) {

        if((window.touchData.touchDis>70))params.movedown();
        if((window.touchData.touchDis<-30))params.moveup();
        document.body.removeChild(window.touchData.waitImg)
        delete window.touchData;

    }, false);
}