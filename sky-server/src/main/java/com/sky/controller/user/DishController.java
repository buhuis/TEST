package com.sky.controller.user;

import com.sky.constant.RedisKeyConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.utils.RedisUtil;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 根据分类id查询菜品（带 Redis 缓存）
     *
     * @param categoryId 分类id
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        //构造redis中的key，规则：dish_分类id
        String key = RedisKeyConstant.DISH_KEY_PREFIX + categoryId;

        //查询redis中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisUtil.get(key);
        if (list != null && list.size() > 0) {
            //如果存在，直接返回，无须查询数据库
            log.info("缓存命中，直接返回菜品数据：{}", key);
            return Result.success(list);
        }

        //如果不存在，查询数据库，并手动指定查询起售中的菜品
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);

        list = dishService.listWithFlavor(dish);

        //将查询到的数据放入redis中
        redisUtil.set(key, list, 1, TimeUnit.HOURS);
        log.info("缓存未命中，已查询数据库并回填缓存：{}", key);

        return Result.success(list);
    }

}
