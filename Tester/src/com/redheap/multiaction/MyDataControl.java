package com.redheap.multiaction;

import java.util.ArrayList;
import java.util.List;

public class MyDataControl {

    public String returnSomething(String arg) {
        System.out.println(arg);
        return arg;
    }

    public void doSomething(String arg) {
        System.out.println(arg);
        throw new RuntimeException("error");
    }

    public List<Employee> getEmployees() {
        return new ArrayList<Employee>(0);
    }

}
