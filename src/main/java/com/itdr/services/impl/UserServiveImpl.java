package com.itdr.services.impl;

import com.itdr.common.Const;
import com.itdr.common.ServerResponse;
import com.itdr.common.TokenCache;
import com.itdr.mappers.UsersMapper;
import com.itdr.pojo.Users;
import com.itdr.services.UserService;
import com.itdr.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("userService")
public class UserServiveImpl implements UserService {

    @Autowired
    UsersMapper usersMapper;

    //用户登录
    @Override
    public ServerResponse<Users> login(String username, String password) {
        if (username == null || username.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_USER.getDesc());
        }

        if (password == null || password.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_PASSWORD.getDesc());
        }

        //MD5加密
        String md5Password = MD5Utils.getMD5Code(password);
        //根据用户名查找是否存在该用户
        int i = usersMapper.selectByUserNameOrEmail(username, "username");
        if (i <= 0) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_USER.getDesc());
        }

        //根据用户名和密码查找用户
        Users u = usersMapper.selectByUsernameAndPassword(username, md5Password);
        if (u == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.ERROR_PASSWORD.getDesc());
        }


        //封装数据并返回
        ServerResponse sr = ServerResponse.successRS(u);
        return sr;
    }

    //用户注册
    @Override
    public ServerResponse<Users> register(Users u) {
        if (u.getUsername() == null || u.getUsername().equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_INFORMATION.getDesc());
        }
        if (u.getPassword() == null || u.getPassword().equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_INFORMATION.getDesc());
        }
        //检查注册用户名是否存在
        int i2 = usersMapper.selectByUserNameOrEmail(u.getUsername(), "username");
        if (i2 > 0) {
            return ServerResponse.defeatedRS(Const.UsersEnum.REPEAT_UESR.getDesc());
        }

        //MD5加密
        u.setPassword(MD5Utils.getMD5Code(u.getPassword()));

        int i = usersMapper.insert(u);//id为自增长改为null，创建时间和更新时间使用now()函数
        if (i <= 0) {
            return ServerResponse.defeatedRS("用户注册失败");
        }
        return ServerResponse.successRS(200, null, "用户注册成功");
    }

    //检查用户名或者邮箱是否存在
    @Override
    public ServerResponse<Users> checkUserName(String str, String type) {
        if (str == null || str.equals("")) {
            return ServerResponse.defeatedRS("参数不能为空");
        }
        if (type == null || type.equals("")) {
            return ServerResponse.defeatedRS("参数类型不能为空");
        }
        int i = usersMapper.selectByUserNameOrEmail(str, type);
        if (i > 0 && type.equals("username")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.REPEAT_UESR.getDesc());
        }
        if (i > 0 && type.equals("email")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.REPEAT_EMAIL.getDesc());
        }
        return ServerResponse.successRS(200, null, "校验成功");
    }

    //获取当前登录用户的详细信息
    @Override
    public ServerResponse getInforamtion(Users users) {
        Users users1 = usersMapper.selectByPrimaryKey(users.getId());
        if (users1 == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_USER.getDesc());
        }
        users1.setPassword("");
        return ServerResponse.successRS(users1);
    }

    //登录状态更新个人信息
    @Override
    public ServerResponse updateInformation(Users u) {
        int i2 = usersMapper.selectByEmailAndId(u.getEmail(), u.getId());
        if (i2 > 0) {
            return ServerResponse.defeatedRS(Const.UsersEnum.REPEAT_EMAIL.getDesc());
        }
        if (u.getPhone().equals("")) {
            u.setPhone(null);
        }
        if (u.getAnswer().equals("")) {
            u.setAnswer(null);
        }
        if (u.getQuestion().equals("")) {
            u.setQuestion(null);
        }
        int i = usersMapper.updateByPrimaryKeySelective(u);
        if (i <= 0) {
            return ServerResponse.defeatedRS("更新失败");
        }
        return ServerResponse.successRS("更新个人信息成功");
    }

    //忘记密码
    @Override
    public ServerResponse<Users> forgetGetQuestion(String username) {
        if (username == null || username.equals("")) {
            return ServerResponse.defeatedRS("参数不能为空");
        }
        int i = usersMapper.selectByUserNameOrEmail(username, Const.USERNAME);
        if (i <= 0) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_USER.getDesc());
        }
        //根据用户名查找用户密码问题
        String question = usersMapper.selectByUsername(username);
        if (question == null || question.equals("")) {
            return ServerResponse.defeatedRS("该用户未设置找回密码问题");
        }

        return ServerResponse.successRS(question);
    }

    //提交问题答案
    @Override
    public ServerResponse<Users> forgetCheckAnswer(String username, String question, String answer) {
        //参数是否为空
        if (username == null || username.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_USER.getDesc());
        }
        if (question == null || question.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_QUESTION.getDesc());
        }
        if (answer == null || answer.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_ANSWER.getDesc());
        }
        //用户是否存在

        //根据用户名和问题和答案查询数据是否存在
        int i = usersMapper.selectByUserNameAndQuestionAndAnswer(username, question, answer);
        if (i > 0) {
            return ServerResponse.defeatedRS(Const.UsersEnum.ERROR_ANSWER.getDesc());
        }
        //产生随机字符令牌
        String token = UUID.randomUUID().toString();
        //把令牌放入缓存中，这里使用的是Google的guava缓存，后期会使用redis替代
        //可以根据"token_"+username这个键，调用token
        TokenCache.set(Const.TOKEN + username, token);

        return ServerResponse.successRS(token);
    }

    //忘记密码的重设密码
    @Override
    public ServerResponse<Users> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (username == null || username.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_USER.getDesc());
        }
        if (passwordNew == null || passwordNew.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.EMPTY_PASSWORD.getDesc());
        }
        if (forgetToken == null || forgetToken.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.ERROR_TOKEN.getDesc());
        }
        //判断缓存中的token是否为空，是否等于传进来的token
        String token = TokenCache.get(Const.TOKEN + username);
        if (token == null || token.equals("")) {
            return ServerResponse.defeatedRS(Const.UsersEnum.TIMEOUT_TOKEN.getDesc());
        }
        if (!token.equals(forgetToken)) {
            return ServerResponse.defeatedRS(Const.UsersEnum.ERROR_TOKEN.getDesc());
        }
        //MD5加密
        String md5passwordNew = MD5Utils.getMD5Code(passwordNew);
        //根据用户名去更新密码
        int i = usersMapper.updateByUsernameAndPassword(username, md5passwordNew);
        if (i <= 0) {
            return ServerResponse.defeatedRS(Const.UsersEnum.ERROR_CHANGE.getDesc());
        }
        return ServerResponse.successRS("修改密码成功");
    }

    //登录状态中重置密码
    @Override
    public ServerResponse<Users> resetPassword(Users users, String passwordOld, String passwordNew) {
        if (passwordOld == null || passwordOld.equals("")) {
            return ServerResponse.defeatedRS("参数不能为空");
        }
        if (passwordNew == null || passwordNew.equals("")) {
            return ServerResponse.defeatedRS("参数不能为空");
        }
        //MD5加密
        String md5passwordOld = MD5Utils.getMD5Code(passwordOld);
        //根据用户ID查询密码是否正确
        int i = usersMapper.selectByIdAndPassword(users.getId(), md5passwordOld);
        if (i <= 0) {
            return ServerResponse.defeatedRS(Const.UsersEnum.ERROR_PASSWORDOLD.getDesc());
        }
        //MD5加密
        String md5passwordNew = MD5Utils.getMD5Code(passwordNew);
        int i2 = usersMapper.updateByUsernameAndPassword(users.getUsername(), md5passwordNew);
        if (i2 <= 0) {
            return ServerResponse.defeatedRS("修改密码失败");
        }
        return ServerResponse.successRS("修改密码成功");
    }
}
