/**
 * Created by xuxk on 2017/8/16.
 */
//选择城市超过三个字隐藏
function province(){
    if ($('.cityLine .province').html().length <= 3) {
        $('.cityLine .province').html()
    } else {
        $('.cityLine .province').html().substring(0, 3)
        $('.cityLine .province').html($('.cityLine .province').html().substring(0, 3))
    }
}
function city(){
    if( $('.cityLine .city').html().length<=3){
        $('.cityLine .city').html()
    }else{
        $('.cityLine .city').html().substring(0,3)
        $('.cityLine .city').html($('.cityLine .city').html().substring(0,3))
    }
}
function country(){
    if ($('.cityLine .country').html().length <= 3) {
        $('.cityLine .country').html()
    } else {
        $('.cityLine .country').html().substring(0, 3)
        $('.cityLine .country').html($('.cityLine .country').html().substring(0, 3))
    }
}
