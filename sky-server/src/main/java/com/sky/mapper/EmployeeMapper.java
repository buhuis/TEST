package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    @Select("<script>" +
            "select * from employee " +
            "<where>" +
            "<if test='name != null and name != \"\"'>" +
            "and name like concat('%', #{name}, '%') " +
            "</if>" +
            "</where>" +
            "order by create_time desc " +
            "</script>")
    List<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    /**
     * 根据主键动态修改员工属性
     * @param employee
     */
    @AutoFill(value = OperationType.UPDATE)
    @Update("<script>" +
            "update employee " +
            "<set>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='username != null'>username = #{username},</if>" +
            "<if test='phone != null'>phone = #{phone},</if>" +
            "<if test='sex != null'>sex = #{sex},</if>" +
            "<if test='idNumber != null'>id_number = #{idNumber},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='updateTime != null'>update_time = #{updateTime},</if>" +
            "<if test='updateUser != null'>update_user = #{updateUser},</if>" +
            "</set>" +
            "where id = #{id}" +
            "</script>")
    void update(Employee employee);

}
