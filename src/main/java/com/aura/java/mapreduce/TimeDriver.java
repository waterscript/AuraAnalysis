package com.aura.java.mapreduce;

import com.aura.java.config.Config;
import com.aura.java.dao.DimensionDao;
import com.aura.java.db.DBHelper;
import com.aura.java.entity.Dimension;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.sql.Connection;
import java.util.List;
import java.io.BufferedReader;
import com.aura.java.util.HadoopUtil;


public class TimeDriver {
	
	public void run() throws Exception {
		Configuration conf = new Configuration();
		conf.set("mapred.job.queue.name","aura");
		
		String input_path = Config.input_path;
		String output_path = Config.output_path;

		HadoopUtil.delServerOrLocalFolder(conf, output_path);

		Job job = Job.getInstance(conf, "Time");
		job.setJarByClass(TimeDriver.class);
		
		job.setMapperClass(TimeMapper.class);
		job.setReducerClass(TimeReducer.class);
		
		job.setGroupingComparatorClass(TimeComparator.class);
		job.setPartitionerClass(TimePartition.class);
		
		job.setMapOutputKeyClass(TimeWritable.class);
	    job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(TimeWritable.class);
		job.setOutputValueClass(NullWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(input_path));
		FileOutputFormat.setOutputPath(job, new Path(output_path));
		
		job.waitForCompletion(true);

		getResultAndSaveData(output_path);
	}

	public void getResultAndSaveData(String output_path) throws Exception {
		List<BufferedReader> list = HadoopUtil.getReduceResultList(output_path);
		if(list != null) {
			String line = null;
			for(BufferedReader br : list) {
				while ((line = br.readLine()) != null) {
					long time = Long.parseLong(line.trim());
					Dimension dimension = new Dimension();
					dimension.setTime(time);
					dimension.setDay(Config.day);
					saveMapreduceDimensionData(dimension);
				}
			}
		}
	}

	public void saveMapreduceDimensionData(Dimension dimension) {
		Connection conn = DBHelper.getConnection();
		try {
			DimensionDao.saveMapreduceDimensionData(dimension, conn);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBHelper.close(conn);
		}
	}
	
	public static void main(String [] args) {
		
	}
}
