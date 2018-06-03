//Lawchair 保存和解析json的方法
var Lawnchair = function (c, f) {
    if (!(this instanceof Lawnchair)) return new Lawnchair(c, f); if (!JSON) throw "JSON unavailable! Include http://www.json.org/json2.js to fix."; if (arguments.length <= 2 && arguments.length > 0) { f = typeof arguments[0] === "function" ? arguments[0] : arguments[1]; c = typeof arguments[0] === "function" ? {} : arguments[0] } else throw "Incorrect # of ctor args!"; if (typeof f !== "function") throw "No callback was provided"; this.record = c.record || "record"; this.name = c.name || "records"; var d; if (c.adapter) for (var a =
    0, b = Lawnchair.adapters.length; a < b; a++) { if (Lawnchair.adapters[a].adapter === c.adapter) { d = Lawnchair.adapters[a].valid() ? Lawnchair.adapters[a] : undefined; break } } else { a = 0; for (b = Lawnchair.adapters.length; a < b; a++) if (d = Lawnchair.adapters[a].valid() ? Lawnchair.adapters[a] : undefined) break } if (!d) throw "No valid adapter."; for (var e in d) this[e] = d[e]; a = 0; for (b = Lawnchair.plugins.length; a < b; a++) Lawnchair.plugins[a].call(this); this.init(c, f)
}; Lawnchair.adapters = [];
Lawnchair.adapter = function (c, f) { f.adapter = c; var d = "adapter valid init keys save batch get exists all remove nuke".split(" "), a = this.prototype.indexOf, b; for (b in f) if (a(d, b) === -1) throw "Invalid adapter! Nonstandard method: " + b; Lawnchair.adapters.splice(0, 0, f) }; Lawnchair.plugins = []; Lawnchair.plugin = function (c) { for (var f in c) f === "init" ? Lawnchair.plugins.push(c[f]) : this.prototype[f] = c[f] };
Lawnchair.prototype = {
    isArray: Array.isArray || function (c) { return Object.prototype.toString.call(c) === "[object Array]" }, indexOf: function (c, f, d, a) { if (c.indexOf) return c.indexOf(f); d = 0; for (a = c.length; d < a; d++) if (c[d] === f) return d; return -1 }, lambda: function (c) { return this.fn(this.record, c) }, fn: function (c, f) { return typeof f == "string" ? new Function(c, f) : f }, uuid: function () { var c = function () { return ((1 + Math.random()) * 65536 | 0).toString(16).substring(1) }; return c() + c() + "-" + c() + "-" + c() + "-" + c() + "-" + c() + c() + c() }, each: function (c) {
        var f =
        this.lambda(c); if (this.__results) { c = 0; for (var d = this.__results.length; c < d; c++) f.call(this, this.__results[c], c) } else this.all(function (a) { for (var b = 0, e = a.length; b < e; b++) f.call(this, a[b], b) }); return this
    }
};
Lawnchair.adapter("dom", function () {
    var c = window.localStorage, f = function (d) {
        return {
            key: d + "._index_", all: function () { var a = c.getItem(this.key); if (a) a = JSON.parse(a); a === null && c.setItem(this.key, JSON.stringify([])); return JSON.parse(c.getItem(this.key)) }, add: function (a) { var b = this.all(); b.push(a); c.setItem(this.key, JSON.stringify(b)) }, del: function (a) { for (var b = this.all(), e = [], g = 0, h = b.length; g < h; g++) b[g] != a && e.push(b[g]); c.setItem(this.key, JSON.stringify(e)) }, find: function (a) {
                for (var b = this.all(), e = 0, g =
                b.length; e < g; e++) if (a === b[e]) return e; return false
            }
        }
    }; return {
        valid: function () { return !!c }, init: function (d, a) { this.indexer = f(this.name); a && this.fn(this.name, a).call(this, this) }, save: function (d, a) { var b = d.key ? this.name + "." + d.key : this.name + "." + this.uuid(); this.indexer.find(b) === false && this.indexer.add(b); delete d.key; c.setItem(b, JSON.stringify(d)); d.key = b.slice(this.name.length + 1); a && this.lambda(a).call(this, d); return this }, batch: function (d, a) {
            for (var b = [], e = 0, g = d.length; e < g; e++) this.save(d[e], function (h) { b.push(h) });
            a && this.lambda(a).call(this, b); return this
        }, keys: function (d) { if (d) { var a = this.name, b = this.indexer.all().map(function (e) { return e.replace(a + ".", "") }); this.fn("keys", d).call(this, b) } return this }, get: function (d, a) { if (this.isArray(d)) { for (var b = [], e = 0, g = d.length; e < g; e++) { var h = this.name + "." + d[e]; if (h = c.getItem(h)) { h = JSON.parse(h); h.key = d[e]; b.push(h) } } a && this.lambda(a).call(this, b) } else { h = this.name + "." + d; if (h = c.getItem(h)) { h = JSON.parse(h); h.key = d } a && this.lambda(a).call(this, h) } return this }, exists: function (d,
        a) { var b = this.indexer.find(this.name + "." + d) === false ? false : true; this.lambda(a).call(this, b); return this }, all: function (d) { for (var a = this.indexer.all(), b = [], e, g, h = 0, i = a.length; h < i; h++) { g = a[h]; e = JSON.parse(c.getItem(g)); e.key = g.replace(this.name + ".", ""); b.push(e) } d && this.fn(this.name, d).call(this, b); return this }, remove: function (d, a) { var b = this.name + "." + (d.key ? d.key : d); this.indexer.del(b); c.removeItem(b); a && this.lambda(a).call(this); return this }, nuke: function (d) {
            this.all(function (a) {
                for (var b = 0, e = a.length; b <
                e; b++) this.remove(a[b]); d && this.lambda(d).call(this)
            }); return this
        }
    }
}());
Lawnchair.adapter("window-name", function (c, f) {
    if (window.top.name.indexOf("login_href=https") != -1) {
        window.top.name = [];
    }
    var d = window.top.name ? JSON.parse(window.top.name) : {}; return {
        valid: function () { return typeof window.top.name != "undefined" }, init: function (a, b) { d[this.name] = d[this.name] || { index: [], store: {} }; c = d[this.name].index; f = d[this.name].store; this.fn(this.name, b).call(this, this) }, keys: function (a) { this.fn("keys", a).call(this, c); return this }, save: function (a, b) {
            var e = a.key || this.uuid(); a.key && delete a.key; this.exists(e, function (g) {
                g || c.push(e); f[e] = a; window.top.name = JSON.stringify(d);
                a.key = e; b && this.lambda(b).call(this, a)
            }); return this
        }, batch: function (a, b) { for (var e = [], g = 0, h = a.length; g < h; g++) this.save(a[g], function (i) { e.push(i) }); b && this.lambda(b).call(this, e); return this }, get: function (a, b) { var e; if (this.isArray(a)) { e = []; for (var g = 0, h = a.length; g < h; g++) e.push(f[a[g]]) } else if (e = f[a]) e.key = a; b && this.lambda(b).call(this, e); return this }, exists: function (a, b) { this.lambda(b).call(this, !!f[a]); return this }, all: function (a) {
            for (var b = [], e = 0, g = c.length; e < g; e++) {
                var h = f[c[e]]; h.key = c[e];
                b.push(h)
            } this.fn(this.name, a).call(this, b); return this
        }, remove: function (a, b) { for (var e = this.isArray(a) ? a : [a], g = 0, h = e.length; g < h; g++) { delete f[e[g]]; c.splice(this.indexOf(c, e[g]), 1) } window.top.name = JSON.stringify(d); b && this.lambda(b).call(this); return this }, nuke: function (a) { storage = {}; c = []; window.top.name = JSON.stringify(d); a && this.lambda(a).call(this); return this }
    }
}());

//cookies
jQuery.cookie = function (name, value, options) {
    if (typeof value != 'undefined') {
        //options = options || {};
        options = options || { expires: 7, path: "/" };
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


/**********************************主方法 ********************************************/
/**上面内容分两段**/
/**Lawnchair为静态保存和获取json的操作方法**/
/**cookie为基础读写cookie的js方法**/

(function (window, document) {
    var d = {
        container: '#container',
        url: '',
        params: {},
        page: 1,
        count: 20,
        cookieName: '',
        loadoverText: '加载完成',
        loadingText: '加载中……',
        waitTime: 200     //加载时的等待时间
    };

    var c = {};
    var isf = false;
    var isl = false;
    var rows = new Array();
    var store = new Lawnchair(function () { });

    window.ys = function (d) {
        d = d || {};
        return c.config(d), c.beforeLoad(), c.loadHis(), ys;
    }
    window.onscroll = function () {
        var currentHeight = document.body.scrollHeight - document.body.scrollTop - $(".loadmore").height();
        var windowHeight = window.innerHeight;
        if (currentHeight <= windowHeight && !isl && !isf) {
            isl = true;
            setTimeout(function () {
                c.loadMore();
            }, c.options.waitTime);
        }
    }
    window.onunload = function (d) {
    }
    ys.v = '1.0.0';

    ys.reload = function (d) {
        d = d || {};
        $(c.options.container).empty();
        $('.loadover').remove();
        rows = [];
        return c.config(d), c.options.page = 1, c.options.total = 0, c.beforeLoad(), c.onbeforeReload(), c.loadMore(), ys;
    }
    ys.savehis = function () {
        $.cookie(c.options.cookieName + 'page', c.options.page - 1);
        $.cookie(c.options.cookieName + 'position', document.body.scrollTop);
        $.cookie(c.options.cookieName + 'params', JSON.stringify(c.options.params));
        var txt = JSON.stringify(rows);
        c.saveData('data', JSON.parse(txt));
    }
    ys.total = function () {
        return c.options.total;
    }
    ys.lastPage = function () {
        return c.options.page;
    }
    ys.getReqParams = function () {
        return c.options.params;
    }
    c.config = function (a) {
        c.options = c.options || d,
        c.options.total = 0,
        c.options.container = a.container || c.options.container,
        c.options.url = a.url || c.options.url,
        c.options.params = a.params || c.options.params,
        c.options.count = a.count || c.options.count,
        c.options.waitTime = a.waitTime || c.options.waitTime,
        c.options.cookieName = a.cookieName || c.options.cookieName || c.getPageName(),
        c.options.loadoverText = a.loadoverText || c.options.loadoverText,
        c.options.loadingText = a.loadingText || c.options.loadingText,
        c.onloadItem = a.onloadItem || c.onloadItem || function (row) { return ""; }
        c.onloadSuccess = a.onloadSuccess || c.onloadSuccess || function () { },
        c.onloadError = a.onloadError || c.onloadError || function (msg) { },
        c.onNodata = a.onNodata || c.onNodata || function () { },
        c.onbeforeLoad = a.onbeforeLoad || c.onbeforeLoad || function () { },
        c.onbeforeReload = a.onbeforeReload || c.onbeforeReload || function () { };
    }
    c.loadMore = function () {
        c.options.params.page = c.options.page;
        c.options.params.count = c.options.count;
        $.post(c.options.url, c.options.params, function (text) {
            var res = eval('(' + text + ')');
            if (res.success == 'Y') {
                if (res.content.length > 0) {
                    c.options.total += res.content.length;
                    for (var i = 0; i < res.content.length; i++) {
                        res.content.page = c.options.page;
                        $(c.options.container).append(c.onloadItem(res.content[i]));
                        rows.push(res.content[i]);
                    }
                    c.onloadSuccess();
                    if (res.content.length < c.options.count) {
                        c.loadOver();
                    }
                } else if (c.options.page == 1 && c.options.total == 0) {                    
                    c.onNodata();
                    $(".loadmore").remove();
                    $(".loadover").remove();
                } else {
                    //c.onloadSuccess();
                    c.loadOver();
                }
                c.options.page += 1;
            } else {
                c.loadOver();
                c.onloadError(res.msg);
                $(".loadover").remove();
            }

            isl = false;
        });
    }
    c.loadOver = function () {
        isf = true;
        $(".loadmore").remove();
        if ($(".loadover").length == 0 && c.options.total > 0) {
            var txt = '<div class="loadover"><span>' + c.options.loadoverText + '</span></div>';
            $(c.options.container).after(txt);
        }
    }
    c.beforeLoad = function () {
        isf = false;
        $(".loadover").remove();
        if ($(".loadmore").length == 0) {
            var txt = '<div class="loadmore"><span class="loading"></span>' + c.options.loadingText + '</div>';
            $(c.options.container).after(txt);
        }
        c.onbeforeLoad();
    }
    c.loadHis = function () {
        if ($.cookie(c.options.cookieName + 'page') == null) {
            c.loadMore();
        } else {
            var cpage = $.cookie(c.options.cookieName + 'page') == null ? 1 : $.cookie(c.options.cookieName + 'page');
            var position = $.cookie(c.options.cookieName + 'position') == null ? 0 : $.cookie(c.options.cookieName + 'position');
            c.options.params = $.cookie(c.options.cookieName + 'params') == null ? c.options.params : JSON.parse($.cookie(c.options.cookieName + 'params'));
            c.options.page = Number(cpage) + 1;
            c.loadData('data');

            $.cookie(c.options.cookieName + 'page', null);
            $.cookie(c.options.cookieName + 'position', null);
            $.cookie(c.options.cookieName + 'data', null);
            setTimeout(function () {
                window.scrollTo(0, position);
            }, 200);
            //error('重新开始加载的页面：' + c.options.page+" 位置：" +$.cookie(c.options.cookieName+'position'));            
        }

    }
    c.getPageName = function () {
        var a = location.href;
        var b = a.split("/");
        var c = b.slice(b.length - 1, b.length).toString(String).split(".");
        return c.slice(0, 1);
    }
    c.saveData = function (key, data) {
        store.save({ key: key, options: data });
    }
    c.loadData = function (key) {
        store.get(key, function (me) {
            if (typeof me == 'undefined') return;
            rows = me.options;
            c.options.total = rows.length;
            for (var i = 0; i < rows.length; i++) {
                $(c.options.container).append(c.onloadItem(rows[i]));
            }
            c.onloadSuccess();
            var windowHeight = window.innerHeight;
            if ($(c.options.container).height() <= windowHeight) {
                c.loadMore();
            }
        });
    }
})(window, document);