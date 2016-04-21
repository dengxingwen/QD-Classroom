package com.common.teacher;

import com.jfinal.plugin.activerecord.Model;

public class Teacher extends Model<Teacher>{
	
	
	private static final long serialVersionUID = 1L;
	public static final Teacher tea = new Teacher();
	
//	public List<User> findAddUserList(List<Friend> list) {
//		List<User> users = new ArrayList<User>();
//		for(Friend friend:list) {
//			users.add(User.me.findById(friend.getStr("addId")));
//		}
//		return users;
//	}
//	
//	public List<User> findAddedUserList(List<Friend> list) {
//		List<User> users = new ArrayList<User>();
//		for(Friend friend:list) {
//			users.add(User.me.findById(friend.getStr("addedId")));
//		}
//		return users;
//	} 
	
//	/**
//	 * @param account 账号
//	 * @return 检查账号是否存在
//	 */
	public boolean queryAccount(String teaId) {
		String sql = "SELECT * FROM zm_teacher WHERE teaId=?;";
		Teacher nowteacher = Teacher.tea.findFirst(sql, teaId);
		if (nowteacher != null) {
			return true;
		}
		return false;
	}
}
