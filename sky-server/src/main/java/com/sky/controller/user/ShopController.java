package com.sky.controller.user;

import com.sky.constant.RedisKeyConstant;
import com.sky.result.Result;
import com.sky.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C端-店铺操作接口
 */
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "C端-店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取营业状态
     *
     * @return 1为营业，0为打烊
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getStatus() {
        //与管理端共用同一个 key，管理端设置后这里能立即读到
        Integer status = (Integer) redisUtil.get(RedisKeyConstant.SHOP_STATUS);
        log.info("获取店铺的营业状态为：{}", status == null ? "未设置（默认打烊）" : (status == 1 ? "营业中" : "打烊中"));
        return Result.success(status == null ? 0 : status);
    }
}
