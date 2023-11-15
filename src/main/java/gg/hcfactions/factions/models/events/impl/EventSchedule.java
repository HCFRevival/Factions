package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.IScheduleable;
import lombok.Getter;

public record EventSchedule(@Getter int hour, @Getter int minute, @Getter int day) implements IScheduleable {}
