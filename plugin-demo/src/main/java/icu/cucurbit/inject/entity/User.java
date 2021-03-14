package icu.cucurbit.inject.entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	private String id;
	private String username;
	private String password;
	private String nickname;
	private Integer status;
	private String createBy;
	private String updateBy;
	private Date createTime;
	private Date updateTime;


	public static void main(String[] args) throws SQLException, ParseException {
		Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/inject", "postgres", "postgres");
		PreparedStatement statement = connection.prepareStatement("where username = ?");

		statement.setString(1, "1' or '1' = '1");
		System.out.println(statement.toString());


	}
}
