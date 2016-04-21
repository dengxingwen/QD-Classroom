package com.common.config;

import com.common.index.IndexController;
import com.common.score.Attendance;
import com.common.score.ClassworkScore;
import com.common.score.Score;
import com.common.user.Finalscore;
import com.common.teacher.TeaCourse;
import com.common.teacher.Teacher;
import com.common.teacher.TeacherController;
import com.common.user.User;
import com.common.user.UserController;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;

/**
 * API引导式配置
 */
public class DemoConfig extends JFinalConfig {
	
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		
		// 加载少量必要配置，随后可用getProperty(...)获取值
			loadPropertyFile("a_little_config.txt");
			me.setDevMode(getPropertyToBoolean("devMode", true));
	}
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add("/", IndexController.class, "/index");	// 第三个参数为该Controller的视图存放路径
		me.add("/user", UserController.class);          //学生
		me.add("/teacher", TeacherController.class);    //教师
	}
	
	public static C3p0Plugin createC3p0Plugin() {
		return new C3p0Plugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
	}
	
	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {

		// 配置C3p0数据库连接池插件
		C3p0Plugin c3p0Plugin = new C3p0Plugin(getProperty("jdbcUrl"), getProperty("user"), getProperty("password").trim());
		me.add(c3p0Plugin);
		
		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(c3p0Plugin);
		me.add(arp);
		arp.addMapping("zm_student", User.class);	
		arp.addMapping("zm_teacher", Teacher.class);	
		arp.addMapping("zm_stuscore", Score.class);
		arp.addMapping("zm_student_attendance", Attendance.class);
		arp.addMapping("zm_teacher_course", TeaCourse.class);
		arp.addMapping("zm_student_finalscore", Finalscore.class);
		arp.addMapping("zm_student_classwork", ClassworkScore.class);
	}
	
	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		
	}
	
	/**
	 * 建议使用 JFinal 手册推荐的方式启动项目
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 */
	public static void main(String[] args) {
		JFinal.start("WebRoot", 8085, "/", 5);
	}
}
