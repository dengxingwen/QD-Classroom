package com.common.teacher;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.synth.SynthSeparatorUI;

import org.apache.log4j.or.RendererMap;

import com.common.base.BaseController;
import com.common.base.Constant;
import com.common.score.Attendance;
import com.common.score.ClassworkScore;
import com.common.score.Score;
import com.common.user.User;
import com.google.gson.JsonArray;
import com.jfinal.plugin.activerecord.Db;

import io.rong.ApiHttpClient;
import io.rong.models.FormatType;
import io.rong.models.SdkHttpResult;
import net.sf.cglib.core.NamingPolicy;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TeacherController extends BaseController {
	public void index() {
		System.out.println(getCookie("xys"));
		renderJson("{\"state\":0}");
	}

	// 教师登录
	public void loginT() {
		Map<String, Object> map = new HashMap<String, Object>();

		SdkHttpResult result = null;
		String imgurl = "http://ww2.sinaimg.cn/crop.0.0.1440.1440.1024/a219013ejw8eup91e3jxuj214014076y.jpg";

		String teaId = getPara("teaId");
		String password = getPara("password");
		String sql = "SELECT * FROM zm_teacher WHERE teaId=? AND password=?;";
		Teacher nowteacher = Teacher.tea.findFirst(sql, teaId, password);
		if (nowteacher == null) {
			renderJson("{\"code\":400}");
			return;
		}
		try {
			result = ApiHttpClient.getToken(Constant.key, Constant.secret, nowteacher.getInt("id") + "",
					nowteacher.getStr("username"), imgurl, FormatType.json);

			System.out.println("gettoken=" + result);
		} catch (Exception e) {
			e.printStackTrace();
			renderJson("{\"code\":400}");
			return;
		}

		JSONObject jsonObject = JSONObject.fromObject(result.getResult().toString());

		// 获取同学信息列表
		String sql2 = "SELECT id, stuId, username, sex, major, phone, photo, signer, latitude, longitude  FROM zm_student;";
		List<User> stuInfo = User.me.find(sql2);
		map.put("allStudentInfo", stuInfo);
		map.put("tokenResult", jsonObject); // 里面包含code状态值
		map.put("code", jsonObject.get("code"));

		// 教师信息
		String sql3 = "SELECT id, teaId, username, sex, phone, photo, signer, latitude, longitude  FROM zm_teacher;";
		List<Teacher> allTeacherInfo = Teacher.tea.find(sql3);
		map.put("allTeacherInfo", allTeacherInfo);

		renderJson(map);
	}

	// 教师注册
	public void registerT() {
		Map<String, Object> map = new HashMap<String, Object>();
		Teacher teacher = new Teacher();
		teacher.put("teaId", getPara("teaId"));
		teacher.put("username", getPara("username"));
		teacher.put("password", getPara("password"));
		teacher.put("sex", getPara("sex"));
		teacher.put("phone", getPara("phone"));
		if (Teacher.tea.queryAccount(getPara("teaId"))) {
			map.put("code", 400);
			map.put("error", "账户已经存在");
			renderJson(map);
			return;
		}
		if (teacher.save()) {
			// map.put("code", 200);
			// renderJson(map);

			// 添加出勤表对应数据
			TeaCourse teacourse = new TeaCourse();
			teacourse.put("teaId", getPara("teaId"));
			teacourse.put("username", getPara("username"));
			if (TeaCourse.teacourse.queryAccount(getPara("teaId"))) {
				map.put("code", 400);
				map.put("error", "账户已经存在(教师课程表)");
				renderJson(map);
				return;
			}
			if (teacourse.save()) {
				map.put("code", 200);
				renderJson(map);
			} else {
				map.put("code", 400);
				map.put("error", "注册失败(教师)");
				renderJson(map);
			}
		} else {
			map.put("code", 400);
			map.put("error", "注册失败");
			renderJson(map);
		}
	}

	// 录入成绩
	public void addscore() {
		Map<String, Object> map = new HashMap<String, Object>();
		Score sco = new Score();
		sco.put("stuId", getPara("stuId"));
		sco.put("username", getPara("username"));
		sco.put("score", getPara("score"));
		if (Score.score.queryScore(getPara("stuId"))) {
			map.put("code", 400);
			map.put("error", "已经存在成绩");
			renderJson(map);
			return;
		}
		if (sco.save()) {
			map.put("code", 200);
			renderJson(map);
		} else {
			map.put("code", 400);
			map.put("error", "添加成绩失败");
			renderJson(map);
		}

	}

	// //获取学生列表
	// public void getstudentsList() {
	// Map<String, Object> map = new HashMap<String, Object>();
	// String sql = "select stuId from zm_student_attendance;";
	// java.util.List<Attendance> list = Attendance.attendance.find(sql);
	//
	// for(Attendance obj : list) {
	//
	// System.out.println(obj);
	// String name = obj.get("stuId");
	// System.out.println(name);
	// }
	//
	// }

	// 获取学生姓名列表
	public void studentNameList() {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> strArray = new ArrayList<String>();
		String sql = "select username from zm_student_attendance;";
		List<Attendance> list = Attendance.attendance.find(sql); // java.util.

		for (Attendance obj : list) {

			System.out.println(obj);

			strArray.add((String) obj.get("username"));
		}

		map.put("code", 200);
		map.put("namelist", strArray);

		renderJson(map);

	}

	// 提交学生出勤记录
	public void submitAttendence() {
		Map<String, Object> map = new HashMap<String, Object>();

		// 迟到
		Object object = new Object();
		object = getPara("lateNameList");
		System.out.println(object);
		// 缺席
		Object objectabsences = new Object();
		objectabsences = getPara("absencesArray");
		System.out.println(objectabsences);

		// 解析数组
		// 迟到
		JSONArray jsonArray = JSONArray.fromObject(getPara("lateNameList"));
		String namelate[] = new String[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			namelate[i] = jsonArray.getString(i);
			System.out.println(jsonArray.get(i));
		}
		// 缺席
		JSONArray jsonArrayabsences = JSONArray.fromObject(getPara("absencesArray"));
		String nameabsences[] = new String[jsonArrayabsences.size()];
		for (int i = 0; i < jsonArrayabsences.size(); i++) {
			nameabsences[i] = jsonArrayabsences.getString(i);
			System.out.println(jsonArrayabsences.get(i));
		}

		// 服务器获取数据成功

		// 操作数据库 获取表结构 列名
		ArrayList<String> strArray = new ArrayList<String>();
		String sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_student_attendance';";
		List<Attendance> list = Attendance.attendance.find(sql); // java.util.

		for (Attendance obj : list) {

			String ss = obj.get("COLUMN_NAME");
			System.out.println(ss);

			// map.put("stuName", obj.get("COLUMN_NAME"));
			strArray.add((String) obj.get("COLUMN_NAME"));
		}

		String colname = "lesson" + (strArray.size() - 2);
		System.out.println(colname);
		sql = "ALTER TABLE zm_student_attendance ADD COLUMN " + colname + " int(10) DEFAULT 0;";

		Db.update(sql);
		// 增加列成功

		// 把点名数据添加到数据库
		// for(int i=0;i<strArray.size();i++){
		// attendance.set("lesson2", "1");
		// attendance.set("username", strArray.get(i));
		// attendance.update();
		// }

		for (int i = 0; i < namelate.length; i++) {

			sql = "UPDATE `zm_student_attendance` SET " + colname + " = 1 WHERE `username`= '" + namelate[i] + "';";
			System.out.println(sql);
			Db.update(sql);
		}

		for (int i = 0; i < nameabsences.length; i++) {

			sql = "UPDATE `zm_student_attendance` SET " + colname + " = 2 WHERE `username`= '" + nameabsences[i] + "';";
			System.out.println(sql);
			Db.update(sql);
		}

		map.put("code", 200);
		renderJson(map);
	}

	/*
	 * 原生executeUpdate方法 Connection conn = null; Statement stm =null; try {
	 * conn=DbKit.getConfig(sourceDb).getConnection(); stm =
	 * conn.createStatement(); stm.executeUpdate(sql); }catch(Exception e){
	 * ............... }
	 */
	public void settingCourse() {
		Map<String, Object> map = new HashMap<String, Object>();
		// 解析数组
		JSONObject jsonObject = JSONObject.fromObject(getPara("teacherSetting"));
		jsonObject.get("College");
		System.out.println(jsonObject.get("College"));

		String teaId = getPara("teaId");

		String sql = "UPDATE zm_teacher_course SET College = '" + jsonObject.get("College") + "', Major = '"
				+ jsonObject.get("Major") + "', Class = '" + jsonObject.get("Class") + "', Course = '"
				+ jsonObject.get("Course") + "' WHERE teaId = '" + teaId + "';";
		System.out.println(sql);
		Db.update(sql);

		map.put("code", 200);
		renderJson(map);
	}

	// 获取学生所以信息
	public void getStuNames() {
		Map<String, Object> map = new HashMap<String, Object>();

		String sql = "SELECT stuId, username, sex, major, phone, photo, signer, latitude, longitude  FROM zm_student;";
		List<User> stuInfo = User.me.find(sql);
		if (stuInfo == null) {
			map.put("code", 400);
			renderJson(map);
			return;
		}
		map.put("code", 200);
		map.put("allStudentInfo", stuInfo);
		renderJson(map);
	}

	// 接收学生期末成绩
	public void saveStuFinalScore() {
		Map<String, Object> map = new HashMap<String, Object>();

		String stuFinalScore = getPara("stufinalscore");
		HashMap<String, Float> stuMap = new HashMap<String, Float>();
		JSONObject jsonObject = JSONObject.fromObject(stuFinalScore);
		Iterator it = jsonObject.keys();
		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			String valueStr = (String) jsonObject.get(key);
			Float value = Float.parseFloat(valueStr);
			stuMap.put(key, value);

			// 成绩保存数据库
			String sql = "UPDATE `zm_student_finalscore` SET finalscore = " + value + " WHERE `stuId`= '" + key + "';";
			System.out.println(sql);
			int result = Db.update(sql);
			System.out.println(result);
		}

		map.put("code", 200);
		renderJson(map);
	}

	// 接收学生课堂成绩
	public void saveStuClassScore() {
		Map<String, Object> map = new HashMap<String, Object>();

		String stuFinalScore = getPara("stufinalscore");
		HashMap<String, Float> stuMap = new HashMap<String, Float>();
		JSONObject jsonObject = JSONObject.fromObject(stuFinalScore);
		Iterator it = jsonObject.keys();
		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			String valueStr = (String) jsonObject.get(key);
			Float value = Float.parseFloat(valueStr);
			stuMap.put(key, value);

			// 成绩保存数据库
			String sql = "UPDATE `zm_student_finalscore` SET finalscore = " + value + " WHERE `stuId`= '" + key + "';";
			System.out.println(sql);
			// int result = Db.update(sql);
			// System.out.println(result);
		}

		map.put("code", 200);
		renderJson(map);
	}

	// 保存随机点名五星评分
	public void saveStarScore() {
		Map<String, Object> map = new HashMap<String, Object>();

		String stuId = getPara("stuId");
		String starScore = getPara("starScore");
		int starscore = Integer.parseInt(starScore);

		System.out.println(stuId);
		System.out.println(starScore);

		// 先判断是否存在
		String sqlscore = "select * from zm_stuscore where `stuId`= '" + stuId + "';";
		Score score = Score.score.findFirst(sqlscore);
		if (score == null) {

			map.put("code", 400);
			map.put("result", "对象不存在");
			renderJson(map);
			return;
		}
		//
		// int scoreSum = scorenum + starscore;
		//
		// String sql = "UPDATE `zm_stuscore` SET starscore = " + scoreSum + "
		// WHERE `stuId`= '" + stuId + "';";
		// System.out.println(sql);
		// int result = Db.update(sql);
		// System.out.println(result);

		// 操作数据库 获取表结构 列名
		String sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_stuscore';";
		List<Score> list = Score.score.find(sql); // java.util.

		// 判断列名时间
		String lastColumnName = "";
		String sysDatetime = "";

		// 生成当前日期 供比较
		Calendar rightNow = Calendar.getInstance();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");// 格式大小写有区别
		sysDatetime = fmt.format(rightNow.getTime());
		System.out.println(sysDatetime);

		// 获取列名
		lastColumnName = list.get(list.size() - 1).get("COLUMN_NAME");

		// 如果相等 则更新当前列 否则增加新列
		if (lastColumnName.equals(sysDatetime)) {
			// 更新

		} else {
			// 增加列
			sql = "ALTER TABLE `zm_stuscore` ADD COLUMN `" + sysDatetime + "` VARCHAR(200) DEFAULT '0';";
			System.out.println(sql);
			Db.update(sql);

			// 填充数据

		}

		String getscore = "select `" + sysDatetime + "` from zm_stuscore where `stuId`= '" + stuId + "';";
		Score score2 = Score.score.findFirst(getscore);
		String getscoreValue = "";
		int totalscore = 0;
		if (score2 != null) {
			getscoreValue = score2.get(sysDatetime);
			totalscore = starscore + Integer.parseInt(getscoreValue);
		} else {
			totalscore = starscore;
		}

		// 成绩保存数据库
		sql = "UPDATE `zm_stuscore` SET `" + sysDatetime + "` = " + totalscore + " WHERE `stuId`= '" + stuId + "';";
		System.out.println(sql);
		int result = Db.update(sql);
		System.out.println(result);

		map.put("code", 200);
		renderJson(map);
	}

	// 获取评分
	public void getStarScore() {
		Map<String, Object> map = new HashMap<String, Object>();

		String sql = "SELECT id, stuId, username, starscore FROM zm_stuscore;";
		List<Score> ScoreTable = Score.score.find(sql);

		map.put("starScore", ScoreTable);
		map.put("code", 200);
		renderJson(map);
	}

	// 保存作业成绩
	public void savaClassworkScore() {
		Map<String, Object> map = new HashMap<String, Object>();

		// 接收数据
		String classworkScore = getPara("classworkScore");
		JSONObject jsonObject = JSONObject.fromObject(classworkScore);
		Iterator it = jsonObject.keys();

		// 操作数据库 获取表结构 列名
		String sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_student_classwork';";
		List<ClassworkScore> list = ClassworkScore.classwork.find(sql); // java.util.

		// 判断列名时间
		String lastColumnName = "";
		String sysDatetime = "";

		// 生成当前日期 供比较
		Calendar rightNow = Calendar.getInstance();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");// 格式大小写有区别
		sysDatetime = fmt.format(rightNow.getTime());
		System.out.println(sysDatetime);

		// 获取列名
		lastColumnName = list.get(list.size() - 1).get("COLUMN_NAME");

		// 如果相等 则更新当前列 否则增加新列
		if (lastColumnName.equals(sysDatetime)) {
			// 更新

		} else {
			// 增加列
			sql = "ALTER TABLE `zm_student_classwork` ADD COLUMN `" + sysDatetime + "` VARCHAR(200) DEFAULT '0';";
			System.out.println(sql);
			Db.update(sql);

			// 填充数据

		}

		while (it.hasNext()) {
			String key = String.valueOf(it.next());
			String valueStr = (String) jsonObject.get(key);
			int value = Integer.parseInt(valueStr);

			// 成绩保存数据库
			sql = "UPDATE `zm_student_classwork` SET `" + sysDatetime + "` = " + valueStr + " WHERE `stuId`= '" + key
					+ "';";
			System.out.println(sql);
			int result = Db.update(sql);
			System.out.println(result);
		}

		map.put("code", 200);
		renderJson(map);

	}
}
