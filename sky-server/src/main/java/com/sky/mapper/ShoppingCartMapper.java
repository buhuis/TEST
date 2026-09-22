package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 批量插入购物车数据（"再来一单"时把订单明细批量转存购物车用）
     *
     * @param shoppingCartList
     */
    @Insert("<script>" +
            "insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) values " +
            "<foreach collection='shoppingCartList' item='sc' separator=','>" +
            "(#{sc.name}, #{sc.image}, #{sc.userId}, #{sc.dishId}, #{sc.setmealId}, #{sc.dishFlavor}, #{sc.number}, #{sc.amount}, #{sc.createTime}) " +
            "</foreach>" +
            "</script>")
    void insertBatch(List<ShoppingCart> shoppingCartList);

    /**
     * 动态条件查询购物车数据
     * 条件：userId、dishId、setmealId、dishFlavor
     * 注意：dishFlavor 只有菜品且选了口味时才有值，套餐为 null 时不参与过滤
     *
     * @param shoppingCart
     * @return
     */
    @Select("<script>" +
            "select * from shopping_cart " +
            "<where>" +
            "<if test='userId != null'> and user_id = #{userId} </if>" +
            "<if test='dishId != null'> and dish_id = #{dishId} </if>" +
            "<if test='setmealId != null'> and setmeal_id = #{setmealId} </if>" +
            "<if test='dishFlavor != null and dishFlavor != \"\"'> and dish_flavor = #{dishFlavor} </if>" +
            "</where>" +
            " order by create_time desc" +
            "</script>")
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 添加购物车数据
     *
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     *
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 根据id删除购物车数据
     *
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据用户id清空购物车
     *
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

}
