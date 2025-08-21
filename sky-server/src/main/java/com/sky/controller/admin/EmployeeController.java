package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Tag(name = "用户上下线接口", description = "提供用户登录和登出的一系列功能")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "员工登录", description = "提供员工登录功能")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "员工登出", description = "提供员工登出功能")
    public Result<String> logout() {
        return Result.success();
    }


    @PostMapping
    @Operation(summary = "新增员工", description = "提供新增员工功能")
    public Result<Integer> saveEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工:{}", employeeDTO);
        System.err.println(Thread.currentThread().getId() + " - EmployeeController saveEmployee");
        Integer success = employeeService.saveEmployee(employeeDTO);
        return Result.success(success);
    }

    @GetMapping("/page")
    @Operation(summary = "查询员工分页数据", description = "提供员工分页查询功能")
    public Result<PageResult<Employee>> queryEmployeePage(@ParameterObject EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("查询员工分页数据:{}", employeePageQueryDTO);
        PageResult<Employee> employees = employeeService.queryEmployeesPage(employeePageQueryDTO);
        return Result.success(employees);
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "启用、禁用员工账号", description = "提供修改员工状态功能")
    public Result<Integer> updateEmployeeStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("修改员工状态: id={}, status={}", id, status);
        Integer success = employeeService.updateEmployeeStatus(status, id);
        return Result.success(success);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询员工信息", description = "提供根据ID查询员工信息功能")
    public Result<Employee> getEmployeeById(@PathVariable Long id) {
        log.info("根据ID查询员工信息: id={}", id);
        Employee employeeById = employeeService.getEmployeeById(id);
        return Result.success(employeeById);
    }

    @PutMapping
    @Operation(summary = "编辑员工信息", description = "提供编辑员工信息功能")
    public Result<Integer> editEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息:{}", employeeDTO);
        Integer success = employeeService.editEmployee(employeeDTO);
        return Result.success(success);
    }
}
