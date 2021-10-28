package me.zohar.runscore.merchant.param;

import java.util.Date;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import lombok.Data;
import me.zohar.runscore.common.utils.IdUtils;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.merchant.domain.MerchantOrder;
import me.zohar.runscore.merchant.domain.MerchantOrderPayInfo;

/**
 * 手动从商户端发起订单
 * 
 * @author zohar
 * @date 2019年6月26日
 *
 */
@Data
public class ManualStartOrderParam {

	/**
	 * 商户号
	 */
	@NotBlank
	private String merchantNum;

	/**
	 * 收款渠道
	 */
	@NotBlank
	private String gatheringChannelCode;

	/**
	 * 收款金额
	 */
	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Double gatheringAmount;

	/**
	 * 商户订单号
	 */
	private String orderNo;

	@NotBlank
	private String notifyUrl;

	@NotBlank
	private String returnUrl;

	private String attch;

	private String sign;
	/**
	 * 外部订单号就是外部商户号
	 */
	@NotBlank
	private String outTradeNo;

	public MerchantOrder convertToPo(String merchantId, Integer orderEffectiveDuration) {
		MerchantOrder po = new MerchantOrder();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setOrderNo(po.getId());//订单号
		po.setSubmitTime(new Date());
		po.setOrderState(Constant.商户订单状态_等待接单);
		po.setMerchantId(merchantId);//商户状态
		po.setUsefulTime(DateUtil.offset(po.getSubmitTime(), DateField.MINUTE, orderEffectiveDuration));
		return po;
	}

	public MerchantOrderPayInfo convertToPayInfoPo(String merchantOrderId) {
		MerchantOrderPayInfo po = new MerchantOrderPayInfo();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());//订单号
		po.setMerchantOrderId(merchantOrderId);//订单号
		po.setAmount(this.getGatheringAmount());
		po.setPayType(this.getGatheringChannelCode());
		po.setNoticeState(Constant.商户订单支付通知状态_未通知);
		po.setOrderNo(this.outTradeNo);//外部订单号
		return po;
	}

}
