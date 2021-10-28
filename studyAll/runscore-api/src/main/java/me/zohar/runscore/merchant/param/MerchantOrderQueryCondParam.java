package me.zohar.runscore.merchant.param;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zohar.runscore.common.param.PageParam;

@Data
@EqualsAndHashCode(callSuper = false)
public class MerchantOrderQueryCondParam extends PageParam {

	private String orderNo;//订单号

	private String platformName;//商户
	/**
	 * 收款渠道
	 */
	private String gatheringChannelCode;//收款渠道

	/**
	 * 订单状态
	 */
	private String orderState;//订单状态
	/**
	 * 接单人
	 */
	private String receiverUserName;

	/**
	 * 提交开始时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date submitStartTime;

	/**
	 * 提交结束时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date submitEndTime;

	/**
	 * 接单开始时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date receiveOrderStartTime;

	/**
	 * 接单结束时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date receiveOrderEndTime;

	/**
	 * 商户订单号
	 */
	private String outTradeNo;

}
