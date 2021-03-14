package icu.cucurbit.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

@SuppressWarnings("all")
public class FilterFactory {

	private static final Set<String> COMPARE_FILTER_RELATIONS = Sets.newHashSet(">", ">=", "=", "<=", "<", "!=", "<>");
	private static final Set<String> IN_FILTER_RELATIONS = Sets.newHashSet("IN", "NOT IN");
	private static final Set<String> IS_FILTER_RELATIONS = Sets.newHashSet("IS", "IS NOT");
	private static final Set<String> LIKE_FILTER_RELATIONS = Sets.newHashSet("LIKE", "NOT LIKE");
	private static final Set<String> BETWEEN_FILTER_RELATIONS = Sets.newHashSet("BETWEEN", "NOT BETWEEN");

	public static AbstractTableFilter createFilter(TableRule tableRule) {
		Objects.requireNonNull(tableRule);
		AbstractTableFilter filter = null;

		String relation = Optional.ofNullable(tableRule.getRelation()).map(String::trim).map(String::toUpperCase).orElse("");

		// compare
		if (COMPARE_FILTER_RELATIONS.contains(relation) && tableRule.getTarget() != null) {
			filter = new CompareFilter(tableRule.getField(), relation, tableRule.getTarget());
		}
		// in
		else if (IN_FILTER_RELATIONS.contains(relation) && (tableRule.getTarget() instanceof Collection)) {
			Collection collection = (Collection) tableRule.getTarget();
			if (!collection.isEmpty()) {
				filter = new InFilter(tableRule.getField(), relation, new ArrayList<>(collection));
			}
		}
		// is
		else if (IS_FILTER_RELATIONS.contains(relation) && (tableRule.getTarget() instanceof Collection)) {
			filter = new IsFilter(tableRule.getField(), relation);
		}
		// like
		else if (LIKE_FILTER_RELATIONS.contains(relation) && tableRule.getTarget() != null) {
			filter = new LikeFilter(tableRule.getField(), relation, tableRule.getTarget().toString());
		}
		// between
		else if (BETWEEN_FILTER_RELATIONS.contains(relation) && tableRule.getTarget() instanceof List) {
			List params = (List) tableRule.getTarget();
			if (params != null && params.size() == 2) {
				filter = new BetweenFilter(tableRule.getField(), relation, params.get(0), params.get(1));
			}
		}


		if (filter == null) {
			throw new IllegalArgumentException("table rule not support, " + tableRule);
		}
		filter.setTable(tableRule.getTableName());

		return filter;
	}
}
