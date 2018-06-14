//index.js
//获取应用实例
const app = getApp()

Page({
  data: {

      avatarUrl: "",//用户头像  
      nickName: "",//用户昵称  
      showBind:'0',
      mall_id: '',
      userId:'',
  },
  //事件处理函数
 
  onLoad: function () {
    
    var userId = wx.getStorageSync('userId')
    that.setData({
      userId: userId,
    })
   
  },
 onShow(){
   var that = this
   var mall_id = wx.getStorageSync('mall_id')
   that.setData({
     mall_id: mall_id,
   })
   var token = wx.getStorageSync('token')
   console.log(token)
   // 获取头像信息等
   wx.login({
     success: function (res) {
       console.log(res.code)
       if (res.code) {
         wx.getUserInfo({
           success: function (res) {
             var userInfo = res.userInfo
             var nickName = userInfo.nickName
             var avatarUrl = userInfo.avatarUrl
             var gender = userInfo.gender //性别 0：未知、1：男、2：女
             var province = userInfo.province
             var city = userInfo.city
             var country = userInfo.country
             console.log(avatarUrl)
             that.setData({
               avatarUrl: avatarUrl,
               nickName: nickName,
             })
           }
         })
       } else {
         console.log('登录失败！' + res.errMsg)
       }
     }
   });
   // 获取用户信息
   wx.request({
     url: 'https://passion.njshangka.com/easywin/m/useraction/selfinfo',
     method: "POST",
     data: {
       token: token
     },
     header: {
       "Content-Type": "application/x-www-form-urlencoded",
     },
     success: function (res) {
       if (res.data.code == 0) {
         // console.log(res.data.data.phone)
         if (res.data.data.phone == '' || res.data.data.phone == null) {
           that.setData({
             showBind: '1'
           })
         } else {
           that.setData({
             showBind: ''
           })
         }
       }
     },
   })
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
