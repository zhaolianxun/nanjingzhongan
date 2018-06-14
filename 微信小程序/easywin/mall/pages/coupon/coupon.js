// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    token: '',
    toPageNo: '',
    coupons: [],
    state: '0',
    from:'1',
    mall_id: '',
    userId:'',
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
  // 分页
  lastPage: function (useId) {
    var that = this;
    var pageSize = 50;
    var toPageNo = parseInt(toPageNo) + 1
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/mc/mycoupons',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        used: useId,
        page_no: 1,
        page_size: pageSize,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          console.log(res.data.data.coupons)
          that.setData({
            coupons: res.data.data.coupons
          })
        }else if (res.data.code == 20 || res.data.code == 26) {
          wx.hideToast()
        }
        var type1Starttime, type1Endtime
        for (var i = 0; i < that.data.coupons.length; i++) {
          type1Starttime = that.data.coupons[i].type1Starttime
          type1Endtime = that.data.coupons[i].type1Endtime
          that.data.coupons[i].type1Starttime = that.dateChange(type1Starttime)
          that.data.coupons[i].type1Endtime = that.dateChange(type1Endtime)
        }
        var coupons = that.data.coupons
        console.log(coupons)
        that.setData({
          coupons: coupons
        })
      },
    })

  },
  // 未使用
  nouse:function(e){
    var that=this
    that.setData({
      coupons:[],
      state:'0'
    })
    that.lastPage(0)
  },
  // 已使用
  use: function (e) {
    var that = this
    that.setData({
      coupons: [],
      state: '1'
    })
    that.lastPage(1)
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