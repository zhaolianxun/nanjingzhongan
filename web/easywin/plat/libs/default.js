function add0(m){return m<10?'0'+m:m }//时间戳转化成时间格式function timeFormat(timestamp){    //timestamp是整数，否则要parseInt转换,不会出现少个0的情况    var time = new Date(timestamp);    var year = time.getFullYear();    var month = time.getMonth()+1;    var date = time.getDate();    return year+'年'+add0(month)+'月'+add0(date)+'日';}//url 参数获得function parseQueryStr(queryStr){    var str=decodeURIComponent(queryStr);    var arr=str.split("&");    var obj = {};    for(var i=0;i < arr.length;i++){        var arrsub=arr[i].split("=");        obj[arrsub[0]]=arrsub[1];    }    return obj;}function imgShow(domEle) {    var div=document.createElement("div");    div.style.position='absolute';    div.style['background-color']='rgba(0,0,0,0.1)';    div.style.top='0';    div.style.left='0';    div.style.bottom='0';    div.style.right='0';    addEvent(div,'click',function(){        this.parentElement.removeChild(this)    })    var img=document.createElement("img");    img.style.position='absolute';    img.style.top='50%';    img.style.left='50%';    img.style.transform='translate(-50%,-50%)';    img.src=domEle.src;    div.appendChild(img);    document.body.appendChild(div);}function waitShow() {    var div=document.createElement("div");    div.id='waitShow123123'    div.style.position='absolute';    div.style['background-color']='rgba(0,0,0,0)';    div.style.top='0';    div.style.left='0';    div.style.bottom='0';    div.style.right='0';    document.body.appendChild(div);    var img=document.createElement("img");    img.style.width='60px';    img.style.position='absolute';    img.style.top='20%';    img.style.left='50%';    img.style.transform='translate(-50%,-50%)';    img.src='/easywin/plat/img/5-121204193R0-50.gif';    div.appendChild(img);}function waitShowStop() {    document.body.removeChild(document.getElementById('waitShow123123'))}function previewImg(getUrlSuccess){    var inputId = Math.round(Math.random()*10);    var input=document.createElement("input");    input.type='file';    input.id=inputId;    input.name='file';    input.style.display='none';    addEvent(input,'change',function(){        var file=this.files[0] // 获取input上传的图片数据;        if(file){            if(!file.type || file.type.indexOf('image') != 0){                alert('请选择一张图片');            }else{            var img=new Image() ;            url=window.URL.createObjectURL(file) // 得到bolb对象路径，可当成普通的文件路径一样使用，赋值给src;            img.src=url;            var div=document.createElement("div");            div.style.position='absolute';            div.style['background-color']='rgba(0,0,0,0.1)';            div.style.top='0';            div.style.left='0';            div.style.bottom='0';            div.style.right='0';            var btnsDiv=document.createElement("div");            btnsDiv.style.position='absolute';            btnsDiv.style.left='50%';            btnsDiv.style.top='10px';            btnsDiv.style.transform='translateX(-50%)';            var confirmBtn=document.createElement("button");            confirmBtn.innerHTML='确定'            confirmBtn.style.width='100px'            confirmBtn.style.height='30px'            confirmBtn.onclick=function(){                getUrlSuccess(uploadImg(inputId));            }            var cancelBtn=document.createElement("button");            cancelBtn.innerHTML='取消'            cancelBtn.style.width='100px'            cancelBtn.style.height='30px'            cancelBtn.style['margin-left']='20px'            btnsDiv.appendChild(confirmBtn);            btnsDiv.appendChild(cancelBtn);            addEvent(div,'click',function(){                this.parentElement.removeChild(this)            })            img.style.position='absolute';            //img.style.top='50%';            img.style.left='50%';            img.style.transform='translateX(-50%)';            div.appendChild(btnsDiv);            div.appendChild(img);            div.appendChild(input);            document.body.appendChild(div);            }        }    })    input.click();}function addEvent(el, type, fn) {    if(el.addEventListener){        el.addEventListener(type,fn,false)    }else if(el.attachEvent()){        el.attachEvent('on' + type,fn)    }else{        return false    }}function cancelHandler(event){    var event=event||window.event;//兼容IE    //取消事件相关的默认行为    if(event.preventDefault)    //标准技术        event.preventDefault();    if(event.returnValue)    //兼容IE9之前的IE        event.returnValue=false;    return false;    //用于处理使用对象属性注册的处理程序}