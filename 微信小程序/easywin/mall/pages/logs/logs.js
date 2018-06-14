const util = require('../../utils/util.js')

Page({
  data: {
    logs: [],
    userId:''
  },
  onLoad: function () {
    var userId = wx.getStorageSync('userId')
    this.setData({
      userId: userId,
    })
  console.log(111)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/c/useraction/logincheck',
      method: "POST",
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.loginIs == 1) {

        }
      }
    })
    wx.request({
      url: 'https://passion.njshangka.com/easywin/c/useraction/getandchkwxopenid',
      method: "POST",
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      data: {
        jscode: jscode,
      },
      success: function (res) {
        if (res.data.loginIs == 1) {

        }
      }
    })
    wx.request({
      url: 'https://passion.njshangka.com/easywin/c/useraction/wxopenidbindphone',
      method: "POST",
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.loginIs == 1) {
          
        }
      }
    })
    this.setData({
      logs: (wx.getStorageSync('logs') || []).map(log => {
        return util.formatTime(new Date(log))
      })
    })
    
  },
  

})
