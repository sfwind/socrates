package com.iquanwai.util.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class JobAspect {
    @Pointcut(value = "execution(* com.iquanwai.job..*.*(..)) && @annotation(CatInspect)")
    public void pointCut() {

    }

    @Around(value = "pointCut()")
    public void aroundMethod(ProceedingJoinPoint pjp){
        MethodSignature joinPointObject = (MethodSignature) pjp.getSignature();
        Method method = joinPointObject.getMethod();
        Class clazz= pjp.getTarget().getClass();
        CatInspect catInspect = method.getAnnotation(CatInspect.class);
        String name = catInspect.name();
        Transaction t = Cat.newTransaction("JOB", clazz.getSimpleName() + "." + name);
        try {
            pjp.proceed();
            t.setStatus(Transaction.SUCCESS);
        } catch (Throwable e) {
            t.setStatus(e);
            Cat.logError(e);
        } finally {
            t.complete();
        }
    }


}
