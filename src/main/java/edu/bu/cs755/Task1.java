package edu.bu.cs755;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

// successfully returns the number of trips taken in an hour per day
public class Task1 {

    public static class GetErrors extends Mapper<Object, Text, Text, IntWritable>{

        // Set the variable which will be the value in the output map
        private final static IntWritable one = new IntWritable(1);

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            String line = value.toString();
            String[] fields = line.split(",");
            // if there are emptry or == 0 fields in the GPS coordinates (errors), write to the context the hour of the day and the value 1
            if  (fields.length == 17) {
                if (fields[6].equals("0.000000") || fields[7].equals("0.000000") || fields[8].equals("0.000000") || fields[9].equals("0.000000") || fields[6].equals("") || fields[7].equals("") || fields[8].equals("") || fields[9].equals("")) {
                    context.write(new Text(fields[2].substring(11, 13)), one);
                }
            }
        }
    }

    public static class IntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();
        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            // sum the total number of errors per hour
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            // write the hour and error sum to the context
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job =  new Job(conf, "task1");
        job.setJarByClass(Task1.class);
        job.setMapperClass(Task1.GetErrors.class);
        job.setCombinerClass(Task1.IntSumReducer.class);
        job.setReducerClass(Task1.IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
