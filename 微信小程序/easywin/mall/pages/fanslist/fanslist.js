// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    list:[],
    userId:'',
  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
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
  // 分页
  lastPage: function (toPageNo) {

    var that = this;
    var pageSize = 5


    var toPageNo = parseInt(toPageNo) + 1
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/mlc/myfans/ent',
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
          var ordersfootlistArr = that.data.list

          var newSchemefootlistArr = ordersfootlistArr.concat(res.data.data.items)
          if (res.data.data.items.length == 0) {
            that.setData({
              list: ordersfootlistArr,
              toPageNo: String(toPageNo)
            });
            console.log(that.data.list)
            wx.showToast({
              title: '数据已全部加载',
              // icon: 'loading',
            })
            console.log(res.data.data.items.length)
          } else {
            console.log(res.data.data.items.length)
            that.setData({
              list: newSchemefootlistArr,
              toPageNo: String(toPageNo)
            });
            console.log(that.data.list)
          }

          var registerTime
          for (var i = 0; i < that.data.list.length; i++) {
            // userLogo = that.data.list[i].userLogo
            // console.log(userLogo)
            // that.data.list[i].userLogo = encodeURIComponent(userLogo)
            registerTime = that.data.list[i].registerTime
            that.data.list[i].registerTime = that.dateChange(registerTime)
          }
          var list = that.data.list
          console.log(list)
          that.setData({
            list: list
          })


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
  // 时间转换
  dateChange: function (data) {
    var date = new Date(data)
    var Y = date.getFullYear() + '/';
    var M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '/';
    var D = date.getDate() < 10 ? '0' + date.getDate() : date.getDate();
    return (Y + M + D)
  },
})