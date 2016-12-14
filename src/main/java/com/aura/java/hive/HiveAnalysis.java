package com.aura.java.hive;

import com.aura.java.config.Config;
import com.aura.java.db.DBHelper;
import com.aura.java.entity.Dimension;

import java.sql.Connection;
import com.aura.java.basic.BasicDao;
import com.aura.java.dao.DimensionDao;

/**
 * Created by An on 2016/11/28.
 */
public class HiveAnalysis {

    public static Dimension getHiveDimension(Dimension dimension) {
        Dimension result = new Dimension();
        Connection conn = DBHelper.getHiveConnection();
        try {
            String sql = "select count(*) pv,count(distinct(uuid)) uv,count(distinct(ip)) ip from aura " +
                         "where day = #{day} and ip is not null and uuid is not null";
            result = (Dimension)BasicDao.getSqlObject(sql, dimension, conn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBHelper.close(conn);
        }
        return result;
    }

    public static void saveHiveDimensionData(Dimension dimension) {
        Connection conn = DBHelper.getConnection();
        try {
            DimensionDao.saveHiveDimensionData(dimension, conn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBHelper.close(conn);
        }
    }

    public static void runAnalysis() {
        Dimension dimension = new Dimension();
        dimension.setDay(Config.day.replace("-",""));
        Dimension result = getHiveDimension(dimension);
        result.setDay(Config.day);
        saveHiveDimensionData(result);
    }

    public static void main(String[] args) {
        HiveAnalysis.runAnalysis();
    }
}
