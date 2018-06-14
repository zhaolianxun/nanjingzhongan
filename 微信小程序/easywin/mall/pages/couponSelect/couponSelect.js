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
    from: '0',
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
    var usableCoupons = wx.getStorageSync('usableCoupons')
    console.log(wx.getStorageSync('usableCoupons'))
    var token = wx.getStorageSync('token')
    var mall_id = wx.getStorageSync('mall_id')
    that.setData({
      mall_id: mall_id,
      coupons: usableCoupons,
      token: token,
    })
  },
  use:function(e){
    var id = e.currentTarget.dataset.id;
    wx.setStorageSync('coupon_ids', id)
    console.log(id)
    wx.navigateBack({
      url:"../comfireOrder/comfireOrder",
    })
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