// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    rootcateList: ["男装", '女装'],
    showList: [],
    type1_id: '',
    mall_id: '',
    userId:''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var that=this
    var userId = wx.getStorageSync('userId')
    that.setData({
      userId: userId,
    })
  },


  showList: function (type1_id) {
    var that = this
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/goodtype/type2s',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        type1_id: type1_id
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          that.setData({
            showList: res.data.data.type2s,
          })
        }
      },
    })
  },

  // 选择大类
  changeCate: function (e) {

    var that = this
    var id = e.currentTarget.dataset.id;
    wx.setStorageSync('type1_id', id)
    that.showList(id)



    console.log(id)

    for (var i = 0; i < that.data.rootcateList.length; i++) {
      if (that.data.rootcateList[i].typeId == id ) {
        that.data.rootcateList[i].color = '#fff'
        that.data.rootcateList[i].colors = 'red'
      } else {
        that.data.rootcateList[i].color = '#f5f5f5'
        that.data.rootcateList[i].colors = '#f5f5f5'
        that.setData({
          colors: ''
        })
      }

    }
    var rootcateList = that.data.rootcateList
    console.log(that.data.rootcateList)
    that.setData({
      rootcateList: rootcateList,
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
  onShow: function (options) {

    var that = this



    var mall_id = wx.getStorageSync('mall_id')
    that.setData({
      mall_id: mall_id,
    })
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/goodtype',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,

      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          var typeId= res.data.data.type1s[0].typeId
          that.showList(typeId)
          for (var i = 0; i < res.data.data.type1s.length; i++) {
            res.data.data.type1s[i].colors = '#f5f5f5'
          }
          res.data.data.type1s[0].colors = 'red'
          res.data.data.type1s[0].color = '#fff'
          that.setData({
            rootcateList: res.data.data.type1s,
          })
          console.log(res.data.data.type1s)
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
})