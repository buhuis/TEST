package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into dish (name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dish dish);

    /**
     * 菜品分页查询（关联分类表查询分类名称）
     * @param dishPageQueryDTO
     * @return
     */
    @Select("<script>" +
            "select d.*, c.name as category_name from dish d " +
            "left join category c on d.category_id = c.id " +
            "<where>" +
            "<if test='name != null and name != \"\"'>" +
            "and d.name like concat('%', #{name}, '%') " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "and d.category_id = #{categoryId} " +
            "</if>" +
            "<if test='status != null'>" +
            "and d.status = #{status} " +
            "</if>" +
            "</where>" +
            "order by d.create_time desc " +
            "</script>")
    List<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据ids批量删除菜品
     * @param ids
     */
    @Delete("<script>" +
            "delete from dish where id in " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteByIds(List<Long> ids);

    /**
     * 根据id动态修改菜品基本属性
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    @Update("<script>" +
            "update dish " +
            "<set>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='categoryId != null'>category_id = #{categoryId},</if>" +
            "<if test='price != null'>price = #{price},</if>" +
            "<if test='image != null'>image = #{image},</if>" +
            "<if test='description != null'>description = #{description},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='updateTime != null'>update_time = #{updateTime},</if>" +
            "<if test='updateUser != null'>update_user = #{updateUser},</if>" +
            "</set>" +
            "where id = #{id}" +
            "</script>")
    void update(Dish dish);

    /**
     * 根据分类id查询菜品（套餐添加菜品页面下拉用）
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId} order by create_time desc")
    List<Dish> list(Long categoryId);

    /**
     * 根据套餐id查询套餐内包含的菜品（套餐起售时校验菜品状态用）
     * @param setmealId
     * @return
     */
    @Select("select d.* from dish d left join setmeal_dish sd on d.id = sd.dish_id where sd.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

}
