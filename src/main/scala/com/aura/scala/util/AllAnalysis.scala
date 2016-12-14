package com.aura.scala.util

import com.aura.scala.spark.core._
import com.aura.scala.spark.mllib._
import com.aura.java.hive.HiveAnalysis

/**
  * Created by An on 2016/11/30.
  */
object AllAnalysis {
  def main(args: Array[String]): Unit = {
    HiveAnalysis.runAnalysis()

    FlowAnalysis.runAnalysis()
    SearchAnalysis.runAnalysis()
    ProvinceAnalysis.runAnalysis()
    CountryAnalysis.runAnalysis()
    ContentAnalysis.runAnalysis()

    GenderAnalysis.runAnalysis()
    ChannelAnalysis.runAnalysis()
  }
}