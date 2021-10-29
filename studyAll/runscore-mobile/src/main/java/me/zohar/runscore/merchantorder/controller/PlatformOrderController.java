package me.zohar.runscore.merchantorder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.config.security.UserAccountDetails;
import me.zohar.runscore.merchant.param.LowerLevelAccountReceiveOrderQueryCondParam;
import me.zohar.runscore.merchant.param.MyReceiveOrderRecordQueryCondParam;
import me.zohar.runscore.merchant.service.MerchantOrderService;

@Controller
@RequestMapping("/platformOrder")
public class PlatformOrderController {

	@Autowired
	private MerchantOrderService platformOrderService;

	@GetMapping("/userConfirmToPaid")
	@ResponseBody
	public Result userConfirmToPaid(String orderId) {
		UserAccountDetails user = (UserAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		platformOrderService.userConfirmToPaid(user.getUserAccountId(), orderId);
		return Result.success();
	}

	/**
	 * 审核订单列表
	 * @return
	 */
	@GetMapping("/findMyWaitConfirmOrder")
	@ResponseBody
	public Result findMyWaitConfirmOrder() {
		UserAccountDetails user = (UserAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		return Result.success().setData(platformOrderService.findMyWaitConfirmOrder(user.getUserAccountId()));
	}

	/**
	 * 查询接口列表订单列表
	 * @return
	 */
	@GetMapping("/findMyWaitReceivingOrder")
	@ResponseBody
	public Result findMyWaitReceivingOrder() {
		UserAccountDetails user = (UserAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		return Result.success().setData(platformOrderService.findMyWaitReceivingOrder(user.getUserAccountId()));
	}

	/**
	 * 立即接单
	 * @param orderId
	 * @return
	 */
	@GetMapping("/receiveOrder")
	@ResponseBody
	public Result receiveOrder(String orderId) {
		UserAccountDetails user = (UserAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		System.out.println("立即接单》》》》》》》》》》》》》》》");
		System.out.println("用户ID号="+user.getUserAccountId());
		System.out.println("订单号="+orderId);
		platformOrderService.receiveOrder(user.getUserAccountId(), orderId);
		return Result.success();
	}

	@GetMapping("/findMyReceiveOrderRecordByPage")
	@ResponseBody
	public Result findMyReceiveOrderRecordByPage(MyReceiveOrderRecordQueryCondParam param) {
		UserAccountDetails user = (UserAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		param.setReceiverUserName(user.getUsername());
		return Result.success().setData(platformOrderService.findMyReceiveOrderRecordByPage(param));
	}

	@GetMapping("/findLowerLevelAccountReceiveOrderRecordByPage")
	@ResponseBody
	public Result findLowerLevelAccountReceiveOrderRecordByPage(LowerLevelAccountReceiveOrderQueryCondParam param) {
		UserAccountDetails user = (UserAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		param.setCurrentAccountId(user.getUserAccountId());
		return Result.success().setData(platformOrderService.findLowerLevelAccountReceiveOrderRecordByPage(param));
	}

}
