package rrightway.constant;

public class SysParam {

	// 买家已提交评价图的自动返现时间 2*60 * 60 * 1000l
	public static long reviewedExpiredReturnTime = 2 * 60 * 1000l;
	// 买家未提交评价图的自动返现时间15 * 60 * 60 * 1000l
	public static long unreviewedExpiredReturnTime = 4 * 60 * 1000l;
	// 钱包中的金额从不可转入余额到可转入的时间间隔 15*24*60*60*1000
	public static long walletOutableTime = 2 * 60 * 1000l;
}
