package com.apigeesample;

import java.util.ArrayList;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Hello world!
 *
 */
public class Timer {

  private ArrayList<Timestamp> timestamps;
  private Instant start;
  private String prefix;

  class Timestamp {
    Instant timepoint;
    String label;

    Timestamp(String l) {
      this.timepoint = Instant.now();
      this.label = l;
    }
  }

  public void addTimestamp(String label) {
    timestamps.add(new Timestamp(label));
  }

  public Timer(String label){
    timestamps = new ArrayList<Timestamp>();
    start = Instant.now();
    addTimestamp("Start");
    prefix = label;
  }

  public String getTimestamps(){
    String ret = "";
    addTimestamp("Finish");
    Iterator<Timestamp> itr=timestamps.iterator();
    while (itr.hasNext()) {
      Timestamp t = itr.next();
      ret += String.format("%s : %s %,d (microseconds)\n", prefix, t.label, Duration.between(start, t.timepoint).toNanos() / 1000);
    }
    return ret;
  }
}
  

