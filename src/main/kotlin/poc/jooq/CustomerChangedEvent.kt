package poc.jooq

import org.springframework.context.ApplicationEvent
import poc.jooq.generated.db.tables.records.CustomerRecord

data class CustomerChangedEvent(
    val customer: CustomerRecord
): ApplicationEvent(customer)