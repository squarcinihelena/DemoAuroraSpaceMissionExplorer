package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Solution {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        //teste local
        String firstAgency = "SpaceX";
        String secondAgency = "ISRO";

        String result = Result.findLongestDurationMission(firstAgency, secondAgency);
        System.out.println(result);

        bufferedReader.close();
    }
}
