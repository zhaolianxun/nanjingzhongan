var rxw1={};
rxw1.debug=true
rxw1.relativePath=''

rxw1.errorpad=function (content) {
    if ($("#errorpad").length <= 0) {
        $("body").append('<div  id="errorpad" style="width:200px;padding:0 40px;position:absolute;position:fixed;top:30%;left:50%;transform:translateX(-50%);margin-top:-1.25rem;z-index:999999999;font-size:0.8rem;color:#fff;background:rgba(0,0,0,0.8);line-height:2.25rem;border-radius:0.25rem;text-align:center;">' + content + '</div>');
    }
    else {
        $("#errorpad").html(content);
    }
    $("#errorpad").fadeIn();
    setTimeout(function () {
        $("#errorpad").fadeOut("slow");
    }, 2000);
}

//{content:'',
// data,{},
// confirm:function(){}}
rxw1.confirmpad=function(param) {
    this.layer({init:function(layer){
        layer.style['background-color']='rgba(0, 0, 0, 0)';
        var confirmpad =$('<div  style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="font-size:14px;font-weight:600;width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="font-size:14px;font-weight:600;width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');
        $(layer).append(confirmpad);

        $(confirmpad).find('[name=cancel]').click(function(){
            $(this).parent().parent().parent().remove()
        });
        $(confirmpad).find('[name=confirm]').click(function(){
            $(this).parent().parent().parent().remove()
            if(param.confirm){
                param.confirm();
            }
        });
    }})
}

rxw1.alertpad=function(param) {
    this.layer({init:function(layer){
        layer.style['background-color']='rgba(0, 0, 0, 0)';
        var alertpad =$('<div  style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="confirm" style="font-size:14px;font-weight:600;width:100%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px;border-bottom-left-radius: 10px;cursor: pointer" >确认</button></div></div>');
        $(layer).append(alertpad);


        $(alertpad).find('[name=confirm]').click(function(){
            $(this).parent().parent().parent().remove()
            if(param.confirm){
                param.confirm();
            }
        });
    }})
}

rxw1.inputtextpad=function input(param) {
    this.layer({init:function(layer){
        layer.style['background-color']='rgba(0, 0, 0, 0)';
        var inputpad =$('<div  style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div  style="width:90%;text-align:center;margin:10px auto;"><textarea type="text"  style="width:100%;height:200px">'+rxw1.trimStrToEmpty(param.def)+'</textarea></div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="font-size:14px;font-weight:600;width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="font-size:14px;font-weight:600;width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');
        $(layer).append(inputpad);

        //$(layer).click(function(){
        //    var e = rxw1.getEvent();
        //    var target = rxw1.getEventTarget(e);
        //    if(target == layer){
        //        $(this).remove();
        //    }
        //})

        $(inputpad).find('[name=cancel]').click(function(){
            $(this).parent().parent().parent().remove()
        });
        $(inputpad).find('[name=confirm]').click(function(){
            var val = $(layer).find("textarea").val()
            $(this).parent().parent().parent().remove()
            if(param.confirm){
                param.confirm(val);
            }
        });
    }})
}

rxw1.inputpad=function input(param) {
    this.layer({init:function(layer){
        layer.style['background-color']='rgba(0, 0, 0, 0)';
        var inputpad =$('<div  style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div  style="width:90%;text-align:center;margin:10px auto;"><input type="text" value="'+rxw1.trimStrToEmpty(param.def)+'" style="text-align: center;width:100%"/></div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="font-size:14px;font-weight:600;width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="font-size:14px;font-weight:600;width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');
        $(layer).append(inputpad);

        $(inputpad).find('[name=cancel]').click(function(){
            $(this).parent().parent().parent().remove()
        });
        $(inputpad).find('[name=confirm]').click(function(){
            var val = $(layer).find("input").val()
            $(this).parent().parent().parent().remove()
            if(param.confirm){
                param.confirm(val);
            }
        });
    }})
}

rxw1.inputUMPad = function (param){
    this.layer({init:function(layer){
       // layer.style['background-color']='rgba(0, 0, 0, 0)';
        var html = '<div  style="background-color:white;z-index:999999;border-radius: 10px;max-width:500px;width:90%;border: 1px solid lightgrey;margin:auto;position:fixed:top:0;bottom:0;" >';
        html=html+      '<div  style=text-align:center;margin:10px auto">'+param.content+'</div>';
        html=html+      '<div id="myEditor"  style="width:500px;"></div>';
        html=html+      '<div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;">';
        html=html+          '<button name="cancel" style="cursor:pointer;font-size:14px;font-weight:600;width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button>';
        html=html+          '<button name="confirm" style="cursor:pointer;font-size:14px;font-weight:600;width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button>';
        html=html+      '</div>';
        html=html+'</div>';
        $(layer).append(html);
        var um = UM.getEditor('myEditor',{autoHeightEnabled:false});
        if(param.def)
            um.setContent(param.def)
        $(layer).find('[name=cancel]').click(function(){
            um.destroy()
            $(layer).remove()
        });
        $(layer).find('[name=confirm]').click(function(){
            if(param.confirm){
                param.confirm(um.getContent());
                um.destroy()
            }
            $(layer).remove()
        });
    }})


}


rxw1.inputBooleanPad=function input(param) {
    this.layer({init:function(layer){
        layer.style['background-color']='rgba(0, 0, 0, 0)';
        var inputpad =$('<div  style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " ><div  style="width:90%;text-align:center;margin:10px auto">'+param.content+'</div><div  style="width:90%;text-align:center;margin:10px auto;"><label for="rxw1-radio-yes">是</label><input type="radio" name="rxw1-radio"  id="rxw1-radio-yes" value="1" style="text-align: center;"/><label for="rxw1-radio-no" style="margin-left:10px">否</label><input type="radio" name="rxw1-radio" id="rxw1-radio-no" value="0" style="text-align: center;"/></div><div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="font-size:14px;font-weight:600;width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="font-size:14px;font-weight:600;width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');
        $(layer).append(inputpad);
        $(layer).find("input[name=rxw1-radio][value="+param.def+"]").prop("checked",true);
        //$(layer).click(function(){
        //    var e = rxw1.getEvent();
        //    var target = rxw1.getEventTarget(e);
        //    if(target == layer){
        //        $(this).remove();
        //    }
        //})

        $(inputpad).find('[name=cancel]').click(function(){
            $(this).parent().parent().parent().remove()
        });
        $(inputpad).find('[name=confirm]').click(function(){
            var val = $(layer).find("input[name=rxw1-radio]:checked").val()
            $(this).parent().parent().parent().remove()
            if(param.confirm){
                param.confirm(val);
            }
        });
    }})
}


rxw1.getEvent = function(){
    return window.event || arguments.callee.caller.arguments[0]
}

rxw1.getEventTarget = function(e){
    return e.srcElement||e.target;
}

rxw1.inputmorepad=function input(param) {
    this.layer({init:function(layer){
        layer.style['background-color']='rgba(0, 0, 0, 0)';
        var a = '';
        for(index in param.items){
            a=a+'<div style="margin:15px 10px"><span>'+param.items[index].name+':</span><input value="'+rxw1.trimStrToEmpty(param.items[index].def)+'" style="margin-left:5px" name="'+param.items[index].code+'"/></div>'
        }
        var inputpad =$('<div  style="background-color:white;z-index:999999;border-radius: 10px;min-width:300px;position: absolute;border: 1px solid lightgrey;left:50%;top:20%;transform:translateX(-50%) " >'+a+'<div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="font-size:14px;font-weight:600;width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="font-size:14px;font-weight:600;width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div></div>');

        $(layer).append(inputpad);

        $(layer).click(function(){
            var e = rxw1.getEvent();
            var target = rxw1.getEventTarget(e);
            if(target == layer){
                $(this).remove();
            }
        })
        $(inputpad).find('[name=cancel]').click(function(){
            $(this).parent().parent().parent().remove()
        });
        $(inputpad).find('[name=confirm]').click(function(){
            var inputs = $(layer).find("input")
            var vals = {};
            for(var i=0 ;i<inputs.length;i++){
                vals[inputs[i].name]=inputs[i].value
            }
            $(this).parent().parent().parent().remove()
            if(param.confirm){
                param.confirm(vals);
            }
        });
    }})
}

rxw1.randomString=function (randomFlag, min, max){
    var str = "",
        range = min,
        arr = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

    // 随机产生
    if(randomFlag){
        range = Math.round(Math.random() * (max-min)) + min;
    }
    for(var i=0; i<range; i++){
        pos = Math.round(Math.random() * (arr.length-1));
        str += arr[pos];
    }
    return str;
}

rxw1.imgPreview=function(src){
    this.layer({init:function(layer){
        $(layer).append('<img style="margin:50px auto;display:block;position:relative;max-width:95%" src="'+src+'">')
        layer.onclick= function (){
            this.parentNode.removeChild(this)
        }
    }})
}

rxw1.waitLock = function () {
    this.layer({init:function(layer){
        layer.style['background-color']='rgba(0,0,0,0)'
        var img=document.createElement("img");
        img.style.width='60px';
        img.style.position='absolute';
        img.style.top='20%';
        img.style.left='50%';
        img.style.transform='translate(-50%,-50%)';
        img.src=rxw1.relativePath+'rxw/img/wait.gif';
        layer.classList.add('rxw1-waitLock')
        layer.appendChild(img);
    }})

}
rxw1.waitLock.remove = function(){
    $('.rxw1-waitLock').remove()
}

rxw1.layer=function (params){
    var div = document.createElement('div');
    if(params.id==undefined||params.id==null)
        div.id='rxw1-layer'+ rxw1.randomString(false,4)
    else
        div.id=params.id;
    div.classList.add('rxw1-layer')
    div.style.position='fixed';
    div.style.display='none';
    div.style.top='0';
    div.style.left='0';
    div.style.bottom='0';
    div.style.right='0';
    div.style.overflow='auto';
    div.style['z-index']=9999999;
    div.style['background-color']='rgba(0, 0, 0, 0.3)';
    document.body.appendChild(div);
    if(params.init!=undefined||params.init!=null){
        params.init(div);
        //div.rxw1_layerInit=params.init;
        //div.rxw1_layerInit();
    }
    div.style.display='block';

    if(params.clickClose){
        $(div).click(function(){
            var e = rxw1.getEvent();
            var target = rxw1.getEventTarget(e);
            if(target == div){
                $(this).remove();
            }
        })
    }
}

//{complete:function({canvas:,imgType:'image/png'}){},xRadio:1,yRadio:1}
rxw1.cutImg = function (params){
    rxw1.chooseFile({chooseEnd:function(input){
        var file=input.files[0];
        if(!file.type || file.type.indexOf('image') != 0){
            throw '选择的文件不是图片格式';
        }else {
            var img = new Image();
            url = window.URL.createObjectURL(file) // 得到bolb对象路径，可当成普通的文件路径一样使用，赋值给src;

            rxw1.layer({
                init:function(layer){
                    layer.style.padding='30px 0';
                    layer.innerHTML="<div style='margin:auto;width:100%;text-align: center;margin-bottom:30px'><button style='width:100px' name='cancel'>取消</button><span style='width:30px;display: inline-block'></span><button style='width:100px' name='confirm'>确定</button></div><img name='targetImg' style='margin:30px auto;display:block;' src='"+url+"'>";

                    var xRadio = params.xRadio||1;
                    var yRadio = params.yRadio||1;

                    $(layer).find('[name=targetImg]').cropper({
                        aspectRatio: xRadio / yRadio,
                        viewMode:1,
                        background:false
                    });

                    $(layer).find('[name=cancel]').click(function(){
                        $(layer).remove();
                    })


                    $(layer).find('[name=confirm]').click(function(){
                        var cas=$(layer).find('[name=targetImg]').cropper('getCroppedCanvas');
                        if(params.complete)
                            params.complete({input:input,canvas:cas})
                        $(layer).remove();
                    })
                }
            })
        }
    }});
}

//{img:, width:, height:, ratio:}
//ratio:0 - 1
rxw1.compressImg = function (params) {
    var canvas, ctx, img64;

    canvas = document.createElement('canvas');
    var width = params.width || params.img.width;
    var height = params.height || params.img.height;
    var ratio = params.ratio||1;

    canvas.width = width;
    canvas.height = height;

    ctx = canvas.getContext("2d");
    ctx.drawImage(params.img, 0, 0, width, height);

    img64 = canvas.toDataURL("image/jpeg", ratio);

    return img64;
}

rxw1.formatTime=function (timestamp) {
    var date = new Date(timestamp);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    var second = date.getSeconds();
    minute = minute < 10 ? ('0' + minute) : minute;
    second = second < 10 ? ('0' + second) : second;
    return y + '-' + m + '-' + d+' '+h+':'+minute+':'+second;
};

rxw1.formatDate=function (timestamp) {
    var date = new Date(timestamp);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    return y + '-' + m + '-' + d;
};
rxw1.convertBase64UrlToBlob=function(urlData){
    var arr = urlData.split(','), mime = arr[0].match(/:(.*?);/)[1],
        bstr = atob(arr[1]), n = bstr.length, u8arr = new Uint8Array(n);
    while(n--){
        u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], {type:mime});
}

rxw1.chooseFile = function (params){
    var inputId = 'rxw1-'+rxw1.randomString(false,12);
    var input=document.createElement("input");
    input.type='file';
    input.id=inputId;
    input.name='file';
    input.style.display='none';
    rxw1.addEvent(input,'change',function(){
        var file=this.files[0] // 获取input上传的图片数据;
        if(params.chooseEnd)
            params.chooseEnd(this)
      document.body.removeChild(input)
    })
    document.body.appendChild(input);
    input.click();
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
    var event=event||window.event;


    if(event.preventDefault)
        event.preventDefault();
    if(event.returnValue)
        event.returnValue=false;
    return false;
}

rxw1.parseQueryStr =function (queryStr){
    var str=decodeURIComponent(queryStr);
    var arr=str.split("&");
    var obj = {};
    for(var i=0;i < arr.length;i++){
        var arrsub=arr[i].split("=");
        obj[arrsub[0]]=arrsub[1];
    }
    return obj;
}


rxw1.isEmptyStr =function (str){
   if(str == undefined || str == null || this.trimStrToEmpty(str).length==0)
        return true;
    return false;
}

rxw1.trimStrToEmpty = function(str){
    if(str == undefined || str == null || str.length==0)
        return '';
    return str.replace(/^\s+|\s+$/gm,'');
}




rxw1.windowScrollPaging = function (params){
    function getScrollTop() {
        var scrollTop = 0;
        if(document.documentElement && document.documentElement.scrollTop) {
            scrollTop = document.documentElement.scrollTop;
        } else if(document.body) {
            scrollTop = document.body.scrollTop;
        }
        return scrollTop;
    }

    //获取当前可视范围的高度
    function getClientHeight() {
        var clientHeight = 0;
        if(document.body.clientHeight && document.documentElement.clientHeight) {
            clientHeight = Math.min(document.body.clientHeight, document.documentElement.clientHeight);
        } else {
            clientHeight = Math.max(document.body.clientHeight, document.documentElement.clientHeight);
        }
        return clientHeight;
    }

    //获取文档完整的高度
    function getScrollHeight() {
        return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);
    }

    //滚动事件触发
    window.onscroll = function() {
        if(getScrollTop() + getClientHeight() >= getScrollHeight()) {
            if(params.down)
                params.down();
        }else if(getScrollTop() == 0){
            //向上到顶
        }
    }
}

rxw1.scrollEvent= function (ele,down,top){
    ele.onscroll = function() {
        if (this.scrollTop + this.offsetHeight >= this.scrollHeight) {
            if(down)
                down();
        }else if(this.scrollTop == 0){
            if(top)
                top()
        }

    }
}

rxw1.hierarchySelect= function (padId,callback,param){
    this.layer({init:function(layer){
        layer.style['background-color']='rgba(0, 0, 0, 0)';
        var pad=document.createElement("div");
        pad.id=padId;
        pad.style['position']='absolute';
        pad.style['top']='30%';
        pad.style['left']='50%';
        pad.style['transform']='translateX(-50%)';
        pad.style['background-color']='white';
        pad.style['border-radius']='10px';
        pad.style['min-width']='300px';
        pad.style['border']='1px solid lightgrey';

        var selectsDiv=document.createElement("div");
        selectsDiv.style['padding']='20px';
        selectsDiv.style['padding-top']='10px';
        $.each(param,function(index,ele){
            var div=document.createElement("div");
            div.style['margin-top']='10px';
            var span=document.createElement("span");
            span.innerHTML=ele.title+':'
            div.appendChild(span)
            var select=document.createElement("select");
                select.name=ele.name;
                layer.appendChild(select);
                if(ele.init)
                     ele.init(select);
            select.onchange = ele.onchange;
            div.appendChild(select)
            selectsDiv.appendChild(div)
        })
        pad.appendChild(selectsDiv)
        $(pad).append('<div style="width:100%;position: relative;bottom:0;border-top: 1px solid buttonface;"><button name="cancel" style="font-size:14px;font-weight:600;width:50%;height:35px;color: #999;border:none;border-bottom-left-radius: 10px" >取消</button><button name="confirm" style="font-size:14px;font-weight:600;width:50%;height:35px;color:#2f97f0;background: white;border:none;border-bottom-right-radius: 10px" >确认</button></div>')
        layer.appendChild(pad)

        $(pad).find('[name=cancel]').click(function(){
            layer.remove()
        });
        $(pad).find('[name=confirm]').click(function(){
            var pp = {}
            $.each( $(pad).find("select"),function(index,ele){
                var aa = {name:ele.name,value:ele.value,text:$(ele).find('option:selected').text()}
                pp[ele.name]=aa;
            })
            layer.remove()
            if(callback){
                callback(pp);
            }
        });

    }})

}