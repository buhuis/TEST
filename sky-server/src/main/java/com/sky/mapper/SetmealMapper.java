package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 插入套餐数据
     * @param setmeal
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into setmeal (category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) " +
            "values (#{categoryId}, #{name}, #{price}, #{status}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Setmeal setmeal);

    /**
     * 套餐分页查询（关联分类表查询分类名称）
     * @param setmealPageQueryDTO
     * @return
     */
    @Select("<script>" +
            "select s.*, c.name as category_name from setmeal s " +
            "left join category c on s.category_id = c.id " +
            "<where>" +
            "<if test='name != null and name != \"\"'>" +
            "and s.name like concat('%', #{name}, '%') " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "and s.category_id = #{categoryId} " +
            "</if>" +
            "<if test='status != null'>" +
            "and s.status = #{status} " +
            "</if>" +
            "</where>" +
            "order by s.create_time desc " +
            "</script>")
    List<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 根据id动态修改套餐
     * @param setmeal
     */
    @AutoFill(value = OperationType.UPDATE)
    @Update("<script>" +
            "update setmeal " +
            "<set>" +
            "<if test='categoryId != null'>category_id = #{categoryId},</if>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='price != null'>price = #{price},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='description != null'>description = #{description},</if>" +
            "<if test='image != null'>image = #{image},</if>" +
            "<if test='updateTime != null'>update_time = #{updateTime},</if>" +
            "<if test='updateUser != null'>update_user = #{updateUser},</if>" +
            "</set>" +
            "where id = #{id}" +
            "</script>")
    void update(Setmeal setmeal);

    /**
     * 根据id删除套餐
     * @param id
     */
    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long id);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    /**
     * 根据状态统计套餐数量（工作台套餐总览用）
     * @param map status
     * @return
     */
    @Select("<script>" +
            "select count(id) from setmeal " +
            "<where>" +
            "<if test='status != null'> and status = #{status} </if>" +
            "</where>" +
            "</script>")
    Integer countByMap(Map map);

}
