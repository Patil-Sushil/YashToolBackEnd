package com.kalibyte.YashTools.common.multi_company;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Aspect
@Component
public class CompanyFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void enableCompanyFilter() {
        UUID companyId = CompanyContextHolder.getCompanyId();
        if (companyId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("companyFilter")
                   .setParameter("companyId", companyId);
        }
    }
}
