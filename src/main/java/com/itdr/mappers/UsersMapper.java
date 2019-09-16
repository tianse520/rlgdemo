package com.itdr.mappers;

import com.itdr.pojo.Users;
import org.apache.ibatis.annotations.Param;

public interface UsersMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Users record);

    int insertSelective(Users record);

    Users selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Users record);

    int updateByPrimaryKey(Users record);

    //根据用户名和密码查找用户
    Users selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    //检查用户名或者邮箱是否存在
    int selectByUserNameOrEmail(@Param("str") String str, @Param("type") String type);

    //根据邮箱查找是否存在
    int selectByEmailAndId(@Param("email") String email,@Param("id")Integer id);

    //根据用户名查找用户密码问题
    String selectByUsername(@Param("username") String username);

    //根据用户名和问题和答案查询数据是否存在
    int selectByUserNameAndQuestionAndAnswer(@Param("username") String username, @Param("question")String question, @Param("answer")String answer);

    //根据用户名去更新密码
    int updateByUsernameAndPassword(@Param("username")String username, @Param("passwordNew")String passwordNew);

    //根据用户ID查询密码是否正确
    int selectByIdAndPassword(@Param("id")Integer id, @Param("passwordOld")String passwordOld);
}