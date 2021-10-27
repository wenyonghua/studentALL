package me.zohar.runscore.rechargewithdraw.repo;

import java.util.List;

import me.zohar.runscore.merchant.domain.MerchantOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import me.zohar.runscore.rechargewithdraw.domain.PayType;

public interface PayTypeRepo extends JpaRepository<PayType, String>, JpaSpecificationExecutor<PayType> {

	List<PayType> findByEnabledIsTrueOrderByOrderNoAsc();



	PayType findByAndMerchantNum(String merchantNum);//根据商户号获取支付类型

}
