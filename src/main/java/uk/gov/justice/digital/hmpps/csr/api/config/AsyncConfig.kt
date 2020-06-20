package uk.gov.justice.digital.hmpps.csr.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurerSupport
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class AsyncConfig : AsyncConfigurerSupport() {
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 7
        executor.maxPoolSize = 42
        executor.setQueueCapacity(11)
        executor.threadNamePrefix = "TenantAwareTaskExecutor-"
        executor.setTaskDecorator(TenantAwareTaskDecorator())
        executor.initialize()
        return executor
    }
}