package me.zohar.runscore.merchant.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

import com.alibaba.fastjson.JSONObject;
import me.zohar.runscore.common.vo.Result;
import me.zohar.runscore.rechargewithdraw.domain.PayChannel;
import me.zohar.runscore.rechargewithdraw.domain.PayType;
import me.zohar.runscore.rechargewithdraw.repo.PayChannelRepo;
import me.zohar.runscore.rechargewithdraw.repo.PayTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import me.zohar.runscore.common.exception.BizError;
import me.zohar.runscore.common.exception.BizException;
import me.zohar.runscore.common.utils.ThreadPoolUtils;
import me.zohar.runscore.common.valid.ParamValid;
import me.zohar.runscore.common.vo.PageResult;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.dictconfig.DictHolder;
import me.zohar.runscore.dictconfig.vo.DictItemVO;
import me.zohar.runscore.gatheringcode.domain.GatheringCode;
import me.zohar.runscore.gatheringcode.repo.GatheringCodeRepo;
import me.zohar.runscore.mastercontrol.domain.ReceiveOrderSetting;
import me.zohar.runscore.mastercontrol.repo.ReceiveOrderSettingRepo;
import me.zohar.runscore.merchant.domain.Merchant;
import me.zohar.runscore.merchant.domain.MerchantOrder;
import me.zohar.runscore.merchant.domain.MerchantOrderPayInfo;
import me.zohar.runscore.merchant.domain.OrderRebate;
import me.zohar.runscore.merchant.param.LowerLevelAccountReceiveOrderQueryCondParam;
import me.zohar.runscore.merchant.param.ManualStartOrderParam;
import me.zohar.runscore.merchant.param.MerchantOrderQueryCondParam;
import me.zohar.runscore.merchant.param.MyReceiveOrderRecordQueryCondParam;
import me.zohar.runscore.merchant.param.StartOrderParam;
import me.zohar.runscore.merchant.repo.MerchantOrderPayInfoRepo;
import me.zohar.runscore.merchant.repo.MerchantOrderRepo;
import me.zohar.runscore.merchant.repo.MerchantRepo;
import me.zohar.runscore.merchant.repo.OrderRebateRepo;
import me.zohar.runscore.merchant.vo.MerchantOrderDetailsVO;
import me.zohar.runscore.merchant.vo.MerchantOrderVO;
import me.zohar.runscore.merchant.vo.MyWaitConfirmOrderVO;
import me.zohar.runscore.merchant.vo.MyWaitReceivingOrderVO;
import me.zohar.runscore.merchant.vo.OrderGatheringCodeVO;
import me.zohar.runscore.merchant.vo.ReceiveOrderRecordVO;
import me.zohar.runscore.merchant.vo.StartOrderSuccessVO;
import me.zohar.runscore.useraccount.domain.AccountChangeLog;
import me.zohar.runscore.useraccount.domain.UserAccount;
import me.zohar.runscore.useraccount.repo.AccountChangeLogRepo;
import me.zohar.runscore.useraccount.repo.UserAccountRepo;

@Validated
@Slf4j
@Service
public class MerchantOrderService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private MerchantOrderRepo merchantOrderRepo;

	@Autowired
	private MerchantOrderPayInfoRepo merchantOrderPayInfoRepo;

	@Autowired
	private MerchantRepo merchantRepo;

	@Autowired
	private UserAccountRepo userAccountRepo;

	@Autowired
	private GatheringCodeRepo gatheringCodeRepo;

	@Autowired
	private AccountChangeLogRepo accountChangeLogRepo;

	@Autowired
	private ReceiveOrderSettingRepo platformOrderSettingRepo;

	@Autowired
	private OrderRebateRepo orderRebateRepo;

	@Autowired
	private PayTypeRepo payTypeRepo;

	@Autowired
	private PayChannelRepo payChannelRepo;


	@Transactional(readOnly = true)
	public MerchantOrderDetailsVO findMerchantOrderDetailsById(@NotBlank String orderId) {
		MerchantOrderDetailsVO vo = MerchantOrderDetailsVO.convertFor(merchantOrderRepo.getOne(orderId));
		return vo;
	}

	/**
	 * ????????????????????????
	 * 
	 * @param orderId
	 */
	@Transactional
	public void customerServiceCancelOrderRefund(@NotBlank String orderId) {
		MerchantOrder merchantOrder = merchantOrderRepo.getOne(orderId);
		if (!(Constant.??????????????????_?????????.equals(merchantOrder.getOrderState()))) {
			throw new BizException(BizError.??????????????????????????????????????????????????????);
		}
		UserAccount userAccount = merchantOrder.getReceivedAccount();
		Double cashDeposit = NumberUtil.round(userAccount.getCashDeposit() + merchantOrder.getGatheringAmount(), 4)
				.doubleValue();
		userAccount.setCashDeposit(cashDeposit);
		userAccountRepo.save(userAccount);
		merchantOrder.customerCancelOrderRefund();
		merchantOrderRepo.save(merchantOrder);
		accountChangeLogRepo.save(AccountChangeLog.buildWithCustomerCancelOrderRefund(userAccount, merchantOrder));
	}

	// @Transactional
	// public void merchantConfirmToPaid(@NotBlank String secretKey, @NotBlank
	// String orderId) {
	// Merchant merchant = merchantRepo.findByMerchantNum(secretKey);
	// if (merchant == null) {
	// throw new BizException(BizError.???????????????);
	// }
	// MerchantOrder order = merchantOrderRepo.findById(orderId).orElse(null);
	// if (order == null) {
	// log.error("?????????????????????;secretKey:{},orderId:{}", secretKey, orderId);
	// throw new BizException(BizError.?????????????????????);
	// }
	// if (!order.getMerchantId().equals(merchant.getId())) {
	// log.error("??????????????????????????????????????????????????????;secretKey:{},orderId:{}", secretKey, orderId);
	// throw new BizException(BizError.??????????????????????????????????????????????????????);
	// }
	// if (!Constant.??????????????????_?????????.equals(order.getOrderState())) {
	// throw new BizException(BizError.?????????????????????????????????????????????????????????);
	// }
	// order.merchantConfirmToPaid();
	// merchantOrderRepo.save(order);
	// }

	@Transactional(readOnly = true)
	public OrderGatheringCodeVO getOrderGatheringCode(@NotBlank String orderNo) {
		MerchantOrder order = merchantOrderRepo.findByOrderNo(orderNo);
		if (order == null) {
			log.error("?????????????????????;orderNo:{}", orderNo);
			throw new BizException(BizError.?????????????????????);
		}
		String gatheringCodeStorageId = getGatheringCodeStorageId(order.getReceivedAccountId(),
				order.getGatheringChannelCode(), order.getGatheringAmount());
		OrderGatheringCodeVO vo = OrderGatheringCodeVO.convertFor(order);
		vo.setGatheringCodeStorageId(gatheringCodeStorageId);
		return vo;
	}

	@Transactional(readOnly = true)
	public String getGatheringCodeStorageId(String receivedAccountId, String gatheringChannelCode,
			Double gatheringAmount) {
		ReceiveOrderSetting merchantOrderSetting = platformOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (merchantOrderSetting.getUnfixedGatheringCodeReceiveOrder()) {
			GatheringCode gatheringCode = gatheringCodeRepo
					.findTopByUserAccountIdAndGatheringChannelCodeAndFixedGatheringAmountIsFalse(receivedAccountId,
							gatheringChannelCode);
			if (gatheringCode != null) {
				return gatheringCode.getStorageId();
			}
		} else {
			GatheringCode gatheringCode = gatheringCodeRepo
					.findTopByUserAccountIdAndGatheringChannelCodeAndGatheringAmount(receivedAccountId,
							gatheringChannelCode, gatheringAmount);
			if (gatheringCode != null) {
				return gatheringCode.getStorageId();
			}
		}
		return null;
	}

	@Transactional
	public void userConfirmToPaid(@NotBlank String userAccountId, @NotBlank String orderId) {
		MerchantOrder platformOrder = merchantOrderRepo.findByIdAndReceivedAccountId(orderId, userAccountId);
		if (platformOrder == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (!(Constant.??????????????????_?????????.equals(platformOrder.getOrderState())
				|| Constant.??????????????????_?????????????????????.equals(platformOrder.getOrderState()))) {
			throw new BizException(BizError.???????????????????????????????????????????????????????????????????????????);
		}
		platformOrder.confirmToPaid(null);
		merchantOrderRepo.save(platformOrder);
		receiveOrderBountySettlement(platformOrder);
	}

	/**
	 * ?????????????????????
	 * 
	 * @param orderId
	 */
	@Transactional
	public void customerServiceConfirmToPaid(@NotBlank String orderId, String note) {
		MerchantOrder platformOrder = merchantOrderRepo.findById(orderId).orElse(null);
		if (platformOrder == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (!Constant.??????????????????_?????????.equals(platformOrder.getOrderState())) {
			throw new BizException(BizError.???????????????????????????????????????????????????);
		}
		platformOrder.confirmToPaid(note);
		merchantOrderRepo.save(platformOrder);
		receiveOrderBountySettlement(platformOrder);
	}

	/**
	 * ?????????????????????
	 */
	@Transactional
	public void receiveOrderBountySettlement(MerchantOrder merchantOrder) {
		UserAccount userAccount = merchantOrder.getReceivedAccount();
		double bounty = NumberUtil.round(merchantOrder.getGatheringAmount() * userAccount.getRebate() * 0.01, 4)
				.doubleValue();
		merchantOrder.updateBounty(bounty);
		merchantOrderRepo.save(merchantOrder);
		userAccount.setCashDeposit(NumberUtil.round(userAccount.getCashDeposit() + bounty, 4).doubleValue());
		userAccountRepo.save(userAccount);
		accountChangeLogRepo
				.save(AccountChangeLog.buildWithReceiveOrderBounty(userAccount, bounty, userAccount.getRebate()));
		generateOrderRebate(merchantOrder);
		ThreadPoolUtils.getPaidMerchantOrderPool().schedule(() -> {
			redisTemplate.opsForList().leftPush(Constant.????????????ID, merchantOrder.getId());
		}, 1, TimeUnit.SECONDS);
		System.out.println("???redis ????????????");
	}

	/**
	 * ??????????????????
	 * 
	 * @param bettingOrder
	 */
	public void generateOrderRebate(MerchantOrder merchantOrder) {
		UserAccount userAccount = merchantOrder.getReceivedAccount();
		UserAccount superior = merchantOrder.getReceivedAccount().getInviter();
		while (superior != null) {
			double rebate = NumberUtil.round(superior.getRebate() - userAccount.getRebate(), 4).doubleValue();
			if (rebate < 0) {
				log.error("??????????????????,?????????????????????????????????????????????;????????????id:{},????????????id:{}", userAccount.getId(), superior.getId());
				break;
			}
			double rebateAmount = NumberUtil.round(merchantOrder.getGatheringAmount() * rebate * 0.01, 4).doubleValue();
			OrderRebate orderRebate = OrderRebate.build(rebate, rebateAmount, merchantOrder.getId(), superior.getId());
			orderRebateRepo.save(orderRebate);
			userAccount = superior;
			superior = superior.getInviter();
		}
	}

	@Transactional(readOnly = true)
	public void orderRebateAutoSettlement() {
		List<OrderRebate> orderRebates = orderRebateRepo.findBySettlementTimeIsNull();
		for (OrderRebate orderRebate : orderRebates) {
			redisTemplate.opsForList().leftPush(Constant.????????????ID, orderRebate.getId());
		}
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param issueId
	 */
	@Transactional(readOnly = true)
	public void noticeOrderRebateSettlement(@NotBlank String orderId) {
		List<OrderRebate> orderRebates = orderRebateRepo.findByMerchantOrderId(orderId);
		for (OrderRebate orderRebate : orderRebates) {
			redisTemplate.opsForList().leftPush(Constant.????????????ID, orderRebate.getId());
		}
	}

	/**
	 * ??????????????????
	 */
	@Transactional
	public void orderRebateSettlement(@NotBlank String orderRebateId) {
		OrderRebate orderRebate = orderRebateRepo.getOne(orderRebateId);
		if (orderRebate.getSettlementTime() != null) {
			log.warn("????????????????????????????????????,??????????????????;id:{}", orderRebateId);
			return;
		}
		orderRebate.settlement();
		orderRebateRepo.save(orderRebate);
		UserAccount userAccount = orderRebate.getRebateAccount();
		double cashDeposit = userAccount.getCashDeposit() + orderRebate.getRebateAmount();
		userAccount.setCashDeposit(NumberUtil.round(cashDeposit, 4).doubleValue());
		userAccountRepo.save(userAccount);
		accountChangeLogRepo.save(AccountChangeLog.buildWithOrderRebate(userAccount, orderRebate));
	}

	@Transactional(readOnly = true)
	public List<MyWaitConfirmOrderVO> findMyWaitConfirmOrder(@NotBlank String userAccountId) {
		return MyWaitConfirmOrderVO
				.convertFor(merchantOrderRepo.findByOrderStateInAndReceivedAccountIdOrderBySubmitTimeDesc(
						Arrays.asList(Constant.??????????????????_?????????, Constant.??????????????????_?????????????????????), userAccountId));
	}

	@Transactional(readOnly = true)
	public List<MyWaitReceivingOrderVO> findMyWaitReceivingOrder(@NotBlank String userAccountId) {
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);//??????????????????

		List<GatheringCode> gatheringCodes = gatheringCodeRepo.findByUserAccountId(userAccountId);
		if (CollectionUtil.isEmpty(gatheringCodes)) {
			throw new BizException(BizError.??????????????????????????????);
		}
		if(userAccount.getCashDeposit().equals(0.00)){//?????????
			throw new BizException(BizError.??????????????????????????????);
		}
		ReceiveOrderSetting merchantOrderSetting = platformOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (merchantOrderSetting.getUnfixedGatheringCodeReceiveOrder()) {
			Map<String, String> gatheringChannelCodeMap = new HashMap<>();
			for (GatheringCode gatheringCode : gatheringCodes) {
				gatheringChannelCodeMap.put(gatheringCode.getGatheringChannelCode(),
						gatheringCode.getGatheringChannelCode());
			}
			List<MerchantOrder> waitReceivingOrders = merchantOrderRepo
					.findTop10ByOrderStateAndGatheringAmountIsLessThanEqualAndGatheringChannelCodeInOrderBySubmitTimeDesc(
							Constant.??????????????????_????????????, userAccount.getCashDeposit(),
							new ArrayList<>(gatheringChannelCodeMap.keySet()));
			return MyWaitReceivingOrderVO.convertFor(waitReceivingOrders);//?????????
		}
		Map<String, List<Double>> gatheringChannelCodeMap = new HashMap<>();
		for (GatheringCode gatheringCode : gatheringCodes) {
			if (gatheringChannelCodeMap.get(gatheringCode.getGatheringChannelCode()) == null) {
				gatheringChannelCodeMap.put(gatheringCode.getGatheringChannelCode(), new ArrayList<>());
			}
			if (userAccount.getCashDeposit() < gatheringCode.getGatheringAmount()) {
				continue;
			}
			gatheringChannelCodeMap.get(gatheringCode.getGatheringChannelCode())
					.add(gatheringCode.getGatheringAmount());
		}
		List<MerchantOrder> waitReceivingOrders = new ArrayList<>();
		for (Entry<String, List<Double>> entry : gatheringChannelCodeMap.entrySet()) {
			if (CollectionUtil.isEmpty(entry.getValue())) {
				continue;
			}
			List<MerchantOrder> tmpOrders = merchantOrderRepo
					.findTop10ByOrderStateAndGatheringAmountInAndGatheringChannelCodeOrderBySubmitTimeDesc(
							Constant.??????????????????_????????????, entry.getValue(), entry.getKey());
			waitReceivingOrders.addAll(tmpOrders);
		}
		Collections.sort(waitReceivingOrders, new Comparator<MerchantOrder>() {

			@Override
			public int compare(MerchantOrder o1, MerchantOrder o2) {
				return o1.getSubmitTime().before(o2.getSubmitTime()) ? 1 : -1;
			}
		});
		if (waitReceivingOrders.isEmpty()) {
			return MyWaitReceivingOrderVO.convertFor(waitReceivingOrders);
		}
		return MyWaitReceivingOrderVO.convertFor(
				waitReceivingOrders.subList(0, waitReceivingOrders.size() >= 10 ? 10 : waitReceivingOrders.size()));
	}

	@ParamValid
	@Transactional
	public MerchantOrderVO manualStartOrder(ManualStartOrderParam param) {
		Merchant merchant = merchantRepo.findByMerchantNum(param.getMerchantNum());//?????????????????????????????????????????????user_account
		if (merchant == null) {
			throw new BizException(BizError.???????????????);
		}
		System.out.println(">>>>>>????????????="+merchant.getRelevanceAccount().getBankCardAccount());//????????????
		PayType payType= payTypeRepo.findByAndMerchantNum(param.getMerchantNum());//?????????????????????????????????
		System.out.println(payType.getId());

		List<PayChannel> payChannelList=payChannelRepo.findByPayTypeId(payType.getId());//???????????????????????????????????????



		String sign = param.getMerchantNum() + param.getOrderNo()
				+ new DecimalFormat("###################.###########").format(param.getGatheringAmount())
				+ param.getNotifyUrl() + merchant.getSecretKey();
		sign = new Digester(DigestAlgorithm.MD5).digestHex(sign);//md5??????
		param.setSign(sign);

		Integer orderEffectiveDuration = Constant.??????????????????????????????;
		ReceiveOrderSetting setting = platformOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting != null) {
			orderEffectiveDuration = setting.getReceiveOrderEffectiveDuration();
		}
		MerchantOrder merchantOrder = param.convertToPo(merchant.getId(), orderEffectiveDuration);
		MerchantOrderPayInfo payInfo = param.convertToPayInfoPo(merchantOrder.getId());
		merchantOrder.setPayInfoId(payInfo.getId());
		merchantOrderRepo.save(merchantOrder);//????????????
		merchantOrderPayInfoRepo.save(payInfo);//?????????????????????


		return MerchantOrderVO.convertFor(merchantOrder);
	}


	/**
	 * ??????????????????
	 * @param param
	 * @return
	 */
	@ParamValid
	@Transactional
	public Result outstartOrder(ManualStartOrderParam param,HttpServletRequest request) {

		Merchant merchant = merchantRepo.findByMerchantNum(param.getMerchantNum());//?????????????????????????????????????????????user_account
		if (merchant == null) {
			throw new BizException(BizError.???????????????);
		}
		System.out.println(">>>>>>????????????="+merchant.getRelevanceAccount().getBankCardAccount());//????????????
		String ip=getRemortIP(request);
		System.out.println(">>>>>>?????????IP="+ip);//????????????
		if(!merchant.getIpWhitelist().contains(ip)){//ip?????????????????????
			throw new BizException(BizError.IP???????????????????????????);
		}

		PayType payType= payTypeRepo.findByAndMerchantNum(param.getMerchantNum());//?????????????????????????????????
		if(payType == null){
			throw new BizException(BizError.??????????????????????????????);
		}
		System.out.println(payType.getId());


		List<PayChannel> payChannelList=payChannelRepo.findByPayTypeId(payType.getId());//???????????????????????????????????????
		PayChannel payChannel=null;
if(payChannelList!=null){
	for(PayChannel payChannelValue:payChannelList){
		System.out.println("????????????????????????="+payChannelValue.getAccountHolder());
	    System.out.println("????????????="+payChannelValue.getBankName());
	    System.out.println("????????????="+payChannelValue.getBankCardAccount());
		payChannel=payChannelValue;
	}
}

		String sign = param.getMerchantNum() + param.getOrderNo()
				+ new DecimalFormat("###################.###########").format(param.getGatheringAmount())
				+ param.getNotifyUrl() + merchant.getSecretKey();
		sign = new Digester(DigestAlgorithm.MD5).digestHex(sign);//md5??????
		param.setSign(sign);

		Integer orderEffectiveDuration = Constant.??????????????????????????????;
		ReceiveOrderSetting setting = platformOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting != null) {
			orderEffectiveDuration = setting.getReceiveOrderEffectiveDuration();
		}
		MerchantOrder merchantOrder = param.convertToPo(merchant.getId(), orderEffectiveDuration);
		merchantOrder.setAccountHolder(payChannel.getAccountHolder());//???????????????
		merchantOrder.setBankCardAccount(payChannel.getBankCardAccount());//??????
		merchantOrder.setBankName(payChannel.getBankName());//????????????
		merchantOrder.setCymbalCode("12345");//?????????
		merchantOrder.setOutTradeNo(param.getOutTradeNo());//???????????????
		merchantOrder.setMerchantNum(param.getMerchantNum());//?????????


		BigDecimal  bigDecimalAmount=new BigDecimal(param.getGatheringAmount());//????????????
		BigDecimal bigServiceCharge = bigDecimalAmount.multiply(new BigDecimal(merchant.getRate()));//?????????=????????????*??????(0.1)
		merchantOrder.setServiceCharge(bigServiceCharge.toString());//?????????=??????*??????
		merchantOrder.setNetAmout(bigDecimalAmount.subtract(bigServiceCharge).toString());//??????=????????????-?????????




		MerchantOrderPayInfo payInfo = param.convertToPayInfoPo(merchantOrder.getId());
		merchantOrder.setPayInfoId(payInfo.getId());//??????payinfoid ???



		merchantOrderRepo.save(merchantOrder);//????????????
		merchantOrderPayInfoRepo.save(payInfo);//?????????????????????



		JSONObject jsonObjectData=new JSONObject();

		jsonObjectData.put("bankName","????????????");//????????????
		jsonObjectData.put("bankCardAccount","1244242");//????????????
		jsonObjectData.put("accountHolder","??????");//???????????????
		jsonObjectData.put("cymbalCode","1223");//???????????????
		jsonObjectData.put("orderNo",merchantOrder.getId());//?????????
		jsonObjectData.put("urlpay","http://www.baidu.com");//????????????????????????

		return Result.success(jsonObjectData);
				//jsonObject.toJSONString();
				//MerchantOrderVO.convertFor(merchantOrder);
	}

	/**
	 * ??????
	 * 
	 * @param param
	 * @return
	 */
	//@Lock(keys = "'receiveOrder_' + #orderId")
	@Transactional
	public void receiveOrder(@NotBlank String userAccountId, @NotBlank String orderId) {
		List<GatheringCode> gatheringCodes = gatheringCodeRepo.findByUserAccountId(userAccountId);//?????????
		if (CollectionUtil.isEmpty(gatheringCodes)) {
			throw new BizException(BizError.??????????????????????????????);
		}
		MerchantOrder platformOrder = merchantOrderRepo.getOne(orderId);
		if (platformOrder == null) {
			throw new BizException(BizError.?????????????????????);
		}
		System.out.println("????????????:"+platformOrder.getOrderState());
		System.out.println("????????????="+platformOrder.getGatheringChannelCode());
		System.out.println("????????????="+platformOrder.getGatheringAmount());

		if (!Constant.??????????????????_????????????.equals(platformOrder.getOrderState())) {
			throw new BizException(BizError.???????????????????????????);
		}
		String gatheringCodeStorageId = getGatheringCodeStorageId(userAccountId,
				platformOrder.getGatheringChannelCode(), platformOrder.getGatheringAmount());

		if (StrUtil.isBlank(gatheringCodeStorageId)) {
			throw new BizException(BizError.?????????????????????????????????????????????);
		}
		// ????????????????????????????????????,???????????????,???????????????
		ReceiveOrderSetting setting = platformOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting != null && setting.getReceiveOrderUpperLimit() != null) {
			List<MyWaitConfirmOrderVO> waitConfirmOrders = findMyWaitConfirmOrder(userAccountId);
			if (waitConfirmOrders.size() >= setting.getReceiveOrderUpperLimit()) {
				throw new BizException(BizError.???????????????????????????);
			}
		}
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		if (setting != null && setting.getCashDepositMinimumRequire() != null) {
			if (userAccount.getCashDeposit() < setting.getCashDepositMinimumRequire()) {
				throw new BizException(BizError.????????????????????????????????????);
			}
		}
		Double cashDeposit = NumberUtil.round(userAccount.getCashDeposit() - platformOrder.getGatheringAmount(), 4)
				.doubleValue();
		if (cashDeposit < 0) {
			throw new BizException(BizError.???????????????????????????);
		}

		userAccount.setCashDeposit(cashDeposit);
		userAccountRepo.save(userAccount);

		Integer orderEffectiveDuration = Constant.??????????????????????????????;
		if (setting != null && setting.getOrderPayEffectiveDuration() != null) {
			orderEffectiveDuration = setting.getOrderPayEffectiveDuration();
		}
		platformOrder.updateReceived(userAccount.getId(), gatheringCodeStorageId);
		platformOrder.updateUsefulTime(
				DateUtil.offset(platformOrder.getReceivedTime(), DateField.MINUTE, orderEffectiveDuration));
		merchantOrderRepo.save(platformOrder);
		accountChangeLogRepo.save(AccountChangeLog.buildWithReceiveOrderDeduction(userAccount, platformOrder));
	}

	@Transactional(readOnly = true)
	public PageResult<MerchantOrderVO> findMerchantOrderByPage(MerchantOrderQueryCondParam param) {
		Specification<MerchantOrder> spec = new Specification<MerchantOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MerchantOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo()));
				}//?????????
				if (StrUtil.isNotBlank(param.getOutTradeNo())) {
					predicates.add(builder.equal(root.get("outTradeNo"), param.getOutTradeNo()));
				}//???????????????
				if (StrUtil.isNotBlank(param.getMerchantNum())) {
					predicates.add(builder.equal(root.get("merchantNum"), param.getMerchantNum()));
				}//?????????
				if (StrUtil.isNotBlank(param.getBankCardAccount())) {
					predicates.add(builder.equal(root.get("bankCardAccount"), param.getBankCardAccount()));
				}//????????????

				if (StrUtil.isNotBlank(param.getPlatformName())) {//????????????
					predicates.add(
							builder.equal(root.join("merchant", JoinType.INNER).get("name"), param.getPlatformName()));
				}
				if (StrUtil.isNotBlank(param.getGatheringChannelCode())) {
					predicates.add(builder.equal(root.get("gatheringChannelCode"), param.getGatheringChannelCode()));
				}//gatheringChannelCode ????????????

				if (StrUtil.isNotBlank(param.getOrderState())) {
					predicates.add(builder.equal(root.get("orderState"), param.getOrderState()));
				}//????????????
				if (StrUtil.isNotBlank(param.getReceiverUserName())) {
					predicates.add(builder.equal(root.join("userAccount", JoinType.INNER).get("userName"),
							param.getReceiverUserName()));
				}//?????????
				if (param.getSubmitStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("submitTime").as(Date.class),
							DateUtil.beginOfDay(param.getSubmitStartTime())));
				}
				if (param.getSubmitEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("submitTime").as(Date.class),
							DateUtil.endOfDay(param.getSubmitEndTime())));
				}
				if (param.getReceiveOrderStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.beginOfDay(param.getReceiveOrderStartTime())));
				}
				if (param.getReceiveOrderEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.endOfDay(param.getReceiveOrderEndTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<MerchantOrder> result = merchantOrderRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("submitTime"))));

		PageResult<MerchantOrderVO> pageResult = new PageResult<>(MerchantOrderVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());

		return pageResult;
	}

	@Transactional(readOnly = true)
	public PageResult<ReceiveOrderRecordVO> findMyReceiveOrderRecordByPage(MyReceiveOrderRecordQueryCondParam param) {
		Specification<MerchantOrder> spec = new Specification<MerchantOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MerchantOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getGatheringChannelCode())) {
					predicates.add(builder.equal(root.get("gatheringChannelCode"), param.getGatheringChannelCode()));
				}
				if (StrUtil.isNotBlank(param.getReceiverUserName())) {
					predicates.add(builder.equal(root.join("receivedAccount", JoinType.INNER).get("userName"),
							param.getReceiverUserName()));
				}
				if (param.getReceiveOrderTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.beginOfDay(param.getReceiveOrderTime())));
					predicates.add(builder.lessThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.endOfDay(param.getReceiveOrderTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<MerchantOrder> result = merchantOrderRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("submitTime"))));
		PageResult<ReceiveOrderRecordVO> pageResult = new PageResult<>(
				ReceiveOrderRecordVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	/**
	 * ????????????
	 * 
	 * @param id
	 */
	@Transactional
	public void cancelOrder(@NotBlank String id) {
		MerchantOrder platformOrder = merchantOrderRepo.getOne(id);
		if (!Constant.??????????????????_????????????.equals(platformOrder.getOrderState())) {
			throw new BizException(BizError.???????????????????????????????????????????????????);
		}
		platformOrder.setOrderState(Constant.??????????????????_????????????);
		platformOrder.setDealTime(new Date());
		merchantOrderRepo.save(platformOrder);
	}

	/**
	 * ??????????????????
	 * 
	 * @param id
	 */
	@Transactional
	public void merchatCancelOrder(@NotBlank String merchantId, @NotBlank String id) {
		MerchantOrder platformOrder = merchantOrderRepo.getOne(id);
		if (!merchantId.equals(platformOrder.getMerchantId())) {
			throw new BizException(BizError.??????????????????);
		}
		if (!Constant.??????????????????_????????????.equals(platformOrder.getOrderState())) {
			throw new BizException(BizError.???????????????????????????????????????????????????);
		}
		platformOrder.setOrderState(Constant.??????????????????_??????????????????);
		platformOrder.setDealTime(new Date());
		merchantOrderRepo.save(platformOrder);
	}

	@Transactional
	public void orderTimeoutDeal() {
		Date now = new Date();
		List<MerchantOrder> orders = merchantOrderRepo.findByOrderStateAndUsefulTimeLessThan(Constant.??????????????????_????????????, now);
		for (MerchantOrder order : orders) {
			order.setDealTime(now);
			order.setOrderState(Constant.??????????????????_????????????);
			merchantOrderRepo.save(order);
		}
	}

	@ParamValid
	@Transactional
	public StartOrderSuccessVO startOrder(StartOrderParam param) {
		Merchant merchant = merchantRepo.findByMerchantNum(param.getMerchantNum());
		if (merchant == null) {
			throw new BizException(BizError.???????????????);
		}
		boolean unknownPayTypeFlag = true;
		List<DictItemVO> payTypes = DictHolder.findDictItem("gatheringChannel");
		for (DictItemVO payType : payTypes) {
			if (payType.getDictItemCode().equals(param.getPayType())) {
				unknownPayTypeFlag = false;
			}
		}
		if (unknownPayTypeFlag) {
			throw new BizException(BizError.????????????????????????);
		}
		String sign = param.getMerchantNum() + param.getOrderNo()
				+ new DecimalFormat("###################.###########").format(param.getAmount()) + param.getNotifyUrl()
				+ merchant.getSecretKey();
		sign = new Digester(DigestAlgorithm.MD5).digestHex(sign);
		if (!sign.equals(param.getSign())) {
			throw new BizException(BizError.???????????????);
		}
		Integer orderEffectiveDuration = Constant.??????????????????????????????;
		ReceiveOrderSetting setting = platformOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting != null) {
			orderEffectiveDuration = setting.getReceiveOrderEffectiveDuration();
		}
		MerchantOrder merchantOrder = param.convertToPo(merchant.getId(), orderEffectiveDuration);
		MerchantOrderPayInfo payInfo = param.convertToPayInfoPo(merchantOrder.getId());
		merchantOrder.setPayInfoId(payInfo.getId());
		merchantOrderRepo.save(merchantOrder);
		merchantOrderPayInfoRepo.save(payInfo);
		return StartOrderSuccessVO.convertFor(merchantOrder.getOrderNo());
	}

	/**
	 * ????????????????????????
	 * 
	 * @param merchantOrderId
	 */
	@Transactional
	public String paySuccessAsynNotice(@NotBlank String merchantOrderId) {
		MerchantOrderPayInfo payInfo = merchantOrderPayInfoRepo.findByMerchantOrderId(merchantOrderId);
		if (Constant.??????????????????????????????_????????????.equals(payInfo.getNoticeState())) {
			log.warn("?????????????????????????????????,??????????????????;????????????id???{}", merchantOrderId);
			return Constant.?????????????????????????????????;
		}
		Merchant merchant = merchantRepo.findByMerchantNum(payInfo.getMerchantNum());//???????????????????????????
		if (merchant == null) {
			throw new BizException(BizError.???????????????);
		}

		String sign = Constant.???????????????????????? + payInfo.getMerchantNum() + payInfo.getOrderNo()
				+ new DecimalFormat("###################.###########").format(payInfo.getAmount())
				+ merchant.getSecretKey();
		sign = new Digester(DigestAlgorithm.MD5).digestHex(sign);//??????+?????????+???????????????+??????+??????
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("merchantNum", payInfo.getMerchantNum());//?????????
		//paramMap.put("orderNo", payInfo.getOrderNo());//???????????????
		//outTradeNo
		paramMap.put("outTradeNo", payInfo.getOrderNo());//???????????????
		paramMap.put("platformOrderNo", payInfo.getMerchantOrder().getOrderNo());
		paramMap.put("amount", payInfo.getAmount());//????????????
		//paramMap.put("attch", payInfo.getAttch());
		paramMap.put("state", Constant.????????????????????????);//????????????
		paramMap.put("payTime",
				DateUtil.format(payInfo.getMerchantOrder().getConfirmTime(), DatePattern.NORM_DATETIME_PATTERN));
		paramMap.put("sign", sign);
		String result = "fail";
		// ??????3???
		for (int i = 0; i < 3; i++) {
			try {
				result = HttpUtil.get(payInfo.getNotifyUrl(), paramMap, 2500);
				if (Constant.?????????????????????????????????.equals(result)) {
					break;
				}
			} catch (Exception e) {
				result = e.getMessage();
				log.error(MessageFormat.format("??????????????????????????????????????????????????????,id???{0}", merchantOrderId), e);
			}
		}
		payInfo.setNoticeState(
				Constant.?????????????????????????????????.equals(result) ? Constant.??????????????????????????????_???????????? : Constant.??????????????????????????????_????????????);
		merchantOrderPayInfoRepo.save(payInfo);
		return result;
	}

	@Transactional(readOnly = true)
	public PageResult<ReceiveOrderRecordVO> findLowerLevelAccountReceiveOrderRecordByPage(
			LowerLevelAccountReceiveOrderQueryCondParam param) {
		UserAccount currentAccount = userAccountRepo.getOne(param.getCurrentAccountId());
		UserAccount lowerLevelAccount = currentAccount;
		if (StrUtil.isNotBlank(param.getUserName())) {
			lowerLevelAccount = userAccountRepo.findByUserName(param.getUserName());
			if (lowerLevelAccount == null) {
				throw new BizException(BizError.??????????????????);
			}
			// ??????????????????????????????????????????????????????????????????
			if (!lowerLevelAccount.getAccountLevelPath().startsWith(currentAccount.getAccountLevelPath())) {
				throw new BizException(BizError.???????????????????????????????????????????????????????????????);
			}
		}
		String lowerLevelAccountId = lowerLevelAccount.getId();
		String lowerLevelAccountLevelPath = lowerLevelAccount.getAccountLevelPath();

		Specification<MerchantOrder> spec = new Specification<MerchantOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MerchantOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getUserName())) {
					predicates.add(builder.equal(root.get("receivedAccountId"), lowerLevelAccountId));
				} else {
					predicates.add(builder.like(root.join("receivedAccount", JoinType.INNER).get("accountLevelPath"),
							lowerLevelAccountLevelPath + "%"));
				}
				if (StrUtil.isNotBlank(param.getOrderState())) {
					predicates.add(builder.equal(root.get("orderState"), param.getOrderState()));
				}
				if (StrUtil.isNotBlank(param.getGatheringChannelCode())) {
					predicates.add(builder.equal(root.get("gatheringChannelCode"), param.getGatheringChannelCode()));
				}
				if (param.getStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.beginOfDay(param.getStartTime())));
				}
				if (param.getEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("receivedTime").as(Date.class),
							DateUtil.endOfDay(param.getEndTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<MerchantOrder> result = merchantOrderRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("submitTime"))));
		PageResult<ReceiveOrderRecordVO> pageResult = new PageResult<>(
				ReceiveOrderRecordVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}
	public String getRemortIP(HttpServletRequest request) {
		if (request.getHeader("x-forwarded-for") == null) {
			return request.getRemoteAddr();
		}
		return request.getHeader("x-forwarded-for");
	}
	public String getRemoteHost(javax.servlet.http.HttpServletRequest request){
		String ip = request.getHeader("x-forwarded-for");
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1")?"127.0.0.1":ip;
	}
}
