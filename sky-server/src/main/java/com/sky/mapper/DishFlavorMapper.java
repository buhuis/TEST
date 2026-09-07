package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     * @param flavors
     */
    @Insert("<script>" +
            "insert into dish_flavor (dish_id, name, value) values " +
            "<foreach collection='flavors' item='flavor' separator=','>" +
            "(#{flavor.dishId}, #{flavor.name}, #{flavor.value})" +
            "</foreach>" +
            "</script>")
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品ids批量删除口味数据
     * @param dishIds
     */
    @Delete("<script>" +
            "delete from dish_flavor where dish_id in " +
            "<foreach collection='dishIds' item='dishId' separator=',' open='(' close=')'>" +
            "#{dishId}" +
            "</foreach>" +
            "</script>")
    void deleteByDishIds(List<Long> dishIds);

}