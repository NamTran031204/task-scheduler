package com.practice.task_scheduler.utils;

import java.util.UUID;

public class GenerateShareCode {
    public static String generateShareCode(String username, String taskListName){
        String salt = UUID.randomUUID().toString();
        int length = Math.max(username.length(), Math.max(20, taskListName.length()));

//        username = (username + salt).substring(0,20);
//        taskListName = (taskListName + salt).substring(0,20);

        String input1 = username + salt + taskListName;
        String input2 = taskListName + salt + username;

        String result = "";
        for (int i=0; i<input1.length() ; i++){
            char x,y;
            x = input1.charAt(i);
            y = input2.charAt(i);
            int s;
            s = x^y;
            s ^= i*31; // pick bua 1 so nguyen to nhung lon hon 26 chu cai
            s ^= username.length() ^ taskListName.length();

            result += s;
        }
        result = hashToFixedLength(result, 20);
        return result;
    }

    private static String hashToFixedLength(String input, int length){
        final char[] CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

        byte[] inputBytes = input.getBytes();
        long seed = createSeed(inputBytes);
        char[] output = new char[length];

        for (int i = 0; i < length; i++) {
            // Formula: seed = (a * seed + c) mod m
            seed = (seed * 1664525L + 1013904223L) & 0x7fffffffL;

            long mixed = seed ^ (i * 0x9e3779b9L);

            mixed ^= (inputBytes.length << 16);

            // Mix với byte tại i
            if (i < inputBytes.length) {
                mixed ^= (inputBytes[i] & 0xFF) << 8;
            }

            // mixing
            mixed ^= (mixed >>> 17);
            mixed ^= (mixed << 5);
            mixed ^= (mixed >>> 13);

            // Map voi charset
            int index = (int) (Math.abs(mixed) % CHARSET.length);
            output[i] = CHARSET[index];
        }

        return new String(output);
    }


    // su dung thuat toan FNV-1a hash
    private static long createSeed(byte[] data) {
        // FNV-1a constants (64-bit)
        final long FNV_OFFSET_BASIS = 0xcbf29ce484222325L;  // wikipedia
        final long FNV_PRIME = 0x100000001b3L;

        long hash = FNV_OFFSET_BASIS;

        for (byte b : data) {
            hash ^= (b & 0xFF);
            hash *= FNV_PRIME;
        }

        return hash;
    }
}
