package icu.cucurbit.inject.dao;

import icu.cucurbit.inject.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserDao {

    @Select("select * from users")
    List<User> findAll();
}
