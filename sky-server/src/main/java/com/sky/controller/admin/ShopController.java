package com.sky.controller.admin;

import com.sky.constant.RedisKeyConstant;
import com.sky.result.Result;
import com.sky.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺操作接口
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    public static final String KEY = RedisKeyConstant.SHOP_STATUS;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 设置营业状态
     *
     * @param status 1为营业，0为打烊
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺的营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置店铺的营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        redisUtil.set(KEY, status);
        return Result.success();
    }

    /**
     * 获取营业状态
     *
     * @return 1为营业，0为打烊
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisUtil.get(KEY);
        log.info("获取店铺的营业状态为：{}", status == null ? "未设置（默认打烊）" : (status == 1 ? "营业中" : "打烊中"));
        return Result.success(status == null ? 0 : status);
    }
}
