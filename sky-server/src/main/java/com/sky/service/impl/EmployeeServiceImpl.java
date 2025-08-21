package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 使用SHA256进行加密和比对
        // 这里的DigestUtils来自Apache Commons Codec库
        // 而不是Spring Util
        String hashedPassword = DigestUtils.sha256Hex(password);

        if (!hashedPassword.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 保存员工信息
     * @param employeeDTO
     * @return
     */
    @Override
    public Integer saveEmployee(EmployeeDTO employeeDTO) {
        Employee employee = BeanUtil.copyProperties(employeeDTO, Employee.class);
        employee.setStatus(StatusConstant.ENABLE);  // 默认启用
        employee.setPassword(DigestUtils.sha256Hex(PasswordConstant.DEFAULT_PASSWORD)); // 默认密码
        return employeeMapper.insert(employee);
    }

    /**
     * 分页查询员工信息
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult<Employee> getEmployeesPage(EmployeePageQueryDTO employeePageQueryDTO) {
        int page = employeePageQueryDTO.getPage();
        int pageSize = employeePageQueryDTO.getPageSize();
        String name = employeePageQueryDTO.getName();

        Page<Employee> p = Page.of(page, pageSize);
        p.addOrder(OrderItem.desc("update_time"));

        Page<Employee> queryPage = lambdaQuery()
                .like(name != null && !name.isEmpty(), Employee::getName, name)
                .page(p);
        return PageResult.of(queryPage);
    }

    /**
     * 更新员工状态
     * @param status
     * @param id
     * @return
     */
    @Override
    public Integer updateEmployeeStatus(Integer status, Long id) {
        return employeeMapper.update(new LambdaUpdateWrapper<Employee>()
                .eq(Employee::getId, id)
                .set(Employee::getStatus, status));
    }

    /**
     * 根据ID查询员工信息
     * @param id
     * @return
     */
    @Override
    public Employee getEmployeeById(Long id) {
        return employeeMapper.selectById(id);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    @Override
    public Integer editEmployee(EmployeeDTO employeeDTO) {
        Employee employee = BeanUtil.copyProperties(employeeDTO, Employee.class);
        return employeeMapper.updateById(employee);
    }
}
