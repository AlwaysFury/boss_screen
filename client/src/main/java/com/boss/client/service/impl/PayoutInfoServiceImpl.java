package com.boss.client.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.PayoutInfoDao;
import com.boss.client.dao.ShopDao;
import com.boss.client.service.PayoutInfoService;
import com.boss.client.vo.PayoutInfoVO;
import com.boss.common.enities.PayoutInfo;
import com.boss.common.util.BeanCopyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务
 */
@Service
@Slf4j
public class PayoutInfoServiceImpl extends ServiceImpl<PayoutInfoDao, PayoutInfo> implements PayoutInfoService {

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private PayoutInfoDao payoutInfoDao;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private EscrowInfoServiceImpl escrowInfoService;

    @Override
    public PayoutInfoVO getPayoutInfoBySn(String orderSn) {
        PayoutInfo payoutInfo = payoutInfoDao.selectOne(new QueryWrapper<PayoutInfo>().eq("id", orderSn));
        if (payoutInfo == null) {
            return null;
        }
        PayoutInfoVO payoutInfoVO = BeanCopyUtils.copyObject(payoutInfo, PayoutInfoVO.class);
        payoutInfoVO.setData(payoutInfo.getData() == null || payoutInfo.getData().isEmpty() ? new JSONArray() : JSONArray.parseArray(payoutInfo.getData()));

        return payoutInfoVO;
    }
}
