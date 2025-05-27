package challenge.dev.raniery.itaubackend.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* challenge.dev.raniery.itaubackend.controller.*.*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        logger.info("→ Executando {}.{}", className, methodName);
        if (args.length > 0) {
            logger.debug("   Argumentos: {}", Arrays.toString(args));
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            logger.info("✓ {}.{} executado com sucesso em {}ms", className, methodName, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            logger.error("✗ Erro em {}.{} após {}ms: {}", className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* challenge.dev.raniery.itaubackend.service.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        logger.debug("→ Service: {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            logger.debug("✓ Service: {}.{} concluído", className, methodName);
            return result;
        } catch (Exception e) {
            logger.error("✗ Erro no service {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
