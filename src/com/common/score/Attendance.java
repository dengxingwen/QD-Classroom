package com.common.score;

import com.common.user.User;
import com.jfinal.plugin.activerecord.Model;

public class Attendance extends Model<Attendance>{
	
	private static final long serialVersionUID = 1L;
	public static final Attendance attendance = new Attendance();
	
	
	/**
	 * @param account 账号
	 * @return 检查账号是否存在
	 */
	public boolean queryAccount(String stuId) {
		String sql = "SELECT * FROM zm_student_attendance WHERE stuId=?;";
		Attendance att = Attendance.attendance.findFirst(sql, stuId);
		if (att != null) {
			return true;
		}
		return false;
	}
}
