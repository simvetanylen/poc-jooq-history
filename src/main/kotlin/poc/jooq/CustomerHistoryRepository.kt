package poc.jooq

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.*
import org.springframework.stereotype.Repository
import poc.jooq.generated.db.Tables.*
import poc.jooq.generated.db.tables.records.CustomerHistoryRecord
import poc.jooq.generated.db.tables.records.CustomerRecord

@Repository
class CustomerHistoryRepository(
    private val jooq: DSLContext
) {

    fun save(customer: CustomerRecord) {
        val history = jooq.newRecord(CUSTOMER_HISTORY)
        history.id = customer.id
        history.firstname = customer.firstname
        history.lastname = customer.lastname
        history.email = customer.email
        history.phoneNumber = customer.phoneNumber
        history.version = getLastVersion(customer.id) + 1

        history.store()
    }

    fun findAllFor(customerId: Long): List<CustomerHistoryRecord> {
        return jooq.selectFrom(CUSTOMER_HISTORY)
            .where(CUSTOMER_HISTORY.ID.eq(customerId))
            .orderBy(CUSTOMER_HISTORY.VERSION.desc())
            .fetch()
            .toList()
    }

    fun findDistinctFor(customerId: Long): List<Record> {
        return jooq.select()
            .distinctOn(
                CUSTOMER_HISTORY.FIRSTNAME,
                CUSTOMER_HISTORY.LASTNAME,
                CUSTOMER_HISTORY.EMAIL,
                CUSTOMER_HISTORY.PHONE_NUMBER
            )
            .from(CUSTOMER_HISTORY)
            .where(CUSTOMER_HISTORY.ID.eq(customerId))
            .fetch()
            .toList()
    }

    private fun getLastVersion(customerId: Long): Long {
        val result = jooq.select(max(CUSTOMER_HISTORY.VERSION))
            .from(CUSTOMER_HISTORY)
            .where(CUSTOMER_HISTORY.ID.eq(customerId))
            .fetchOne()

        return if (result?.get("max") != null) {
            result.get("max") as Long
        } else {
            0
        }
    }
}