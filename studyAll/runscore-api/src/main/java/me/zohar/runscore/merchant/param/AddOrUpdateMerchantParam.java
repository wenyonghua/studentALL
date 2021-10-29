package me.zohar.runscore.merchant.param;

import java.util.Date;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import lombok.Data;
import me.zohar.runscore.common.utils.IdUtils;
import me.zohar.runscore.merchant.domain.Merchant;

@Data
public class AddOrUpdateMerchantParam {

	private String id;
	
	@NotBlank
	private String merchantNum;//商户号

	@NotBlank
	private String name;//商户名称

	@NotBlank
	private String secretKey;
	
	@NotBlank
	private String relevanceAccountUserName;//用户名称

	private String ipWhitelist;//ip白名单

	/**
	 * 费率
	 */
	private String rate;


	public Merchant convertToPo() {
		Merchant po = new Merchant();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(new Date());
		return po;
	}

}
