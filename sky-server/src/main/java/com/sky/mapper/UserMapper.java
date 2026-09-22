package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     *
     * @param openid 微信用户唯一标识
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户数据（新用户自动注册）
     *
     * @param user
     */
    @Insert("insert into user (openid, name, phone, sex, id_number, avatar, create_time) " +
            "values (#{openid}, #{name}, #{phone}, #{sex}, #{idNumber}, #{avatar}, #{createTime})")
    void insert(User user);

    /**
     * 根据动态条件统计用户数量（begin/end 按注册时间过滤，新增用户统计用）
     *
     * @param map begin、end
     * @return
     */
    @Select("<script>" +
            "select count(id) from user " +
            "<where>" +
            "<if test='begin != null'> and create_time &gt; #{begin} </if>" +
            "<if test='end != null'> and create_time &lt; #{end} </if>" +
            "</where>" +
            "</script>")
    Integer countByMap(Map map);

}
