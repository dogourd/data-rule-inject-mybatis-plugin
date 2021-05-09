package icu.cucurbit.sql.modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public class BoundSqlModifier {

	private final MetaObject boundSql;
	private final BoundSql delegate;

	BoundSqlModifier(BoundSql boundSql) {
		this.delegate = boundSql;
		this.boundSql = SystemMetaObject.forObject(boundSql);
	}

	public String sql() {
		return delegate.getSql();
	}

	public void withSql(String sql) {
		boundSql.setValue("sql", sql);
	}

	public List<ParameterMapping> parameterMappings() {
		List<ParameterMapping> parameterMappings = delegate.getParameterMappings();
		return new ArrayList<>(parameterMappings);
	}


	public void withParameterMappings(List<ParameterMapping> parameterMappings) {
		boundSql.setValue("parameterMappings", Collections.unmodifiableList(parameterMappings));
	}

	public Object parameterObject() {
		return get("parameterObject");
	}

	public Map<String, Object> additionalParameters() {
		return get("additionalParameters");
	}

	public void addAdditionalParameter(String name, Object value) {
		delegate.setAdditionalParameter(name, value);
	}

	@SuppressWarnings("unchecked")
	private <T> T get(String property) {
		return (T) boundSql.getValue(property);
	}
}
