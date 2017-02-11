
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;


public class OutputGraphMain {
	public static long count = 0;

	 public static void main(String[] args) throws Exception {
	 
		 	OutputGraphMain mainObject = new OutputGraphMain();
		 
	        String input=args[0];
	        String output=args[1];
	        String Final = output + "/graph/";
	        String temp = output + "/tmp/";
	        System.out.println(Final + " " + temp);
	        String OutputLink="OutputWikiAdjacencyGraph.out";
	        String OutputLinkStage1="temp_leve1.out";
	        
	        mainObject.GenerateOutputGraph1(input, temp + OutputLinkStage1);
	        mainObject.GenerateOutputGraph2(temp + OutputLinkStage1, temp + OutputLink );
            mainObject.MergeFiles(temp + OutputLink, Final + OutputLink);
     }
	


	    public void GenerateOutputGraph1(String input, String output) throws IOException {
	        JobConf conf = new JobConf(OutputGraphMain.class);

	        conf.set(InputXMLFile.START_TAG_KEY, "<page>");
	        conf.set(InputXMLFile.END_TAG_KEY, "</page>");
	        conf.setJarByClass(OutputGraphMain.class);
            System.out.println(input);
                        
           
	        FileInputFormat.setInputPaths(conf, new Path(input));
	        conf.setInputFormat(InputXMLFile.class);
	        conf.setMapperClass(MapperOutput1.class);

	       
	        FileOutputFormat.setOutputPath(conf, new Path(output));
	        conf.setReducerClass(ReducerOutput1.class);

	        
	        conf.setOutputKeyClass(Text.class);
	        conf.setOutputValueClass(Text.class);

	       
	        JobClient.runJob(conf);
	    }
	    

	    public void GenerateOutputGraph2(String input, String output) throws IOException {
	        JobConf conf = new JobConf(OutputGraphMain.class);
	        conf.setJarByClass(OutputGraphMain.class);

	       
	        FileInputFormat.setInputPaths(conf, new Path(input));
	        conf.setMapperClass(MapperOutput2.class);

	       
	        FileOutputFormat.setOutputPath(conf, new Path(output));
	        conf.setReducerClass(ReducerOutput2.class);

	        
	        conf.setOutputKeyClass(Text.class);
	        conf.setOutputValueClass(Text.class);

	       
	        JobClient.runJob(conf);
	    }



	    public void SortJob(String input, String output, String linkcountfile) throws IOException{
	        JobConf conf = new JobConf(OutputGraphMain.class);
	        conf.setJarByClass(OutputGraphMain.class);

	     
	        FileInputFormat.setInputPaths(conf, new Path(input));
	        conf.setMapperClass(MapperClass.class);

	        
	        FileOutputFormat.setOutputPath(conf, new Path(output));
	        conf.setReducerClass(ReducerClass.class);

	      
	        conf.setMapOutputKeyClass(DoubleWritable.class);
	        conf.setMapOutputValueClass(Text.class);

	       
	        conf.setOutputKeyClass(Text.class);
	        conf.setOutputValueClass(DoubleWritable.class);

	       
	        conf.setOutputKeyComparatorClass(KeyComparator.class);
	        conf.setOutputValueGroupingComparator(GroupComparator.class);
	        conf.setPartitionerClass(FirstPartitioner.class);

	        
	        Integer count = readFile(linkcountfile, conf);
	        conf.set("count", Integer.toString(count));

	       
	        JobClient.runJob(conf);
	    }

	    public static class FirstPartitioner implements Partitioner<DoubleWritable, Text> {

	        @Override
	        public void configure(JobConf job) {}

	        @Override
	        public int getPartition(DoubleWritable key, Text value, int numPartitions) {
	            double d = (Double.parseDouble(key.toString()));
	            int n =(int) d * 100;
	            return (int)(n / numPartitions) ;
	        }
	    }

	    public static class GroupComparator extends WritableComparator {
	        protected GroupComparator() {
	            super(DoubleWritable.class, true);
	        }
	        @Override
	        public int compare(WritableComparable w1, WritableComparable w2) {
	            DoubleWritable ip1 = (DoubleWritable) w1;
	            DoubleWritable ip2 = (DoubleWritable) w2;
	            return ip1.compareTo(ip2);
	        }
	    }

	    public static class KeyComparator extends WritableComparator {
	        protected KeyComparator() {
	            super(DoubleWritable.class, true);
	        }
	        @Override
	        public int compare(WritableComparable w1, WritableComparable w2) {
	            DoubleWritable d1 = (DoubleWritable) w1;
	            DoubleWritable d2 = (DoubleWritable) w2;
	            int cmp = d1.compareTo(d2);
	            return cmp * -1; //reverse
	        }
	    }

	    private Integer readFile (String filepath, Configuration conf) throws IOException
	    {
	        String fileName = filepath + "/part-r-00";
	        Integer count = 1;
	        BufferedReader br = null;
	        FileSystem fs = null;
	        Path path ;//= new Path(fileName);
	        NumberFormat nf = new DecimalFormat("000");
	        Configuration config = new Configuration();

	        try {

	            path = new Path (fileName + nf.format(0));
	            fs = path.getFileSystem(new Configuration());
	            String line = "";

	            if (!fs.isFile(path)){
	                fileName = filepath + "/part-00";
	            }

	          
	            for (int i=0; i<=999; i++){
	                path = new Path (fileName + nf.format(i));
	                fs = path.getFileSystem(config);
	                if (fs.isFile(path)){
	                    br = new BufferedReader(new InputStreamReader(fs.open(path)));
	                    line = br.readLine();

	                    if (line!= null && !line.isEmpty() && line.length() >= 2)
	                        break;
	                }
	            }

	            if (line != null && !line.isEmpty())
	            {
	                String[] splits = line.split("=");
	                count = Integer.parseInt(splits[1]);
	                return count;
	            }

	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } finally {
	            try {
	                br.close();
	                fs.close();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }
	        return count;
	    }

	    private void MergeFiles(String input, String output) throws  IOException {
	        String fileName = input + "/part-r-00";
	        NumberFormat nf = new DecimalFormat("000");

	        Configuration conf = new Configuration();
	        FileSystem outFS = null;

	        try {

	            Path outFile = new Path(output);
	            outFS = outFile.getFileSystem(new Configuration());
	            if (outFS.exists(outFile)){
	                System.out.println(outFile + " already exists");
	                System.exit(1);
	            }

	            FSDataOutputStream out = outFS.create(outFile);

	            Path inFile = new Path (fileName + nf.format(0));
	            FileSystem inFS = inFile.getFileSystem(new Configuration());

	            if (!inFS.exists(inFile)){
	                fileName = input + "/part-00";
	            }

	           
	            for (int i=0; i<=999; i++){

	                inFile = new Path (fileName + nf.format(i));
	                inFS = inFile.getFileSystem(new Configuration());

	                if (inFS.isFile(inFile)){

	                    int bytesRead=0;
	                    byte[] buffer = new byte[4096];

	                    FSDataInputStream in = inFS.open(inFile);

	                    while ((bytesRead = in.read(buffer)) > 0) {
	                        out.write(buffer, 0, bytesRead);
	                    }

	                    in.close();
	                }else{
	                    break;
	                }

	                inFS.close();
	            }

	            out.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                outFS.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    
	 
}
