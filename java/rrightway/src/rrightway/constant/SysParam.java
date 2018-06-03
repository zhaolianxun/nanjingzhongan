package rrightway.constant;

public class SysParam {

	// 买家已提交评价图的自动返现时间，2天
	public static long reviewedExpiredReturnTime = 0;
	// 买家未提交评价图的自动返现时间，15天
	public static long unreviewedExpiredReturnTime = 0;
	// 钱包中的金额从不可转入余额到可转入的时间间隔，15天
	public static long walletOutableTime = 0;
	// 从同一商家处购买商品间隔时间，30天
	public static long buyFromSameSellerInterval = 0;
	// 买家超出时间未处理，自动同意维权，2天
	public static long autoConfirmRightProjectPeriod = 0;
	// 订单可取消的时间，2小时
	public static long cancelableOrderTime = 0;

	public static String alipay_receipt_qrcode = null;
	public static String service_qq = null;

}
