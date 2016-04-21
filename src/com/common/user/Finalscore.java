package com.common.user;

import com.jfinal.plugin.activerecord.Model;

public class Finalscore extends Model<Finalscore>{
	private static final long serialVersionUID = 1L;
	public static final Finalscore finalscore = new Finalscore();
	
	public boolean queryAccount(String stuId) {
		String sql = "SELECT * FROM zm_student_finalscore WHERE stuId=?;";
		Finalscore finalscores = Finalscore.finalscore.findFirst(sql, stuId);
		if (finalscores != null) {
			return true;
		}
		return false;
	}
}
