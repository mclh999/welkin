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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工管理")
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
    @ApiOperation("员工登录")
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
    @ApiOperation("登出")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 新增员工
     *
     * @param employeeDTO 专门作为接收参数的对象
     */
    @PostMapping
    @ApiOperation("新增员工")
    public  Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工");
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 分页查询员工信息
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")//一般是查询的接口才会指定Result类型
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询");
        PageResult pageResult = employeeService.page(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 更新员工账号状态
     */
    @PostMapping("/status/{status}")
    @ApiOperation("更新员工账号状态")
    public Result updateStatus(@PathVariable Integer status, long id){
        log.info("员工账号状态：{},员工id：{}", status, id);
        employeeService.updateStatus(status, id);
        return Result.success();
    }

    /**
     * 查询回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")//别忘记打/
    @ApiOperation("查询回显")
    public Result queryById(@PathVariable long id){
        log.info("查询回显，员工id：{}", id);
        Employee employee = employeeService.queryById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("员工信息：{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

}
