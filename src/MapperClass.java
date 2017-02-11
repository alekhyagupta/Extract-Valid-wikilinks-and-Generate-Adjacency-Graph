
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;


public class MapperClass extends MapReduceBase implements Mapper<LongWritable, Text, DoubleWritable, Text> {

    public void map(LongWritable key, Text value, OutputCollector<DoubleWritable, Text> output, Reporter reporter) throws IOException {

        String svalues = value.toString();

        int titleIndex = svalues.indexOf("\t");
        int rankIndex = svalues.indexOf( "\t", titleIndex+1);

        if ( rankIndex == -1){
            rankIndex = svalues.length();
        }

        String srank = svalues.substring(titleIndex+1,rankIndex);

        double rank = Double.parseDouble(srank);

        output.collect(new DoubleWritable(Double.parseDouble(srank)), new Text(svalues.substring(0,titleIndex)));
    }
}