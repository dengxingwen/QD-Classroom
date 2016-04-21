package com.common.user;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.common.base.BaseController;
import com.common.base.Constant;
import com.common.score.Attendance;
import com.common.score.ClassworkScore;
import com.common.score.Score;
import com.common.teacher.Teacher;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;

import io.rong.ApiHttpClient;
import io.rong.models.FormatType;
import io.rong.models.SdkHttpResult;
import net.sf.json.JSONObject;

import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;


import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.Table;

import com.jfinal.plugin.activerecord.TableMapping;

public class UserController extends BaseController {

	public void index() {
		System.out.println(getCookie("xys"));
		renderJson("{\"state\":0}");
	}

	// 学生登录
	public void loginS() {
		Map<String, Object> map = new HashMap<String, Object>();

		SdkHttpResult result = null;
		String imgurl = "http://ww2.sinaimg.cn/crop.0.0.1440.1440.1024/a219013ejw8eup91e3jxuj214014076y.jpg";

		String stuId = getPara("stuId");
		String password = getPara("password");
		String sql = "SELECT * FROM zm_student WHERE stuId=? AND password=?;";
		User nowUser = User.me.findFirst(sql, stuId, password);
		if (nowUser == null) {
			renderJson("{\"code\":400}");
			return;
		}
		try {
			result = ApiHttpClient.getToken(Constant.key, Constant.secret, nowUser.getInt("id") + "",
					nowUser.getStr("username"), imgurl, FormatType.json);
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

		renderJson(map);

	}

	// 学生注册 同时添加出勤对应学生信息
	public void registerS() {
		Map<String, Object> map = new HashMap<String, Object>();
		User user = new User();
		user.put("stuId", getPara("stuId"));
		user.put("username", getPara("username"));
		user.put("sex", getPara("sex"));
		user.put("password", getPara("password"));
		user.put("phone", getPara("phone"));
		if (User.me.queryAccount(getPara("stuId"))) {
			map.put("code", 400);
			map.put("error", "账户已经存在");
			renderJson(map);
			return;
		}
		if (user.save()) {
			// 添加出勤表对应数据
			Attendance attendance = new Attendance();
			attendance.put("stuId", getPara("stuId"));
			attendance.put("username", getPara("username"));
			if (Attendance.attendance.queryAccount(getPara("stuId"))) {
				map.put("code", 400);
				map.put("error", "账户已经存在(出勤表)");
				renderJson(map);
				return;
			}
			if (attendance.save()) {

				Finalscore finalscore = new Finalscore();
				finalscore.put("stuId", getPara("stuId"));
				finalscore.put("username", getPara("username"));
				if (Finalscore.finalscore.queryAccount(getPara("stuId"))) {
					map.put("code", 400);
					map.put("error", "账户已经存在(期末成绩表)");
					renderJson(map);
					return;
				}
				if (finalscore.save()) {

					Score scoretable = new Score();
					scoretable.put("stuId", getPara("stuId"));
					scoretable.put("username", getPara("username"));
					if (scoretable.score.queryScore(getPara("stuId"))) {
						map.put("code", 400);
						map.put("error", "账户已经存在(评分成绩表)");
						renderJson(map);
						return;
					}
					if (scoretable.save()) {

						ClassworkScore classworkScore = new ClassworkScore();
						classworkScore.put("stuId", getPara("stuId"));
						classworkScore.put("username", getPara("username"));
						if (classworkScore.classwork.queryAccount(getPara("stuId"))) {
							map.put("code", 400);
							map.put("error", "账户已经存在(课堂成绩表)");
							renderJson(map);
							return;
						}
						if (classworkScore.save()) {

							map.put("code", 200);
							renderJson(map);
							System.out.println("注册成功");
						} else {
							map.put("code", 400);
							map.put("error", "注册失败(classwork) ");
							renderJson(map);
						}

					} else {
						map.put("code", 400);
						map.put("error", "注册失败(starscore) ");
						renderJson(map);
					}
					// map.put("code", 200);
					// renderJson(map);
					// System.out.println("注册成功");
				} else {
					map.put("code", 400);
					map.put("error", "注册失败(期末) ");
					renderJson(map);
				}

			} else {
				map.put("code", 400);
				map.put("error", "注册失败(出勤) ");
				renderJson(map);
			}

		} else {
			map.put("code", 400);
			map.put("error", "注册失败");
			renderJson(map);
		}
	}

	// 学生查询成绩方法
	public void queryscore() {
		Map<String, Object> map = new HashMap<String, Object>();

		String stuId = getPara("stuId");
		String sql = "SELECT * FROM zm_student WHERE stuId=?;";
		User nowUser = User.me.findFirst(sql, stuId);
		if (nowUser == null) {
			renderJson("{\"code\":400}");
			return;
		}
		if (Score.score.queryScore(getPara("stuId"))) {
			String sql2 = "SELECT score from zm_stuscore WHERE stuId=?;";
			Score score = Score.score.findFirst(sql2, stuId);
			map.put("code", 200);
			map.put("score", score.get("score"));
			map.put("stuId", stuId);
			renderJson(map);
			return;
		}
		renderJson("{\"code\":400}");

	}

	public void getnames() {
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> strArray = new ArrayList<String>();
		String sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_student_attendance';";
		java.util.List<User> list = User.me.find(sql);

		User ooUser = list.get(2);
		System.out.println(ooUser);

		for (User obj : list) {

			String ss = obj.get("COLUMN_NAME");
			System.out.println(ss);

			// map.put("stuName", obj.get("COLUMN_NAME"));
			strArray.add((String) obj.get("COLUMN_NAME"));
		}

		map.put("code", 200);
		map.put("namelist", strArray);

		renderJson(map);

		// Table table = TableMapping.me().getTable(User.me.getClass());
		//
		// String s = table.getColumnTypeMap().toString();
		//
		// Map<String, Class<?>> m = table.getColumnTypeMap();
		// //字段数量
		// int count = table.getColumnTypeMap().size();
		// System.out.println(count);
		// for(Map.Entry<String, Class<?>> entry:m.entrySet()){
		// System.out.println(entry.getKey()+"--->"+entry.getValue());
		//
		// }
		//

	}

	// 获取学生所以信息
	public void getStuNames() {
		Map<String, Object> map = new HashMap<String, Object>();

		String sql = "SELECT stuId, username, sex, major, phone, photo, signer, latitude, longitude  FROM zm_student;";
		List<User> stuInfo = User.me.find(sql);
		map.put("code", 200);
		map.put("allStudentInfo", stuInfo);
		renderJson(map);

	}

	// 学生获取自己课堂成绩 课堂表现
	public void getStuClassScore() {
		Map<String, Object> map = new HashMap<String, Object>();
		String stuId = getPara("stuId");

		ArrayList<String> strArray = new ArrayList<String>();
		String sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_stuscore';";
		List<Score> list = Score.score.find(sql);

		for (int i = 5; i < list.size(); i++) {
			Score obj = list.get(i);
			strArray.add((String) obj.get("COLUMN_NAME"));
		}

		String sqlColumnNames = "`";
		for (int i = 0; i < strArray.size(); i++) {
			if (i != strArray.size() - 1) {
				sqlColumnNames += strArray.get(i) + "`, `";
			} else {
				sqlColumnNames += strArray.get(i) + "`";
			}
		}

		sql = "SELECT " + sqlColumnNames + " FROM `zm_stuscore` WHERE stuId = ?;";
		List<Score> scores = Score.score.find(sql, stuId);
		System.out.println(scores);
		if (scores != null) {
			map.put("classStarScore", scores);
			map.put("code", 200);
			renderJson(map);
		} else {
			map.put("code", 400);
			renderJson(map);
		}

	}

	// 学生获取自己期末成绩
	public void getStuFinalScore() {
		Map<String, Object> map = new HashMap<String, Object>();
		String stuId = getPara("stuId");
		String sql = "SELECT finalscore FROM zm_student_finalscore WHERE stuId = ?;";
		Finalscore finalscore = Finalscore.finalscore.findFirst(sql, stuId);
		if (finalscore != null) {
			map.put("code", 200);
			map.put("finalscore", finalscore.get("finalscore"));
			map.put("stuId", stuId);
			renderJson(map);
		} else {
			map.put("code", 400);
			renderJson(map);
		}

	}

	// 学生获取自己课堂成绩
	public void getStuClassworkScore() {
		Map<String, Object> map = new HashMap<String, Object>();
		String stuId = getPara("stuId");

		ArrayList<String> strArray = new ArrayList<String>();
		String sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_student_classwork';";
		List<ClassworkScore> list = ClassworkScore.classwork.find(sql);

		for (int i = 4; i < list.size(); i++) {
			ClassworkScore obj = list.get(i);
			strArray.add((String) obj.get("COLUMN_NAME"));
		}

		String sqlColumnNames = "`";
		for (int i = 0; i < strArray.size(); i++) {
			if (i != strArray.size() - 1) {
				sqlColumnNames += strArray.get(i) + "`, `";
			} else {
				sqlColumnNames += strArray.get(i) + "`";
			}
		}

		sql = "SELECT " + sqlColumnNames + " FROM `zm_student_classwork` WHERE stuId = ?;";
		List<ClassworkScore> classwork = ClassworkScore.classwork.find(sql, stuId);
		System.out.println(classwork);
		if (classwork != null) {
			map.put("classworkScore", classwork);
			map.put("code", 200);
			renderJson(map);
		} else {
			map.put("code", 400);
			renderJson(map);
		}

	}

	// 获取出勤分数
	public void getAttendanceScore() {
		Map<String, Object> map = new HashMap<String, Object>();

		String stuId = getPara("stuId");

		// 获取列名
		ArrayList<String> strArray = new ArrayList<String>();
		String sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_student_attendance';";
		List<Attendance> list = Attendance.attendance.find(sql);

		for (int i = 3; i < list.size(); i++) {
			Attendance obj = list.get(i);
			strArray.add((String) obj.get("COLUMN_NAME"));
		}

		// 列名字符串拼接
		String sqlColumnNames = "`";
		for (int i = 0; i < strArray.size(); i++) {
			if (i != strArray.size() - 1) {
				sqlColumnNames += strArray.get(i) + "`, `";
			} else {
				sqlColumnNames += strArray.get(i) + "`";
			}
		}

		sql = "SELECT " + sqlColumnNames + " FROM `zm_student_attendance` WHERE stuId = ?;";
		List<Attendance> attendances = Attendance.attendance.find(sql, stuId);
		System.out.println(attendances);
		if (attendances != null) {
			map.put("attendanceScore", attendances);
			map.put("code", 200);
			renderJson(map);
		} else {
			map.put("code", 400);
			renderJson(map);
		}

	}

	// 期末汇总
	public void getFinalSummary() {

		float finalScore = 0;
		int classPerScore = 0; // 满分6分
		int attendanceScore = 8; // 出勤总分8分
		int classworkScore = 0; // 满分6分
		float summaryScore = 0;

		Map<String, Object> map = new HashMap<String, Object>();

		String stuId = getPara("stuId");
		// 1.卷面 ----------------------->>>>>
		String sql = "SELECT finalscore FROM zm_student_finalscore WHERE stuId = ?;";
		Finalscore finalscores = Finalscore.finalscore.findFirst(sql, stuId);
		if (finalscores != null) {

			finalScore = finalscores.get("finalscore");
		} else {

		}

		// 2.课堂 ----------------------->>>>>
		sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_stuscore';";
		List<Score> list = Score.score.find(sql);
		int totalClassScore = 0;
		for (int i = 5; i < list.size(); i++) {
			Score obj = list.get(i);

			sql = "SELECT `" + obj.get("COLUMN_NAME") + "` FROM `zm_stuscore` WHERE stuId = ?;";
			Score scores = Score.score.findFirst(sql, stuId);
			if (scores != null) {
				totalClassScore += Integer.parseInt((String) scores.get((String) obj.get("COLUMN_NAME")));
			} else {
				totalClassScore = 0;
			}

		}

		classPerScore = 100 * totalClassScore / ((list.size() - 5) * 5);

		System.out.println(classPerScore);

		// 3.出勤 ----------------------->>>>>
		// 获取列名
		sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_student_attendance';";
		List<Attendance> list2 = Attendance.attendance.find(sql);

		for (int i = 3; i < list2.size(); i++) {
			Attendance obj = list2.get(i);

			sql = "SELECT `" + obj.get("COLUMN_NAME") + "` FROM `zm_student_attendance` WHERE stuId = ?;";
			Attendance attendances = Attendance.attendance.findFirst(sql, stuId);
			if (attendances != null) {
				int attCode = attendances.get((String) obj.get("COLUMN_NAME"));

				// 1：迟到 扣0.5分 2:旷课 扣1分
				if (attCode == 1) {
					attendanceScore -= 0.5;
				} else if (attCode == 2) {
					attendanceScore -= 1;
				}
			}

		}

		if (attendanceScore <= 0) {
			attendanceScore = 0;
		} else {
			attendanceScore = 100 * attendanceScore / 8;
		}

		System.out.println(attendanceScore);

		// 4.作业 ----------------------->>>>>
		sql = "SELECT `COLUMN_NAME` FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE `TABLE_SCHEMA`='score_manage' AND `TABLE_NAME`='zm_student_classwork';";
		List<ClassworkScore> list3 = ClassworkScore.classwork.find(sql);

		int classworkAllScore = 0;
		for (int i = 4; i < list3.size(); i++) {
			ClassworkScore obj = list3.get(i);

			sql = "SELECT `" + obj.get("COLUMN_NAME") + "` FROM `zm_student_classwork` WHERE stuId = ?;";
			ClassworkScore classwork = ClassworkScore.classwork.findFirst(sql, stuId);
			if (classwork != null) {
				int everclassworkScore = Integer.parseInt((String) classwork.get((String) obj.get("COLUMN_NAME")));
				classworkAllScore += everclassworkScore;
			} else {
				classworkAllScore = 0;
			}

		}

		classworkScore = 100 * classworkAllScore / ((list3.size() - 4) * 10);
		System.out.println(classworkScore);
		// 总分 ----------------------->>>>>
		summaryScore = (float) (finalScore * 0.8
				+ (classPerScore * 6 / 20 + attendanceScore * 8 / 20 + classworkScore * 6 / 20) * 0.2);

		if (summaryScore >= 0 || summaryScore <= 100) {
			map.put("code", 200);
			map.put("summaryScore", summaryScore);// 总评
			map.put("finalScore", finalScore); // 卷面
			map.put("classPerScore", classPerScore);// 课堂表现
			map.put("attendanceScore", attendanceScore);// 出勤
			map.put("classworkScore", classworkScore);// 作业
			renderJson(map);
		} else {
			map.put("code", 400);
			renderJson(map);
		}

	}

}
