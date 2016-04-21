package com.common.score;

import com.jfinal.plugin.activerecord.Model;

public class ClassworkScore extends Model<ClassworkScore>{
	private static final long serialVersionUID = 1L;
	public static final ClassworkScore classwork = new ClassworkScore();
	
	
	/**
	 * @param account 账号
	 * @return 检查账号是否存在
	 */
	public boolean queryAccount(String stuId) {
		String sql = "SELECT * FROM zm_student_classwork WHERE stuId=?;";
		ClassworkScore classwork = ClassworkScore.classwork.findFirst(sql, stuId);
		if (classwork != null) {
			return true;
		} 
		return false;
	}
}
