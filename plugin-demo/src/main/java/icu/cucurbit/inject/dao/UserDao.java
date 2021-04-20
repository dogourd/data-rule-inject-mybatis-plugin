package icu.cucurbit.inject.dao;

import icu.cucurbit.inject.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserDao {

    @Select("select * from users")
    List<User> findAll();

    @Select("select * from users where username = #{username}")
    List<User> findByUser(User user);

    @Select("select * from users where username = #{username} and nickname = #{nickname}")
    List<User> findByUsernamePassword(@Param("username") String username, @Param("nickname") String nickname);

    @Select("select * from users where username = #{user.username} and nickname = #{nickname}")
    List<User> findByUserAndPassword(@Param("user") User user, @Param("nickname") String nickname);

    @Update("update users set username = username")
    int update();
}
