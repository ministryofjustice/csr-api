package uk.gov.justice.digital.hmpps.csr.api.config.async

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurerSupport
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class AsyncConfig : AsyncConfigurerSupport() {
  override fun getAsyncExecutor(): Executor {
    val executor = ThreadPoolTaskExecutor()
    executor.threadNamePrefix = "RegionAwareTaskExecutor-"
    executor.setTaskDecorator(RegionAwareTaskDecorator())
    executor.initialize()
    return executor
  }
}
