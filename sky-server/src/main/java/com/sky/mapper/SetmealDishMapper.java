package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
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

    /**
     * 批量插入套餐菜品关系数据
     * @param setmealDishes
     */
    @Insert("<script>" +
            "insert into setmeal_dish (setmeal_id, dish_id, name, price, copies) values " +
            "<foreach collection='setmealDishes' item='sd' separator=','>" +
            "(#{sd.setmealId}, #{sd.dishId}, #{sd.name}, #{sd.price}, #{sd.copies})" +
            "</foreach>" +
            "</script>")
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐菜品关系数据（修改套餐时先删旧数据）
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

    /**
     * 根据套餐id查询套餐菜品关系数据（编辑回显用）
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

}
