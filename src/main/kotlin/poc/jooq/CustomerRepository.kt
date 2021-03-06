package poc.jooq

import org.jooq.DSLContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Repository
import poc.jooq.generated.db.Tables.*
import poc.jooq.generated.db.tables.records.CustomerRecord


@Repository
class CustomerRepository(
    private val jooq: DSLContext,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    fun new(): CustomerRecord {
        return jooq.newRecord(CUSTOMER)
    }

    fun store(customer: CustomerRecord) {
        customer.store()
        customer.refresh()
        applicationEventPublisher.publishEvent(CustomerChangedEvent(customer))
    }

    fun find(id: Long): CustomerRecord {
        return jooq.selectFrom(CUSTOMER)
            .where(CUSTOMER.ID.eq(id))
            .fetchOne()
    }

    fun findAll(): List<CustomerRecord> {
        return jooq.selectFrom(CUSTOMER)
            .fetch()
            .toList()
    }
}