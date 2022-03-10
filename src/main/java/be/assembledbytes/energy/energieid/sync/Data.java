package be.assembledbytes.energy.energieid.sync;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

@JsonSerialize
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public record Data(Date timestamp,
                   double amount) {}
