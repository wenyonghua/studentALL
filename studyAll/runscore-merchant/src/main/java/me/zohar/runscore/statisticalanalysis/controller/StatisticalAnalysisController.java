package me.zohar.runscore.statisticalanalysis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.config.security.MerchantAccountDetails;
import me.zohar.runscore.statisticalanalysis.param.MerchantIndexQueryParam;
import me.zohar.runscore.statisticalanalysis.service.MerchantStatisticalAnalysisService;

@Controller
@RequestMapping("/statisticalAnalysis")
public class StatisticalAnalysisController {

	@Autowired
	private MerchantStatisticalAnalysisService statisticalAnalysisService;

	/**
	 * 累计交易总额度
	 * @return
	 */
	@GetMapping("/findTotalStatistical")
	@ResponseBody
	public Result findTotalStatistical() {
		MerchantAccountDetails user = (MerchantAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		return Result.success().setData(statisticalAnalysisService.findTotalStatistical(user.getMerchantId()));
	}

	/**
	 * 本月交易金额
	 * @return
	 */
	@GetMapping("/findMonthStatistical")
	@ResponseBody
	public Result findMonthStatistical() {
		MerchantAccountDetails user = (MerchantAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		return Result.success().setData(statisticalAnalysisService.findMonthStatistical(user.getMerchantId()));
	}

	/**
	 *今日交易金额
	 * @return
	 */
	@GetMapping("/findTodayStatistical")
	@ResponseBody
	public Result findTodayStatistical() {
		MerchantAccountDetails user = (MerchantAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		return Result.success().setData(statisticalAnalysisService.findTodayStatistical(user.getMerchantId()));
	}

	@GetMapping("/findEverydayStatistical")
	@ResponseBody
	public Result findEverydayStatistical(MerchantIndexQueryParam param) {
		MerchantAccountDetails user = (MerchantAccountDetails) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		param.setMerchantId(user.getMerchantId());
		return Result.success().setData(statisticalAnalysisService.findEverydayStatistical(param));
	}

}
