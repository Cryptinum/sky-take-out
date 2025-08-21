package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService extends IService<Employee> {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 保存员工信息
     * @param employeeDTO
     * @return
     */
    Integer saveEmployee(EmployeeDTO employeeDTO);

    /**
     * 分页查询员工信息
     * @param employeePageQueryDTO
     * @return
     */
    PageResult<Employee> queryEmployeesPage(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 更新员工状态
     * @param status
     * @param id
     * @return
     */
    Integer updateEmployeeStatus(Integer status, Long id);

    /**
     * 根据ID查询员工信息
     * @param id
     * @return
     */
    Employee getEmployeeById(Long id);

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    Integer editEmployee(EmployeeDTO employeeDTO);
}
