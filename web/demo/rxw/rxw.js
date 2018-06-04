var rxw1={};
rxw1.debug=false
rxw1.relativePath=''

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
    this.layer({init:function(){
        this.style['background-color']='rgba(0, 0, 0, 0.1)';
    var confirmpad =$('<div  style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');
        $(this).append(confirmpad);

        $(confirmpad).find('[name=cancel]').click(function(){
            $(this).parent().parent().parent().remove()
        });
        $(confirmpad).find('[name=confirm]').click(function(){
            $(this).parent().parent().parent().remove()
            if(param.confirm){
                param.confirm(param.data);
            }
        });
    }})

    //if ($("#confirmpad").length <= 0) {
    //    $("body").append('<div id = "confirmpad" style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >鍙栨秷</button><button name="confirm" style="width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >纭</button></div></div>');
    //    $('#confirmpad [name=cancel]').click(function(){
    //        $("#confirmpad").remove()
    //    });
    //    $('#confirmpad [name=confirm]').click(function(){
    //        $("#confirmpad").remove()
    //        if(param.confirm){
    //            param.confirm(param.data);
    //        }
    //    });
    //
    //}else{
    //    $("#confirmpad").show()
    //}


}

rxw1.inputpad=function input(param) {
    this.layer({init:function(){
        this.style['background-color']='rgba(0, 0, 0, 0.1)';
        var inputpad =$('<div  style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div  style="width:90%;text-align:center;margin:10px auto;"><input type="text" value="" style="text-align: center;width:100%"/></div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');
        $(this).append(inputpad);

        $(inputpad).find('[name=cancel]').click(function(){
            $(this).parent().parent().parent().remove()
        });
        $(inputpad).find('[name=confirm]').click(function(){
            var val = $(this).parent().parent().find("input").val()
            $(this).parent().parent().parent().remove()
            if(param.confirm){
                param.confirm(val);
            }
        });
    }})
}

//rxw1.inputpad=function input(param) {
//    if ($("#inputpad").length <= 0) {
//        $("body").append('<div id = "inputpad" style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div  style="width:90%;text-align:center;margin:10px auto;"><input type="text" value="'+param.default+'" style="text-align: center;width:100%"/></div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >鍙栨秷</button><button name="confirm" style="width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >纭</button></div></div>');
//
//
//        $('#inputpad [name=cancel]').click(function(){
//            $("#inputpad").remove()});
//        $('#inputpad [name=confirm]').click(function(){
//            var val = $("#inputpad input").val()
//            $("#inputpad").remove()
//            if(param.confirm){
//                param.confirm(val);
//            }
//        });
//    }else{
//        $("#inputpad").show()
//    }
//}


//rxw1.imgPreview=function (url){
//    $("body").append('<div onclick="$(this).remove();$(\'body\').css(\'overflow\',\'auto\')" style="z-index:999999;position: absolute;left: 0;top:0;background-color: rgba(0,0,0,0.1);width:100%;height:500px;"> <img style="display:block;margin:auto;position:relative;" src="'+url+'"> /*<img style="display:block;position:absolute;top:50%;left:50%;transform: translate(-50%,-50%)" src="'+url+'">*/ </div>')
//}

rxw1.imgPreview=function(src){
    this.layer({init:function(){
        $(this).append('<img style="margin:auto;display:block;position:relative;top:50%;transform: translateY(-50%)" src="'+src+'">')
        this.onclick= function (){
            this.parentNode.removeChild(this)
        }
    }})
}

rxw1.waitShow = function () {
    this.layer({init:function(){
        this.style['background-color']='rgba(0,0,0,0)'
        var img=document.createElement("img");
        img.style.width='60px';
        img.style.position='absolute';
        img.style.top='20%';
        img.style.left='50%';
        img.style.transform='translate(-50%,-50%)';
        img.src=rxw1.relativePath+'rxw/img/wait.gif';
        this.appendChild(img);
        var layer = this;
        rxw1.waitShow.remove = function(){
            layer.parentNode.removeChild(layer);
        }
    }})
}

rxw1.layer=function (params){
    var div = document.createElement('div');
    if(params.id==undefined||params.id==null)
        div.id='rxw1-layer'
    else
        div.id=params.id;
    div.style.position='fixed';
    div.style.display='none';
    div.style.top='0';
    div.style.left='0';
    div.style.bottom='0';
    div.style.right='0';
    div.style.overflow='scroll';
    div.style['z-index']=9999999,
    div.style['background-color']='rgba(0, 0, 0, 0.3)';
    document.body.appendChild(div);
    if(params.init!=undefined||params.init!=null){
        div.init=params.init;
        div.init();
    }
    div.style.display='block';
}






rxw1.windowTouch = function(params){
    if(params== undefined||params==null)
        params={};
    window.addEventListener('touchstart', function(event) {

        var touch = event.targetTouches[0];
        var touchData ={};
        window.touchData=touchData

        touchData.touchYStart = touch.pageY;
        touchData.touchXStart = touch.pageX;
        touchData.touchYDis=0;
        touchData.touchXDis=0;
        if(rxw1.debug){
            if(document.getElementById('rxw1-windowTouch-debug-touchDataShow'))
                 document.body.removeChild( document.getElementById('rxw1-windowTouch-debug-touchDataShow'))

            var div = document.createElement('div');
            div.id='rxw1-windowTouch-debug-touchDataShow'
            div.innerText= touchData.touchXStart+' '+ touchData.touchYStart +' '+touchData.touchXDis+' '+ touchData.touchYDis
            div.style.position='fixed';
            div.style.top='0';
            div.style.left='0';
            div.style['z-index']=9999999
            document.body.appendChild(div);
        }


        var img=document.createElement("img");

        img.style.display='none';
        img.style.width='12%';
        img.style.position='absolute';
        img.style.top='10%';
        img.style.left='50%';
        img.style.transform='translateX(-50%)';
        img.style['z-index']=9999999
        img.src=rxw1.relativePath+'rxw/img/wait.gif';

        window.touchData.waitImg=img;
        document.body.appendChild(img);
    }, false);
    window.addEventListener('touchmove', function(event) {
        var touch = event.targetTouches[0];

        window.touchData.touchYDis=touch.pageY-window.touchData.touchYStart;
        window.touchData.touchXDis=touch.pageX-window.touchData.touchXStart;
        if(rxw1.debug) {
            var touchDataShow = document.getElementById('rxw1-windowTouch-debug-touchDataShow');
            touchDataShow.innerText = touch.pageX + ' ' + touch.pageY +' '+touchData.touchXDis+' '+ touchData.touchYDis
        }
        if(window.touchData.touchYDis>10){
            window.touchData.waitImg.style.display='';
        }else if(window.touchData.touchYDis<10){

        }

    }, false);
    window.addEventListener('touchend', function(event) {
        if(rxw1.debug) {
            var touchDataShow = document.getElementById('rxw1-windowTouch-debug-touchDataShow');
            touchDataShow.innerText = window.touchData.touchXDis + ' ' + window.touchData.touchYDis
        }
        if((window.touchData.touchYDis>70)){
            if( params.movedown){
                params.movedown();
            }else{
                location.reload();
            }

        }
        if((window.touchData.touchYDis<-20)) {
            if (params.moveup) {
                params.moveup();
            }
        }
        if(window.touchData.touchXDis>100 && window.touchData.touchXDis<150){
            if(params.moveright)
                params.moveright();
            else{
                history.back()
            }
        }
        if(window.touchData.touchXDis>150){
                history.back()
        }
        if(window.touchData.touchXDis<-100  && window.touchData.touchXDis>-150){
            if( params.moveleft)
                params.moveleft();
        }
        if(window.touchData.touchXDis<-150){
            history.go()
        }
        document.body.removeChild(window.touchData.waitImg)
        delete window.touchData;

    }, false);
}




rxw1.addEvent=function addEvent(el, type, fn,useCapture) {
    if(el.addEventListener){
        el.addEventListener(type,fn,!!useCapture)
    }else if(el.attachEvent()){
        el.attachEvent('on' + type,fn)
    }else{
        return false
    }
}
rxw1.cancelHandler=function (event){
    var event=event||window.event;//兼容IE

    //取消事件相关的默认行为
    if(event.preventDefault)    //标准技术
        event.preventDefault();
    if(event.returnValue)    //兼容IE9之前的IE
        event.returnValue=false;
    return false;    //用于处理使用对象属性注册的处理程序
}
function parseQueryStr(queryStr){
    var str=decodeURIComponent(queryStr);
    var arr=str.split("&");
    var obj = {};
    for(var i=0;i < arr.length;i++){
        var arrsub=arr[i].split("=");
        obj[arrsub[0]]=arrsub[1];
    }
    return obj;
}