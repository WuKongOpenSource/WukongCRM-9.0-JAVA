package com.kakarote.crm9.erp.admin.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.core.paragetter.Para;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.redis.Redis;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户登录
 *
 * @author z
 */
@Clear
public class AdminLoginController extends Controller {

    @Inject
    private AdminRoleService adminRoleService;

    public void index() {
        redirect("/index.html");
    }

    /**
     * @param username 用户名
     * @param password 密码
     * @author zhangzhiwei
     * 用户登录
     */
    public void login(@Para("username") String username, @Para("password") String password) {
        if (StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
            renderJson(R.error("请输入用户名和密码！"));
            return;
        }
        AdminUser user = AdminUser.dao.findFirst(Db.getSql("admin.user.queryByUserName"), username.trim());
        if (user == null) {
            renderJson(R.error("用户名或密码错误！"));
        } else {
            if (user.getStatus() == 0) {
                renderJson(R.error("账户被禁用！"));
            }
            if (BaseUtil.verify(username + password, user.getSalt(), user.getPassword())) {
                if (user.getStatus() == 2) {
                    user.setStatus(1);
                }
                String token = IdUtil.simpleUUID();
                user.setLastLoginIp(BaseUtil.getLoginAddress(getRequest()));
                user.setLastLoginTime(new Date());
                user.update();
                user.setRoles(adminRoleService.queryRoleIdsByUserId(user.getUserId()));
                Redis.use().setex(token, 360000, user);
                user.remove("password","salt");
                setCookie("Admin-Token", token, 360000);
                renderJson(R.ok().put("Admin-Token", token).put("user", user).put("auth", adminRoleService.auth(user.getUserId())));
            } else {
                Log.getLog(getClass()).warn("用户登录失败");
                renderJson(R.error("用户名或密码错误！"));
            }
        }
    }

    /**
     * @author zhangzhiwei
     * 退出登录
     */
    public void logout() {
        String token = BaseUtil.getToken(getRequest());
        if (StrUtil.isEmpty(token)) {
            Redis.use().del(token);
            removeCookie("Admin-Token");
        }
        renderJson(R.ok());
    }

    public void version() {
        renderJson(R.ok().put("name", BaseConstant.NAME).put("version", BaseConstant.VERSION));
    }

    public void ping() {
        List<String> arrays = new ArrayList<>();
        try {
            Connection connection = Db.use().getConfig().getConnection();
            if (connection != null) {
                arrays.add("数据库连接成功");
            }
        } catch (Exception e) {
            arrays.add("数据库连接异常");
        }
        try {
            String ping = Redis.use().ping();
            if ("PONG".equals(ping)) {
                arrays.add("Redis配置成功");
            } else {
                arrays.add("Redis配置失败");
            }
        } catch (Exception e) {
            arrays.add("Redis配置失败");
        }
        renderJson(R.ok().put("data", arrays));
    }
}
