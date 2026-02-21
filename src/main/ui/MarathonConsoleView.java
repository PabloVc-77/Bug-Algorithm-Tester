package main.ui;

import main.simulation.*;

public class MarathonConsoleView {

    public static void show(MarathonResult r) {

        System.out.println("\n===== MARATHON RESULTS =====");
        System.out.println("Total maps: " + r.totalMaps);
        System.out.println("Completed: " + r.completed);
        System.out.println("Gave up: " + r.gaveUp);
        System.out.println("Terminated: " + r.terminated);

        System.out.printf("Average steps: %.2f%n", r.averageSteps());
        System.out.printf("Average optimal: %.2f%n", r.averageOptimal());
        System.out.printf("Efficiency: %.3f%n", r.efficiency());

        System.out.println("============================");

        System.out.println("Wins: " + r.wins);
        System.out.println("Losses: " + r.losses);
        System.out.println("Ties: " + r.ties);
    }
}
