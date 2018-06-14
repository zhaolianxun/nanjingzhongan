// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    footlist:[],
    tabList:['我的足迹','我的收藏'],
    currentTab:'0',
    left: '',
    startX: 0, //开始坐标
    startY: 0,
    mall_id: '',
    userId:'',
  },
  // 添加购物车
  addOrder: function () {
    var that = this
    // // console.log(that.data.list[0].amount)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/gooddetail',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        good_id: 552446375867,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {

        }
      },
    })

    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/useraction/recordtrack',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        good_id: 552446375867,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {

        }
      },
    })
  },

  // 删除收藏
  del: function (e) {
    var that = this
    var goodId = e.currentTarget.dataset.id;
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/mytrack/remove',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        good_id: goodId,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          for (var i = 0; i < that.data.footlist.length; i++) {
            if (that.data.footlist[i].goodId == goodId) {
              var footlist = that.data.footlist
              console.log(i)
              footlist.splice(i, 1)
              that.setData({
                footlist: footlist
              })
            }
          }
        }
      },
    })
  },
  // 分页
  lastPage: function (toPageNo) {

    var that = this;
    var pageSize = 5


    var toPageNo = parseInt(toPageNo) + 1
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/mytrack',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        page_no: toPageNo,
        page_size: pageSize,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          var ordersfootlistArr = that.data.footlist

          var newSchemefootlistArr = ordersfootlistArr.concat(res.data.data.tracks)
          if (res.data.data.tracks.length == 0) {
            that.setData({
              footlist: ordersfootlistArr,
              toPageNo: String(toPageNo)
            });
            console.log(that.data.footlist)
            wx.showToast({
              title: '数据已全部加载',
              // icon: 'loading',
            })
            console.log(res.data.data.tracks.length)
          } else {
            console.log(res.data.data.tracks.length)
            that.setData({
              footlist: newSchemefootlistArr,
              toPageNo: String(toPageNo)
            });
            console.log(that.data.footlist)
          }




          // console.log(that.data.imgUrls)

        } else if (res.data.code == 20 || res.data.code == 26) {
          wx.hideToast()
          // wx.navigateTo({
          //   url: '../login/login',
          // })
        }
      },
    })

  },
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var that = this
    var userId = wx.getStorageSync('userId')
    that.setData({
      userId: userId,
    })
    var token = wx.getStorageSync('token')
    var mall_id = wx.getStorageSync('mall_id')
    that.setData({
      mall_id: mall_id,
    })
    that.setData({
      token: token,
    })
    that.lastPage(0)

  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },

  /**
  * 用户点击右上角分享
  */
  onShareAppMessage: function () {
    var that = this
    return {
      title: '三分钟小程序',
      path: 'pages/indexPage/index?shareUserId=' + that.data.userId,
      success: function (res) {
        // 转发成功
      },
      fail: function (res) {
        // 转发失败
      }
    }
  },
  //手指触摸动作开始 记录起点X坐标
  touchstart: function (e) {
    //开始触摸时 重置所有删除
    this.data.footlist.forEach(function (v, i) {
      if (v.isTouchMove)//只操作为true的
        v.isTouchMove = false;
    })
    this.setData({
      startX: e.changedTouches[0].clientX,
      startY: e.changedTouches[0].clientY,
      footlist: this.data.footlist
    })
  },
  //滑动事件处理
  touchmove: function (e) {
    var that = this,
      index = e.currentTarget.dataset.index,//当前索引
      startX = that.data.startX,//开始X坐标
      startY = that.data.startY,//开始Y坐标
      touchMoveX = e.changedTouches[0].clientX,//滑动变化坐标
      touchMoveY = e.changedTouches[0].clientY,//滑动变化坐标
      //获取滑动角度
      angle = that.angle({ X: startX, Y: startY }, { X: touchMoveX, Y: touchMoveY });
    that.data.footlist.forEach(function (v, i) {
      v.isTouchMove = false
      //滑动超过30度角 return
      if (Math.abs(angle) > 30) return;
      if (i == index) {
        if (touchMoveX > startX) //右滑
          v.isTouchMove = false
        else //左滑
          v.isTouchMove = true
      }
    })
    //更新数据
    that.setData({
      footlist: that.data.footlist
    })
  },
  /**
   * 计算滑动角度
   * @param {Object} start 起点坐标
   * @param {Object} end 终点坐标
   */
  angle: function (start, end) {
    var _X = end.X - start.X,
      _Y = end.Y - start.Y
    //返回角度 /Math.atan()返回数字的反正切值
    return 360 * Math.atan(_Y / _X) / (2 * Math.PI);
  },
})
