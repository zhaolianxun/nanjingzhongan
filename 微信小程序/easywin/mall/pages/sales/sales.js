// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    popWithdraw:false,
    canWithdrawMoney: '',
    money: '',
    myFans: '',
    myFansCount: '',
    rewardMoney: '',
    withdrewMoney: '',
    mall_id: '',
    amount:'',
    token:'',
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
    var mall_id = wx.getStorageSync('mall_id')
    var token = wx.getStorageSync('token')
    that.setData({
      mall_id: mall_id,
      token: token,
    })
   
    // console.log(that.data.list[0].amount)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/mlc/ent',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        token: token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
            that.setData({
              canWithdrawMoney: res.data.data.canWithdrawMoney,
              money: res.data.data.money,
              myFans: res.data.data.myFans,
              myFansCount: res.data.data.myFansCount,
              rewardMoney: res.data.data.rewardMoney,
              withdrewMoney: res.data.data.withdrewMoney,
              rewardCount: res.data.data.rewardCount,
              withdrewCount: res.data.data.withdrewCount,
            })
        }
      },
    })
  },
  loginPhone: function (e) {
    this.setData({
      amount: e.detail.value
    })
  },
  makesure:function(e){
    var that=this
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/mlc/withdraw/apply',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        token: that.data.token,
        amount: that.data.amount*100,
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          that.setData({
            popWithdraw:false
          })
          that.onLoad()
        }else{
          wx.showToast({
            title: res.data.codeMsg,
            icon: "loading"
            // duration:"loadding"
          })
        }
      },
    })
  },

  goWithDraw:function(){
    var that=this
    that.setData({
      popWithdraw: true
    })
  },
  makesures:function(){
    var that = this
    that.setData({
      popWithdraw: false
    })
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
})