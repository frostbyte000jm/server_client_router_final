package com.Main;

import java.util.Scanner;

public class test {
    public static void main (String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("This is a test:");
        String in = scanner.nextLine();
        if (in.charAt(0) == '/'){
            System.out.println("Success");
        } else {
            System.out.println("fail");
        }
    }
}
