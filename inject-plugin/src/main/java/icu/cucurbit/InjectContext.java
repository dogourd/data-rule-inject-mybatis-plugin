package icu.cucurbit;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import icu.cucurbit.rule.AbstractTableFilter;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class InjectContext {

    private static final ThreadLocal<List<AbstractTableFilter>> filterThreadLocal = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<TypeHandlerRegistry> registryThreadLocal = new ThreadLocal<>();

    public static void setFilters(List<AbstractTableFilter> rules) {
        filterThreadLocal.set(rules);
    }

    public static List<AbstractTableFilter> getFilters() {
        return filterThreadLocal.get();
    }

    public static void setConnection(Connection connection) {
    	connectionThreadLocal.set(connection);
	}

	public static Connection getConnection() {
    	return connectionThreadLocal.get();
	}

	public static void setRegistry(TypeHandlerRegistry registry) {
    	registryThreadLocal.set(registry);
	}

	public static TypeHandlerRegistry getRegistry() {
    	return registryThreadLocal.get();
	}



    public static void clear() {
        filterThreadLocal.remove();
    }


}
