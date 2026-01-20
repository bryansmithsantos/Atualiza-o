package com.example.economia.features.missions;

public record Mission(String id, String title, MissionType type, int goal, double reward) {
}
