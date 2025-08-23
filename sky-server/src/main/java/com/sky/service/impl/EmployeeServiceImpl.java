package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

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
        //对前端传来的密码进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
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
     * 新增员工
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        //创建对象
        Employee employee = new Employee();

        //调用spring下的BeanUtils，在属性名相同的情况下拷贝对象属性
        //左侧为源对象， 右侧为目标对象
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置默认密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

//        //设置当前记录的创建时间和修改时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        //设置当前记录创建人,修改人
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        //设置账号状态
        employee.setStatus(StatusConstant.ENABLE);

        //调用mapper方法,保存数据
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        //自动生成并执行对应的分页sql
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.page(employeePageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());

    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, long id) {
        //使用build注解直接赋值
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);

    }

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    @Override
    public Employee queryById(long id) {
        Employee employee = employeeMapper.selectById(id);
        return employee;
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

}
