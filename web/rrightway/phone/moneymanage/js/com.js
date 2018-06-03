
//添加cookie
var setCookie = function (c_name, value, expiredays) {
    var exdate = new Date()
    exdate.setDate(exdate.getDate() + expiredays)
    cookieVal = c_name + "=" + escape(value) + ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString());
    //    alert(cookieVal);
    document.cookie = cookieVal;
}
//获取cookie
function getCookie(c_name) {
    if (document.cookie.length > 0) {
        c_start = document.cookie.indexOf(c_name + "=")
        if (c_start != -1) {
            c_start = c_start + c_name.length + 1
            c_end = document.cookie.indexOf(";", c_start)
            if (c_end == -1) c_end = document.cookie.length
            //        document.write(document.cookie.substring(c_start,c_end)+"<br>");
            return unescape(document.cookie.substring(c_start, c_end))
        }
    }
    return ""
}

function AppTipsClose() {
    $(".DownLoadTips").hide();
	if($(".viewport").hasClass("pt115"))
	{
		$(".viewport").addClass("pt50").removeClass("pt115");	
		$("header").removeClass("topy");
	}
    setCookie("floatcode", "1", 1);
}

function close_ad(){
	$(".top_ad").hide();
	setCookie("topad", "1", 1);	
}

function n(t, a) {
	"use strict";
	var n = t.style;
	n.webkitTransform = n.MsTransform = n.msTransform = n.MozTransform = n.OTransform = n.transform = a
}

function sethtml(id,value){
	$("#"+id).html(value);	
}

function exit(obj){
	window.location.href='/index.html';
}

//刷新到url页面
function freshpage(url){
	setTimeout(function(){
		window.location.href=url;
	},500);	
}


function clearallcokie() {
    var colist = document.cookie.split(";");
    var cname = "";
    for (var i = 0; i < colist.length; i++) {
        if (colist[i] != "") {
            cname = colist[i].split("=")[0].replace(" ", "");
            console.log(cname);
            $.cookie(cname, null);
        }
    }
}
	
$(function () {

   

           //点击非返回  清除缓存如右上角菜单，底部通用菜单非
    $(document.body).on("click", "a", function () {
        clearallcokie();
    });
    

	var divloading=document.createElement("div");
	divloading.id="loadings";
	divloading.innerHTML="<div></div>玩命加载中<b></b>";
	
	document.body.appendChild(divloading);
	
	var kfqq = "";

	$.ajax({
	    type: "POST",
	    url: "/ashx/getqq.ashx",
	    data: "rad=" + Math.random(),
	    async: false,
	    success: function (res) {


	        kfqq = res;

	    }
	});




    //通用内容
	var comstr = '<div class="post-func post-func-6" id="post_more_btn"><ul id="circle_box"><li><a href="javascript:;" class="i-top"><span>顶部</span></a></li><li><a href="mqqwpa://im/chat?chat_type=wpa&uin=413038044&version=1&src_type=web&web_src=oicqzone.com" target="_blank" class="i-next i-off"><span>客服</span></a></li><li><a href="../search.html" class="i-last i-off"><span style="transform: rotate(-120deg);">搜索</span></a></li><li><a href="../myIndexGuest.html" class="i-tail"><span style="transform: rotate(-180deg);">我的商家联盟</span></a></li><li><a href="../popularize.html" class="i-fav"><span style="transform: rotate(-240deg);">推广</span></a></li><li><a href="../phoneIndex.html" class="i-see"><span style="transform: rotate(-300deg);">首页</span></a></li></ul><div class="i-close" id="post_close_btn"></div></div><div class="post-func-close"></div>';
	$("body").append(comstr);
	
	var c = $("#post_more_btn");	
	
	$(".post-func-close").click(function(){
		$(this).hide();
		
		var p = $("#circle_box").find("li").size(),
		d = 360 / p,
		u = -1 * ($("body").width() / 2 - 125-10),
		h = -1 * ($(window).height() / 2 - 125 - 100),
		t=60;
		$(c).addClass("post-func-open");
		n(document.getElementById("post_more_btn"), "translate3d(" + u + "px," + h + "px,0px) scale(1)");
		
	});
	
	$("#post_close_btn").click(function(){
		$(c).removeClass("post-func-open");
		$(".post-func-close").show();
		n(document.getElementById("post_more_btn"), "translate3d(0,0,0) scale(0.2)");
	});
	
	$(".i-top").click(function() {
        window.scrollTo(0, 0);
		$(c).removeClass("post-func-open");
		$(".post-func-close").show();
		n(document.getElementById("post_more_btn"), "translate3d(0,0,0) scale(0.2)");
    });
	
	//返回顶部
	//var t=window.innerHeight||window.screen.height,i=$("#backtop");
	//$(window).on("scroll",function(){window.pageYOffset<1.2*t?i.addClass("hidden"):i.removeClass("hidden")});
	
	setTimeout(function(){
		$("#loadings").remove();
	},100);
	//alert("隐藏");
	
	//分享
	$("#cshare").click(function(){
	    $(".share_dialog").show();
	});
	
	//购买窗口 
	$(".b_buy").toggle(function(){
		$("#bottom_pop").show();
		$("#bottom_pop").animate({ opacity:1}, 300);
		$(".product_countedit").animate({ bottom: "0" }, 300);
	},function(){
		$(".product_countedit").animate({ bottom: "-311" }, 300,function(){
			$(".search_buy").css("z-index","10"); 
			$("#bottom_pop").hide();});	
	});
	
	//tab
	$(".tab_comm a").click(function(){
		if(!$(this).hasClass("crently"))
		{			
			$(".tabtxt").hide();
			$(".tab"+$(this).attr("rel")).show();
			$(this).addClass("crently").siblings().removeClass("crently");	
		}
	});	
	
	$(".exit").click(function(){
		corfirm("您确定退出登录吗？",exit,$(this));
	});
	
	$(".flbtn").click(function(){
		$(".blank").hide();
		$("#box01").removeClass("add");
	});
	
	$(".blank").click(function(){
		$(this).hide();
		$("#box01").removeClass("add");
		$(".content-box").removeClass("show");
		
	});
	
	$(".return").click(function(){
		$("#"+$(this).attr("rel")).addClass("show");
	});
	
	$(".rebtn").click(function(){
		$(this).parents(".content-box").removeClass("show");
	});
	
	
	//商品筛选
	$(".list_order a,#qsearch").click(function(){
		openrightmenu();
	});
	
	
	//价值下拉
	$(".menu p a").click(function(){
		if($(this).hasClass("order_by"))
		{
			//$(".pop2_nav span a").removeClass("crently");
			$(this).addClass("crently").siblings().removeClass('crently');
			if($(".top_ad").css("display")=="block")
			{
				$(".pop3 .pop3_bg").css({"top":"160px"});
			}
			if($("#pop_2").css("display")=="block")
			{
				
				$("#pop_2").hide();
			}
			else
			{			
				$("#pop_2").show();
			}
			$("#pop_2 a").click(function(){
				if(!$(this).hasClass("crently"))
				{
					//if($(this).index()==0 || $(this).index()==2)
//					{	
//						$(".menu p a.order_by").find("b").eq(0).addClass("down").removeClass("up");
//					}
//					else
//					{						
//						$(".menu p a.order_by").find("b").eq(0).addClass("up").removeClass("down");
//					}
					$("#pop_2 a").removeClass("crently");
					$(this).addClass("crently");
					$("#pop_2").hide();
				}
				else
				{					
					$(".pop3").hide();
				}
			});
			$("#pop_2 .pop3_bg").click(function(){
				$("#pop_2").hide();							 
			});
		}
		else
		{
			$(".menu p a.order_by").find("b").eq(0).removeClass("up").addClass("down");
			$(".pop3").hide();
			if($(this).hasClass("crently"))
			{
				$(this).removeClass("crently");
			}
			else
			{
				$(".menu p a").removeClass("crently");
				$(".menu p a.order_by").removeClass("crently1");
				$(this).addClass("crently");
			}
		}
	});
	
	$("#product_count").toggle(function(){
		$(".search_buy").css("z-index","10001");
		$("#product_countedit").show();
		$("#product_countedit").animate({ opacity:1}, 300);
		$(".product_countedit").animate({ bottom: "0" }, 300);
	},function(){
		$(".product_countedit").animate({ bottom: "-350" }, 300,function(){
			$(".search_buy").css("z-index","10"); 
			$("#product_countedit").hide();});	
	}); 
	
	//倒计时
	$(".lefttime").each(function(i){
		//alert(i);
		if($(this).length>0)
		{
			var obj=$(this);
			var activeInterval=setInterval(function(){
				//alert(i);
				var severtime=$(obj).attr("date").replace(/-/g,"/");
				//alert(severtime);		
				var stdf=getDiffDate(new Date(severtime)-new Date());
				
				if(stdf!="已经结束！")
				{
					//alert(stdf);
					
					$(obj).html(stdf);// 参数为两个时间的毫秒差
					
				}
				else
				{
					$(obj).html(stdf);
					//clearInterval(activeInterval);
					window.location.reload();				
				}
			}, 1000);
		}
	}); 
	
	
	//图片放大
	$(".expendimg").click(function(){
		if($(".expendimg").length>0)
		{
			//停止滚动事件
			$("body").bind("touchmove",function(event){event.preventDefault;});
			
			$("body").append('<div class="expand_show"><img src="'+$(this).attr("bigsrc")+'" /><div class="expandbg"></div></div>');
			$(".expand_show").click(function(){
				$(".expand_show").remove();
				
				//开始滚动事件
				$("body").unbind("touchmove");	
				
			});
			var o=$(".expand_show").find("img").eq(0);
			$(o).load(function () {				
				//alert("图片加载已完成");
				var ow = $(this).width();
				var oh = $(this).height();
				
				var rate = ow / oh;
				var w=$(window).width(),h=$(window).height();
				var nw=ow;
				var nh=oh;
				var xl=w/h;//展示大图片的容器比率
				
				  if (rate < xl) {
					//宽小于高
					if (oh > h) {
					  nh=h;
					  nw=ow * (h / oh);
					}
				  } else if (rate > xl) {
					//高小于宽
					if (ow > w) {
					  nw=w;
					  nh=oh * (w / ow);
					}
				  } else {
					  nw=nh=ow;
				  }
				  $(this).css({"width":0,"height":0,"left":"50%","top":"50%","opacity":"0"});
				 $(this).animate({"width":nw,"height":nh,"display":"block","top":parseInt((h-nh)/2)+"px","left":parseInt((w-nw)/2)+"px","opacity":"1"},300);
			});
		}
	});


    //警告，维权提现
	//$.ajax({
	//    type: "get",
	//    url: "/ashx/getnotice.ashx",
	//    async: false,
	//    data: "rad=" + Math.random(),
	//    success: function (res)
	//    {



	        

	//        if (res == "1") {
	//            //console.log(res);
	//           //  corfirm2("您有订单存在资金风险，请及时核实处理！", "", nuls, $(this));
	//            corfirm5("您有订单存在资金风险，请及时核实处理！", '确定', "/user/msg/msg.aspx?stype=3");

	//        }
	//        else {



	            $.ajax({
	                type: "get",
	                url: "/ashx/wqtx.ashx",
	                async: false,
	                data: "rad=" + Math.random(),
	                success: function (res) {





	                    if (res != "0") {

	                       if(res!="商家")
	                           corfirm5("亲，您有新的维权需要处理！", '确定', res);
	                       else 
	                           corfirm2("【商家请用电脑打开网站进行操作哦！passion.njshangka.com/rrightway/phone/phoneIndex.html","", nuls, $(this));

	                        //  corfirm2("亲，您有新的维权需要处理！", "<a href='#' class='cf60'>马上去处理</a>", nuls, $(this));

	                    }

	                }
	            });

	//        }


	//    }
	//});
	
});

function getDiffDate(m){
	m-=(D=parseInt(m/86400000))*86400000;
	m-=(H=parseInt(m/3600000))*3600000;
	S=parseInt((m-=(M=parseInt(m/60000))*60000)/1000);
	if(M<10 & M>0){	M='0'+M;}
	if(H<10 & H>0){	H='0'+H;}
	if(S<10){S='0'+S;}
	if(D!=0){D+='天';}else{D=""}
	if(m>0)return D+H+'时'+M+'分'+S+'秒';
	else{return "已经下架！";	}
}


//confirm
function corfirm(txt,fun,obj){
	$("body").append('<div class="confirm"><div class="alert"><div>'+txt+'</div><p><a href="javascript:;" class="cancle">取消</a><a href="javascript:;" class="submit"><i></i>确认</a></p></div><div class="con_bg opa50"></div></div>');
	$(".confirm").show();
	$(".submit").click(function(){
		if(fun(obj))
		{
			$(".confirm").remove();
		}
	});
	$(".confirm .cancle,.con_bg").click(function () {
		$(".confirm").remove();
	});
}

//confirm5
function corfirm5(txt, stxt, url) {    
    $("body").append('<div class="confirm"><div class="alert"><div class="f14">' + txt + '</div><p><a href="javascript:;" class="submit all"><i></i>' + stxt + '</a></p></div><div class="con_bg opa50"></div></div>');
    $(".confirm").show();
    $(".submit").click(function () {
        if (fun(obj)) {
            $(".confirm").remove();
        }
    });
    $(".confirm .cancle,.con_bg").click(function () {
        $(".confirm").remove();
    });
}

//confirm3
function corfirm3(txt,sbt_txt,fun,obj){
	$("body").append('<div class="confirm"><div class="alert"><div class="auto f14">'+txt+'</div><p><a href="javascript:;" class="cancle">取消</a><a href="javascript:;" class="submit"><i></i>'+sbt_txt+'</a></p></div><div class="con_bg opa50"></div></div>');
	$(".confirm").show();
	$(".submit").click(function(){
		if(fun(obj))
		{
			$(".confirm").remove();
		}
	});
	$(".confirm .cancle,.con_bg").click(function () {
		$(".confirm").remove();
	});
}

//confirm4 4行
function corfirm4(txt,fun,obj){
	$("body").append('<div class="confirm"><div class="alert row4 f14"><div>'+txt+'</div><p><a href="javascript:;" class="cancle">取消</a><a href="javascript:;" class="submit"><i></i>确定</a></p></div><div class="con_bg opa50"></div></div>');
	$(".confirm").show();
	$(".submit").click(function(){
		if(fun(obj))
		{
			$(".confirm").remove();
		}
	});
	$(".confirm .cancle,.con_bg").click(function () {
		$(".confirm").remove();
	});
}

//余额不足
function nomeny(){
	corfirm2("您的账户余额不足，无法支付！","<a href='#' class='cf60'>马上去充值</a>",nuls,$(this));	
}


function nuls(obj)
{
	$(".confirm2,.confirm").remove();	
}

//confirm2两行提示
function corfirm2(txt,txt2,fun,obj){
	$("body").append('<div class="confirm2"><div class="alert"><div><b>'+txt+'</b><span>'+txt2+'</span></div><p><a href="javascript:;" class="cancle">取消</a><a href="javascript:;" class="submit"><i></i>确认</a></p></div><div class="con_bg opa50"></div></div>');
	$(".confirm2").show();
	$(".submit").click(function(){
		if(fun(obj))
		{
			$(".confirm2").remove();
		}
	});
	$(".confirm2 .cancle,.con_bg").click(function () {
		$(".confirm2").remove();
	});
}

//获取验证码
function getcode() {	
	$(".check_val").addClass("hidden");
	$(".count_val").removeClass("hidden");
	var crenttimes = parseInt($(".count_val i").eq(0).attr("sdata"));
	$(".count_val i").eq(0).text(crenttimes);
	var ints = setInterval(function () {
		
		if (crenttimes <= 1) {
			$(".check_val").removeClass("hidden");
			$(".count_val").addClass("hidden");
			clearInterval(ints);
		}
		else {
			crenttimes = crenttimes - 1;
		}
		$(".count_val i").eq(0).text(crenttimes);
	}, 1000);
}

function error(str){
	if($("#errormsg").length<=0)
	{
		$("body").append('<div class="hidden" id="errormsg"><p>'+str+'</p><div class="opa70"></div></div>');
	}
	else
	{
		$("#errormsg p").html(str);
	}
	$("#errormsg").fadeIn("slow");
	setTimeout(function(){
		$("#errormsg").fadeOut("slow");
	},2000);
}

//弹出右边菜单
function openrightmenu(){
	$(".blank").show();
	$("#box01").addClass("add");
}

//修改数量 
function toDecimal2(x) {  
	var f = parseFloat(x);  
	if (isNaN(f)) {  
		return false;  
	}  
	var f = Math.round(x*100)/100;  
	var s = f.toString();  
	var rs = s.indexOf('.');  
	if (rs < 0) {  
		rs = s.length;  
		s += '.';  
	}  
	while (s.length <= rs + 2) {  
		s += '0';  
	}  
	return s;  
}

function minus(obj) {
	
	var number=$(obj).parent().find(".number").eq(0);	
	var minuesi=$(obj).find("i").eq(0);
	//alert($(number).val());
    var a = parseInt($(number).val(), 10);
	
    if (a <= 1) {
        $(number).val(1);
    } else {
        a--;
        $(number).val(a);
    }
	//alert($(number).val());
	if(a>1)
	{
		$(minuesi).removeClass("desable");
	}
	else if(a==1)
	{
		$(minuesi).addClass("desable");
	}
}
function plus(obj) {
	
    var number=$(obj).parent().find(".number").eq(0);	
	var plusi=$(obj).find("i").eq(0);
	var minuesi=$(obj).parent().find(".minus i").eq(0);
	
	var a = parseInt($(number).val(), 10);
    if (a >= 999) {
        $(number).val(1);
    } else {
        a++;
        $(number).val(a);
    }
	if(a>1)
	{
		$(plusi).removeClass("desable");
		$(minuesi).removeClass("desable");
	}
	else if(a==1)
	{
		$(plusi).addClass("desable");
	}
}
function modify() {
    var a = parseInt($("#number").val(), 10);
    if ("" == $("#number").val()) {
        $("#number").val(1);
        $("#amount").html("1\u4ef6");
        return
    }
    if (!isNaN(a)) {
        if (1 > a || a > 999) {
            $("#number").val(1);
            return
        } else {
            $("#number").val(a);
            $("#amount").html(a + "\u4ef6");
            return
        }
    } else {
        $("#number").val(1);
        $("#amount").html("1\u4ef6")
    }
}



function getArgs(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;

}


function isEmptyValue(value) {

    var type;
    if (value == null) { // 等同于 value === undefined || value === null
        return true;
    }
    type = Object.prototype.toString.call(value).slice(8, -1);
    switch (type) {
        case 'String':
            return !$.trim(value);
        case 'Array':
            return !value.length;
        case 'Object':
            return $.isEmptyObject(value); // 普通对象使用 for...in 判断，有 key 即为 false
        default:
            return false; // 其他对象均视作非空
    }
};


//判断时间
function checktime(s_time, e_time) {
    if (s_time != "" && e_time != "") {
        if (s_time > e_time) {
            error("请选择正确时间！");
            return false;
        }
    }
    return true;
}



//cookies
jQuery.cookie = function (name, value, options) {
    if (typeof value != 'undefined') {
        options = options || {};
        if (value === null) {
            value = '';
            options = $.extend({}, options);
            options.expires = -1;
        }
        var expires = '';
        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
            var date;
            if (typeof options.expires == 'number') {
                date = new Date();
                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
            } else {
                date = options.expires;
            }
            expires = '; expires=' + date.toUTCString();
        }
        var path = options.path ? '; path=' + (options.path) : '';
        var domain = options.domain ? '; domain=' + (options.domain) : '';
        var secure = options.secure ? '; secure' : '';
        document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
    } else {
        var cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
};
