package com.taipeigo.activity.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;

@Repository
public class ActivityJDBCDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ActivityJDBCDAO(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;

    }

    @SuppressWarnings("null")
    public List<ActivityVO> getSearch(MultiValueMap<String, String> map, boolean isFrontend) {

        // String[]複合查詢核心之一，一個key有多個vaule，尤其多個checkbox

        // join 一日活動明細以及票券
        StringBuilder sql = new StringBuilder(

                "SELECT DISTINCT a.* FROM ACTIVITY a " +
                        "JOIN ACTIVITY_DETAIL ad ON a.ACTIVITY_ID = ad.ACTIVITY_ID " +
                        "JOIN TICKET t ON ad.TICKET_ID = t.TICKET_ID " +
                        "WHERE 1=1 ");

        List<Object> args = new ArrayList<>();

        if (isFrontend) {
            sql.append(" AND a.ACTIVITY_STATUS = 1 ");
        }

        // 給前端 input text 使用(模糊查詢)
        if (map.containsKey("keyword") && map.get("keyword").get(0).trim().length() > 0) {

            sql.append(" AND (a.ACTIVITY_NAME LIKE ? OR t.TICKET_NAME LIKE ?) ");

            String keyword = "%" + map.get("keyword").get(0).trim() + "%";

            args.add(keyword);
            args.add(keyword);

        }

        // 給前端 price filter 使用(範圍查詢)
        boolean hasMinPrice = map.containsKey("minPrice") && map.get("minPrice").get(0).trim().length() > 0;
        boolean hasMaxPrice = map.containsKey("maxPrice") && map.get("maxPrice").get(0).trim().length() > 0;

        if (hasMinPrice || hasMaxPrice) {

            String realPriceSql = "((SELECT SUM(t2.ADULT_PRICE) FROM ACTIVITY_DETAIL ad2 JOIN " +
                    "TICKET t2 ON ad2.TICKET_ID = t2.TICKET_ID WHERE ad2.ACTIVITY_ID = a.ACTIVITY_ID) - a.DISCOUNT)";

            if (hasMinPrice) {
                sql.append(" AND " + realPriceSql + " >= ? ");
                args.add(Integer.valueOf(map.get("minPrice").get(0).trim()));
            }

            if (hasMaxPrice) {

                sql.append(" AND " + realPriceSql + " <= ? ");
                args.add(Integer.valueOf(map.get("maxPrice").get(0).trim()));

            }

        }

        // 類別標籤查詢
        if (map.containsKey("cateId") && !map.get("cateId").isEmpty()) {

            List<String> cateId = map.get("cateId");

            sql.append(" AND a.ACTIVITY_ID IN (SELECT ACTIVITY_ID FROM ACTIVITY_CATE_INFO " +
                    "WHERE ACTIVITY_CATE_ID IN (");

            String questionMark = String.join(", ", Collections.nCopies(cateId.size(), "?"));

            sql.append(questionMark);

            sql.append("))");

            for (String id : cateId) {
                args.add(Integer.valueOf(id));
            }

        }

        // 萬用查詢用ID去查

        if (map.containsKey("activityId") && !map.get("activityId").get(0).trim().isEmpty()) {

            sql.append(" AND a.ACTIVITY_ID = ? ");

            args.add(Integer.valueOf(map.get("activityId").get(0).trim()));
        }

        sql.append(" ORDER BY a.ACTIVITY_ID DESC ");

        BeanPropertyRowMapper<ActivityVO> bpr = new BeanPropertyRowMapper<ActivityVO>(ActivityVO.class);

        return jdbcTemplate.query(sql.toString(), bpr, args.toArray());
    }

}
