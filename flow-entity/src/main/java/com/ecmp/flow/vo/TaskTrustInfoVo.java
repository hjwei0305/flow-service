package com.ecmp.flow.vo;

import com.ecmp.flow.basic.vo.Employee;

import java.io.Serializable;
import java.util.List;

public class TaskTrustInfoVo implements Serializable {


    /**
     * 被委托的任务ID
     */
    private String taskId;


    /**
     * 委托的员工集合
     */
    private List<Employee> employeeList;


    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }
}
