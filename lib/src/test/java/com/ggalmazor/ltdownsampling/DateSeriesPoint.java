package com.ggalmazor.ltdownsampling;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class DateSeriesPoint implements Point {
  private final LocalDate date;
  private final double value;

  public DateSeriesPoint(LocalDate date, double value) {
    this.date = date;
    this.value = value;
  }

  @Override
  public double x() {
    return (double) date.atStartOfDay().atOffset(ZoneOffset.UTC).toEpochSecond();
  }

  @Override
  public double y() {
    return value;
  }

  public LocalDate getDate() {
    return date;
  }
}
