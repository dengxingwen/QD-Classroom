package com.common.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Model;

public class User extends Model<User> {
	
	/**
	 * User model.

	将表结构放在此，消除记忆负担
	mysql> sdudb user;
	+----------+--------------+------+-----+---------+----------------+
	| Field    | Type         | Null | Key | Default | Extra          |
	+----------+--------------+------+-----+---------+----------------+
	| id       | int(11)      | NO   | PRI | NULL    | auto_increment |
	| account  | varchar(200) | NO   |     | NULL    |                |
	| password | varchar(200) | NO   |     | NULL    |                |
	| username | varchar(200) | NO   |     | NULL    |                |
	| photo    | varchar(200) | NO   |     | NULL    |                |
	| phone    | varchar(200) | NO   |     | NULL    |                |
	| token    | varchar(200) | NO   |     | NULL    |                |
	| signer   | varchar(200) | NO   |     | NULL    |                |
	| sex      | varchar(200) | NO   |     | NULL    |                |
	| game     | varchar(200) | NO   |     | NULL    |                |
	| latitude | double       | NO   |     | NULL    |                |
	| longitude| double       | NO   |     | NULL    |                |
	+----------+--------------+------+-----+---------+----------------+

	数据库字段名建议使用驼峰命名规则，便于与 java 代码保持一致，如字段名： account
	 */
	private static final long serialVersionUID = 1L;
	public static final User me = new User();
	
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
	
	/**
	 * @param account 账号
	 * @return 检查账号是否存在
	 */
	public boolean queryAccount(String stuId) {
		String sql = "SELECT * FROM zm_student WHERE stuId=?;";
		User nowUser = User.me.findFirst(sql, stuId);
		if (nowUser != null) {
			return true;
		}
		return false;
	}

}
