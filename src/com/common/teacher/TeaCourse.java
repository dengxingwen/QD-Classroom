package com.common.teacher;

import com.jfinal.plugin.activerecord.Model;

public class TeaCourse extends Model<TeaCourse>{
	
	
	private static final long serialVersionUID = 1L;
	public static final TeaCourse teacourse = new TeaCourse();

	
//	/**
//	 * @param account 账号
//	 * @return 检查账号是否存在
//	 */
	public boolean queryAccount(String teaId) {
		String sql = "SELECT * FROM zm_teacher_course WHERE teaId=?;";
		TeaCourse tcourse = TeaCourse.teacourse.findFirst(sql, teaId);
		if (tcourse != null) {
			return true;
		}
		return false;
	}
}
