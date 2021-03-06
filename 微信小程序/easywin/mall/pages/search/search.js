// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    search: '1',
    inputVal: '',
    inputValTrue:'',
    history:[],
    sort:'0',
    toPageNo:'',
    goodsList: [],
    color:'',
    colorThree: '',
    colorOne: '',
    colorTwo: '',
    mall_id: '',
    token:'',
    userId:'',
  },
  // 记录足迹
  recordtrack: function (e) {
    var that = this
    var recoreId = e.currentTarget.dataset.id;
    

    var mall_id;
    console.log(recoreId)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/useraction/recordtrack',
      data: {
        'mall_id': that.data.mall_id,
        'good_id': recoreId,
        token: that.data.token,
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      method: 'post',
      success: function (res) {

      }
    })
  },
  // 监听input框输入值
  searchVal: function (e) {
    console.log(e.detail.value)
    if (e.detail.value == '') {
      this.setData({
        inputVal: 0,
        inputValTrue: e.detail.value
      })
    } else {
      this.setData({
        inputVal: 1,
        inputValTrue: e.detail.value
      })
    }
  },
  // 取消搜索
  cancel: function () {
    wx.navigateBack({
    })
  },

  // 搜索
  search: function () {
    var that = this;
    var history = that.data.history;
    
    that.setData({
      goodsList:[],
      history:history.concat(that.data.inputValTrue)
    })
    console.log(that.data.history)
    wx.setStorageSync(history, that.data.history)
    that.lastPage(0)
  },

  clearHis:function(){
      var that=this
      that.setData({
        history: [],
      
      })
  },
  // 综合
  comprehensive:function(){
    var that = this;
    that.setData({
      colorThree: '#000',
      colorOne: 'red',
      colorTwo: '#000',
      sort:'0',
      goodsList: [],
      color: '',
    })
    that.lastPage(0)
  },
  // 销量
  Sales: function () {
    var that = this;
    
    that.setData({
      colorThree: '#000',
      colorOne: '#000',
      colorTwo: 'red',
      sort: '1',
      goodsList: [],
      color:'',
    })
    that.lastPage(0)
  },
  // 价格降序
  price: function () {
    var that = this;
    
    if (that.data.color == "asc" || that.data.color == ""){
      that.setData({
        colorThree:'red',
        colorOne:'#000',
        colorTwo:'#000',
        sort: '2',
        color:'desc',
        goodsList: [],
      })
    } else if (that.data.color=="desc"){
      that.setData({
        colorThree: 'red',
        colorOne: '#000',
        colorTwo: '#000',
        sort: '3',
        color: 'asc',
        goodsList: [],
      })
    }
    
    that.lastPage(0)
  },
  // 分页
  lastPage: function (toPageNo){
    wx.showToast({
      title: '正在加载数据',
      // icon: 'loading',
      // duration: 1500
    })
    var that = this;
    var pageSize = 15
    var toPageNo = parseInt(toPageNo) + 1
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/homesearch/search',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        key: that.data.inputValTrue,
        page_no: toPageNo,
        page_size: pageSize,
        sort: that.data.sort,
        // token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {

          var goodsListArr = that.data.goodsList
          console.log("114=====" + goodsListArr)
          var newSchemeListArr = goodsListArr.concat(res.data.data.goods)
          if (res.data.data.goods.length == 0) {
            that.setData({
              goodsList: goodsListArr,
              toPageNo: String(toPageNo)
            });
            wx.showToast({
              title: '数据已全部加载',
              // icon: 'loading',
              // duration: 1500
            })
            console.log(res.data.data.goods.length)
          } else {
            console.log(res.data.data.goods.length)
            that.setData({
              goodsList: newSchemeListArr,
              toPageNo: String(toPageNo)
            });
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
    var that=this
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
    // that.setData({
    //   history: wx.getStorageSync(history)
    // })
    // var a = wx.getStorageSync(history)
    // console.log(a)
      
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
    var that = this
    var toPageNo = that.data.toPageNo
    that.lastPage(toPageNo)
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