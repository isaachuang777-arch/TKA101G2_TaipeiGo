package com.taipeigo.activity.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

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

        // 避免JOIN來JOIN去浪費效能，這邊還是乖乖寫個判斷式

        // 確保前端的keyword進來不是空的
        boolean needJoin = map.containsKey("keyword") && map.get("keyword").get(0).trim().length() > 0;

        StringBuilder sql = new StringBuilder("SELECT DISTINCT a.* FROM ACTIVITY a ");

        // 如果前端 keyword 有傳進來，再用判斷式去join

        if(needJoin) {

            sql.append("JOIN ACTIVITY_DETAIL ad ON a.ACTIVITY_ID = ad.ACTIVITY_ID ")
               .append("JOIN TICKET t ON ad.TICKET_ID = t.TICKET_ID ")
               .append("LEFT JOIN ACTIVITY_CATE_INFO aci ON a.ACTIVITY_ID = aci.ACTIVITY_ID")
               .append("LEFT JOIN ACTIVITY_CATE ac ON aci.ACTIVITY_CATE_ID = ac.ACTIVITY_CATE_ID ");
        }

        sql.append("WHERE 1=1 ");


        List<Object> args = new ArrayList<>();

        if (isFrontend) {
            sql.append(" AND a.ACTIVITY_STATUS = 1 ");
        }

        // 給前端 input text 使用(模糊查詢)
        if (map.containsKey("keyword") && map.get("keyword").get(0).trim().length() > 0) {

                sql.append(" AND (a.ACTIVITY_NAME LIKE ? " +
                           "OR a.ACTIVITY_DESC LIKE ? " +
                           "OR t.TICKET_NAME LIKE ? " +
                           "OR ac.CATE_NAME LIKE ?) ");

            String keyword = "%" + map.get("keyword").get(0).trim() + "%";

            args.add(keyword);
            args.add(keyword);
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
       if (map.containsKey("cateId") && !map.get("cateId").get(0).trim().isEmpty()) {

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

        // 首頁推薦活動查詢
        if (map.containsKey("isRecommended") && !map.get("isRecommended").get(0).trim().isEmpty()) {
            sql.append(" AND a.IS_RECOMMENDED = ? ");
            args.add(Integer.valueOf(map.get("isRecommended").get(0).trim()));
        }

        sql.append(" ORDER BY a.ACTIVITY_ID DESC ");

        int pageSize = map.containsKey("pageSize") ? Integer.parseInt(map.get("pageSize").get(0)) : 5;

        int currentPage = map.containsKey("page") ? Integer.parseInt(map.get("page").get(0)) : 1;
        int offset = (currentPage -1 ) * pageSize;

        sql.append(" LIMIT ? OFFSET ? ");

        args.add(pageSize);
        args.add(offset);
        

        // 先把搜尋節果活動暫存在一個 list 裡面
        BeanPropertyRowMapper<ActivityVO> bpr = new BeanPropertyRowMapper<ActivityVO>(ActivityVO.class);

        List<ActivityVO> list = jdbcTemplate.query(sql.toString(), bpr, args.toArray());

        //一日活動如果沒東西直接return這個查詢結果
        if(list.isEmpty()){
            return list;
        }

        Map<Integer, ActivityVO> activityMap = new HashMap<>();
        List<Integer> activityId = new ArrayList<>();


        for(ActivityVO actVO : list){
            activityMap.put(actVO.getActivityId(), actVO);
            activityId.add(actVO.getActivityId());
            actVO.setActivityImage(new ArrayList<>());
        }

        // 產生跟activityId一樣多的"?"
        String joinSql = String.join(",", Collections.nCopies(activityId.size(), "?"));

        // 一次把這頁所有活動的圖片拿回來
        String imgSql = "SELECT ACTIVITY_ID, ACTIVITY_IMAGE_SRC FROM ACTIVITY_IMAGE WHERE ACTIVITY_ID IN (" + joinSql + ")";

        jdbcTemplate.query(imgSql, rs -> {
            Integer actId = rs.getInt("ACTIVITY_ID");
            String src = rs.getString("ACTIVITY_IMAGE_SRC");

            ActivityImageVO imgVO = new ActivityImageVO();
            imgVO.setActivityImageSrc(src);

            // 用map找到對應的 activityVO 會比較快，在把圖片路徑塞進去

            activityMap.get(actId).getActivityImage().add(imgVO);
            
        }, activityId.toArray());


        String priceSql = "SELECT ad.ACTIVITY_ID, " +
                          "COALESCE(SUM(t.ADULT_PRICE),0) AS ADULT_TOTAL, " +
                          "COALESCE(SUM(t.ADULT_ORIGINAL_PRICE),0) AS ADULT_ORIGINAL_TOTAL, " +
                          "COALESCE(SUM(t.CHILD_PRICE), 0) AS CHILD_TOTAL, " +
                          "COALESCE(SUM(t.CONCESSION_PRICE), 0) AS CONCESSION_TOTAL " +
                          "FROM ACTIVITY_DETAIL ad " +
                          "JOIN TICKET t ON ad.TICKET_ID = t.TICKET_ID " +
                          "WHERE ad.ACTIVITY_ID IN (" + joinSql + ") " +
                          "GROUP BY ad.ACTIVITY_ID";

        jdbcTemplate.query(priceSql, rs -> {
            Integer actId = rs.getInt("ACTIVITY_ID");
            ActivityVO act = activityMap.get(actId);
            
            int discount = act.getDiscount() != null ? act.getDiscount() : 0;
            int adultFinal = rs.getInt("ADULT_TOTAL") - discount;
            int childFinal = rs.getInt("CHILD_TOTAL") - discount;
            int concessionFinal = rs.getInt("CONCESSION_TOTAL") - discount;

            act.setAdultPrice(adultFinal <= 0 ? 30 : adultFinal);
            act.setAdultOriginalPrice(rs.getInt("ADULT_ORIGINAL_TOTAL"));
            act.setChildPrice(childFinal <= 0 ? 30 : childFinal);
            act.setConcessionPrice(concessionFinal <= 0 ? 30 : concessionFinal);
        }, activityId.toArray());

        return list;

    }

    //算總頁數的方法，反正只給後台用就把前後台判斷拿掉
    public int getTotalPage(MultiValueMap<String, String> map){

        int pageSize = map.containsKey("pageSize") ? Integer.parseInt(map.get("pageSize").get(0)) : 6;

        StringBuilder sql = new StringBuilder(

            "SELECT COUNT(DISTINCT a.ACTIVITY_ID) FROM ACTIVITY a " +
            "JOIN ACTIVITY_DETAIL ad ON a.ACTIVITY_ID = ad.ACTIVITY_ID " +
            "JOIN TICKET t ON ad.TICKET_ID = t.TICKET_ID " +
            "LEFT JOIN ACTIVITY_CATE_INFO aci ON a.ACTIVITY_ID = aci.ACTIVITY_ID " +
            "LEFT JOIN ACTIVITY_CATE ac ON aci.ACTIVITY_CATE_ID = ac.ACTIVITY_CATE_ID " +

            "WHERE 1=1 "
        );

        List<Object> args = new ArrayList<>();


        //-----------------------if 判斷邏輯一樣 直接copy getSearch的 if 判斷式-----------------------

        // 給前端 input text 使用(模糊查詢)
        if (map.containsKey("keyword") && map.get("keyword").get(0).trim().length() > 0) {

                   sql.append(" AND (a.ACTIVITY_NAME LIKE ? " +
                              "OR a.ACTIVITY_DESC LIKE ? " +
                              "OR t.TICKET_NAME LIKE ? " +
                              "OR ac.CATE_NAME LIKE ?) ");


            String keyword = "%" + map.get("keyword").get(0).trim() + "%";

            args.add(keyword);
            args.add(keyword);
            args.add(keyword);
            args.add(keyword);

        }

        // 給前端 price filter 使用(範圍查詢)
        boolean hasMinPrice = map.containsKey("minPrice") && map.get("minPrice").get(0).trim().length() > 0;
        boolean hasMaxPrice = map.containsKey("maxPrice") && map.get("maxPrice").get(0).trim().length() > 0;

        if (hasMinPrice || hasMaxPrice) {

            String realPriceSql = "((SELECT SUM(t2.ADULT_PRICE) FROM ACTIVITY_DETAIL ad2 JOIN " +
                                     "TICKET t2 ON ad2.TICKET_ID = " +
                                     "t2.TICKET_ID WHERE ad2.ACTIVITY_ID = " + 
                                     "a.ACTIVITY_ID) - a.DISCOUNT)";

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
       if (map.containsKey("cateId") && !map.get("cateId").get(0).trim().isEmpty()) {

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

        // 首頁推薦活動查詢
        if (map.containsKey("isRecommended") && !map.get("isRecommended").get(0).trim().isEmpty()) {
            sql.append(" AND a.IS_RECOMMENDED = ? ");
            args.add(Integer.valueOf(map.get("isRecommended").get(0).trim()));
        }
        
        Integer totalCount = jdbcTemplate.queryForObject
                             (sql.toString(), Integer.class, args.toArray());

        if (totalCount == null || totalCount == 0){

            return 1;
        }


        return (int) Math.ceil((double) totalCount / pageSize);




    }

}
