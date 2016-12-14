package com.aura.scala.db

import java.sql.{Connection, DriverManager}
import com.aura.java.config.Config

object JDBCHelper {
  
  def getConnection(): Connection = {
    Class.forName(Config.driver_class)
    return DriverManager.getConnection(Config.db_url, Config.username, Config.password)
	}
  
  def main(args: Array[String]): Unit = {
    JDBCHelper.getConnection()
  }
}