package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品ids查询关联的套餐id集合（判断菜品是否被套餐引用）
     * @param dishIds
     * @return
     */
    @Select("<script>" +
            "select setmeal_id from setmeal_dish where dish_id in " +
            "<foreach collection='dishIds' item='dishId' separator=',' open='(' close=')'>" +
            "#{dishId}" +
            "</foreach>" +
            "</script>")
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

}