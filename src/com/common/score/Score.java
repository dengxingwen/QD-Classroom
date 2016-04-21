package com.common.score;

import com.jfinal.plugin.activerecord.Model;

public class Score extends Model<Score>{
	

	private static final long serialVersionUID = 1L;
	public static final Score score = new Score();
	

	
//	/**
//	 * @param account 账号
//	 * @return 检查账号是否存在
//	 */
	public boolean queryScore(String stuId) {
		String sql = "SELECT * FROM zm_stuscore WHERE stuId=?;";
		Score sco = Score.score.findFirst(sql, stuId);
		if (sco != null) {
			return true;
		}
		return false;
	}

}
