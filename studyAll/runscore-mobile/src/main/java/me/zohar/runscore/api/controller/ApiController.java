package me.zohar.runscore.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.merchant.param.StartOrderParam;
import me.zohar.runscore.merchant.service.MerchantOrderService;
import me.zohar.runscore.merchant.vo.OrderGatheringCodeVO;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api")
public class ApiController {

	@Autowired
	private MerchantOrderService platformOrderService;

	@PostMapping("/startOrder")
	@ResponseBody
	public Result startOrder(StartOrderParam param) {
		return Result.success().setData(platformOrderService.startOrder(param));
	}

	@GetMapping("/getOrderGatheringCode")
	@ResponseBody
	public Result getOrderGatheringCode(String orderNo) {
		OrderGatheringCodeVO vo = platformOrderService.getOrderGatheringCode(orderNo);
		return Result.success().setData(vo);
	}

	/**
	 * 回调数据
	 * @return
	 */
	@GetMapping("/getReturnString")
	@ResponseBody
	public String getReturnString(HttpServletRequest httpServletRequest) {
		String merchantNum=httpServletRequest.getParameter("merchantNum");//商户号
		String outTradeNo=httpServletRequest.getParameter("outTradeNo");//外部订单号
		String platformOrderNo=httpServletRequest.getParameter("platformOrderNo");//订单号
		String amount=httpServletRequest.getParameter("amount");//金额
		String state=httpServletRequest.getParameter("state");//状态
		String payTime=httpServletRequest.getParameter("payTime");//状态
		String sign=httpServletRequest.getParameter("sign");//签名

		//OrderGatheringCodeVO vo = platformOrderService.getOrderGatheringCode(orderNo);
		return "success";
				//.setData(vo);
	}
}
