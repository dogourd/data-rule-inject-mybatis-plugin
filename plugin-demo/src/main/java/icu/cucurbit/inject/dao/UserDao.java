package icu.cucurbit.inject.dao;

import java.util.List;

import icu.cucurbit.inject.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserDao {

	@Select("select * from users where id = #{id}")
	List<User> findAllById(String id);

	List<User> findAllByIdIn(@Param("ids") Iterable<String> ids);
}
