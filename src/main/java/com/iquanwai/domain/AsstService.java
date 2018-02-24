package com.iquanwai.domain;

import com.iquanwai.domain.dao.UserRoleDao;
import com.iquanwai.domain.po.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsstService {

    @Autowired
    private UserRoleDao userRoleDao;

    /**
     * 获得有效期内的助教
     * @return
     */
    public List<UserRole> getValidAssists() {
        return userRoleDao.loadValidAssists();
    }


}
