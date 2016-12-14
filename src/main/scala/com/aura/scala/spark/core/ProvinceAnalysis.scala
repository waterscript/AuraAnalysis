package com.aura.scala.spark.core

import com.aura.java.config.Config
import com.aura.scala.dao.DimensionDao
import com.aura.scala.entity.Dimension
import com.aura.scala.util.SparkUtil
import org.apache.spark.broadcast.Broadcast

/**
  * Created by An on 2016/11/25.
  * 省份分布计算
  */
object ProvinceAnalysis {

  def runAnalysis(): Unit = {
    /**
      * 获得SparkContext
      */
    val sc = SparkUtil.getSparkContext(this.getClass)
    /**
      * 读取日志
      */
    val lines = sc.textFile(Config.input_path)
    /**
      * 解析日志并过滤其中的错误内容
      */
    val filter = SparkUtil.getFilterLog(lines).cache()
    /**
      * 省份映射
      */
    val provinceMap: Map[String, Dimension] = DimensionDao.getProvinceMap()
    /**
      * 广播变量
      */
    val provinceBc: Broadcast[Map[String, Dimension]] = sc.broadcast(provinceMap)

    /**
      * 计算省份分布(map,reduce)
      */
    val map = filter.map(
      log => {
        val dimension: Dimension = new Dimension(pv = 1,uv = 1,ip = 1)
        val area = log.Area
        dimension.uvs += log.Uuid
        dimension.ips += log.Ip
        if(area != null) {
          if(provinceBc.value.contains(area)) {
            dimension.dimeId = provinceBc.value.get(area).get.dimeId
          }
        }
        (dimension.dimeId, dimension)
      }
    ).filter(_._1 != 0).cache()
    /**
      * 计算维度pv,uv,ip的通用ReduceByKey
      */
    val reduce = SparkUtil.getReduceByKey(map)

    val list: List[Dimension] = reduce.values.collect().toList
    list.foreach(item => {
      item.day = Config.day
    })
    sc.stop()
    /**
      * 写入数据库
      */
    DimensionDao.saveDimensionList(list)
  }

  def main(args: Array[String]): Unit = {
    runAnalysis()
  }
}
